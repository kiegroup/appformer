package org.livespark.formmodeler.editor.backend.service.impl;

import org.guvnor.common.services.project.model.*;
import org.guvnor.common.services.project.model.Package;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datamodeller.model.maindomain.MainDomainAnnotations;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.livespark.formmodeler.editor.model.impl.relations.EmbeddedFormField;
import org.livespark.formmodeler.editor.service.SubFormData;
import org.livespark.formmodeler.editor.service.FormFinderSerivce;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

/**
 * Created by pefernan on 9/18/15.
 */
@Service
@ApplicationScoped
public class FormFinderSerivceImpl implements FormFinderSerivce {
    protected static final String MAIN_RESOURCES_PATH = "src/main/resources/";

    @Inject
    protected DataModelerService dataModelerService;

    @Inject
    protected KieProjectService projectService;

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Override
    public FormDefinition getNewFormInstance() {
        FormDefinition form = new FormDefinition();

        form.setId( UUID.randomUUID().toString() );

        return form;
    }

    @Override
    public List<SubFormData> getAvailableFormsByType(String modelType, Path path) {
        return getAvailableFormsByType(modelType, FORM_VIEW_CLASS, path);
    }

    @Override
    public List<SubFormData> getAvailableMultipleSubFormsByType(String modelType, Path path) {
        return getAvailableFormsByType(modelType, LIST_VIEW_CLASS, path);
    }

    protected List<SubFormData> getAvailableFormsByType(String modelType, String viewType, Path path) {
        KieProject project = projectService.resolveProject(path);

        DataModel dataModel = dataModelerService.loadModel( project  );

        Map<String, SubFormData> subformsMap = new HashMap<String, SubFormData>();

        // Getting all the FormViews
        for ( DataObject object : dataModel.getDataObjects() ) {
            if (object.getSuperClassName().equals( viewType )) {
                Annotation formModelAnnotation = object.getAnnotation( FORM_MODEL_ANNOTATION );
                if (formModelAnnotation != null) {
                    String modelName = formModelAnnotation.getValue( MainDomainAnnotations.VALUE_PARAM ).toString();

                    SubFormData data = subformsMap.get(modelName);
                    if ( data == null) {
                        data = new SubFormData();
                        subformsMap.put(modelName, data);
                    }

                    data.setFormName( modelName.substring( modelName.lastIndexOf(".") + 1) );
                    data.setFormModelClass( modelName );
                    data.setViewClass( object.getClassName() );
                }
            }
        }

        // Filtering the existing FormViews to get only the ones that are valid for this specific modelType
        Iterator<Map.Entry<String,SubFormData>> it = subformsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,SubFormData> entry = it.next();

            DataObject object = dataModel.getDataObject( entry.getKey() );

            if ( object != null ) {
                if ( object.getProperties().size() != 1 ||
                    !object.getProperties().get( 0 ).getClassName().equals( modelType )) {
                    it.remove();
                }
            } else {
                it.remove();
            }
        }
        List<SubFormData> result = new ArrayList<SubFormData>(subformsMap.values());

        Collections.sort(result, new Comparator<SubFormData>() {
            @Override
            public int compare(SubFormData o1, SubFormData o2) {
                return o1.getFormModelClass().compareTo( o2.getFormModelClass() );
            }
        });

        return result;
    }

    @Override
    public String getSubFormTemplate(EmbeddedFormField field, Path path) {
        if ( field == null || path == null ) return null;
        KieProject project = projectService.resolveProject(path);

        String viewType = field.getEmbeddedFormView();

        if ( viewType != null ) {
            String viewName = MAIN_RESOURCES_PATH + viewType.replaceAll("\\.", "/") + ".html";
            org.uberfire.java.nio.file.Path templatePath = Paths.convert( project.getRootPath() ).resolve( viewName );
            return ioService.readAllString( templatePath ).trim();
        }

        return null;
    }
}
