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
