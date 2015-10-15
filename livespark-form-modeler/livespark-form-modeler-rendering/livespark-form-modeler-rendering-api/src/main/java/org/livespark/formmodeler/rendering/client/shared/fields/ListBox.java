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
package org.livespark.formmodeler.rendering.client.shared.fields;

import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Created by pefernan on 10/2/15.
 */
public class ListBox extends org.gwtbootstrap3.client.ui.ListBox implements HasValue<String> {

    public ListBox() {
        this.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                ValueChangeEvent.fire(ListBox.this, getSelectedValue());
            }
        });
    }

    @Override
    public String getValue() {
        return super.getSelectedValue();
    }

    @Override
    public void setValue(String value) {
        setValue( value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvent) {
        SelectElement selectElement= getElement().cast();

        for (int i = 0; i < selectElement.getOptions().getLength(); i ++) {
            OptionElement option = selectElement.getOptions().getItem( i );

            if (option.getValue().equals(value)) {
                setSelectedIndex( i );
                if (fireEvent) {
                    ValueChangeEvent.fire(this, value);
                }
                return;
            }
        }
        setSelectedIndex(-1);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> valueChangeHandler) {
        return addHandler( valueChangeHandler, ValueChangeEvent.getType());
    }
}
