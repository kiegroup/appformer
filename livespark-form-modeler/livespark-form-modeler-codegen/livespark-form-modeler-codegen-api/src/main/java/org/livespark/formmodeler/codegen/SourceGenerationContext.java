package org.livespark.formmodeler.codegen;

import org.livespark.formmodeler.model.FormDefinition;
import org.uberfire.backend.vfs.Path;
import org.guvnor.common.services.project.model.Package;

/**
 * Created by pefernan on 4/28/15.
 */
public class SourceGenerationContext {
    public static final String FORM_MODEL_SUFFIX = "FormModel";
    public static final String FORM_VIEW_SUFFIX = "FormView";
    public static final String LIST_VIEW_SUFFIX = "ListView";
    public static final String LIST_ITEM_VIEW_SUFFIX = "ListItemView";
    public static final String REST_SERVICE_SUFFIX = "RestService";
    public static final String ENTITY_SERVICE_SUFFIX = "EntityService";
    public static final String REST_IMPL_SUFFIX = "RestServiceImpl";

    private FormDefinition formDefinition;
    private Path path;
    private Package root;
    private Package local;
    private Package shared;
    private Package server;



    public SourceGenerationContext( FormDefinition form,
                                    Path path,
                                    Package root,
                                    Package local,
                                    Package shared,
                                    Package server ) {
        this.path = path;
        this.root = root;
        this.local = local;
        this.shared = shared;
        this.server = server;
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

    public String getModelName() {
        return formDefinition.getName() + FORM_MODEL_SUFFIX;
    }

    public String getFormViewName() {
        return formDefinition.getName() + FORM_VIEW_SUFFIX;
    }

    public String getListViewName() {
        return formDefinition.getName() + LIST_VIEW_SUFFIX;
    }

    public String getListItemViewName() {
        return formDefinition.getName() + LIST_ITEM_VIEW_SUFFIX;
    }

    public String getRestServiceName() {
        return formDefinition.getName() + REST_SERVICE_SUFFIX;
    }

    public String getEntityServiceName() {
        return formDefinition.getName() + ENTITY_SERVICE_SUFFIX;
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

    public String getListItemRowId() {
        return getFormDefinition().getName() + "-row";
    }

    public String getListTBodyId() {
        return getFormDefinition().getName() + "-table";
    }

}
