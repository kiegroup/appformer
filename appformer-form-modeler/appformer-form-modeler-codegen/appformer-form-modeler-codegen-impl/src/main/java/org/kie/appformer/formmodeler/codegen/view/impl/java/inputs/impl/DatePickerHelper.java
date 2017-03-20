/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.impl.java.RequiresCustomCode;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.datePicker.definition.DatePickerFieldDefinition;

import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.INIT_FORM_METHOD;

public class DatePickerHelper extends AbstractInputCreatorHelper<DatePickerFieldDefinition> implements RequiresCustomCode<DatePickerFieldDefinition> {

    public static String DATE_TIME_PICKER_NAME = "DateTimePicker";
    public static String DATE_TIME_PICKER_CLASS_NAME = "org.gwtbootstrap3.extras.datetimepicker.client.ui." + DATE_TIME_PICKER_NAME;

    public static String DATE_PICKER_NAME = "DatePicker";
    public static String DATE_PICKER_CLASS_NAME = "org.gwtbootstrap3.extras.datepicker.client.ui." + DATE_PICKER_NAME;

    @Override
    public String getSupportedFieldTypeCode() {
        return DatePickerFieldDefinition.FIELD_TYPE.getTypeName();
    }

    @Override
    public String getInputWidget(DatePickerFieldDefinition fieldDefinition) {
        if (fieldDefinition.getShowTime()) {
            return DATE_TIME_PICKER_CLASS_NAME;
        } else {
            return DATE_PICKER_CLASS_NAME;
        }
    }

    @Override
    public void addCustomCode(DatePickerFieldDefinition fieldDefinition,
                              SourceGenerationContext context,
                              JavaClassSource viewClass) {
        final MethodSource<JavaClassSource> initMethod = viewClass.getMethod(INIT_FORM_METHOD);
        final StringBuffer body = new StringBuffer(initMethod.getBody() == null ? "" : initMethod.getBody());

        body.append(fieldDefinition.getName())
                .append(".setAutoClose( true );")
                .append(fieldDefinition.getName())
                .append(".setHighlightToday( true );")
                .append(fieldDefinition.getName())
                .append(".setShowTodayButton( true );");

        initMethod.setBody(body.toString());
    }
}
