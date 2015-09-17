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

package org.livespark.formmodeler.rendering.client.view;

import com.google.gwt.event.dom.client.ClickHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;

/**
 * Created by pefernan on 6/25/15.
 */
public class FormViewModal extends Modal {

    private Button submit = new Button( "Submit" );
    private Button cancel = new Button( "Cancel" );

    FormView<?> formView;

    public FormViewModal( final FormView<?> formView, final String title, String id ) {
        setHideOtherModals( false );
        setClosable(true);
        setFade(true);
        setDataKeyboard(true);
        setDataBackdrop( ModalBackdrop.FALSE );
        setRemoveOnHide(true);
        setTitle(title);
        getElement().setId(id);

        this.formView = formView;
        add( new ModalBody() {{
            add( formView );
        }} );

        ModalFooter footer = new ModalFooter(  );
        footer.add(submit);
        footer.add(cancel);
        add(footer);
    }

    public void addSubmitClickHandler( ClickHandler handler ) {
        submit.addClickHandler( handler );
    }

    public void addCancelClickHandler( ClickHandler handler ) {
        cancel.addClickHandler( handler );
    }
}
