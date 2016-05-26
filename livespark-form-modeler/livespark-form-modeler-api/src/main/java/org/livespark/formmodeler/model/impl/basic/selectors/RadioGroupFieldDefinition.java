/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.model.impl.basic.selectors;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.metaModel.FieldDef;

@Portable
@Bindable
public class RadioGroupFieldDefinition extends SelectorField {
    public static final String CODE = "RadioGroup";

    private String code = CODE;

    @FieldDef( label = "Show options inline" )
    protected Boolean inline = Boolean.FALSE;

    @Override
    public String getCode() {
        return code;
    }

    public Boolean getInline() {
        return inline;
    }

    public void setInline( Boolean inline ) {
        this.inline = inline;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[] {
                String.class.getName(),
                Integer.class.getName(),
                BigInteger.class.getName(),
                Long.class.getName(),
                BigDecimal.class.getName(),
                Short.class.getName(),
                Byte.class.getName(),
                Character.class.getName(),
                Double.class.getName(),
                Float.class.getName()
        };
    }
}
