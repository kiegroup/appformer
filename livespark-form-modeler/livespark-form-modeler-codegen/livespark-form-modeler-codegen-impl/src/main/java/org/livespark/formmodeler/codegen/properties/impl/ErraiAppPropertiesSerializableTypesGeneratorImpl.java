package org.livespark.formmodeler.codegen.properties.impl;

import java.util.Collection;
import java.util.Iterator;

import org.livespark.formmodeler.codegen.ErraiAppPropertiesSerializableTypesGenerator;

public class ErraiAppPropertiesSerializableTypesGeneratorImpl implements ErraiAppPropertiesSerializableTypesGenerator {

    private static final String DECLARATION_LHS = "errai.marshalling.serializableTypes=";

    @Override
    public String generateSerializableTypesDeclaration( Collection<String> fullyQualifiedClassNames ) {
        final StringBuilder builder = new StringBuilder();

        if ( fullyQualifiedClassNames.size() > 0 ) {
            builder.append( DECLARATION_LHS );

            Iterator<String> iter = fullyQualifiedClassNames.iterator();
            do {
                builder.append( iter.next() );
                if ( iter.hasNext() ) {
                    builder.append( " \\\n" );
                }
            } while ( iter.hasNext() );
        }

        return builder.toString();
    }

}
