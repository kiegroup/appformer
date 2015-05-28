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