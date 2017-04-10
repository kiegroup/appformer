/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.interpeter.util;

import org.kie.appformer.flow.api.Sequenced;
import org.kie.workbench.common.forms.crud.client.component.formDisplay.IsFormView;

import com.google.gwt.user.client.ui.Widget;

public class TestFormView<M> implements IsFormView<M>, Sequenced {

    public M model;
    public boolean currentlyShown, start, end;

    @Override
    public Widget asWidget() {
        throw new RuntimeException( "Should not be required for tests." );
    }

    @Override
    public void setModel( final M model ) {
        this.model = model;
    }

    @Override
    public M getModel() {
        return model;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isStart() {
        return start;
    }

    @Override
    public boolean isEnd() {
        return end;
    }

    @Override
    public void setStart() {
        start = true;
    }

    @Override
    public void setEnd() {
        end = true;
    }

}
