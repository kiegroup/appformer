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

package org.kie.appformer.formmodeler.codegen.properties.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import org.kie.appformer.formmodeler.codegen.ErraiAppPropertiesGenerator;

public class ErraiAppPropertiesGeneratorImpl implements ErraiAppPropertiesGenerator {

    private static final String MARHSALLING_DECLARATION_LHS = "errai.marshalling.serializableTypes=";
    private static final String BINDING_DECLARATION_LHS = "errai.ui.bindableTypes=";

    private static final String GENERATED_COMMENT_START = "# Start generated bindable/serializable type declarations";
    private static final String GENERATED_COMMENT_END = "# End generated bindable/serializable type declarations";

    @Override
    public String generate( final Collection<String> fullyQualifiedClassNames, final Optional<String> previousProperties ) {
        final StringBuilder builder = new StringBuilder( filterPrevious( previousProperties ) );

        builder.append( GENERATED_COMMENT_START ).append( '\n' );
        generateDeclaration( fullyQualifiedClassNames, builder, MARHSALLING_DECLARATION_LHS );
        generateDeclaration( fullyQualifiedClassNames, builder, BINDING_DECLARATION_LHS );
        builder.append( GENERATED_COMMENT_END ).append( '\n' );

        return builder.toString();
    }

    private String filterPrevious( final Optional<String> previousProperties ) {
        return previousProperties
                .map( previous -> {
                    final int startIndex = previous.indexOf( GENERATED_COMMENT_START );
                    final int endIndex = previous.indexOf( GENERATED_COMMENT_END );
                    if ( startIndex != -1 && endIndex != -1 ) {
                        return previous.substring( 0,
                                                   startIndex )
                               + previous.substring( endIndex + GENERATED_COMMENT_END.length(),
                                                     previous.length() );
                    }
                    else {
                        return previous;
                    }
                } )
                .orElse( "" );
    }

    private void generateDeclaration( final Collection<String> fullyQualifiedClassNames, final StringBuilder builder, final String declarationLhs  ) {
        if ( fullyQualifiedClassNames.size() > 0 ) {
            builder.append( declarationLhs );

            final Iterator<String> iter = fullyQualifiedClassNames.iterator();
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
