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

package org.livespark.formmodeler.editor.model.impl.basic;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.impl.HasPlaceHolder;
import org.livespark.formmodeler.editor.model.impl.HasRows;
import org.livespark.formmodeler.editor.model.impl.HasSize;

import javax.enterprise.context.Dependent;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
@Dependent
public class TextAreaFieldDefinition extends FieldDefinition implements HasRows, HasSize, HasPlaceHolder {
    public static final String _CODE = "TextArea";

    protected Integer rows = 4;
    protected Integer size = 12;
    protected String placeHolder = "";

    @Override
    public String getCode() {
        return _CODE;
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
    public Integer getSize() {
        return size;
    }

    @Override
    public void setSize(Integer size) {
        this.size = size;
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
            this.setSize( otherTextArea.getSize() );
            this.setRows(otherTextArea.getRows());
            this.setPlaceHolder( otherTextArea .getPlaceHolder() );
        } else {
            if (other instanceof HasRows) {
                setRows(((HasRows) other).getRows());
            }
            if (other instanceof HasSize) {
                setSize(((HasSize) other).getSize());
            }
            if (other instanceof HasPlaceHolder) {
                setPlaceHolder(((HasPlaceHolder) other).getPlaceHolder());
            }
        }
    }
}
