package org.kie.formModeler.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.kie.formModeler.model.FieldDefinition;
import org.kie.formModeler.model.impl.CheckBoxFieldDefinition;
import org.kie.formModeler.model.impl.DateBoxFieldDefinition;
import org.kie.formModeler.model.impl.DoubleBoxFieldDefinition;
import org.kie.formModeler.model.impl.IntBoxFieldDefinition;
import org.kie.formModeler.model.impl.LongBoxFieldDefinition;
import org.kie.formModeler.model.impl.TextBoxFieldDefinition;
import org.kie.formModeler.service.FieldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pefernan on 4/29/15.
 */
@ApplicationScoped
public class FieldManagerImpl implements FieldManager {
    private static transient Logger log = LoggerFactory.getLogger( FieldManagerImpl.class );

    @Inject
    private Instance<FieldDefinition<Boolean>> booleanFields;

    @Inject
    private Instance<FieldDefinition<Date>> dateFields;

    @Inject
    private Instance<FieldDefinition<Double>> doubleFields;

    @Inject
    private Instance<FieldDefinition<Float>> floatFields;

    @Inject
    private Instance<FieldDefinition<Integer>> intFields;

    @Inject
    private Instance<FieldDefinition<Long>> longFields;

    @Inject
    private Instance<FieldDefinition<Short>> shortFields;

    @Inject
    private Instance<FieldDefinition<String>> stringFields;



    protected Map<String, String> defaultFields = new HashMap<String, String>(  );

    {
        defaultFields.put( Boolean.class.getName(), CheckBoxFieldDefinition.class.getName() );
        defaultFields.put( boolean.class.getName(), CheckBoxFieldDefinition.class.getName() );
        defaultFields.put( Date.class.getName(), DateBoxFieldDefinition.class.getName() );
        defaultFields.put( Double.class.getName(), DoubleBoxFieldDefinition.class.getName() );
        defaultFields.put( double.class.getName(), DoubleBoxFieldDefinition.class.getName() );
        defaultFields.put( Integer.class.getName(), IntBoxFieldDefinition.class.getName() );
        defaultFields.put( int.class.getName(), IntBoxFieldDefinition.class.getName() );
        defaultFields.put( Long.class.getName(), LongBoxFieldDefinition.class.getName() );
        defaultFields.put( long.class.getName(), LongBoxFieldDefinition.class.getName() );
        defaultFields.put( String.class.getName(), TextBoxFieldDefinition.class.getName() );
    }

    protected Map<String, FieldDefinition> fieldDefinitions = new HashMap<String, FieldDefinition>(  );

    protected Map<String, List<String>> compatibleFields = new HashMap<String, List<String>>(  );


    @PostConstruct
    protected void init() {
        List <String> compatibles = new ArrayList<String>(  );

        for (FieldDefinition fieldDefinition : booleanFields ) {
            fieldDefinitions.put( fieldDefinition.getCode(), fieldDefinition );
            compatibles.add( fieldDefinition.getCode() );
        }
        compatibleFields.put( Boolean.class.getName(), compatibles );
        compatibleFields.put( boolean.class.getName(), compatibles );

        compatibles = new ArrayList<String>(  );
        for (FieldDefinition fieldDefinition : dateFields ) {
            fieldDefinitions.put( fieldDefinition.getCode(), fieldDefinition );
            compatibles.add( fieldDefinition.getCode() );
        }
        compatibleFields.put( Date.class.getName(), compatibles );

        compatibles = new ArrayList<String>(  );
        for (FieldDefinition fieldDefinition : doubleFields ) {
            fieldDefinitions.put( fieldDefinition.getCode(), fieldDefinition );
            compatibles.add( fieldDefinition.getCode() );
        }
        compatibleFields.put( Double.class.getName(), compatibles );
        compatibleFields.put( double.class.getName(), compatibles );

        compatibles = new ArrayList<String>(  );
        for (FieldDefinition fieldDefinition : floatFields ) {
            fieldDefinitions.put( fieldDefinition.getCode(), fieldDefinition );
            compatibles.add( fieldDefinition.getCode() );
        }
        compatibleFields.put( Float.class.getName(), compatibles );
        compatibleFields.put( float.class.getName(), compatibles );

        compatibles = new ArrayList<String>(  );
        for (FieldDefinition fieldDefinition : intFields ) {
            fieldDefinitions.put( fieldDefinition.getCode(), fieldDefinition );
            compatibles.add( fieldDefinition.getCode() );
        }
        compatibleFields.put( Integer.class.getName(), compatibles );
        compatibleFields.put( int.class.getName(), compatibles );

        compatibles = new ArrayList<String>(  );
        for (FieldDefinition fieldDefinition : longFields ) {
            fieldDefinitions.put( fieldDefinition.getCode(), fieldDefinition );
            compatibles.add( fieldDefinition.getCode() );
        }
        compatibleFields.put( Long.class.getName(), compatibles );
        compatibleFields.put( long.class.getName(), compatibles );

        compatibles = new ArrayList<String>(  );
        for (FieldDefinition fieldDefinition : shortFields ) {
            fieldDefinitions.put( fieldDefinition.getCode(), fieldDefinition );
            compatibles.add( fieldDefinition.getCode() );
        }
        compatibleFields.put( Short.class.getName(), compatibles );
        compatibleFields.put( short.class.getName(), compatibles );

        compatibles = new ArrayList<String>(  );
        for (FieldDefinition fieldDefinition : stringFields ) {
            fieldDefinitions.put( fieldDefinition.getCode(), fieldDefinition );
            compatibles.add( fieldDefinition.getCode() );
        }
        compatibleFields.put( String.class.getName(), compatibles );
    }

    @Override
    public FieldDefinition getDefinitionByType( String typeCode ) {
        FieldDefinition definition = fieldDefinitions.get( typeCode );
        if (definition != null) {
            try {
                return definition.getClass().newInstance();
            } catch ( Exception e ) {
                log.warn( "Error creating FieldDefinition: ", e );
            }
        }
        return null;
    }

    @Override
    public FieldDefinition getDefinitionByValueType( Class clazz ) {
        return getDefinitionByValueType( clazz.getName() );
    }

    @Override
    public FieldDefinition getDefinitionByValueType( String className ) {
        FieldDefinition definition = fieldDefinitions.get( defaultFields.get( className ) );
        if (definition != null) {
            try {
                return definition.getClass().newInstance();
            } catch ( Exception e ) {
                log.warn( "Error creating FieldDefinition: ", e );
            }
        }
        return null;
    }
}
