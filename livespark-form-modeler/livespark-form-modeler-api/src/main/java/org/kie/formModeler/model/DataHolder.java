package org.kie.formModeler.model;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by pefernan on 4/21/15.
 */
@Portable
public class DataHolder {
    private String name;
    private String type;

    public DataHolder( @MapsTo( "name" ) String name, @MapsTo( "type" ) String type ) {
        this.name = name;
        this.type = type;
    }

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


}
