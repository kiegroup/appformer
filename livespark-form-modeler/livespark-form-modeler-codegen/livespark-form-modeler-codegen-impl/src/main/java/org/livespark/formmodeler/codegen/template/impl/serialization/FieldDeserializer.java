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
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.livespark.formmodeler.editor.model.FieldDefinition;

/**
 * Created by pefernan on 7/9/15.
 */
public class FieldDeserializer implements JsonDeserializer<FieldDefinition> {

    @Override
    public FieldDefinition deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        JsonArray ja = json.getAsJsonArray();

        for (JsonElement je : ja) {

            String type = je.getAsJsonObject().get("code").getAsString();
            Class c = null;
            try {
                c = Class.forName( type );
                return context.deserialize( je, c );
            } catch ( ClassNotFoundException e ) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
