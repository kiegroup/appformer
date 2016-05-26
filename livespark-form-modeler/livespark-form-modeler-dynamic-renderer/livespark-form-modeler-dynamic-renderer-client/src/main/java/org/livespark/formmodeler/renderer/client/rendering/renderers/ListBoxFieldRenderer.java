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

import java.util.Map;
import javax.enterprise.context.Dependent;

import com.google.gwt.user.client.ui.IsWidget;
import org.gwtbootstrap3.client.ui.ValueListBox;
import org.livespark.formmodeler.model.impl.basic.selectors.ListBoxFieldDefinition;
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
    public void initInputWidget() {
        widgetList.setEnabled( !field.getReadonly() );
        widgetList.reset();
        refreshSelectorOptions();
    }

    @Override
    public IsWidget getInputWidget() {
        return widgetList;
    }

    @Override
    public String getSupportedFieldDefinitionCode() {
        return ListBoxFieldDefinition.CODE;
    }

    protected void refreshInput( Map<String, String> optionsValues, String defaultValue) {

        if ( defaultValue != null ) {
            widgetList.setValue( defaultValue );
        }

        optionsRenderer.setValues( optionsValues );
        widgetList.setAcceptableValues( optionsValues.keySet() );
    }
}
