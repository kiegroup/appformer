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

public abstract class DefaultFormDisplayer implements FormDisplayer {

    protected FormDisplayerConfig config;

    @Override
    public void display( FormDisplayerConfig config ) {
        this.config = config;
        doDisplay();
    }

    @Override
    public FormDisplayerConfig getFormDisplayerConfig() {
        return config;
    }

    public abstract void doDisplay();

    public abstract void doHide();

    public void onSubmit() {
        if ( config != null ) {
            if ( config.getFormView().validate() ) {
                config.getCallback().onSubmit();
                doHide();
                config = null;
            }
        }
    }

    public void onCancel() {
        if ( config != null ) {
            config.getCallback().onCancel();
            doHide();
            config = null;
        }
    }
}
