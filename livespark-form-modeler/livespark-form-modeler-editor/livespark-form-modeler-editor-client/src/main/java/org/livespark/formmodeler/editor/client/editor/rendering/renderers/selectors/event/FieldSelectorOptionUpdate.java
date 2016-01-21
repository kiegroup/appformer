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

package org.livespark.formmodeler.editor.client.editor.rendering.renderers.selectors.event;

import org.livespark.formmodeler.model.impl.basic.selectors.SelectorOption;

import java.util.List;

public class FieldSelectorOptionUpdate extends FieldSelectorEvent {

    protected List<SelectorOption> options;

    public FieldSelectorOptionUpdate(String formId, String fieldId, List<SelectorOption> options) {
        super( formId, fieldId );
        this.options = options;
    }

    public List<SelectorOption> getOptions() {
        return options;
    }

    public void setOptions(List<SelectorOption> options) {
        this.options = options;
    }
}
