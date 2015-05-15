package org.livespark.formmodeler.codegen;

import org.livespark.formmodeler.model.FormDefinition;
import org.uberfire.backend.vfs.Path;

/**
 * Created by pefernan on 4/28/15.
 */
public class SourceGenerationContext {
    public static final String FORM_MODEL_SUFFIX = "FormModel";
    public static final String FORM_VIEW_SUFFIX = "FormView";

    private FormDefinition formDefinition;
    private Path path;

    private String modelName;
    private String viewName;


    public SourceGenerationContext( FormDefinition form, Path path ) {
        this.formDefinition = form;
        this.path = path;
        this.modelName = formDefinition.getName() + FORM_MODEL_SUFFIX;
        this.viewName = formDefinition.getName() + FORM_VIEW_SUFFIX;
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

    public void setPath( Path path ) {
        this.path = path;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName( String modelName ) {
        this.modelName = modelName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName( String viewName ) {
        this.viewName = viewName;
    }
}
