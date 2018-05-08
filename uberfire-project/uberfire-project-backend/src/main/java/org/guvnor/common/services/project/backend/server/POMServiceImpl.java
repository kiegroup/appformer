/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.guvnor.common.services.project.backend.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.backend.server.utils.POMContentHandler;
import org.guvnor.common.services.project.model.MavenRepository;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.shared.metadata.MetadataService;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.guvnor.m2repo.service.M2RepoService;
import org.jboss.errai.bus.server.annotations.Service;
import org.uberfire.annotations.Customizable;
import org.uberfire.backend.server.cdi.workspace.WorkspaceScoped;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileAlreadyExistsException;
import org.uberfire.java.nio.file.Files;

@Service
@WorkspaceScoped
public class POMServiceImpl
        implements POMService {

    private IOService ioService;
    private POMContentHandler pomContentHandler;
    private M2RepoService m2RepoService;
    private MetadataService metadataService;
    private MavenXpp3Reader reader;
    private MavenXpp3Writer writer;
    private PomEnhancer pomEnhancer;

    @Inject
    private CommentedOptionFactory optionsFactory;

    public POMServiceImpl() {
        // For Weld
    }

    @Inject
    public POMServiceImpl(final @Named("ioStrategy") IOService ioService,
                          final POMContentHandler pomContentHandler,
                          final M2RepoService m2RepoService,
                          final MetadataService metadataService,
                          final @Customizable PomEnhancer pomEnhancer) {
        this.ioService = ioService;
        this.pomContentHandler = pomContentHandler;
        this.m2RepoService = m2RepoService;
        this.metadataService = metadataService;
        reader = new MavenXpp3Reader();
        writer = new MavenXpp3Writer();
        this.pomEnhancer = pomEnhancer;
    }

    @Override
    public Path create(final Path projectRoot,
                       final String repositoryWebBaseURL,
                       final POM pomModel) {
        org.uberfire.java.nio.file.Path pathToPOMXML = null;
        try {
            final org.uberfire.java.nio.file.Path nioRoot = Paths.convert(projectRoot);
            pathToPOMXML = nioRoot.resolve("pom.xml");
            if (ioService.exists(pathToPOMXML)) {
                throw new FileAlreadyExistsException(pathToPOMXML.toString());
            }
            Model model = getUpdatedPom(pathToPOMXML, repositoryWebBaseURL);
            write(model, pathToPOMXML, ioService);
            //Don't raise a NewResourceAdded event as this is handled at the Project level in ProjectServices
            return Paths.convert(pathToPOMXML);
        } catch (Exception e) {
            throw ExceptionUtilities.handleException(e);
        }
    }

    /*  From PomEditor */

    public Model getUpdatedPom(org.uberfire.java.nio.file.Path pathToPOMXML, String repositoryWebBaseURL) throws Exception{
        Model model = reader.read(new ByteArrayInputStream(Files.readAllBytes(pathToPOMXML)));
        model.getRepositories().add(getRepo(repositoryWebBaseURL));
        return pomEnhancer.execute(model);
    }

    private Repository getRepo(String repositoryWebBaseURL){
        Repository repo = new Repository();
        repo.setId("guvnor-m2-repo");
        repo.setName("Guvnor M2 Repo");
        repo.setUrl(m2RepoService.getRepositoryURL(repositoryWebBaseURL));
        return  repo;
    }

    private boolean write(Model model, org.uberfire.java.nio.file.Path pathToPOMXML, IOService ioService) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writer.write(baos, model);
            ioService.write(pathToPOMXML, new String(baos.toByteArray(), StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                //suppressed
            }
        }
    }

    /*  End from PomEditor */


    @Override
    public POM load(final Path path) {
        try {
            return pomContentHandler.toModel(loadPomXMLString(path));
        } catch (Exception e) {
            throw ExceptionUtilities.handleException(e);
        }
    }

    private String loadPomXMLString(final Path path) {
        final org.uberfire.java.nio.file.Path nioPath = Paths.convert(path);
        return ioService.readAllString(nioPath);
    }

    @Override
    public Path save(final Path path,
                     final POM content,
                     final Metadata metadata,
                     final String comment) {
        try {

            return save(path,
                        content,
                        metadata);
        } catch (Exception e) {
            throw ExceptionUtilities.handleException(e);
        }
    }

    @Override
    public Path save(final Path path,
                     final POM content,
                     final Metadata metadata,
                     final String comment,
                     final boolean updateModules) {

        try {

            ioService.startBatch(Paths.convert(path).getFileSystem(),
                                 optionsFactory.makeCommentedOption(comment != null ? comment : ""));

            save(path,
                 content,
                 metadata);

            saveSubModules(path,
                           content,
                           updateModules);

            return path;
        } catch (Exception e) {
            throw ExceptionUtilities.handleException(e);
        } finally {
            ioService.endBatch();
        }
    }

    private void saveSubModules(final Path path,
                                final POM content,
                                final boolean updateModules) throws IOException, XmlPullParserException {
        if (updateModules &&
                content.isMultiModule() &&
                content.getModules() != null) {
            for (String module : content.getModules()) {

                org.uberfire.java.nio.file.Path childPath = Paths.convert(path).getParent().resolve(module).resolve("pom.xml");

                if (ioService.exists(childPath)) {
                    POM child = load(Paths.convert(childPath));
                    if (child != null) {
                        child.setParent(content.getGav());
                        child.getGav().setGroupId(content.getGav().getGroupId());
                        child.getGav().setVersion(content.getGav().getVersion());

                        save(Paths.convert(childPath),
                             child);
                    }
                }
            }
        }
    }

    private Path save(final Path path,
                      final POM content,
                      final Metadata metadata) throws IOException, XmlPullParserException {
        if (metadata == null) {
            save(path,
                 content);
        } else {
            ioService.write(Paths.convert(path),
                            pomContentHandler.toString(content,
                                                       loadPomXMLString(path)),
                            metadataService.setUpAttributes(path,
                                                            metadata));
        }

        return path;
    }

    private void save(final Path path,
                      final POM content) throws IOException, XmlPullParserException {
        ioService.write(Paths.convert(path),
                        pomContentHandler.toString(content,
                                                   loadPomXMLString(path)));
    }
}
