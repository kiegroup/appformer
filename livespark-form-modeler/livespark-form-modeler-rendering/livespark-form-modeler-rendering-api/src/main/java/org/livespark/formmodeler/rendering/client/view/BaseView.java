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

package org.livespark.formmodeler.rendering.client.view;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

import com.google.gwt.user.client.ui.Composite;

public abstract class BaseView<M extends FormModel> extends Composite implements HasModel<M> {

    protected List<String> inputNames = new ArrayList<String>();

    @Inject
    @AutoBound
    protected DataBinder<M> binder;

    @Override
    public M getModel() {
        return binder.getModel();
    }

    @PostConstruct
    protected void initFormView() {
        initInputNames();
    }

    @Override
    public void setModel( M model ) {
        binder.setModel( model );
    }

    public List<String> getInputNames() {
        return inputNames;
    }

    protected abstract void initInputNames();

}
