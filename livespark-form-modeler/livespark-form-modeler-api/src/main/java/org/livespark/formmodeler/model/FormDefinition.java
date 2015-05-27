package org.livespark.formmodeler.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class FormDefinition {
    private String name;

    private List<DataHolder> dataHolders = new ArrayList<DataHolder>(  );

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public List<DataHolder> getDataHolders() {
        return dataHolders;
    }

    @SuppressWarnings("rawtypes")
    public List<FieldDefinition> getFields() {
        int numFields = getNumberOfFields();
        if ( numFields == 0 ) return Collections.<FieldDefinition>emptyList();

        List<FieldDefinition> fields = new ArrayList<FieldDefinition>( numFields );
        for ( DataHolder holder : dataHolders ) {
            fields.addAll( holder.getFields() );
        }

        return fields;
    }

    private int getNumberOfFields() {
        int accum = 0;
        for ( DataHolder holder : dataHolders ) {
            accum += holder.getFields().size();
        }

        return accum;
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
}
