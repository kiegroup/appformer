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
import org.kie.workbench.common.forms.model.DataHolder;
import org.livespark.formmodeler.codegen.SourceGenerationContext;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ERRAI_MAPS_TO;

public class ConstructorGenerator {

    public void addNoArgConstructor( JavaClassSource modelClass ) {
        modelClass.addMethod()
                .setConstructor( true )
                .setPublic()
                .setBody( "" );
    }

    public void addFormModelConstructor( SourceGenerationContext context,
                                         JavaClassSource modelClass ) {
        if ( !context.getFormDefinition().getDataHolders().isEmpty() ) {
            MethodSource<JavaClassSource> constructor = modelClass.addMethod()
                    .setConstructor( true )
                    .setPublic();
            StringBuffer source = new StringBuffer();

            for ( DataHolder dataHolder : context.getFormDefinition().getDataHolders() ) {
                constructor.addParameter( dataHolder.getType(),
                                          dataHolder.getName() )
                        .addAnnotation( ERRAI_MAPS_TO )
                        .setStringValue( dataHolder.getName() );
                source.append( "this." )
                        .append( dataHolder.getName() )
                        .append( " = " )
                        .append( dataHolder.getName() )
                        .append( ";" );
            }

            constructor.setBody( source.toString() );
        }
    }
}
