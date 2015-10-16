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

package org.livespark.formmodeler.rendering.client.view.util;

import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.view.ListView;
import org.livespark.formmodeler.rendering.client.view.display.FormDisplayer;

/**
 * Created by pefernan on 6/25/15.
 */
public interface ListViewActionsHelper<M extends FormModel> {
    public void startCreate( ListView<M, ?> listView );
    public void startEdit( ListView listView, M model );

    public void create(M model);
    public void update(M model);
    public void delete(M model);

    public FormDisplayer getFormDisplayer();
}
