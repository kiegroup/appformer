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

import org.livespark.formmodeler.editor.model.FieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public abstract class AbstractIntputFieldDefinition extends FieldDefinition {
    protected Integer size = 15;
    protected Integer maxLength = 100;
    protected String placeHolder;

    public Integer getSize() {
        return size;
    }

    public void setSize( Integer size ) {
        this.size = size;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength( Integer maxLength ) {
        this.maxLength = maxLength;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder( String placeHolder ) {
        this.placeHolder = placeHolder;
    }
}
