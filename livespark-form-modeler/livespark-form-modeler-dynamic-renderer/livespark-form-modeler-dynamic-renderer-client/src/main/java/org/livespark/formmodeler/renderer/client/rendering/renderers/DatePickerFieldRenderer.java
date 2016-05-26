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
package org.livespark.formmodeler.renderer.client.rendering.renderers;

import javax.enterprise.context.Dependent;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.extras.datepicker.client.ui.DatePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.livespark.formmodeler.model.impl.basic.DateBoxFieldDefinition;
import org.livespark.formmodeler.renderer.client.rendering.FieldRenderer;

/**
 * Created by pefernan on 9/21/15.
 */
@Dependent
public class DatePickerFieldRenderer extends FieldRenderer<DateBoxFieldDefinition> {

    private  Widget input;

    @Override
    public String getName() {
        return "DatePicker";
    }

    @Override
    public void initInputWidget() {
        input = getDateWidget();
    }

    @Override
    public IsWidget getInputWidget() {
        return input;
    }

    protected Widget getDateWidget() {
        if ( field.getShowTime() ) {
            DateTimePicker box = new DateTimePicker();
            box.setPlaceholder( field.getPlaceHolder() );
            box.setEnabled( !field.getReadonly() );
            box.setAutoClose( true );
            return box;
        }
        DatePicker box = new DatePicker();
        box.setPlaceholder( field.getPlaceHolder() );
        box.setEnabled( !field.getReadonly() );
        box.setAutoClose( true );
        return box;
    }

    @Override
    public String getSupportedFieldDefinitionCode() {
        return DateBoxFieldDefinition.CODE;
    }
}
