package org.livespark.formmodeler.service;

import java.util.Date;

import org.livespark.formmodeler.model.FieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public interface FieldManager {
    public static final String[] BASIC_TYPES = new String[]{
            String.class.getName(),
            Integer.class.getName(),
            Short.class.getName(),
            Long.class.getName(),
            Float.class.getName(),
            Double.class.getName(),
            Boolean.class.getName(),
            Date.class.getName(),
            int.class.getName(),
            long.class.getName(),
            boolean.class.getName(),
            short.class.getName(),
            double.class.getName()
    };

    FieldDefinition getDefinitionByType( String typeCode );

    FieldDefinition getDefinitionByValueType( Class clazz );

    FieldDefinition getDefinitionByValueType( String className );
}
