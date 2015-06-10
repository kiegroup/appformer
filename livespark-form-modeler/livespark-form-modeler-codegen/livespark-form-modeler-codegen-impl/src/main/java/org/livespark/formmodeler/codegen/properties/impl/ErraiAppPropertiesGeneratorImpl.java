package org.livespark.formmodeler.codegen.properties.impl;

import java.util.Collection;
import java.util.Iterator;

import org.livespark.formmodeler.codegen.ErraiAppPropertiesGenerator;

public class ErraiAppPropertiesGeneratorImpl implements ErraiAppPropertiesGenerator {

    private static final String MARHSALLING_DECLARATION_LHS = "errai.marshalling.serializableTypes=";
    private static final String BINDING_DECLARATION_LHS = "errai.ui.bindableTypes=";

    @Override
    public String generate( Collection<String> fullyQualifiedClassNames ) {
        final StringBuilder builder = new StringBuilder();

        generateDeclaration( fullyQualifiedClassNames, builder, MARHSALLING_DECLARATION_LHS );
        generateDeclaration( fullyQualifiedClassNames, builder, BINDING_DECLARATION_LHS );

        return builder.toString();
    }

    private void generateDeclaration( Collection<String> fullyQualifiedClassNames, final StringBuilder builder, String declarationLhs  ) {
        if ( fullyQualifiedClassNames.size() > 0 ) {
            builder.append( declarationLhs );

            Iterator<String> iter = fullyQualifiedClassNames.iterator();
            do {
                builder.append( iter.next() );
                if ( iter.hasNext() ) {
                    builder.append( " \\\n" );
                }
            } while ( iter.hasNext() );
            builder.append( "\n" );
        }
    }

}
