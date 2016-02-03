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

package org.livespark.formmodeler.codegen.properties.impl;

import java.util.Collection;
import java.util.Iterator;

import org.livespark.formmodeler.codegen.ErraiAppPropertiesGenerator;

public class ErraiAppPropertiesGeneratorImpl implements ErraiAppPropertiesGenerator {

    private static final String SECURITY_COOKIE_ENABLED = "errai.security.user_cookie_enabled=true";
    private static final String CDI_ALTERNATIVES = "errai.ioc.enabled.alternatives=" +
            "org.uberfire.security.impl.authz.RuntimeAuthorizationManager \\" +
            "\norg.uberfire.client.workbench.WorkbenchServicesProxyClientImpl";

    private static final String MARHSALLING_DECLARATION_LHS = "errai.marshalling.serializableTypes=";
    private static final String BINDING_DECLARATION_LHS = "errai.ui.bindableTypes=";



    @Override
    public String generate( Collection<String> fullyQualifiedClassNames ) {
        final StringBuilder builder = new StringBuilder();

        builder.append( SECURITY_COOKIE_ENABLED );

        builder.append( "\n" );
        builder.append( CDI_ALTERNATIVES );

        builder.append( "\n\n" );
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
