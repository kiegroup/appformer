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

package org.livespark.formmodeler.renderer.backend.service.impl.processors;

import java.lang.annotation.Annotation;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.livespark.formmodeler.metaModel.TextArea;
import org.livespark.formmodeler.model.impl.basic.textArea.TextAreaFieldDefinition;
import org.livespark.formmodeler.renderer.backend.service.impl.FieldSetting;
import org.livespark.formmodeler.renderer.backend.service.impl.processors.AbstractFieldAnnotationProcessor;
import org.livespark.formmodeler.service.FieldManager;

/**
 * @author Pere Fernandez <pefernan@redhat.com>
 */
@Dependent
public class TextAreaAnnotationProcessor extends AbstractFieldAnnotationProcessor<TextAreaFieldDefinition> {

    @Inject
    public TextAreaAnnotationProcessor( FieldManager fieldManager ) {
        super( fieldManager );
    }

    @Override
    protected TextAreaFieldDefinition buildFieldDefinition( Annotation annotation, FieldSetting setting ) {
        if ( !supportsAnnotation( annotation )) {
            return null;
        }

        TextArea textAreaAnnotation = (TextArea) annotation;
        TextAreaFieldDefinition textArea = (TextAreaFieldDefinition) fieldManager.getFieldFromProvider( TextAreaFieldDefinition.CODE, setting.getTypeInfo() );
        if ( !textAreaAnnotation.placeHolder().isEmpty() ) {
            textArea.setPlaceHolder( textAreaAnnotation.placeHolder() );
        }
        textArea.setRows( textAreaAnnotation.rows() );
        return textArea;
    }

    @Override
    public boolean supportsAnnotation( Annotation annotation ) {
        return annotation instanceof TextArea;
    }
}
