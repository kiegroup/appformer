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
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.user.client.ui.IsWidget;
import org.livespark.formmodeler.editor.model.impl.basic.TextBoxFieldDefinition;

/**
 * Created by pefernan on 7/27/15.
 */
@Dependent
public class TextBoxLayoutComponent extends FieldLayoutComponent<TextBoxFieldDefinition> {

    public TextBoxLayoutComponent() {
    }

    public TextBoxLayoutComponent( String formUri, TextBoxFieldDefinition fieldDefinition ) {
        init( formUri, fieldDefinition );
    }

    @Override
    public IsWidget generateWidget() {
        if (fieldDefinition == null) return null;

        ControlGroup group = new ControlGroup(  );
        Controls controls = new Controls();
        FormLabel label = new FormLabel( fieldDefinition.getLabel() );
        TextBox box = new TextBox();
        label.setFor( box.getId() );
        controls.add( label );
        controls.add( box );
        group.add( controls );
        group.add( new HelpBlock(  ) );
        return group;
    }
}
