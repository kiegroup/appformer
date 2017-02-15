/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.backend;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.workbench.common.forms.dynamic.service.shared.FormRenderingContext;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.kie.workbench.common.stunner.forms.context.PathAware;
import org.uberfire.backend.vfs.Path;

@Dependent
public class ProjectEntityUtil {

    private static final String[] NON_MODEL_SUFFICES = {
                                                        SourceGenerationContext.FORM_MODEL_SUFFIX,
                                                        SourceGenerationContext.REST_SERVICE_SUFFIX
    };

    private final KieProjectService projectService;

    private final DataModelerService dataModeler;

    @Inject
    public ProjectEntityUtil( final KieProjectService projectService, final DataModelerService dataModeler ) {
        this.projectService = projectService;
        this.dataModeler = dataModeler;
    }

    public Stream<String> projectEntityNames( final FormRenderingContext<?> context ) {
        if ( context instanceof PathAware ) {
            final Path path = ((PathAware) context).getPath();
            final KieProject project = projectService.resolveProject( path );
            final DataModel dataModel = dataModeler.loadModel( project );

            return dataModel
                .getDataObjects()
                .stream()
                .filter( dataObj -> dataObj.getPackageName().contains( "client.shared" ) )
                .filter( dataObj -> !dataObj.getPackageName().contains( "builtin" ) )
                .map( dataObj -> dataObj.getName() )
                .filter( name -> !Arrays.stream( NON_MODEL_SUFFICES ).anyMatch( s -> name.endsWith( s ) ) );
        }
        else {
            return Stream.empty();
        }
    }
}
