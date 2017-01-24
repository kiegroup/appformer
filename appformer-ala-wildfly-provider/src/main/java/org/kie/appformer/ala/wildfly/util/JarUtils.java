/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.ala.wildfly.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for manipulating jar files.
 */
public class JarUtils {

    private static final Logger logger = LoggerFactory.getLogger( JarUtils.class );

    /**
     * Gets a String based entry from a given jar.
     *
     * @param jarFilePath Path to the source jar file.
     *
     * @param entry path to the entry, e.g. "WEB-INF/classes/META-INF/persistence.xml"
     *
     * @return The String content for the entry.
     *
     * @throws IOException
     */
    public static String getStrEntry( Path jarFilePath, String entry ) throws IOException {
        JarFile jarFile = new JarFile( jarFilePath.toFile( ) );
        try {
            JarEntry jarEntry = jarFile.getJarEntry( entry );
            if ( jarEntry != null ) {
                StringBuilder builder = new StringBuilder( );
                BufferedReader reader = new BufferedReader( new InputStreamReader( jarFile.getInputStream( jarEntry ),
                        StandardCharsets.UTF_8  ) );
                reader.lines( ).forEach( builder::append );
                safeClose( reader );
                return builder.toString( );
            } else {
                return null;
            }
        } finally {
            safeClose( jarFile );
        }
    }

    /**
     * Adds or replaces a String entry in the target jar file.
     *
     * @param jarFilePath Path to the target file.
     *
     * @param entry path to the entry, e.g. "WEB-INF/classes/META-INF/persistence.xml"
     *
     * @param content The String content for the entry.
     *
     * @throws IOException
     */
    public static void addStrEntry( Path jarFilePath, String entry, String content ) throws IOException {

        JarFile jarFile = new JarFile( jarFilePath.toFile( ) );
        Path tmpJarFilePath;
        JarOutputStream tmpJarOutputStream;
        try {
            tmpJarFilePath = Files.createTempFile( jarFilePath.getFileName( ).toString( ), ".tmp" );
            tmpJarOutputStream = new JarOutputStream( Files.newOutputStream( tmpJarFilePath ) );
        } catch ( IOException e ) {
            logger.error( "It was not possible to create the tmp target file: " +
                    jarFilePath.getFileName( ).toString( ), ".tmp", e );
            safeClose( jarFile );
            throw e;
        }

        try {
            // add the new entry.
            JarEntry newJarEntry = new JarEntry( entry );
            tmpJarOutputStream.putNextEntry( newJarEntry );
            tmpJarOutputStream.write( content.getBytes( ) );

            //Copy the original entries into the result skipping the new one.
            Enumeration< JarEntry > entries = jarFile.entries( );
            JarEntry jarEntry;
            while ( entries.hasMoreElements( ) ) {
                jarEntry = entries.nextElement( );
                if ( !jarEntry.getName( ).equals( entry ) ) {
                    InputStream entryInputStream = jarFile.getInputStream( jarEntry );
                    tmpJarOutputStream.putNextEntry( jarEntry );
                    byte[] buffer = new byte[ 1024 ];
                    int bytesRead;
                    while ( ( bytesRead = entryInputStream.read( buffer ) ) != -1 ) {
                        tmpJarOutputStream.write( buffer, 0, bytesRead );
                    }
                    entryInputStream.close( );
                }
            }
        } catch ( IOException e ) {
            logger.error( "It was not possible to create the tmp target file content for file:" + tmpJarFilePath.toUri( ), e );
            throw e;
        } finally {
            safeClose( jarFile );
            safeClose( tmpJarOutputStream );
        }

        try {
            //swap the files.
            Files.copy( tmpJarFilePath, jarFilePath, StandardCopyOption.REPLACE_EXISTING );
        } catch ( Exception e ) {
            logger.error( "It was not possible to replace file content for file: " + jarFilePath.toUri( ) );
            throw e;
        } finally {
            try {
                Files.delete( tmpJarFilePath );
            } catch ( Exception e ) {
                logger.warn( "An error was produced during tmp file removal: " + tmpJarFilePath );
            }
        }
    }

    private static final void safeClose( Closeable closeable ) {
        try {
            if ( closeable != null ) {
                closeable.close( );
            }
        } catch ( Exception e ) {
            // nothing to do
            logger.error( "A error was produced during close action for the closeable: " + closeable, e );
        }
    }
}