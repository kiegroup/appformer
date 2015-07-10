/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.formmodeler.editor.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.jboss.errai.bus.server.annotations.Remote;
import org.livespark.formmodeler.editor.model.FieldDefinition;

/**
 * Created by pefernan on 4/29/15.
 */
@Remote
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

    FieldDefinition getDefinitionByValueType( String className );

    FieldDefinition getDefinitionByValueType( String className, String type );
}
