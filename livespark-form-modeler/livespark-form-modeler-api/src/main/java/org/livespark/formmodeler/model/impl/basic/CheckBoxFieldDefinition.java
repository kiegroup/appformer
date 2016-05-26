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

package org.livespark.formmodeler.model.impl.basic;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.model.FieldDefinition;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
@Bindable
public class CheckBoxFieldDefinition extends FieldDefinition {
    public static final String CODE = "CheckBox";

    private String code = CODE;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[] {
                Boolean.class.getName()
        };
    }

    @Override
    protected void doCopyFrom(FieldDefinition other) {

    }
}
