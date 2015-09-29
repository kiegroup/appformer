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
package org.livespark.formmodeler.codegen.template.impl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.livespark.formmodeler.codegen.template.FormTemplateGenerator;
import org.livespark.formmodeler.codegen.template.impl.serialization.FieldDeserializer;
import org.livespark.formmodeler.codegen.template.impl.serialization.FieldSerializer;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.FormDefinition;

/**
 * Created by pefernan on 8/31/15.
 */
@Dependent
public class FormTemplateGeneratorImpl implements FormTemplateGenerator {

    @Inject
    private FieldDeserializer fieldDeserializer;

    @Inject
    private FieldSerializer fieldSerializer;

    @Override
    public String generateFormTemplate( FormDefinition form ) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter( FieldDefinition.class, fieldDeserializer );
        builder.registerTypeAdapter( FieldDefinition.class, fieldSerializer );

        Gson gson = builder.create();

        return gson.toJson( form );
    }

    @Override
    public FormDefinition parseFormTemplate( String template ) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter( FieldDefinition.class, fieldDeserializer );
        builder.registerTypeAdapter( FieldDefinition.class, fieldSerializer );

        Gson gson = builder.create();

        return gson.fromJson( template, FormDefinition.class );
    }
}
