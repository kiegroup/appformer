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

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.BASE_REST_SERVICE;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.rest.RestApi;


@ApplicationScoped
@RestApi
public class RoasterRestApiJavaTemplateSourceGenerator extends RoasterRestJavaTemplateSourceGenerator<JavaInterfaceSource> {

    @Override
    public String generateJavaTemplateSource( SourceGenerationContext context ) {
        JavaInterfaceSource restIface = Roaster.create( JavaInterfaceSource.class );
        String packageName = getPackageName( context );

        addTypeSignature( context, restIface, packageName );
        addTypeLevelPath( restIface, context );
        addImports( context, restIface );
        addCrudMethods( context, restIface );

        return restIface.toString();
    }

    private void addCrudMethods( SourceGenerationContext context,
            JavaInterfaceSource restIface ) {
        addCreateMethod( context, restIface );
        addLoadMethod( context, restIface );
        addUpdateMethod( context, restIface );
        addDeleteMethod( context, restIface );
    }

    private void addUpdateMethod( SourceGenerationContext context,
            JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> update = restIface.addMethod();
        setUpdateMethodSignature( context, update );
        addUpdateAnnotations( update );
    }

    private void addUpdateAnnotations( MethodSource<JavaInterfaceSource> update ) {
        update.addAnnotation( Path.class ).setStringValue( "update" );
        update.addAnnotation( PUT.class );
        update.addAnnotation( Consumes.class ).setStringValue( "application/json" );
        update.addAnnotation( Produces.class ).setStringValue( "application/json" );
    }

    private void addCreateMethod( SourceGenerationContext context,
            JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> create = restIface.addMethod(  );

        setCreateMethodSignature( context, create );
        addCreateAnnotations( create );
    }

    private void addCreateAnnotations( MethodSource<JavaInterfaceSource> create ) {
        create.addAnnotation( POST.class );
        create.addAnnotation( Consumes.class ).setStringValue( "application/json" );
        create.addAnnotation( Produces.class ).setStringValue( "application/json" );
    }

    private void addLoadMethod( SourceGenerationContext context,
            JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> load = restIface.addMethod(  );
        setLoadMethodSignature( context, load );
        addLoadAnnotations( load );
    }

    private void addLoadAnnotations( MethodSource<JavaInterfaceSource> load ) {
        load.addAnnotation( Path.class ).setStringValue( "load" );
        load.addAnnotation( GET.class );
        load.addAnnotation( Produces.class ).setStringValue( "application/json" );
    }

    private void addDeleteMethod( SourceGenerationContext context,
            JavaInterfaceSource restIface ) {
        MethodSource<JavaInterfaceSource> delete = restIface.addMethod(  );
        setDeleteMethodSignature( context, delete );
        addDeleteAnnotations( delete );
    }

    private void addDeleteAnnotations( MethodSource<JavaInterfaceSource> delete ) {
        delete.addAnnotation( Path.class ).setStringValue( "delete" );
        delete.addAnnotation( DELETE.class );
        delete.addAnnotation( Consumes.class ).setStringValue( "application/json" );
        delete.addAnnotation( Produces.class ).setStringValue( "application/json" );
    }

    private void addTypeSignature( SourceGenerationContext context,
            JavaInterfaceSource restIface,
            String packageName ) {
        restIface.setPackage( packageName )
                .setPublic()
                .setName( context.getRestServiceName() )
                .addInterface( BASE_REST_SERVICE + "<" + context.getModelName() + ">" );
    }

    private void addTypeLevelPath( JavaInterfaceSource restIface , SourceGenerationContext context  ) {
        restIface.addAnnotation( Path.class ).setStringValue( context.getFormDefinition().getName().toLowerCase() );
    }

    @Override
    protected String getPackageName( SourceGenerationContext context ) {
        return context.getSharedPackage().getPackageName();
    }

    @Override
    protected void addImports( SourceGenerationContext context, JavaInterfaceSource restIface ) {
        super.addImports( context, restIface );
        restIface.addImport( BASE_REST_SERVICE );
    }
}
