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

import javax.enterprise.context.Dependent;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.FormLabel;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.datepicker.client.DatePicker;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.DateBoxFieldDefinition;
import org.uberfire.ext.layout.editor.client.components.RenderingContext;

/**
 * Created by pefernan on 7/27/15.
 */
@Dependent
public class DateBoxLayoutComponent extends FieldLayoutComponent<DateBoxFieldDefinition>  {

    public DateBoxLayoutComponent() {
    }

    public DateBoxLayoutComponent( String formId, DateBoxFieldDefinition fieldDefinition ) {
        init( formId, fieldDefinition );
    }

    @Override
    public IsWidget generateWidget() {
        if (fieldDefinition == null) return null;

        ControlGroup group = new ControlGroup(  );
        Controls controls = new Controls();
        FormLabel label = new FormLabel( fieldDefinition.getLabel() );
        DatePicker box = new DatePicker();
        label.setFor( box.getElement().getId() );
        controls.add( label );
        controls.add( box );
        group.add( controls );
        group.add( new HelpBlock(  ) );
        return group;
    }

    @Override
    public DateBoxLayoutComponent newInstance( String formId, DateBoxFieldDefinition fieldDefinition ) {
        return new DateBoxLayoutComponent( formId, fieldDefinition );
    }

    @Override
    public String getSupportedFieldDefinition() {
        return DateBoxFieldDefinition.class.getName();
    }
}
