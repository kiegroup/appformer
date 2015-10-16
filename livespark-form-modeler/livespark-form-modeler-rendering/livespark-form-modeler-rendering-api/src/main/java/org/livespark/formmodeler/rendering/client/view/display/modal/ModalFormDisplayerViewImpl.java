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

package org.livespark.formmodeler.rendering.client.view.display.modal;

import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.shared.event.ModalHiddenEvent;
import org.gwtbootstrap3.client.shared.event.ModalHiddenHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.livespark.formmodeler.rendering.client.resources.i18n.RenderingConstants;
import org.livespark.formmodeler.rendering.client.view.FormView;

@Dependent
public class ModalFormDisplayerViewImpl extends Modal implements ModalFormDisplayer.ModalFormDisplayerView {

    interface Binder
            extends
            UiBinder<Widget, ModalFormDisplayerViewImpl> {

    }

    private static Binder uiBinder = GWT.create( Binder.class );

    @UiField
    SimplePanel content;

    private Button submit = new Button( RenderingConstants.INSTANCE.submit() );

    private Button cancel = new Button( RenderingConstants.INSTANCE.cancel() );

    protected ModalFormDisplayer presenter;

    public ModalFormDisplayerViewImpl() {
        setHideOtherModals( false );
        setClosable( true );
        setFade( true );
        setDataKeyboard( true );
        setDataBackdrop( ModalBackdrop.FALSE );
        setSize( ModalSize.LARGE );
        setRemoveOnHide( true );

        this.add( new ModalBody() {{
            add( uiBinder.createAndBindUi( ModalFormDisplayerViewImpl.this ) );
        }} );

        submit.setType( ButtonType.PRIMARY );

        add( new ModalFooter() {{
            add( submit );
            add( cancel );
        }} );

        submit.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                presenter.onSubmit();
            }
        } );

        cancel.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                doCancel();
            }
        } );

        this.addHiddenHandler( new ModalHiddenHandler() {
            @Override
            public void onHidden( ModalHiddenEvent evt ) {
                doCancel();
            }
        } );
    }

    @Override
    public void setPresenter( ModalFormDisplayer presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void show( FormView<?> formView ) {
        setTitle( presenter.getFormDisplayerConfig().getFormTitle() );
        content.clear();
        content.add( formView );
        show();
    }

    protected void doCancel() {
        presenter.onCancel();
    }
}
