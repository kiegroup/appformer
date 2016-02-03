/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.widgets.crud.client.component.formDisplay.embedded;

import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;

@Dependent
public class EmbeddedFormDisplayerViewImpl extends Composite implements EmbeddedFormDisplayer.EmbeddedFormDisplayerView {

    interface Binder
            extends
            UiBinder<Widget, EmbeddedFormDisplayerViewImpl> {

    }

    private static Binder uiBinder = GWT.create( Binder.class );

    @UiField
    Heading heading;

    @UiField
    SimplePanel content;

    @UiField
    Button accept;

    @UiField
    Button cancel;

    protected EmbeddedFormDisplayer presenter;

    public EmbeddedFormDisplayerViewImpl() {
        initWidget( uiBinder.createAndBindUi( EmbeddedFormDisplayerViewImpl.this ) );
    }

    @Override
    public void setPresenter( EmbeddedFormDisplayer presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void show( String title, IsFormView formView ) {
        heading.setText( title );
        content.add( formView );
    }

    @Override
    public void clear() {
        content.clear();
    }

    @UiHandler( "accept" )
    public void doAccept( ClickEvent event ) {
        presenter.submitForm();
    }

    @UiHandler( "cancel" )
    public void doCancel( ClickEvent event ) {
        presenter.cancel();
    }
}
