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
package org.livespark.formmodeler.rendering.client.shared.fields;

import java.util.List;

import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

/**
 * Created by pefernan on 7/2/15.
 */
public interface MultipleSubFormModelAdapter <L extends List<M>, M, C extends FormModel, E extends FormModel> {

    public List<ColumnMeta<M>> getCrudColumns();

    public Class<? extends FormView<C>> getCreationForm();

    public Class<? extends FormView<E>> getEditionForm();

    public E getEditionFormModel( M model );

}
