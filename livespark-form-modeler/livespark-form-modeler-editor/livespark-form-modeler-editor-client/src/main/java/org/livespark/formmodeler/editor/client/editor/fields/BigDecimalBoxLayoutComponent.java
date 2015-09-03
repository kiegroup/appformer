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
import org.livespark.formmodeler.editor.model.impl.basic.BigDecimalBoxFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.TextBoxFieldDefinition;
import org.uberfire.ext.properties.editor.model.PropertyEditorCategory;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;
import org.uberfire.ext.properties.editor.model.PropertyEditorType;

/**
 * Created by pefernan on 7/27/15.
 */
@Dependent
public class BigDecimalBoxLayoutComponent extends AbstractInputLayoutComponent<BigDecimalBoxFieldDefinition> {

    public BigDecimalBoxLayoutComponent() {
    }

    public BigDecimalBoxLayoutComponent( String formId, BigDecimalBoxFieldDefinition fieldDefinition ) {
        init( formId, fieldDefinition );
    }

    @Override
    public BigDecimalBoxLayoutComponent newInstance( String formId, BigDecimalBoxFieldDefinition fieldDefinition ) {
        return new BigDecimalBoxLayoutComponent( formId, fieldDefinition );
    }

    @Override
    public String getSupportedFieldDefinition() {
        return BigDecimalBoxFieldDefinition.class.getName();
    }
}
