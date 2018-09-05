package org.uberfire.experimental.client.workbench.type.test;

import org.uberfire.backend.vfs.Path;
import org.uberfire.client.workbench.type.ClientResourceType;
import org.uberfire.workbench.category.Category;

public class WrongClientResourceType implements ClientResourceType {

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public String getSuffix() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getSimpleWildcardPattern() {
        return null;
    }

    @Override
    public boolean accept(Path path) {
        return false;
    }

    @Override
    public Category getCategory() {
        return null;
    }
}
