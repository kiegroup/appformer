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
package org.livespark.formmodeler.editor.client.editor.service;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.basic.CheckBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.DateBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.TextAreaFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.TextBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.selectors.ListBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.selectors.RadioGroupFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.MultipleSubFormFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.SubFormFieldDefinition;
import org.livespark.formmodeler.service.AbstractFieldManager;

@ApplicationScoped
public class ClientFieldManagerImpl extends AbstractFieldManager {

    @PostConstruct
    protected void init() {
        registerFieldDefinition( new TextBoxFieldDefinition() );
        registerFieldDefinition( new TextAreaFieldDefinition() );
        registerFieldDefinition( new CheckBoxFieldDefinition() );
        registerFieldDefinition( new DateBoxFieldDefinition() );
        registerFieldDefinition( new ListBoxFieldDefinition() );
        registerFieldDefinition( new RadioGroupFieldDefinition() );
        registerFieldDefinition( new SubFormFieldDefinition() );
        registerFieldDefinition( new MultipleSubFormFieldDefinition() );
    }

    @Override
    protected FieldDefinition createNewInstance(FieldDefinition definition) throws Exception {
        if ( definition == null ) return null;

        if ( definition.getCode().equals( TextBoxFieldDefinition._CODE )) return new TextBoxFieldDefinition();
        if ( definition.getCode().equals( TextAreaFieldDefinition._CODE )) return new TextAreaFieldDefinition();
        if ( definition.getCode().equals( CheckBoxFieldDefinition._CODE )) return new CheckBoxFieldDefinition();
        if ( definition.getCode().equals( DateBoxFieldDefinition._CODE )) return new DateBoxFieldDefinition();
        if ( definition.getCode().equals( ListBoxFieldDefinition._CODE )) return new ListBoxFieldDefinition();
        if ( definition.getCode().equals( RadioGroupFieldDefinition._CODE )) return new RadioGroupFieldDefinition();
        if ( definition.getCode().equals( SubFormFieldDefinition._CODE )) return new SubFormFieldDefinition();
        if ( definition.getCode().equals( MultipleSubFormFieldDefinition._CODE )) return new MultipleSubFormFieldDefinition();

        return null;
    }
}
