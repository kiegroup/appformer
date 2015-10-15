package org.livespark.formmodeler.editor.service;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by pefernan on 9/18/15.
 */
@Portable
public class SubFormData {
    private String formName;
    private String formModelClass;
    private String viewClass;

    public SubFormData() {
    }

    public SubFormData(@MapsTo("formName") String formName,
                       @MapsTo("formModelClass") String formModelClass,
                       @MapsTo("viewClass") String viewClass) {
        this.formName = formName;
        this.formModelClass = formModelClass;
        this.viewClass = viewClass;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormModelClass() {
        return formModelClass;
    }

    public void setFormModelClass(String formModelClass) {
        this.formModelClass = formModelClass;
    }

    public String getViewClass() {
        return viewClass;
    }

    public void setViewClass(String viewClass) {
        this.viewClass = viewClass;
    }
}
