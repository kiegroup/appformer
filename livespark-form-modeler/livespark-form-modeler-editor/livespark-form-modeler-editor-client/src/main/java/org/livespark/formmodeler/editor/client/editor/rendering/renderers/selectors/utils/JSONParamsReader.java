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
package org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.utils;

import javax.enterprise.context.Dependent;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import org.livespark.formmodeler.editor.client.editor.rendering.DraggableFieldComponent;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.SelectorOptionFormPresenter;

@Dependent
public class JSONParamsReader implements SelectorOptionFormPresenter.ParamsReader {


    @Override
    public String getFormId( String params ) {
        return readParam( params, DraggableFieldComponent.FORM_ID );
    }

    @Override
    public String getFieldId( String params ) {
        return readParam( params, DraggableFieldComponent.FIELD_ID );
    }

    protected String readParam( String params, String paramToRead ) {
        JSONObject object = JSONParser.parseStrict( params ).isObject();

        if ( object != null ) {
            JSONString paramValue = object.get( paramToRead ).isString();
            if ( paramValue != null && paramValue.isString() != null ) return  paramValue.stringValue();
        }

        return "";
    }
}
