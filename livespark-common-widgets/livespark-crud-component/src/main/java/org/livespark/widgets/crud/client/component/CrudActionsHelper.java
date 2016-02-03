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

import java.util.List;

import com.google.gwt.view.client.AsyncDataProvider;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

public interface CrudActionsHelper<T> {

    public int getPageSize();

    public boolean showEmbeddedForms();

    public boolean isAllowCreate();

    public boolean isAllowEdit();

    public boolean isAllowDelete();

    public List<ColumnMeta> getGridColumns();

    public AsyncDataProvider<T> getDataProvider();

    public IsFormView<T> getCreateInstanceForm();

    public void createInstance();

    public IsFormView<T> getEditInstanceForm( Integer index );

    public void editInstance();

    public void deleteInstance( int index );
}
