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

package org.livespark.formmodeler.editor.backend.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.services.datamodeller.util.FileUtils;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.template.FormDefinitionSerializer;
import org.livespark.formmodeler.editor.service.VFSFormFinderService;
import org.livespark.formmodeler.editor.type.FormResourceTypeDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;

@Dependent
@Service
public class VFSFormFinderServiceImpl implements VFSFormFinderService {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private KieProjectService projectService;

    @Inject
    private FormDefinitionSerializer serializer;

    @Override
    public List<FormDefinition> findAllForms( Path path ) {
        return findForms( path, null );
    }

    @Override
    public List<FormDefinition> findFormsForType( final String typeName, Path path ) {
        return findForms( path, new FormSearchConstraint() {
            @Override
            public boolean accepts( FormDefinition form ) {
                if ( form.getDataHolders().size() != 1 ) {
                    return false;
                }
                return form.getDataHolders().get( 0 ).getType().equals( typeName );
            }
        } );
    }

    @Override
    public FormDefinition findFormById( final String id, Path path ) {
        List<FormDefinition> forms = findForms( path, new FormSearchConstraint() {
            @Override
            public boolean accepts( FormDefinition form ) {
                return form.getId().equals( id );
            }
        } );

        if ( forms != null && !forms.isEmpty() ) {
            return forms.get( 0 );
        }
        return null;
    }

    private List<FormDefinition> findForms( Path path, FormSearchConstraint constraint ) {

        List<FormDefinition> result = new ArrayList<>();

        Project project = projectService.resolveProject( path );

        FileUtils utils = FileUtils.getInstance();

        List<org.uberfire.java.nio.file.Path> nioPaths = new ArrayList<>();

        nioPaths.add( Paths.convert( project.getRootPath() ) );

        Collection<FileUtils.ScanResult> forms = utils.scan( ioService, nioPaths, FormResourceTypeDefinition.EXTENSION, true );

        for ( FileUtils.ScanResult form : forms ) {
            org.uberfire.java.nio.file.Path formPath = form.getFile();

            FormDefinition formDefinition = serializer.deserialize( ioService.readAllString( formPath ).trim() );

            if ( constraint == null || constraint.accepts( formDefinition )) {
                result.add( formDefinition );
            }
        }

        return result;
    }

    private interface FormSearchConstraint {
        boolean accepts( FormDefinition form );
    }
}
