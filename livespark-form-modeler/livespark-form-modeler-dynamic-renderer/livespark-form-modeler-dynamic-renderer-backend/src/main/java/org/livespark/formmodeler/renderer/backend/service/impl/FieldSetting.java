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

package org.livespark.formmodeler.renderer.backend.service.impl;

import java.lang.annotation.Annotation;

import org.livespark.formmodeler.metaModel.FieldDef;
import org.livespark.formmodeler.model.FieldTypeInfo;

public class FieldSetting implements Comparable<FieldSetting> {
    private String fieldName;
    private String property;
    private String label;
    private int position;
    private Class type;
    private Annotation[] annotations;
    private FieldTypeInfo typeInfo;

    public FieldSetting( String fieldName, FieldTypeInfo typeInfo, Class type, FieldDef definition, Annotation[] annotations ) {
        this.fieldName = fieldName;
        this.typeInfo = typeInfo;
        this.type = type;
        this.label = definition.label();
        this.position = definition.position();
        this.property = definition.property();
        this.annotations = annotations;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName( String fieldName ) {
        this.fieldName = fieldName;
    }

    public FieldTypeInfo getTypeInfo() {
        return typeInfo;
    }

    public String getLabel() {
        return label;
    }

    public int getPosition() {
        return position;
    }

    public Class getType() {
        return type;
    }

    public String getProperty() {
        return property;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public int compareTo( FieldSetting o ) {
        if ( position == -1 ) return 1;
        if ( o.getPosition() == -1 ) return -1;

        return position - o.getPosition();
    }
}
