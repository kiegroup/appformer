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

package org.livespark.formmodeler.codegen.view.impl.html.inputs;

import org.livespark.formmodeler.codegen.view.impl.html.InputTemplateProvider;
import org.livespark.formmodeler.editor.model.impl.basic.DateBoxFieldDefinition;

import java.io.InputStream;

/**
 * Created by pefernan on 4/29/15.
 */
public class DateBoxTemplate implements InputTemplateProvider {

    @Override
    public String getSupportedFieldType() {
        return DateBoxFieldDefinition.class.getName();
    }

    @Override
    public InputStream getTemplateInputStream() {
        return getClass().getResourceAsStream( "/org/livespark/formmodeler/codegen/view/impl/html/templates/datebox.mv" );
    }
}
