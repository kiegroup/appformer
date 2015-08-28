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

package org.livespark.formmodeler.editor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class FormDefinition {
    private String id;
    private String name;

    private List<FieldDefinition> fields = new ArrayList<FieldDefinition>(  );
    private List<DataHolder> dataHolders = new ArrayList<DataHolder>(  );

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

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

    public FieldDefinition getFieldByName( String name ) {
        for (FieldDefinition definition : fields ) {
            if (definition.getName().equals( name )) {
                return definition;
            }
        }
        return null;
    }
}
