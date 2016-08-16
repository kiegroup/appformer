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

package org.livespark.formmodeler.codegen.rest.impl;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.EJB_REQUIRES_NEW;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.EJB_STATELESS;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.EJB_TRANSACTION_ATTR;
import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.ENTITY_SERVICE_CLASS;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.FormJavaTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.rest.EntityService;

@ApplicationScoped
@EntityService
public class RoasterEntityServiceJavaTemplateSourceGenerator implements FormJavaTemplateSourceGenerator {

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource entityService = Roaster.create( JavaClassSource.class );
        String packageName = getPackageName( context );

        addTypeSignature( context, entityService, packageName );

        return entityService.toString();
    }

    private void addTypeSignature( SourceGenerationContext context,
                                   JavaClassSource entityService,
                                   String packageName ) {
        entityService.setPackage( packageName )
                     .setPublic()
                     .setName( context.getEntityServiceName() )
                     .setSuperType( ENTITY_SERVICE_CLASS );
        entityService.addAnnotation( EJB_STATELESS );
        entityService.addAnnotation( EJB_TRANSACTION_ATTR )
                     .setLiteralValue( EJB_REQUIRES_NEW );
    }

    private String getPackageName( SourceGenerationContext context ) {
        return context.getServerPackage().getPackageName();
    }

}
