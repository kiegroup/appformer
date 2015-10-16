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

import javax.enterprise.context.Dependent;
import java.util.Date;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
@Dependent
public class DateBoxFieldDefinition extends FieldDefinition implements HasPlaceHolder {
    public static final String _CODE = "DatePicker";

    protected String placeHolder = "";

    @Override
    public String getCode() {
        return _CODE;
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

    @Override
    protected void doCopyFrom( FieldDefinition other ) {
        if ( other instanceof  HasPlaceHolder ) {
            setPlaceHolder( ((HasPlaceHolder) other).getPlaceHolder() );
        }
    }
}
