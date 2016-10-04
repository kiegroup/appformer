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
public class RoasterRestImplSourceGenerator extends RoasterRestServiceSourceGenerator<JavaClassSource> {

    private static final String ENTITY_SERVICE = "entityService";

    @Override
    public String generateJavaSource( final SourceGenerationContext context ) {
        final JavaClassSource restImpl = Roaster.create( JavaClassSource.class );

        addImports( context, restImpl );
        addFields( context, restImpl );
        addTypeSignature( context, restImpl );
        addTypeAnnotations( context, restImpl );
        addCrudMethodImpls( context, restImpl );

        return restImpl.toString();
    }

    private void addFields( final SourceGenerationContext context,
                            final JavaClassSource restImpl ) {
        restImpl.addField()
                .setPrivate()
                .setType( context.getEntityServiceName() )
                .setName( ENTITY_SERVICE )
                .addAnnotation( Inject.class );
    }

    private void addTypeAnnotations( final SourceGenerationContext context,
                                     final JavaClassSource restImpl ) {
        restImpl.addAnnotation( EJB_STATELESS );
    }

    @Override
    protected void addImports( final SourceGenerationContext context,
                               final JavaClassSource restImpl ) {
        super.addImports( context, restImpl );
        restImpl.addImport( context.getSharedPackage().getPackageName() + "." + context.getRestServiceName() );
    }

    private void addCrudMethodImpls( final SourceGenerationContext context,
                                     final JavaClassSource restImpl ) {
        addCreateMethodImpl( context, restImpl );
        addLoadMethodImpl( context, restImpl );
        addRangedLoadMethodImpl( context, restImpl );
        addUpdateMethodImpl( context, restImpl );
        addDeleteMethodImpl( context, restImpl );
        addListMethodImpl( context, restImpl );
    }

    private void addUpdateMethodImpl( final SourceGenerationContext context,
                                      final JavaClassSource restImpl ) {
        final MethodSource<JavaClassSource> update = restImpl.addMethod();
        setUpdateMethodSignature( context, update );
        setUpdateMethodBody( context, update );
    }

    private void setUpdateMethodBody( final SourceGenerationContext context,
                                      final MethodSource<JavaClassSource> update ) {
        update.setBody( ENTITY_SERVICE + ".update( model ); return true;" );
    }

