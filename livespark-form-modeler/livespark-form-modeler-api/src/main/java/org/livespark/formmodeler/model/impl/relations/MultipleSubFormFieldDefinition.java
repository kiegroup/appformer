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
package org.livespark.formmodeler.model.impl.relations;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.metaModel.ListBox;
import org.livespark.formmodeler.metaModel.SelectorDataProvider;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.MultipleField;
import org.livespark.formmodeler.metaModel.FieldDef;

/**
 * Created by pefernan on 7/1/15.
 */
@Portable
@Bindable
public class MultipleSubFormFieldDefinition extends FieldDefinition implements EmbeddedFormField, MultipleField {
    public static final String _CODE = "MultipleSubForm";

    private String code = _CODE;

    @FieldDef( label = "Create Form" )
    @ListBox( provider = @SelectorDataProvider(
            type = SelectorDataProvider.ProviderType.REMOTE,
            className = "org.livespark.formmodeler.editor.backend.dataProviders.VFSSelectorFormProvider"))
    @NotEmpty
    protected String creationForm = "";

    @FieldDef( label = "Edit Form")
    @ListBox( provider = @SelectorDataProvider(
            type = SelectorDataProvider.ProviderType.REMOTE,
            className = "org.livespark.formmodeler.editor.backend.dataProviders.VFSSelectorFormProvider"))
    @NotEmpty
    protected String editionForm = "";

    @FieldDef( label = "Table Columns")
    @NotNull
    @NotEmpty
    private List<TableColumnMeta> columnMetas = new ArrayList<TableColumnMeta>();

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[] {
                List.class.getName()
        };
    }

    public List<TableColumnMeta> getColumnMetas() {
        return columnMetas;
    }

    public void setColumnMetas( List<TableColumnMeta> columnMetas ) {
        this.columnMetas = columnMetas;
    }

    public String getCreationForm() {
        return creationForm;
    }

    public void setCreationForm( String creationForm ) {
        this.creationForm = creationForm;
    }

    public String getEditionForm() {
        return editionForm;
    }

    public void setEditionForm( String editionForm ) {
        this.editionForm = editionForm;
    }

    @Override
    protected void doCopyFrom(FieldDefinition other) {
        if ( other instanceof MultipleSubFormFieldDefinition ) {
            MultipleSubFormFieldDefinition otherForm = (MultipleSubFormFieldDefinition) other;
            otherForm.setCreationForm( creationForm );
            otherForm.setEditionForm( editionForm );
            otherForm.setColumnMetas( columnMetas );
        }
        setStandaloneClassName( other.getStandaloneClassName() );
    }
}
