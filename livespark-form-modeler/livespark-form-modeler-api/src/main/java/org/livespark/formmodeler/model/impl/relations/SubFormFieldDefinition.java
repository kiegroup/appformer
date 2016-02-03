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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.metaModel.ListBox;
import org.livespark.formmodeler.metaModel.SelectorDataProvider;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.metaModel.FieldDef;

/**
 * Created by pefernan on 7/1/15.
 */
@Portable
@Bindable
public class SubFormFieldDefinition extends FieldDefinition implements EmbeddedFormField {
    public static final String _CODE = "SubForm";

    private String code = _CODE;

    @FieldDef( label = "Nested Form")
    @ListBox( provider = @SelectorDataProvider(
            type = SelectorDataProvider.ProviderType.REMOTE,
            className = "org.livespark.formmodeler.editor.backend.dataProviders.VFSSelectorFormProvider"))
    @NotNull
    @NotEmpty
    protected String nestedForm = "";

    @Override
    public String getCode() {
        return code;
    }

    public String getNestedForm() {
        return nestedForm;
    }

    public void setNestedForm( String nestedForm ) {
        this.nestedForm = nestedForm;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[] {
                Object.class.getName()
        };
    }

    @Override
    protected void doCopyFrom( FieldDefinition other ) {
        if ( other instanceof SubFormFieldDefinition ) {
            SubFormFieldDefinition otherForm = (SubFormFieldDefinition) other;
            otherForm.setNestedForm( nestedForm );
        }
        setStandaloneClassName( other.getStandaloneClassName() );
    }
}
