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

package org.livespark.formmodeler.codegen.view.impl.java.inputs;

import org.livespark.formmodeler.editor.model.impl.basic.DateBoxFieldDefinition;

/**
 * Created by pefernan on 4/28/15.
 */
public class DateBoxHelper extends AbstractInputCreatorHelper {

    @Override
    public String getSupportedFieldTypeCode() {
        return DateBoxFieldDefinition._CODE;
    }

    @Override
    public String getInputWidget() {
        return "org.gwtbootstrap3.extras.datepicker.client.ui.DatePicker";
    }
}
