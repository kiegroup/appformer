package org.uberfire.security.exceptions;

import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.security.ResourceAction;
import org.uberfire.security.ResourceType;

public class NotAuthorizedException extends RuntimeException {

    public NotAuthorizedException(User identity,
                                  ResourceType resourceType,
                                  ResourceAction resourceAction) {
        super("User :user has no permissions to :type -> :action"
                      .replace(":user",
                               identity.getIdentifier())
                      .replace(":type",
                               resourceType.getName())
                      .replace(":action",
                               resourceAction.getName()));
    }
}
