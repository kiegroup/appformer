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

package org.livespark.formmodeler.codegen.view.impl.inputs.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.livespark.formmodeler.codegen.view.impl.inputs.AbstractInputHelperTest;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.CheckBoxHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.DatePickerHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.SliderHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.TextAreaHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.TextBoxHelper;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.impl.basic.checkBox.CheckBoxFieldDefinition;
import org.kie.workbench.common.forms.model.impl.basic.datePicker.DatePickerFieldDefinition;
import org.kie.workbench.common.forms.model.impl.basic.slider.DoubleSliderDefinition;
import org.kie.workbench.common.forms.model.impl.basic.slider.IntegerSliderDefinition;
import org.kie.workbench.common.forms.model.impl.basic.textArea.TextAreaFieldDefinition;
import org.kie.workbench.common.forms.model.impl.basic.textBox.CharacterBoxFieldDefinition;
import org.kie.workbench.common.forms.model.impl.basic.textBox.TextBoxFieldDefinition;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public class BasicInputHelpersTest extends AbstractInputHelperTest {

    @Override
    protected List<FieldDefinition> getFieldsToTest() {
        List<FieldDefinition> fields = new ArrayList<>();

        fields.add( initFieldDefinition( new TextBoxFieldDefinition() ) );
        fields.add( initFieldDefinition( new CheckBoxFieldDefinition() ) );
        fields.add( initFieldDefinition( new CharacterBoxFieldDefinition() ) );
        fields.add( initFieldDefinition( new DatePickerFieldDefinition() ) );

        DatePickerFieldDefinition date = new DatePickerFieldDefinition();
        date.setShowTime( Boolean.FALSE );
        fields.add( initFieldDefinition( date ) );

        fields.add( initFieldDefinition( new IntegerSliderDefinition() ) );
        fields.add( initFieldDefinition( new DoubleSliderDefinition() ) );
        fields.add( initFieldDefinition( new TextAreaFieldDefinition() ) );

        return fields;
    }

    @Override
    protected List<InputCreatorHelper> getInputHelpersToTest() {
        List<InputCreatorHelper> helpers = new ArrayList<>();

        helpers.add( new TextBoxHelper() );
        helpers.add( new CheckBoxHelper() );
        helpers.add( new DatePickerHelper() );
        helpers.add( new SliderHelper() );
        helpers.add( new TextAreaHelper() );

        return helpers;
    }
}
