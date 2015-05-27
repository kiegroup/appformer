package org.livespark.formmodeler.model;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by pefernan on 4/21/15.
 */
@Portable
public class DataHolder {
    private String name;
    private String type;
    private List<FieldDefinition> fields;

    public DataHolder( @MapsTo("name") String name,
                       @MapsTo("type") String type,
                       @MapsTo("fields") List<FieldDefinition> fields ) {
        this.name = name;
        this.type = type;
        this.setFields( fields );
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

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public void setFields( List<FieldDefinition> fields ) {
        this.fields = fields;
    }


}
