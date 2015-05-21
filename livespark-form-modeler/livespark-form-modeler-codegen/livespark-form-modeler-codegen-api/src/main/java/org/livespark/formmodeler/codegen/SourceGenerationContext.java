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

    private FormDefinition formDefinition;
    private Path path;

    private String modelName;
    private String viewName;
    private String listViewName;
    private String listItemViewName;


    public SourceGenerationContext( FormDefinition form, Path path ) {
        this.path = path;
        setFormDefinition( form );
    }

    public FormDefinition getFormDefinition() {
        return formDefinition;
    }

    public void setFormDefinition( FormDefinition formDefinition ) {
        this.formDefinition = formDefinition;
        this.modelName = formDefinition.getName() + FORM_MODEL_SUFFIX;
        this.viewName = formDefinition.getName() + FORM_VIEW_SUFFIX;
        this.listViewName = formDefinition.getName() + LIST_VIEW_SUFFIX;
        this.listItemViewName = formDefinition.getName() + LIST_ITEM_VIEW_SUFFIX;
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

    public String getListViewName() {
        return listViewName;
    }

    public void setListViewName( String listViewName ) {
        this.listViewName = listViewName;
    }

    public String getListItemViewName() {
        return listItemViewName;
    }

    public void setListItemViewName( String listItemViewName ) {
        this.listItemViewName = listItemViewName;
    }
}
