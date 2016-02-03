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

import javax.validation.constraints.Min;

import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.metaModel.FieldDef;

/**
 * Created by pefernan on 4/29/15.
 */
public abstract class AbstractIntputFieldDefinition extends FieldDefinition implements HasMaxLength, HasPlaceHolder {
    @FieldDef( label = "MaxLength", position = 2)
    @Min(1)
    protected Integer maxLength = 100;
    @FieldDef( label = "Placeholder", position = 1)
    protected String placeHolder = "";

    @Override
    public Integer getMaxLength() {
        return maxLength;
    }

    @Override
    public void setMaxLength( Integer maxLength ) {
        this.maxLength = maxLength;
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
    protected void doCopyFrom( FieldDefinition other ) {
        if ( other instanceof AbstractIntputFieldDefinition ) {
            AbstractIntputFieldDefinition otherInput = (AbstractIntputFieldDefinition) other;
            setMaxLength( otherInput.getMaxLength() );
            setPlaceHolder( otherInput.getPlaceHolder() );
        } else {
            if (other instanceof  HasMaxLength) {
                setMaxLength(((HasMaxLength) other).getMaxLength());
            }
            if (other instanceof  HasPlaceHolder) {
                setPlaceHolder(((HasPlaceHolder) other).getPlaceHolder());
            }
        }
    }
}
