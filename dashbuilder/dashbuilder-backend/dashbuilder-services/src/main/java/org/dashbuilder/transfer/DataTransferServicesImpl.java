/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dashbuilder.transfer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.dashbuilder.navigation.storage.NavTreeStorage;
import org.dashbuilder.navigation.event.NavTreeChangedEvent;
import org.dashbuilder.dataset.DataSetDefRegistryCDI;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.events.DataSetDefRegisteredEvent;
import org.jboss.errai.bus.server.annotations.Service;
import org.uberfire.ext.plugin.event.PluginAdded;
import org.uberfire.ext.plugin.model.Plugin;
import org.uberfire.ext.plugin.type.TypeConverterUtil;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.IOException;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.FileVisitResult;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.Paths;
import org.uberfire.java.nio.file.SimpleFileVisitor;
import org.uberfire.java.nio.file.attribute.BasicFileAttributes;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.spaces.SpacesAPI;

@ApplicationScoped
@Service
public class DataTransferServicesImpl implements DataTransferServices {

    private IOService ioService;
    private FileSystem datasetsFS;
    private FileSystem perspectivesFS;
    private FileSystem navigationFS;
    private FileSystem systemFS;
    private DataSetDefRegistryCDI dataSetDefRegistryCDI;
    private transient SessionInfo sessionInfo;
    private Event<DataSetDefRegisteredEvent> dataSetDefRegisteredEvent;
    private Event<PluginAdded> pluginAddedEvent;
    private Event<NavTreeChangedEvent> navTreeChangedEvent;
    private NavTreeStorage navTreeStorage;
    private byte[] buffer = new byte[1024];

    public DataTransferServicesImpl() {
    }

    @Inject
    public DataTransferServicesImpl(
            final @Named("ioStrategy") IOService ioService,
            final @Named("datasetsFS") FileSystem datasetsFS,
            final @Named("perspectivesFS") FileSystem perspectivesFS,
            final @Named("navigationFS") FileSystem navigationFS,
            final @Named("systemFS") FileSystem systemFS,
            final DataSetDefRegistryCDI dataSetDefRegistryCDI,
            final SessionInfo sessionInfo,
            final Event<DataSetDefRegisteredEvent> dataSetDefRegisteredEvent,
            final Event<PluginAdded> pluginAddedEvent,
            final Event<NavTreeChangedEvent> navTreeChangedEvent,
            final NavTreeStorage navTreeStorage) {

        this.ioService = ioService;
        this.datasetsFS = datasetsFS;
        this.perspectivesFS = perspectivesFS;
        this.navigationFS = navigationFS;
        this.systemFS = systemFS;
        this.dataSetDefRegistryCDI = dataSetDefRegistryCDI;
        this.sessionInfo = sessionInfo;
        this.dataSetDefRegisteredEvent = dataSetDefRegisteredEvent;
        this.pluginAddedEvent = pluginAddedEvent;
        this.navTreeChangedEvent = navTreeChangedEvent;
        this.navTreeStorage = navTreeStorage;
    }

    @Override
    public String doExport() throws Exception {
        String zipLocation = new StringBuilder()
            .append(System.getProperty("java.io.tmpdir"))
            .append(File.separator)
            .append(FILE_PATH)
            .append(File.separator)
            .append(EXPORT_FILE_NAME)
            .toString();

        new File(zipLocation).getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(zipLocation);
        ZipOutputStream zos = new ZipOutputStream(fos);

        zipFileSystem(datasetsFS, zos);
        zipFileSystem(perspectivesFS, zos);
        zipFileSystem(navigationFS, zos);

        zipFile(createVersionFile(), "VERSION", zos);

        zos.close();
        fos.close();

        moveZipToFileSystem(zipLocation, systemFS);

        String exportPath = new StringBuilder()
            .append(SpacesAPI.Scheme.GIT)
            .append("://")
            .append(systemFS.getName())
            .append(File.separator)
            .append(FILE_PATH)
            .append(File.separator)
            .append(EXPORT_FILE_NAME)
            .toString();

        return exportPath;
    }

