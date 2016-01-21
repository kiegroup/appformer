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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.Radio;
import org.livespark.formmodeler.model.impl.basic.selectors.RadioGroupFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.selectors.SelectorOption;
import org.livespark.formmodeler.renderer.client.rendering.renderers.bs3.StringRadioGroup;

@Dependent
public class RadioGroupFieldRenderer extends SelectorFieldRenderer<RadioGroupFieldDefinition> {

    private StringRadioGroup input;

    @Override
    public String getName() {
        return "RadioGroup";
    }

    @Override
    public void initInputWidget() {
        input = new StringRadioGroup( field.getName() );
        refreshSelectorOptions();
    }

    @Override
    public IsWidget getInputWidget() {
        return input;
    }

    @Override
    public String getSupportedFieldDefinitionCode() {
        return RadioGroupFieldDefinition._CODE;
    }

    @Override
    public void refreshSelectorOptions() {
        if ( field != null ) {
            input.clear();
            for ( SelectorOption option : field.getOptions() ) {
                Radio radio;
                if ( field.getInline() ) {
                    radio = new InlineRadio( field.getId(), getOptionLabel( option ) );
                } else {
                    radio = new Radio( field.getId(), getOptionLabel( option ) );
                }
                radio.setValue( option.getDefaultValue() );
                radio.setFormValue( option.getValue() );
                input.add( radio );
            }
        }
    }

    protected SafeHtml getOptionLabel( SelectorOption option ) {
        if ( option.getText() == null || option.getText().isEmpty() ) {
            return SafeHtmlUtils.fromTrustedString( "&nbsp;" );
        }
        return SafeHtmlUtils.fromString( option.getText() );
    }
}