    @Override
    protected void setUpdateMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<JavaClassSource> update ) {
        super.setUpdateMethodSignature( context, update );
        update.addAnnotation( Override.class );
    }

    private void addDeleteMethodImpl( final SourceGenerationContext context,
                                      final JavaClassSource restImpl ) {
        final MethodSource<JavaClassSource> delete = restImpl.addMethod();
        setDeleteMethodSignature( context, delete );
        setDeleteMethodBody( context, delete );
    }

    private void setDeleteMethodBody( final SourceGenerationContext context,
                                      final MethodSource<JavaClassSource> delete ) {
        delete.setBody( ENTITY_SERVICE + ".delete( model ); return true;" );
    }

    @Override
    protected void setDeleteMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<JavaClassSource> delete ) {
        super.setDeleteMethodSignature( context, delete );
        delete.addAnnotation( Override.class );
    }

    private void addListMethodImpl( final SourceGenerationContext context,
                                    final JavaClassSource restImpl ) {
        final MethodSource<JavaClassSource> list = restImpl.addMethod();
        setListMethodSignature( context, list );
        setListMethodBody( context, list );
    }

    @Override
    protected void setListMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<JavaClassSource> list ) {
        super.setListMethodSignature( context, list );
        list.addAnnotation( Override.class );
    }

    private void setListMethodBody( final SourceGenerationContext context,
                                      final MethodSource<JavaClassSource> list ) {
        final StringBuilder body = new StringBuilder();
        final List<DataHolder> dataHolders = context.getFormDefinition().getDataHolders();
        if ( dataHolders.size() > 1 )
            throw new UnsupportedOperationException( "Cannot load form models with multiple data models." );

        body.append( "return " )
                .append( ENTITY_SERVICE )
                .append( ".list( " )
                .append( context.getEntityName() )
                .append( ".class, criteria );" );

        list.setBody( body.toString() );
    }

    private void addLoadMethodImpl( final SourceGenerationContext context,
                                    final JavaClassSource restImpl ) {
        final MethodSource<JavaClassSource> load = restImpl.addMethod();
        setLoadMethodSignature( context, load );
        setLoadMethodBody( context, load );
    }

    /*
     * TODO support loading form models with multiple data models
     */
    private void setLoadMethodBody( final SourceGenerationContext context,
                                    final MethodSource<JavaClassSource> load ) {
        final StringBuilder body = new StringBuilder();
        final List<DataHolder> dataHolders = context.getFormDefinition().getDataHolders();
        if ( dataHolders.size() > 1 )
            throw new UnsupportedOperationException( "Cannot load form models with multiple data models." );

        body.append( "return " )
                .append( ENTITY_SERVICE )
                .append( ".listAll( " )
                .append( context.getEntityName() )
                .append( ".class );" );

        load.setBody( body.toString() );
    }

    @Override
    protected void setLoadMethodSignature( final SourceGenerationContext context,
                                           final MethodSource<JavaClassSource> load ) {
        super.setLoadMethodSignature( context, load );
        load.addAnnotation( Override.class );
    }

    private void addRangedLoadMethodImpl( final SourceGenerationContext context,
                                          final JavaClassSource restImpl ) {
        final MethodSource<JavaClassSource> load = restImpl.addMethod();
        setRangedLoadMethodSignature( context, load );
        setRangedLoadMethodBody( context, load );
    }

    private void setRangedLoadMethodBody( final SourceGenerationContext context,
                            final MethodSource<JavaClassSource> load ) {
        final StringBuilder body = new StringBuilder();
        final List<DataHolder> dataHolders = context.getFormDefinition().getDataHolders();
        if ( dataHolders.size() > 1 )
            throw new UnsupportedOperationException( "Cannot load form models with multiple data models." );

        body.append( "return " )
                .append( ENTITY_SERVICE )
                .append( ".list( " )
                .append( context.getEntityName() )
                .append( ".class, start, end );" );

        load.setBody( body.toString() );
    }

    private void setRangedLoadMethodSignature( final SourceGenerationContext context,
                                               final MethodSource<JavaClassSource> load ) {
        load
        .setName( "load" )
        .setPublic()
        .setReturnType( "List<" + context.getEntityName() + ">" );
        load.addParameter( int.class, "start" );
        load.addParameter( int.class, "end" );
        load.addAnnotation( Override.class );
    }

    private void addCreateMethodImpl( final SourceGenerationContext context,
                                      final JavaClassSource restImpl ) {
        final MethodSource<JavaClassSource> create = restImpl.addMethod();
        setCreateMethodSignature( context, create );
        setCreateMethodBody( context, create );
    }

    @Override
    protected void setCreateMethodSignature( final SourceGenerationContext context,
                                             final MethodSource<JavaClassSource> create ) {
        super.setCreateMethodSignature( context, create );
        create.addAnnotation( Override.class );
    }

    private void setCreateMethodBody( final SourceGenerationContext context,
                                      final MethodSource<JavaClassSource> create ) {
        final StringBuilder body = new StringBuilder();
        body.append( "return " )
            .append( ENTITY_SERVICE )
            .append( ".create( model );" );

        create.setBody( body.toString() );
    }

    private void addTypeSignature( final SourceGenerationContext context,
                                   final JavaClassSource restImpl ) {
        restImpl.setPackage( context.getServerPackage().getPackageName() )
                .setPublic()
                .setName( context.getRestServiceName() + "Impl" )
                .addInterface( context.getRestServiceName() );
    }

    @Override
    protected String getPackageName( final SourceGenerationContext context ) {
        return context.getServerPackage().getPackageName();
    }

}
