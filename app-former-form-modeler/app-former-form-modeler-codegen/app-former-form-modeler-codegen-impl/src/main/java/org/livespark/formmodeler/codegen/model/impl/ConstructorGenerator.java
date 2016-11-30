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

package org.livespark.formmodeler.codegen.model.impl;


import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.workbench.common.forms.model.FormModel;
import org.kie.workbench.common.forms.model.IsJavaModel;
import org.livespark.formmodeler.codegen.SourceGenerationContext;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public class ConstructorGenerator {

    public void addNoArgConstructor( JavaClassSource modelClass ) {
        modelClass.addMethod()
                .setConstructor( true )
                .setPublic()
                .setBody( "" );
    }

    public void addFormModelConstructor( SourceGenerationContext context,
                                         JavaClassSource modelClass ) {
        FormModel model = context.getFormDefinition().getModel();
        if ( model != null && model instanceof IsJavaModel ) {
            MethodSource<JavaClassSource> constructor = modelClass.addMethod()
                    .setConstructor( true )
                    .setPublic();
            StringBuffer source = new StringBuffer();

            constructor.addParameter( ( (IsJavaModel) model ).getType(),
                                      model.getName() )
                    .addAnnotation( ERRAI_MAPS_TO )
                    .setStringValue( model.getName() );


            source.append( "this." )
                    .append( model.getName() )
                    .append( " = " )
                    .append( model.getName() )
                    .append( ";" );

            constructor.setBody( source.toString() );
        }
    }
}
