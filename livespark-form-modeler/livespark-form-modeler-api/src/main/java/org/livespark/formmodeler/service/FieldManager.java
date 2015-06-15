package org.livespark.formmodeler.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.livespark.formmodeler.model.FieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
public interface FieldManager {
    public static final String[] BASIC_TYPES = new String[]{
            BigDecimal.class.getName(),
            BigInteger.class.getName(),
            Byte.class.getName(),
            byte.class.getName(),
            Boolean.class.getName(),
            boolean.class.getName(),
            Character.class.getName(),
            char.class.getName(),
            Date.class.getName(),
            Double.class.getName(),
            double.class.getName(),
            Float.class.getName(),
            float.class.getName(),
            Integer.class.getName(),
            int.class.getName(),
            Long.class.getName(),
            long.class.getName(),
            Short.class.getName(),
            short.class.getName(),
            String.class.getName()
    };

    FieldDefinition getDefinitionByType( String typeCode );

    FieldDefinition getDefinitionByValueType( Class clazz );

    FieldDefinition getDefinitionByValueType( String className );
}
