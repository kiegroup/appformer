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
package org.livespark.formmodeler.editor.model.impl.relations;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.formmodeler.editor.model.FieldDefinition;

/**
 * Created by pefernan on 7/1/15.
 */
@Portable
public class SubFormFieldDefinition extends FieldDefinition implements EmbeddedFormField {
    protected String embeddedFormView = "";
    protected String embeddedModel = "";
    protected String standaloneType = "";

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
}
