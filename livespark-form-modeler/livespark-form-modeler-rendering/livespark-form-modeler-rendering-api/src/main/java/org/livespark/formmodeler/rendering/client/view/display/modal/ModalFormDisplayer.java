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

package org.livespark.formmodeler.rendering.client.view.display.modal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.client.view.display.DefaultFormDisplayer;

@Dependent
public class ModalFormDisplayer extends DefaultFormDisplayer {

    public interface ModalFormDisplayerView extends IsWidget {
        public void setPresenter( ModalFormDisplayer presenter );
        public void show( FormView<?> formView );
        public void hide();
    }

    private ModalFormDisplayerView view;

    @Inject
    public ModalFormDisplayer( ModalFormDisplayerView view ) {
        this.view = view;
        view.setPresenter( this );
    }


    @Override
    public void doDisplay() {
        view.show( config.getFormView() );
    }

    @Override
    public void doHide() {
        view.hide();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }
}
