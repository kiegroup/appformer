package org.kie.formModeler.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class FormDefinition {
    private String name;

    private List<DataHolder> dataHolders = new ArrayList<DataHolder>(  );

    private List<FieldDefinition> fields = new ArrayList<FieldDefinition>(  );

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public List<DataHolder> getDataHolders() {
        return dataHolders;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public void addDataHolder (DataHolder dataH) {
        dataHolders.add( dataH );
    }

    public void removeDataHolder( String holderName ) {
        for (Iterator<DataHolder> it = dataHolders.iterator(); it.hasNext();) {
            DataHolder dataHolder = it.next();
            if (dataHolder.getName().equals( holderName ) ) {
                it.remove();
                return;
            }
        }
    }

    public void addField( FieldDefinition field ) {
        this.fields.add( field );
    }

    public void removeField( String fieldId) {
        for (Iterator<FieldDefinition> it = fields.iterator(); it.hasNext();) {
            FieldDefinition definition = it.next();
            if (definition.getName().equals( fieldId ) ) {
                it.remove();
                return;
            }
        }
    }
}
