package org.livespark.formmodeler.codegen;

import org.livespark.formmodeler.model.FormDefinition;
import org.uberfire.backend.vfs.Path;

/**
 * Created by pefernan on 4/28/15.
 */
public class SourceGenerationContext {
    public static final String FORM_MODEL_SUFFIX = "FormModel";
    public static final String FORM_VIEW_SUFFIX = "FormView";
    public static final String LIST_VIEW_SUFFIX = "ListView";
    public static final String LIST_ITEM_VIEW_SUFFIX = "ListItemView";
    public static final String REST_SERVICE_SUFFIX = "RestService";

    private FormDefinition formDefinition;
    private Path path;



    public SourceGenerationContext( FormDefinition form, Path path ) {
        this.path = path;
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

    public void setPath( Path path ) {
        this.path = path;
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
}
