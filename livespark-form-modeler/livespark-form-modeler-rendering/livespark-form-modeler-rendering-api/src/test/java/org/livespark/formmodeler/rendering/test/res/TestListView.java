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

import org.jboss.errai.common.client.api.RemoteCallback;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.client.view.ListItemView;
import org.livespark.formmodeler.rendering.client.view.ListView;

public class TestListView extends ListView<TestFormModel, ListItemView<TestFormModel>> {

    private FormView<TestFormModel> formView;

    private LiveSparkRestService<TestFormModel> service;

    public RemoteCallback<Object> lastLoadDataCallback;

    /* (non-Javadoc)
     * @see org.livespark.formmodeler.rendering.client.view.ListView#createRestCaller(org.jboss.errai.common.client.api.RemoteCallback)
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public <S extends LiveSparkRestService<TestFormModel>, R> S createRestCaller( RemoteCallback<R> callback ) {
        lastLoadDataCallback = (RemoteCallback<Object>) callback;
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
}
