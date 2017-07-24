/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.kie.workbench.common.forms.model.FieldDefinition;

/**
 * This Factory matches a basic Java Type to the right Converter className in order to generate the right @Bound annotation
 */
public class SourceGenerationValueConvertersFactory {

    public static final String CHARACTER_TO_STRING_CONVERTER = "org.kie.workbench.common.forms.common.rendering.client.util.valueConverters.CharacterToStringConverter";
    public static final String BIG_INTEGER_TO_LONG_CONVERTER = "org.kie.workbench.common.forms.common.rendering.client.util.valueConverters.BigIntegerToLongConverter";
    public static final String INTEGER_TO_LONG_CONVERTER = "org.kie.workbench.common.forms.common.rendering.client.util.valueConverters.IntegerToLongConverter";
    public static final String SHORT_TO_LONG_CONVERTER = "org.kie.workbench.common.forms.common.rendering.client.util.valueConverters.ShortToLongConverter";
    public static final String BYTE_TO_LONG_CONVERTER = "org.kie.workbench.common.forms.common.rendering.client.util.valueConverters.ByteToLongConverter";
    public static final String FLOAT_TO_DOUBLE_CONVERTER = "org.kie.workbench.common.forms.common.rendering.client.util.valueConverters.FloatToDoubleConverter";
    public static final String BIG_DECIMAL_TO_DOUBLE_CONVERTER = "org.kie.workbench.common.forms.common.rendering.client.util.valueConverters.BigDecimalToDoubleConverter";

    static Map<String, String> converters = new HashMap<>();

    static {
        converters.put(Character.class.getName(), CHARACTER_TO_STRING_CONVERTER);
        converters.put(char.class.getName(), CHARACTER_TO_STRING_CONVERTER);
        converters.put(BigInteger.class.getName(), BIG_INTEGER_TO_LONG_CONVERTER);
        converters.put(Integer.class.getName(), INTEGER_TO_LONG_CONVERTER);
        converters.put(int.class.getName(), INTEGER_TO_LONG_CONVERTER);
        converters.put(Short.class.getName(), SHORT_TO_LONG_CONVERTER);
        converters.put(short.class.getName(), SHORT_TO_LONG_CONVERTER);
        converters.put(Byte.class.getName(), BYTE_TO_LONG_CONVERTER);
        converters.put(byte.class.getName(), BYTE_TO_LONG_CONVERTER);
        converters.put(Float.class.getName(), FLOAT_TO_DOUBLE_CONVERTER);
        converters.put(float.class.getName(), FLOAT_TO_DOUBLE_CONVERTER);
        converters.put(BigDecimal.class.getName(), BIG_DECIMAL_TO_DOUBLE_CONVERTER);
    }

    /**
     * Given a {@link FieldDefinition} this method returns the suitable Converter className.
     * @param fieldDefinition a {@link FieldDefinition}
     * @return A String specifying the Converter className or null if the field doesn't require any Converter.
     */
    public static String getConverterClassName(FieldDefinition fieldDefinition) {
        return getConverterClassNameForType(fieldDefinition.getStandaloneClassName());
    }

    /**
     * Given a ClassName this method returns the suitable Converter className.
     * @param className a String containing a Class Name
     * @return A String specifying the Converter className or null if the class doesn't require any Converter.
     */

    public static String getConverterClassNameForType(String className) {
        return converters.get(className);
    }
}
