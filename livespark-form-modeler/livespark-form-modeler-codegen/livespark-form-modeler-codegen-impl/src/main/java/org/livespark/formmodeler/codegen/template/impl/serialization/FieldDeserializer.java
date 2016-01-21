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
package org.livespark.formmodeler.codegen.template.impl.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.service.FieldManager;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by pefernan on 7/9/15.
 */
@Dependent
public class FieldDeserializer implements JsonDeserializer<FieldDefinition> {

    @Inject
    private FieldManager fieldManager;

    @Override
    public FieldDefinition deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        JsonArray ja = json.getAsJsonArray();

        for (JsonElement je : ja) {

            String typeCode = je.getAsJsonObject().get("code").getAsString();

            FieldDefinition definition = fieldManager.getDefinitionByTypeCode(typeCode);

            return context.deserialize( je, definition.getClass() );
        }

        return null;
    }
}
