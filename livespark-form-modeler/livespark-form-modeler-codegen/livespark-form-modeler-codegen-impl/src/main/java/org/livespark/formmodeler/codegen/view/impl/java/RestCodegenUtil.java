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

package org.livespark.formmodeler.codegen.view.impl.java;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_REST_CLIENT;

import org.livespark.formmodeler.codegen.SourceGenerationContext;


public class RestCodegenUtil {

    public static String generateRestCall( String restMethodName,
                                     String callbackParamName,
                                     SourceGenerationContext context,
                                     String... params) {
        StringBuilder body = new StringBuilder()
                                  .append( ERRAI_REST_CLIENT )
                                  .append( ".create(" )
                                  .append( context.getRestServiceName() )
                                  .append( ".class, " )
                                  .append( callbackParamName )
                                  .append( ")." )
                                  .append( restMethodName )
                                  .append( "(" );

        for (String p : params) {
            body.append( p )
                .append( ", " );
        }
        if (params.length > 0) body.delete( body.length() - ", ".length(), body.length() );

        body.append( ");" );

        return body.toString();
    }

}
