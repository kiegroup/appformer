/*
 * Copyright 2015 JBoss Inc
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

package org.livespark.project;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

import org.guvnor.common.services.project.model.Project;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.IOException;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.NoSuchFileException;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.Paths;

public class ProjectUnpacker {

    private final org.uberfire.java.nio.file.DirectoryStream.Filter<Path> filter;
    private final IOService ioService;

    public ProjectUnpacker( IOService ioService, org.uberfire.java.nio.file.DirectoryStream.Filter<Path> filter ) {
        this.ioService = ioService;
        this.filter = filter;
    }

    public void writeSourceFileSystemToDisk( Project project,
                                             org.uberfire.java.nio.file.Path tmpRoot ) throws NoSuchFileException,
                                                                                       IllegalArgumentException,
                                                                                       UnsupportedOperationException,
                                                                                       IOException,
                                                                                       SecurityException,
                                                                                       java.io.IOException {
        org.uberfire.backend.vfs.Path root = project.getRootPath();
        DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream = Files.newDirectoryStream( org.uberfire.backend.server.util.Paths.convert( root ) );

        visitPaths( directoryStream, root.toURI(), tmpRoot );
    }

    private org.uberfire.java.nio.file.Path visitPaths( DirectoryStream<org.uberfire.java.nio.file.Path> directoryStream,
                                                        String projectPrefix,
                                                        org.uberfire.java.nio.file.Path tmp ) throws NoSuchFileException,
                                                                                              IllegalArgumentException,
                                                                                              UnsupportedOperationException,
                                                                                              IOException,
                                                                                              SecurityException,
                                                                                              java.io.IOException {

        for ( final org.uberfire.java.nio.file.Path path : directoryStream ) {
            final String destinationPath = filterPrefix( projectPrefix,
                                                         path );
            if ( Files.isDirectory( path ) ) {
                new File( tmp.toFile(), destinationPath ).mkdir();
                visitPaths( Files.newDirectoryStream( path ),
                            projectPrefix,
                            tmp );
            } else {
                //Don't process dotFiles
                if ( !filter.accept( path ) ) {
                    //Add new resource
                    final InputStream is = ioService.newInputStream( path );
                    final BufferedInputStream bis = new BufferedInputStream( is );
                    writePath( destinationPath,
                               bis,
                               tmp.toUri().toString() );
                }
            }
        }

        return tmp;
    }

    private String filterPrefix( String pathPrefix,
                                 org.uberfire.java.nio.file.Path path ) {
        return path.toUri().toString().substring( pathPrefix.length() + 1 );
    }

    private void writePath( String destinationPath,
                            BufferedInputStream bis,
                            String outputRoot ) {
        org.uberfire.java.nio.file.Path fullPath = Paths.get( outputRoot + "/" + destinationPath );
        ioService.copy( bis,
                        fullPath );
    }

}