    @Override
    public List<String> doImport() throws Exception {
        List<String> imported = new ArrayList<String>();
        Path root = systemFS.getRootDirectories().iterator().next();
        String expectedPath = new StringBuilder()
            .append(File.separator)
            .append(FILE_PATH)
            .append(File.separator)
            .append(IMPORT_FILE_NAME)
            .toString();

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (!path.toString().equalsIgnoreCase(expectedPath)) {
                    return FileVisitResult.CONTINUE;
                }

                try {
                    imported.addAll(importFiles(path));
                    return FileVisitResult.TERMINATE;

                } catch (Exception e) {
                    e.printStackTrace();
                    return FileVisitResult.TERMINATE;
                }
            }
        });

        return imported;
    }

    private List<String> importFiles(Path path) throws Exception {
        String tempPath = new StringBuilder()
            .append(System.getProperty("java.io.tmpdir"))
            .append(File.separator)
            .append(FILE_PATH)
            .append(File.separator)
            .toString();

        List<String> imported = new ArrayList<String>();
        File destDir = new File(tempPath);
        ZipInputStream zis = new ZipInputStream(new FileInputStream(path.toFile()));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            File newFile = unzipFile(destDir, zipEntry, zis);
            zipEntry = zis.getNextEntry();
            FileSystem fileSystem = getImportFileSystem(newFile, tempPath);

            if (fileSystem == null) {
                continue;
            }

            URI uri = URI.create(
                new StringBuilder()
                    .append(SpacesAPI.Scheme.GIT)
                    .append("://")
                    .append(fileSystem.getName())
                    .toString());

               String newFilePath = newFile.toPath()
                   .toString()
                   .replace(
                       new StringBuilder(tempPath).append(fileSystem.getName()),
                       "");

            ioService.write(
                Paths.get(uri).resolve(newFilePath),
                java.nio.file.Files.readAllBytes(newFile.toPath()));

            imported.add(
                new StringBuilder()
                    .append(fileSystem.getName())
                    .append(newFilePath)
                    .toString());

           fireEvent(newFile, tempPath, uri, newFilePath);
        }

        zis.closeEntry();
        zis.close();

        FileUtils.deleteDirectory(destDir);

        return imported;
    }

    private void fireEvent(File newFile, String tempPath, URI uri, String newFilePath) throws Exception {
        String filePath = newFile.toURI().toString();

        if (filePath.contains(tempPath + datasetsFS.getName()) && newFilePath.endsWith(DataSetDefRegistryCDI.DATASET_EXT)) {
            fireDatasetEvent(uri, newFilePath);

           } else if (filePath.contains(tempPath + perspectivesFS.getName()) && newFilePath.endsWith(Plugin.FILE_EXT)) {
               firePerspectiveEvent(newFile, uri, newFilePath);

           } else if (filePath.contains(tempPath + navigationFS.getName()) && newFilePath.endsWith(NavTreeStorage.NAV_TREE_FILE_NAME)) {
               fireNavigationEvent();
           }
    }

    private void fireDatasetEvent(URI uri, String newFilePath) throws Exception {
        String json = ioService.readAllString(Paths.get(uri).resolve(newFilePath));
        DataSetDef newDef = dataSetDefRegistryCDI.getDataSetDefJsonMarshaller().fromJson(json);
        dataSetDefRegisteredEvent.fire(new DataSetDefRegisteredEvent(newDef));
    }

    private void firePerspectiveEvent(File newFile, URI uri, String newFilePath) {
        org.uberfire.backend.vfs.Path pluginPath =
           org.uberfire.backend.server.util.Paths.convert(
               Paths.get(uri).resolve(newFilePath));

        Plugin plugin = new Plugin(
            newFile.toPath().getParent().getFileName().toString(),
            TypeConverterUtil.fromPath(pluginPath),
            pluginPath);

        pluginAddedEvent.fire(new PluginAdded(plugin, sessionInfo));
    }

    private void fireNavigationEvent() {
        navTreeChangedEvent.fire(
            new NavTreeChangedEvent(
                navTreeStorage.loadNavTree()));
    }

    private FileSystem getImportFileSystem(File file, String tempPath) {
        List<FileSystem> fileSystems = Arrays.asList(datasetsFS, perspectivesFS, navigationFS);
        String filePath = file.toURI().toString();

        return fileSystems.stream()
            .filter(fs -> filePath.contains(tempPath + fs.getName()))
            .findFirst()
            .orElse(null);
    }

    private void moveZipToFileSystem(String zipLocation, FileSystem fileSystem) {
        String sourceLocation = new StringBuilder()
            .append(SpacesAPI.Scheme.FILE)
            .append("://")
            .append(zipLocation)
            .toString();

        Path source = Paths.get(URI.create(sourceLocation));

        Path target = Paths.get(
        	URI.create(
        		new StringBuilder()
        			.append(SpacesAPI.Scheme.GIT)
		        	.append("://")
		        	.append(fileSystem.getName())
		        	.append(File.separator)
		        	.append(FILE_PATH)
		        	.append(File.separator)
		            .append(EXPORT_FILE_NAME)
		            .toString()));

        ioService.write(target, Files.readAllBytes(source));

        Files.delete(source);
    }

    private File unzipFile(File destinationDir, ZipEntry zipEntry, ZipInputStream zis) throws Exception {
        File destFile = new File(destinationDir, zipEntry.getName());

        if (!destFile.exists()) {
            destFile.getParentFile().mkdirs();
            destFile.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(destFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();

        return destFile;
    }

    private void zipFileSystem(FileSystem fs, ZipOutputStream zos) {
        Path root = fs.getRootDirectories().iterator().next();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                try {
                    String location = fs.getName() + path.toString();
                    zipFile(path.toFile(), location, zos);
                    return FileVisitResult.CONTINUE;

                } catch (Exception e) {
                    e.printStackTrace();
                    return FileVisitResult.TERMINATE;
                }
            }
        });
    }

    private void zipFile(File file, String path, ZipOutputStream zos) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(path);
        zos.putNextEntry(zipEntry);

        int length;
        while ((length = fis.read(buffer)) >= 0) {
            zos.write(buffer, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    private File createVersionFile() throws Exception {
        File version = File.createTempFile("temp", "version");
        BufferedWriter out = new BufferedWriter(new FileWriter(version));
        out.write("1.0.0");
        out.close();
        return version;
    }
}
