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

package org.livespark.client.handler;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.uberfire.backend.vfs.Path;

@Portable
public class AppendedPath implements Path {

    private final String toAppend;
    private final Path path;

    AppendedPath( @MapsTo("toAppend") String toAppend,
                          @MapsTo("path") Path path ) {
        this.toAppend = toAppend;
        this.path = path;
    }

    @Override
    public int compareTo( Path o ) {
        return toURI().compareTo( o.toURI() );
    }

    @Override
    public String toURI() {
        return path.toURI() + toAppend;
    }

    @Override
    public String getFileName() {
        return path.getFileName();
    }
}