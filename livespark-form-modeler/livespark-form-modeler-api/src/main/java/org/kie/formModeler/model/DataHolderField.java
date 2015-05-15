package org.kie.formModeler.model;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by pefernan on 4/22/15.
 */
@Portable
public class DataHolderField {
    private String name;
    private String type;
    private String bag;

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }

    public String getBag() {
        return bag;
    }

    public void setBag( String bag ) {
        this.bag = bag;
    }
}
