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
package org.livespark.formmodeler.editor.client.editor.rendering;

import com.google.gwt.user.client.Command;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.livespark.formmodeler.editor.client.resources.i18n.FieldProperties;
import org.uberfire.ext.properties.editor.client.PropertyEditorWidget;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKButton;

/**
 * Created by pefernan on 8/6/15.
 */
public class FieldDefinitionPropertiesModal extends BaseModal {

    private ModalFooterOKButton footer;
    private ModalBody body = new ModalBody();

    public FieldDefinitionPropertiesModal( Command command ) {
        setTitle( FieldProperties.INSTANCE.title() );
        add( body );
        footer = new ModalFooterOKButton( command );
        add( footer );
    }

    public void addPropertiesEditor( PropertyEditorWidget widget ) {
        body.clear();
        body.add( widget );
    }
}
