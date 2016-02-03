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
package org.livespark.formmodeler.model.impl.basic.selectors;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.metaModel.FieldDef;

/**
 * Created by pefernan on 10/2/15.
 */
public abstract class SelectorField extends FieldDefinition {

    @FieldDef( label = "Options")
    @NotNull
    @NotEmpty
    protected List<SelectorOption> options = new ArrayList<SelectorOption>();

    protected String dataProvider = "";

    public List<SelectorOption> getOptions() {
        return options;
    }

    public void setOptions(List<SelectorOption> options) {
        this.options = options;
    }

    public String getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider( String dataProvider ) {
        this.dataProvider = dataProvider;
    }

    @Override
    protected void doCopyFrom( FieldDefinition other ) {
        if ( other instanceof  SelectorField ) {
            this.setOptions( ((SelectorField) other).getOptions() );
            this.setDataProvider( ((SelectorField) other).getDataProvider() );
        }
    }
}
