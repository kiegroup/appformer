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

package org.livespark.formmodeler.editor.backend.service.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.livespark.formmodeler.editor.service.FormEditorRenderingContext;
import org.livespark.formmodeler.editor.service.FieldPropertiesService;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.formmodeler.renderer.service.Model2FormTransformerService;
import org.uberfire.backend.vfs.Path;

@Service
@ApplicationScoped
public class FieldPropertiesServiceImpl implements FieldPropertiesService {
    @Inject
    protected Model2FormTransformerService model2FormTransformerService;

    @Override
    public FormEditorRenderingContext getFieldPropertiesRenderingContext( FieldDefinition fieldDefinition, Path formPath ) {
        FormRenderingContext context = model2FormTransformerService.createContext( fieldDefinition );

        FormEditorRenderingContext editorContext = new FormEditorRenderingContext( formPath );
        editorContext.setRootForm( context.getRootForm() );
        editorContext.setModel( fieldDefinition );

        editorContext.getAvailableForms().putAll( context.getAvailableForms() );

        return editorContext;
    }
}
