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

package org.kie.appformer.formmodeler.codegen.model.impl;


import static org.kie.appformer.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_MAPS_TO;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.workbench.common.forms.model.FormModel;
import org.kie.workbench.common.forms.model.JavaModel;

public class ConstructorGenerator {

    public void addNoArgConstructor( final JavaClassSource modelClass ) {
        modelClass.addMethod()
                .setConstructor( true )
                .setPublic()
                .setBody( "" );
    }

    public void addFormModelConstructor( final SourceGenerationContext context,
                                         final JavaClassSource modelClass ) {
        final FormModel model = context.getFormDefinition().getModel();
        if ( model != null && model instanceof JavaModel ) {
            final MethodSource<JavaClassSource> constructor = modelClass.addMethod()
                    .setConstructor( true )
                    .setPublic();
            final StringBuffer source = new StringBuffer();

            constructor.addParameter( ( (JavaModel) model ).getType(),
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
