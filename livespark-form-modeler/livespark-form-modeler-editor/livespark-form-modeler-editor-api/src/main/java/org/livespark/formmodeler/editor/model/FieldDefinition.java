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

package org.livespark.formmodeler.editor.model;

public abstract class FieldDefinition {

    protected final String code = this.getClass().getName();

    protected boolean annotatedId;

    protected String name;

    protected String label;

    protected Boolean required = Boolean.FALSE;

    protected Boolean readonly = Boolean.FALSE;

    protected String bindingExpression;
    
    protected String boundPropertyName;

    public abstract String getStandaloneClassName();

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel( String label ) {
        this.label = label;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired( Boolean required ) {
        this.required = required;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly( Boolean readonly ) {
        this.readonly = readonly;
    }

    public String getBindingExpression() {
        return bindingExpression;
    }

    public void setBindingExpression( String bindingExpression ) {
        this.bindingExpression = bindingExpression;
    }
    
    public boolean isAnnotatedId() {
        return annotatedId;
    }
    
    public void setAnnotatedId( boolean annotatedId ) {
        this.annotatedId = annotatedId;
    }
    
    public String getBoundPropertyName() {
        return boundPropertyName;
    }
    
    public void setBoundPropertyName( String boundPropertyName ) {
        this.boundPropertyName = boundPropertyName;
    }
}
