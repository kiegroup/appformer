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
package org.livespark.formmodeler.editor.client.editor.fields;

import org.livespark.formmodeler.editor.model.impl.basic.ByteBoxFieldDefinition;

import javax.enterprise.context.Dependent;

/**
 * Created by pefernan on 7/27/15.
 */
@Dependent
public class ByteBoxLayoutComponent extends AbstractInputLayoutComponent<ByteBoxFieldDefinition> {

    public ByteBoxLayoutComponent() {
    }

    public ByteBoxLayoutComponent( String formId, ByteBoxFieldDefinition fieldDefinition ) {
        init( formId, fieldDefinition );
    }

    @Override
    public ByteBoxLayoutComponent newInstance( String formId, ByteBoxFieldDefinition fieldDefinition ) {
        return new ByteBoxLayoutComponent( formId, fieldDefinition );
    }

    @Override
    public String getSupportedFieldDefinition() {
        return ByteBoxFieldDefinition.class.getName();
    }
}
