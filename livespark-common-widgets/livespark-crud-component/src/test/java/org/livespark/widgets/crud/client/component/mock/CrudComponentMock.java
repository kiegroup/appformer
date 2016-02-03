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

package org.livespark.widgets.crud.client.component.mock;

import org.livespark.widgets.crud.client.component.CrudComponent;
import org.livespark.widgets.crud.client.component.formDisplay.embedded.EmbeddedFormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.modal.ModalFormDisplayer;

public class CrudComponentMock extends CrudComponent {

    public CrudComponentMock( CrudComponentView view, EmbeddedFormDisplayer embeddedFormDisplayer, ModalFormDisplayer modalFormDisplayer ) {
        super( view, embeddedFormDisplayer, modalFormDisplayer );
    }

    @Override
    public void refresh() {
    }
}
