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

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.validation.constraints.Min;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.metaModel.FieldDef;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
@Bindable
public class TextAreaFieldDefinition extends FieldDefinition implements HasRows, HasPlaceHolder {
    public static final String _CODE = "TextArea";

    private String code = _CODE;

    @FieldDef( label = "Placeholder", position = 1)
    protected String placeHolder = "";

    @FieldDef( label = "Visible Lines", position = 2)
    @Min(1)
    protected Integer rows = 4;


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public Integer getRows() {
        return rows;
    }

    @Override
    public void setRows( Integer rows ) {
        this.rows = rows;
    }

    @Override
    public String getPlaceHolder() {
        return placeHolder;
    }

    @Override
    public void setPlaceHolder( String placeHolder ) {
        this.placeHolder = placeHolder;
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

    @Override
    protected void doCopyFrom(FieldDefinition other) {
        if (other instanceof  TextAreaFieldDefinition ) {
            TextAreaFieldDefinition otherTextArea = (TextAreaFieldDefinition) other;
            this.setRows(otherTextArea.getRows());
            this.setPlaceHolder( otherTextArea .getPlaceHolder() );
        } else {
            if (other instanceof HasRows) {
                setRows(((HasRows) other).getRows());
            }
            if (other instanceof HasPlaceHolder) {
                setPlaceHolder(((HasPlaceHolder) other).getPlaceHolder());
            }
        }
    }
}
