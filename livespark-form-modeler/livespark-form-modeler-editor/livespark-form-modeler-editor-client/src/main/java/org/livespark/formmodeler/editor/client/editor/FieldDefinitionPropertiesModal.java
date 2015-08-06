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
package org.livespark.formmodeler.editor.client.editor;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import org.uberfire.ext.layout.editor.client.components.ModalConfigurationContext;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;

/**
 * Created by pefernan on 8/6/15.
 */
public class FieldDefinitionPropertiesModal extends BaseModal {

    private ModalConfigurationContext ctx;
    private FlowPanel content = new FlowPanel(  );

    private final Command okCommand = new Command() {
        @Override
        public void execute() {
            if (ctx != null) ctx.configurationFinished();
            hide();
        }
    };

    private final Command cancelCommand = new Command() {
        @Override
        public void execute() {
            if (ctx != null) ctx.configurationCancelled();
            hide();
        }
    };

    private final ModalFooterOKCancelButtons footer = new ModalFooterOKCancelButtons( okCommand,
            cancelCommand );

    public FieldDefinitionPropertiesModal( ) {
        add( content );
        add( footer );
    }

    public void init(ModalConfigurationContext ctx) {
        content.clear();
        this.ctx = ctx;
        content.add( new HTML( "Editing a field" ) );
    }
}
