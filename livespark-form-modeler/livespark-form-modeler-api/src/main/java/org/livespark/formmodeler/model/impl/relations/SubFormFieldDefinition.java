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

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.annotation.FieldDef;

/**
 * Created by pefernan on 7/1/15.
 */
@Portable
@Bindable
public class SubFormFieldDefinition extends FieldDefinition implements EmbeddedFormField {
    public static final String _CODE = "SubForm";

    private String code = _CODE;

    @FieldDef( label = "Default Form")
    protected String nestedForm = "";

    protected String embeddedFormView = "";
    protected String embeddedModel = "";

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

    public String getEmbeddedFormView() {
        return embeddedFormView;
    }

    public void setEmbeddedFormView( String embeddedFormView ) {
        this.embeddedFormView = embeddedFormView;
    }

    public String getEmbeddedModel() {
        return embeddedModel;
    }

    public void setEmbeddedModel( String embeddedModel ) {
        this.embeddedModel = embeddedModel;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[] {
                Object.class.getName()
        };
    }

    @Override
    protected void doCopyFrom(FieldDefinition other) {
        if ( other instanceof EmbeddedFormField ) {
            EmbeddedFormField otherForm = (EmbeddedFormField) other;
            setEmbeddedModel( otherForm.getEmbeddedModel() );
            setEmbeddedFormView( otherForm.getEmbeddedFormView() );
        }
        setStandaloneClassName( other.getStandaloneClassName() );
    }
}
