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
package org.livespark.formmodeler.rendering.test.res;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.core.java.lang.Boolean_CustomFieldSerializer;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.client.view.ListView;
import org.livespark.widgets.crud.client.component.CrudActionsHelper;
import org.livespark.widgets.crud.client.component.CrudComponent;
import org.livespark.widgets.crud.client.component.mock.CrudModel;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

public class TestListView extends ListView<CrudModel, TestFormModel> {

    private FormView<TestFormModel> formView;

    private LiveSparkRestService<CrudModel> service;

    public RemoteCallback<Object> crudModelListCallback;

    public TestListView( final CrudComponent crudComponent, FormView<TestFormModel> formView, LiveSparkRestService<CrudModel> restService ) {
        this.crudComponent = crudComponent;
        this.formView = formView;
        this.service = restService;

        crudActionsHelper = new ListViewCrudActionsHelper() {
            @Override
            public void createInstance() {
                super.createInstance();
                crudModelListCallback.callback( getModel( currentForm.getModel() ) );
            }

            @Override
            public void editInstance() {
                super.editInstance();
                crudModelListCallback.callback( Boolean.TRUE );
            }

            @Override
            public void deleteInstance( int index ) {
                super.deleteInstance( index );
                crudModelListCallback.callback( Boolean.TRUE );
            }
        };
    }

    @Override
    public List<ColumnMeta> getCrudColumns() {

        List<ColumnMeta> metas = new ArrayList<ColumnMeta>();

        ColumnMeta<CrudModel> columnMeta = new ColumnMeta<CrudModel>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( CrudModel model ) {
                if ( model.getName() == null ) {
                    return "";
                }
                return String.valueOf( model.getName() );
            }
        }, "Name" );

        metas.add( columnMeta );

        columnMeta = new ColumnMeta<CrudModel>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( CrudModel model ) {
                if ( model.getLastName() == null ) {
                    return "";
                }
                return String.valueOf( model.getLastName() );
            }
        }, "Last Name" );

        metas.add( columnMeta );

        columnMeta = new ColumnMeta<CrudModel>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( CrudModel model ) {
                if ( model.getBirthday() == null ) {
                    return "";
                }
                return String.valueOf( model.getBirthday() );
            }
        }, "Birthday" );

        metas.add( columnMeta );

        return metas;
    }

    @Override
    public CrudModel getModel( TestFormModel formModel ) {
        return formModel.getModel();
    }

    @Override
    public TestFormModel createFormModel( CrudModel model ) {
        return new TestFormModel( model );
    }

    /* (non-Javadoc)
         * @see org.livespark.formmodeler.rendering.client.view.ListView#createRestCaller(org.jboss.errai.common.client.api.RemoteCallback)
         */
    @SuppressWarnings( "unchecked" )
    @Override
    public <S extends LiveSparkRestService<CrudModel>, R> S createRestCaller( RemoteCallback<R> callback ) {
        crudModelListCallback = (RemoteCallback<Object>) callback;
        return (S) service;
    }

    /* (non-Javadoc)
     * @see org.livespark.formmodeler.rendering.client.view.ListView#getFormTitle()
     */
    @Override
    public String getFormTitle() {
        return "Test Form";
    }

    /* (non-Javadoc)
     * @see org.livespark.formmodeler.rendering.client.view.ListView#getFormId()
     */
    @Override
    protected String getFormId() {
        return "TestFormId";
    }

    /* (non-Javadoc)
     * @see org.livespark.formmodeler.rendering.client.view.ListView#getForm()
     */
    @Override
    public FormView<TestFormModel> getForm() {
        return formView;
    }

    /* (non-Javadoc)
     * @see org.livespark.formmodeler.rendering.client.view.ListView#getRemoteServiceClass()
     */
    @SuppressWarnings( "unchecked" )
    @Override
    protected Class<TestRestService> getRemoteServiceClass() {
        return TestRestService.class;
    }

    /* (non-Javadoc)
     * @see org.livespark.formmodeler.rendering.client.view.ListView#getFormType()
     */
    @Override
    protected Class< ? extends FormView<TestFormModel>> getFormType() {
        throw new RuntimeException( "Not yet implemented." );
    }

    /* (non-Javadoc)
     * @see org.livespark.formmodeler.rendering.client.view.ListView#getListTitle()
     */
    @Override
    public String getListTitle() {
        throw new RuntimeException( "Not yet implemented." );
    }

    public CrudActionsHelper getActionsHelper() {
        return crudActionsHelper;
    }
}
