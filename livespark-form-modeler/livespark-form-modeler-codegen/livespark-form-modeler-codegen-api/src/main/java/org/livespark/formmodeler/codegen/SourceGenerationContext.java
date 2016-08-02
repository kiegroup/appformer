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

package org.livespark.formmodeler.codegen;

import java.util.List;

import org.kie.workbench.common.forms.model.FormDefinition;
import org.uberfire.backend.vfs.Path;
import org.guvnor.common.services.project.model.Package;

public class SourceGenerationContext {
    public static final String FORM_MODEL_SUFFIX = "FormModel";
    public static final String FORM_VIEW_SUFFIX = "FormView";
    public static final String LIST_VIEW_SUFFIX = "ListView";
    public static final String REST_SERVICE_SUFFIX = "RestService";
    public static final String ENTITY_SERVICE_SUFFIX = "EntityService";
    public static final String REST_IMPL_SUFFIX = "RestServiceImpl";

    private FormDefinition formDefinition;
    private Path path;
    private Package root;
    private Package local;
    private Package shared;
    private Package server;
    private List<FormDefinition> projectForms;

    public SourceGenerationContext( FormDefinition form,
                                    Path path,
                                    Package root,
                                    Package local,
                                    Package shared,
                                    Package server,
                                    List<FormDefinition> projectForms ) {
        this.path = path;
        this.root = root;
        this.local = local;
        this.shared = shared;
        this.server = server;
        this.projectForms = projectForms;
        setFormDefinition( form );
    }

    public FormDefinition getFormDefinition() {
        return formDefinition;
    }

    public void setFormDefinition( FormDefinition formDefinition ) {
        this.formDefinition = formDefinition;
    }

    public Path getPath() {
        return path;
    }

    public String getFormModelName() {
        return formDefinition.getName() + FORM_MODEL_SUFFIX;
    }

    public String getFormViewName() {
        return formDefinition.getName() + FORM_VIEW_SUFFIX;
    }

    public String getListViewName() {
        return formDefinition.getName() + LIST_VIEW_SUFFIX;
    }

    public String getRestServiceName() {
        return formDefinition.getName() + REST_SERVICE_SUFFIX;
    }

    public String getEntityServiceName() {
        return formDefinition.getName() + ENTITY_SERVICE_SUFFIX;
    }

    public String getEntityName() {
        return formDefinition.getName();
    }

    public Package getRootPackage() {
        return root;
    }

    public Package getLocalPackage() {
        return local;
    }

    public Package getSharedPackage() {
        return shared;
    }

    public Package getServerPackage() {
        return server;
    }

    public String getRestServiceImplName() {
        return formDefinition.getName() + REST_IMPL_SUFFIX;
    }

    public List<FormDefinition> getProjectForms() {
        return projectForms;
    }
}
