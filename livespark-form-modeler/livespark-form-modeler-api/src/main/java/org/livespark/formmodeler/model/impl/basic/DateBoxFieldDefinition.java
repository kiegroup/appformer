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

import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.metaModel.FieldDef;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
@Bindable
public class DateBoxFieldDefinition extends FieldDefinition implements HasPlaceHolder {
    public static final String CODE = "DatePicker";

    private String code = CODE;

    @FieldDef( label = "Placeholder", position = 1)
    protected String placeHolder = "";

    @FieldDef( label = "Show Time", position = 2)
    protected Boolean showTime = Boolean.TRUE;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[] {
                Date.class.getName()
        };
    }

    @Override
    public String getPlaceHolder() {
        return placeHolder;
    }

    @Override
    public void setPlaceHolder( String placeHolder ) {
        this.placeHolder = placeHolder;
    }

    public Boolean getShowTime() {
        return showTime;
    }

    public void setShowTime( Boolean showTime ) {
        this.showTime = showTime;
    }

    @Override
    protected void doCopyFrom( FieldDefinition other ) {
        if ( other instanceof DateBoxFieldDefinition ) {
            DateBoxFieldDefinition otherDate = (DateBoxFieldDefinition) other;
            setPlaceHolder( otherDate.getPlaceHolder() );
            setShowTime( otherDate.getShowTime() );
        } else if ( other instanceof  HasPlaceHolder ) {
            setPlaceHolder( ((HasPlaceHolder) other).getPlaceHolder() );
        }
    }
}
