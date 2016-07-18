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

package org.livespark.widgets.crud.client.component;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.livespark.widgets.crud.client.component.formDisplay.embedded.EmbeddedFormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.modal.ModalFormDisplayer;
import org.livespark.widgets.crud.client.component.mock.CrudComponentMock;
import org.livespark.widgets.crud.client.resources.i18n.CrudComponentConstants;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.gwtmockito.GwtMock;

import junit.framework.TestCase;

public abstract class AbstractCrudComponentTest extends TestCase {
    @Mock
    protected CrudComponent.CrudComponentView view;

    @Mock
    protected TranslationService translationService;

    @GwtMock
    protected EmbeddedFormDisplayer embeddedFormDisplayer;

    @GwtMock
    protected ModalFormDisplayer modalFormDisplayer;

    @Mock
    private IsFormView formView;

    protected CrudActionsHelper helper = Mockito.mock( CrudActionsHelper.class );

    protected CrudComponentMock crudComponent;

    protected static final String NEW_INSTANCE_TITLE = "New Instance Title";
    protected static final String EDIT_INSTANCE_TITLE = "Edit Instance Title";

    @Before
    public void init() {
        when( translationService.getTranslation( CrudComponentConstants.CrudComponentViewImplNewInstanceTitle ) ).thenReturn( NEW_INSTANCE_TITLE );
        when( translationService.getTranslation( CrudComponentConstants.CrudComponentViewImplEditInstanceTitle ) ).thenReturn( EDIT_INSTANCE_TITLE );
        when( helper.getCreateInstanceForm() ).thenReturn( getFormView() );
        when( helper.getEditInstanceForm( anyInt() ) ).thenReturn( getFormView() );
        when( embeddedFormDisplayer.isEmbeddable() ).thenReturn( true );
        crudComponent = new CrudComponentMock( view, embeddedFormDisplayer, modalFormDisplayer, translationService );
    }

    protected void initTest() {
        verify( view ).setPresenter( crudComponent );

        crudComponent.init( getActionsHelper() );
        verify( view ).initTableView( helper.getGridColumns(), helper.getPageSize() );

        crudComponent.getCurrentPage();
        verify( view ).getCurrentPage();
    }

    protected void runCreationTest() {
        crudComponent.showCreateForm();

        if ( helper == getActionsHelper() ) {
            verify( helper ).getCreateInstanceForm();
        }
        if ( getActionsHelper().showEmbeddedForms() ) {
            verify( view ).addDisplayer( embeddedFormDisplayer );
            verify( embeddedFormDisplayer ).display( eq( NEW_INSTANCE_TITLE ), eq( getFormView() ), any( FormDisplayer.FormDisplayerCallback.class ) );
        } else {
            verify( modalFormDisplayer ).display( eq( NEW_INSTANCE_TITLE ), eq( getFormView() ), any( FormDisplayer.FormDisplayerCallback.class ) );
        }

        crudComponent.doCreate();

        if ( getActionsHelper().showEmbeddedForms() ) {
            verify( view ).removeDisplayer( embeddedFormDisplayer );
        }
        if ( helper == getActionsHelper() ) {
            verify( helper ).createInstance();
        }
    }

    protected void runCreationCancelTest() {
        crudComponent.showCreateForm();

        if ( helper == getActionsHelper() ) {
            verify( helper ).getCreateInstanceForm();
        }
        if ( getActionsHelper().showEmbeddedForms() ) {
            verify( view ).addDisplayer( embeddedFormDisplayer );
            verify( embeddedFormDisplayer ).display( eq( NEW_INSTANCE_TITLE ), eq( getFormView() ), any( FormDisplayer.FormDisplayerCallback.class ) );
        } else {
            verify( modalFormDisplayer ).display( eq( NEW_INSTANCE_TITLE ), eq( getFormView() ), any( FormDisplayer.FormDisplayerCallback.class ) );
        }

        crudComponent.doCancel();

        if ( getActionsHelper().showEmbeddedForms() ) {
            verify( view ).removeDisplayer( embeddedFormDisplayer );
        }
    }

    protected void runEditTest() {
        crudComponent.showEditForm( 0 );

        if ( helper == getActionsHelper() ) {
            verify( helper ).getEditInstanceForm( 0 );
        }
        if ( getActionsHelper().showEmbeddedForms() ) {
            verify( view ).addDisplayer( embeddedFormDisplayer );
            verify( embeddedFormDisplayer ).display( eq( EDIT_INSTANCE_TITLE ), eq( getFormView() ), any( FormDisplayer.FormDisplayerCallback.class ) );
        } else {
            verify( modalFormDisplayer ).display( eq( EDIT_INSTANCE_TITLE ), eq( getFormView() ), any( FormDisplayer.FormDisplayerCallback.class ) );
        }

        crudComponent.doEdit();

        if ( getActionsHelper().showEmbeddedForms() ) {
            verify( view ).removeDisplayer( embeddedFormDisplayer );
        }
        if ( helper == getActionsHelper() ) {
            verify( helper ).editInstance();
        }
    }

    protected void runEditCancelTest() {
        crudComponent.showEditForm( 0 );

        if ( helper == getActionsHelper() ) {
            verify( helper ).getEditInstanceForm( 0 );
        }
        if ( getActionsHelper().showEmbeddedForms() ) {
            verify( view ).addDisplayer( embeddedFormDisplayer );
            verify( embeddedFormDisplayer ).display( eq( EDIT_INSTANCE_TITLE ), eq( getFormView() ), any( FormDisplayer.FormDisplayerCallback.class ) );
        } else {
            verify( modalFormDisplayer ).display( eq( EDIT_INSTANCE_TITLE ), eq( getFormView() ), any( FormDisplayer.FormDisplayerCallback.class ) );
        }

        crudComponent.doCancel();

        if ( getActionsHelper().showEmbeddedForms() ) {
            verify( view ).removeDisplayer( embeddedFormDisplayer );
        }
    }

    protected void runDeletionTest() {
        crudComponent.deleteInstance( 0 );

        if ( helper == getActionsHelper() ) {
            verify( helper ).deleteInstance( 0 );
        }
    }

    protected CrudActionsHelper getActionsHelper() {
        return helper;
    }

    protected IsFormView getFormView() {
        return formView;
    }
}
