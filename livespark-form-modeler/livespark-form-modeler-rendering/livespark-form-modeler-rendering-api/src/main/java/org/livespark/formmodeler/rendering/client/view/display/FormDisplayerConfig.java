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

package org.livespark.formmodeler.rendering.client.view.display;

import org.livespark.formmodeler.rendering.client.view.FormView;

public class FormDisplayerConfig {
    private FormView<?> formView;
    private String formTitle;
    private FormDisplayer.FormDisplayerCallback callback;

    public FormDisplayerConfig( FormView<?> formView, String formTitle, FormDisplayer.FormDisplayerCallback callback ) {
        this.formView = formView;
        this.formTitle = formTitle;
        this.callback = callback;
    }

    public FormView<?> getFormView() {
        return formView;
    }

    public String getFormTitle() {
        return formTitle;
    }

    public FormDisplayer.FormDisplayerCallback getCallback() {
        return callback;
    }
}
