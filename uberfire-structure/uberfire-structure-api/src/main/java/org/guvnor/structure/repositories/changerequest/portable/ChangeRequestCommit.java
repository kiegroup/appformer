package org.guvnor.structure.repositories.changerequest.portable;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class ChangeRequestCommit {

    private String id;
    private String message;

    public ChangeRequestCommit() {
    }

    public ChangeRequestCommit(final String id,
                               final String message) {
        this.id = id;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
