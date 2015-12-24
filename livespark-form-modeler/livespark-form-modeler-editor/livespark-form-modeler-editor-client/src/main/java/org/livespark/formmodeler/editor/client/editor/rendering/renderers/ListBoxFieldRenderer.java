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
package org.livespark.formmodeler.editor.client.editor.rendering.renderers;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;

import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.ValueListBox;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.ListBoxFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.selectors.SelectorOption;
import org.livespark.formmodeler.rendering.client.view.util.StringListBoxRenderer;

@Dependent
public class ListBoxFieldRenderer extends SelectorFieldRenderer<ListBoxFieldDefinition> {

    protected StringListBoxRenderer optionsRenderer = new StringListBoxRenderer();

    protected ValueListBox<String> widgetList = new ValueListBox<String>(optionsRenderer);

    @Override
    public String getName() {
        return "ListBox";
    }

    @Override
    public IsWidget renderWidget() {
        refreshSelectorOptions();
        FormGroup group = new FormGroup(  );
        FormLabel label = new FormLabel(  );
        widgetList.setEnabled( !field.getReadonly() );
        label.setText( field.getLabel() );
        label.setFor( widgetList.getId() );
        group.add(label);
        group.add( widgetList );
        group.add(new HelpBlock());
        return group;
    }

    @Override
    public String getSupportedFieldDefinitionCode() {
        return ListBoxFieldDefinition._CODE;
    }

    @Override
    protected void refreshSelectorOptions() {
        Map<String, String> optionsValues = new HashMap<String, String>( );
        widgetList.reset();
        if ( field.getOptions() != null ) {

            String defaultValue = null;
            for ( SelectorOption option : field.getOptions() ) {
                optionsValues.put( option.getValue(), option.getText() );
                if ( option.getDefaultValue() ) {
                    defaultValue = option.getValue();
                }
            }

            if ( defaultValue != null ) {
                widgetList.setValue( defaultValue );
            } else {
                widgetList.setValue( "" );
            }
        }
        optionsRenderer.setValues( optionsValues );
        widgetList.setAcceptableValues( optionsValues.keySet() );
    }
}
