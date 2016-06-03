/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.renderer.backend.service.impl.FieldSetting;
import org.livespark.formmodeler.service.FieldManager;

@Dependent
public class DefaultFieldAnnotationProcessor extends AbstractFieldAnnotationProcessor {
    @Inject
    public DefaultFieldAnnotationProcessor( FieldManager fieldManager ) {
        super( fieldManager );
    }

    @Override
    protected FieldDefinition buildFieldDefinition( Annotation annotation, FieldSetting setting ) {
        return null;
    }

    @Override
    public boolean supportsAnnotation( Annotation annotation ) {
        return true;
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
