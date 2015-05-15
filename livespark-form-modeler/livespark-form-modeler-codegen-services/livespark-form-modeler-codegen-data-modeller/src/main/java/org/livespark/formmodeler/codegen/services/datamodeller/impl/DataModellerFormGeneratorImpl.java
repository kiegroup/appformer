package org.livespark.formmodeler.codegen.services.datamodeller.impl;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.livespark.formmodeler.codegen.FormSourcesGenerator;
import org.livespark.formmodeler.codegen.services.datamodeller.DataModellerFormGenerator;
import org.livespark.formmodeler.model.DataHolder;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.impl.AbstractIntputFieldDefinition;
import org.livespark.formmodeler.service.FieldManager;
import org.kie.workbench.common.screens.datamodeller.model.AnnotationDefinitionTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;

/**
 * Created by pefernan on 4/29/15.
 */
public class DataModellerFormGeneratorImpl implements DataModellerFormGenerator {
    private static transient Logger log = LoggerFactory.getLogger( DataModellerFormGeneratorImpl.class );

    public static final String[] RESTRICTED_PROPERTY_NAMES = new String[]{"serialVersionUID"};

    @Inject
    protected FieldManager fieldManager;

    @Inject
    protected FormSourcesGenerator formSourcesGenerator;

    @Override
    public void generateFormForDataObject( DataObject dataObject, Path path ) {

        if (dataObject.getProperties().isEmpty()) return;

        FormDefinition form = new FormDefinition();
        form.setName( dataObject.getName() );

        String holderName = WordUtils.uncapitalize( dataObject.getName() );

        DataHolder holder = new DataHolder( holderName, dataObject.getClassName() );

        form.addDataHolder( holder );

        for (ObjectProperty property : dataObject.getProperties()) {
            if ( ArrayUtils.contains(RESTRICTED_PROPERTY_NAMES, property.getName()) ) continue;

            String propertyName = holderName + "_" + property.getName();
            FieldDefinition field = fieldManager.getDefinitionByValueType( property.getClassName() );

            if (field == null) continue;

            form.addField( field );

            field.setName( propertyName );
            String label = getPropertyLabel( property );
            field.setLabel( label );
            field.setBindingExpression( holderName + "." + property.getName() );

            if (field instanceof AbstractIntputFieldDefinition) {
                ((AbstractIntputFieldDefinition) field).setPlaceHolder( label );
            }
        }

        if (form.getFields().isEmpty()) return;

        formSourcesGenerator.generateFormSources( form, path );

    }

    private String getPropertyLabel( ObjectProperty property ) {
        Annotation labelAnnotation = property.getAnnotation( AnnotationDefinitionTO.LABEL_ANNOTATION );
        if ( labelAnnotation != null ) return labelAnnotation.getValue( AnnotationDefinitionTO.VALUE_PARAM ).toString();

        return property.getName();
    }


}
