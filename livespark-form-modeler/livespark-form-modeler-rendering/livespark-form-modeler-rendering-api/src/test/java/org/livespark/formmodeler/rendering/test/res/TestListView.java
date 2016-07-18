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

import org.jboss.errai.common.client.api.RemoteCallback;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.client.view.ListView;
import org.livespark.widgets.crud.client.component.CrudActionsHelper;
import org.livespark.widgets.crud.client.component.CrudComponent;
import org.livespark.widgets.crud.client.component.mock.CrudModel;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

import com.google.gwt.user.cellview.client.TextColumn;

public class TestListView extends ListView<CrudModel, TestFormModel> {

    private LiveSparkRestService<CrudModel> restService;

    public final List<RemoteCallback<?>> callbacks = new ArrayList<>();

    public void setCrudComponent( final CrudComponent crudComponent ) {
        this.crudComponent = crudComponent;
    }

    @Override
    public List<ColumnMeta> getCrudColumns() {

        final List<ColumnMeta> metas = new ArrayList<ColumnMeta>();

        ColumnMeta<CrudModel> columnMeta = new ColumnMeta<CrudModel>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( final CrudModel model ) {
                if ( model.getName() == null ) {
                    return "";
                }
                return String.valueOf( model.getName() );
            }
        }, "Name" );

        metas.add( columnMeta );

        columnMeta = new ColumnMeta<CrudModel>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( final CrudModel model ) {
                if ( model.getLastName() == null ) {
                    return "";
                }
                return String.valueOf( model.getLastName() );
            }
        }, "Last Name" );

        metas.add( columnMeta );

        columnMeta = new ColumnMeta<CrudModel>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( final CrudModel model ) {
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
    public CrudModel getModel( final TestFormModel formModel ) {
        return formModel.getModel();
    }

    @Override
    public TestFormModel createFormModel( final CrudModel model ) {
        return new TestFormModel( model );
    }

    /* (non-Javadoc)
         * @see org.livespark.formmodeler.rendering.client.view.ListView#createRestCaller(org.jboss.errai.common.client.api.RemoteCallback)
         */
    @SuppressWarnings( "unchecked" )
    @Override
    public <S extends LiveSparkRestService<CrudModel>, R> S createRestCaller( final RemoteCallback<R> callback ) {
        callbacks.add( callback );
        return (S) restService;
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
        return TestFormView.class;
    }

    /* (non-Javadoc)
     * @see org.livespark.formmodeler.rendering.client.view.ListView#getListTitle()
     */
    @Override
    public String getListTitle() {
        return "List Title";
    }

    public CrudActionsHelper getCrudActionsHelper() {
        return crudActionsHelper;
    }
}
