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

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.EJB_STATELESS;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.workbench.common.forms.model.DataHolder;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.rest.RestImpl;


@ApplicationScoped
@RestImpl
public class RoasterRestImplJavaTemplateSourceGenerator extends RoasterRestJavaTemplateSourceGenerator<JavaClassSource> {

    private static final String ENTITY_SERVICE = "entityService";

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaClassSource restImpl = Roaster.create( JavaClassSource.class );

        addImports( context, restImpl );
        addFields( context, restImpl );
        addTypeSignature( context, restImpl );
        addTypeAnnotations( context, restImpl );
        addCrudMethodImpls( context, restImpl );

        return restImpl.toString();
    }

    private void addFields( SourceGenerationContext context,
                            JavaClassSource restImpl ) {
        restImpl.addField()
                .setPrivate()
                .setType( context.getEntityServiceName() )
                .setName( ENTITY_SERVICE )
                .addAnnotation( Inject.class );
    }

    private void addTypeAnnotations( SourceGenerationContext context,
                                     JavaClassSource restImpl ) {
        restImpl.addAnnotation( EJB_STATELESS );
    }

    @Override
    protected void addImports( SourceGenerationContext context,
                               JavaClassSource restImpl ) {
        super.addImports( context, restImpl );
        restImpl.addImport( context.getSharedPackage().getPackageName() + "." + context.getRestServiceName() );
    }

    private void addCrudMethodImpls( SourceGenerationContext context,
                                     JavaClassSource restImpl ) {
        addCreateMethodImpl( context, restImpl );
        addLoadMethodImpl( context, restImpl );
        addUpdateMethodImpl( context, restImpl );
        addDeleteMethodImpl( context, restImpl );
    }

    private void addUpdateMethodImpl( SourceGenerationContext context,
                                      JavaClassSource restImpl ) {
        MethodSource<JavaClassSource> update = restImpl.addMethod();
        setUpdateMethodSignature( context, update );
        setUpdateMethodBody( context, update );
    }

    private void setUpdateMethodBody( SourceGenerationContext context,
                                      MethodSource<JavaClassSource> update ) {
        update.setBody( ENTITY_SERVICE + ".update( model ); return true;" );
    }

    @Override
    protected void setUpdateMethodSignature( SourceGenerationContext context,
                                             MethodSource<JavaClassSource> update ) {
        super.setUpdateMethodSignature( context, update );
        update.addAnnotation( Override.class );
    }

    private void addDeleteMethodImpl( SourceGenerationContext context,
                                      JavaClassSource restImpl ) {
        MethodSource<JavaClassSource> delete = restImpl.addMethod();
        setDeleteMethodSignature( context, delete );
        setDeleteMethodBody( context, delete );
    }

    private void setDeleteMethodBody( SourceGenerationContext context,
                                      MethodSource<JavaClassSource> delete ) {
        delete.setBody( ENTITY_SERVICE + ".delete( model ); return true;" );
    }

    @Override
    protected void setDeleteMethodSignature( SourceGenerationContext context,
                                             MethodSource<JavaClassSource> delete ) {
        super.setDeleteMethodSignature( context, delete );
        delete.addAnnotation( Override.class );
    }

    private void addLoadMethodImpl( SourceGenerationContext context,
                                    JavaClassSource restImpl ) {
        MethodSource<JavaClassSource> load = restImpl.addMethod();
        setLoadMethodSignature( context, load );
        setLoadMethodBody( context, load );
    }

    /*
     * TODO support loading form models with multiple data models
     */
    private void setLoadMethodBody( SourceGenerationContext context,
                                    MethodSource<JavaClassSource> load ) {
        StringBuilder body = new StringBuilder();
        List<DataHolder> dataHolders = context.getFormDefinition().getDataHolders();
        if ( dataHolders.size() > 1 )
            throw new UnsupportedOperationException( "Cannot load form models with multiple data models." );

        DataHolder holder = dataHolders.get( 0 );
        body.append( "return " )
                .append( ENTITY_SERVICE )
                .append( ".listAll( " )
                .append( context.getEntityName() )
                .append( ".class );" );

        load.setBody( body.toString() );
    }

    @Override
    protected void setLoadMethodSignature( SourceGenerationContext context,
                                           MethodSource<JavaClassSource> load ) {
        super.setLoadMethodSignature( context, load );
        load.addAnnotation( Override.class );
    }

    private void addCreateMethodImpl( SourceGenerationContext context,
                                      JavaClassSource restImpl ) {
        MethodSource<JavaClassSource> create = restImpl.addMethod();
        setCreateMethodSignature( context, create );
        setCreateMethodBody( context, create );
    }

    @Override
    protected void setCreateMethodSignature( SourceGenerationContext context,
                                             MethodSource<JavaClassSource> create ) {
        super.setCreateMethodSignature( context, create );
        create.addAnnotation( Override.class );
    }

    private void setCreateMethodBody( SourceGenerationContext context,
                                      MethodSource<JavaClassSource> create ) {
        StringBuilder body = new StringBuilder();
        body.append( ENTITY_SERVICE )
                .append( ".create( model );" )
                .append( "return model;" );

        create.setBody( body.toString() );
    }

    private void addTypeSignature( SourceGenerationContext context,
                                   JavaClassSource restImpl ) {
        restImpl.setPackage( context.getServerPackage().getPackageName() )
                .setPublic()
                .setName( context.getRestServiceName() + "Impl" )
                .addInterface( context.getRestServiceName() );
    }

    @Override
    protected String getPackageName( SourceGenerationContext context ) {
        return context.getServerPackage().getPackageName();
    }

}
