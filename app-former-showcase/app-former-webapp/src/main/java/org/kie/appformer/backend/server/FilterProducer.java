/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.backend.server;

import static org.livespark.formmodeler.codegen.SourceGenerationContext.ENTITY_SERVICE_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_MODEL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.FORM_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.LIST_VIEW_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_IMPL_SUFFIX;
import static org.livespark.formmodeler.codegen.SourceGenerationContext.REST_SERVICE_SUFFIX;

import java.util.Arrays;
import java.util.function.Predicate;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import org.kie.workbench.common.services.backend.builder.JavaSourceFilter;
import org.kie.workbench.common.services.datamodeller.driver.SourceFilter;

@Dependent
public class FilterProducer {

    private static final String[] generatedTypeSuffices = new String[] {
                                                                        ENTITY_SERVICE_SUFFIX,
                                                                        FORM_MODEL_SUFFIX,
                                                                        FORM_VIEW_SUFFIX,
                                                                        LIST_VIEW_SUFFIX,
                                                                        REST_IMPL_SUFFIX,
                                                                        REST_SERVICE_SUFFIX
    };

    @Produces
    public SourceFilter createSourceFilter() {
        return type -> type.getQualifiedName().contains( ".client.local." )
                || Arrays.stream( generatedTypeSuffices ).anyMatch( suffix -> type.getName().endsWith( suffix ) );
    }

    @Produces
    @JavaSourceFilter
    public Predicate<String> createClassFilter() {
        return sourceFileName -> !sourceFileName.contains( "/client/local/" );
    }

}
