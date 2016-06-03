/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.model.impl.basic.selectors.listBox;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.metaModel.FieldDef;
import org.livespark.formmodeler.model.DefaultFieldTypeInfo;
import org.livespark.formmodeler.model.FieldTypeInfo;
import org.livespark.formmodeler.model.impl.basic.selectors.DefaultSelectorOption;

/**
 * @author Pere Fernandez <pefernan@redhat.com>
 */
@Portable
@Bindable
public class EnumListBoxFieldDefinition extends ListBoxBase<DefaultSelectorOption> {

    @FieldDef( label = "Options")
    protected List<DefaultSelectorOption> options = new ArrayList<>();

    public EnumListBoxFieldDefinition() {
        super( CODE );
    }

    @Override
    public List<DefaultSelectorOption> getOptions() {
        return options;
    }

    @Override
    public void setOptions( List<DefaultSelectorOption> options ) {
        this.options = options;
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo() {
        return new DefaultFieldTypeInfo( standaloneClassName, false, true );
    }
}
