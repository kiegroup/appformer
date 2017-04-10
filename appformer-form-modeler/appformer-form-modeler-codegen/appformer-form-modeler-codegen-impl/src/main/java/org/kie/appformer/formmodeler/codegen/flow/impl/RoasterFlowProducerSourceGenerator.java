/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.formmodeler.codegen.flow.impl;

import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.appformer.flow.api.Command;
import org.kie.appformer.flow.api.FormOperation;
import org.kie.appformer.flow.api.Unit;
import org.kie.appformer.formmodeler.codegen.JavaSourceGenerator;
import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.rendering.client.flow.FlowProducer;
import org.kie.appformer.formmodeler.rendering.client.flow.ForEntity;

@org.kie.appformer.formmodeler.codegen.flow.FlowProducer
@ApplicationScoped
public class RoasterFlowProducerSourceGenerator implements JavaSourceGenerator {

    @Override
    public String generateJavaSource( final SourceGenerationContext context ) {
        final JavaClassSource producerClass = Roaster.create( JavaClassSource.class );

        addImports( producerClass, context );
        setTypeSignature( producerClass, context );
        implementAbstractMethods( producerClass, context );
        implementProducerMethods( producerClass, context );

        return producerClass.toString();
    }

    private static void addImports( final JavaClassSource producerClass, final SourceGenerationContext context ) {
        producerClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getEntityName() );
        producerClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getFormModelName() );
        producerClass.addImport( context.getSharedPackage().getPackageName() + "." + context.getRestServiceName() );
        producerClass.addImport( Unit.class.getName() );
        producerClass.addImport( Optional.class.getName() );
        producerClass.addImport( Command.class.getName() );
        producerClass.addImport( FormOperation.class.getName() );
    }

    private static void setTypeSignature( final JavaClassSource producerClass, final SourceGenerationContext context ) {
        producerClass
            .setPublic()
            .setPackage( context.getLocalPackage().getPackageName() )
            .setName( context.getFlowProducerName() )
            .setSuperType( new StringBuilder( FlowProducer.class.getName() )
                               .append( '<' )
                               .append( context.getEntityName() )
                               .append( ", " )
                               .append( context.getFormModelName() )
                               .append( ", " )
                               .append( context.getFormViewName() )
                               .append( ", " )
                               .append( context.getListViewName() )
                               .append( ", " )
                               .append( context.getRestServiceName() )
                               .append( '>' )
                               .toString() )
            .addAnnotation( EntryPoint.class );
    }

    private static void implementProducerMethods( final JavaClassSource producerClass,
                                           final SourceGenerationContext context ) {
        entityType( producerClass, context );
        addProducerMethodsForAllFlows( producerClass, context );
    }

    private static void entityType( final JavaClassSource producerClass,
                             final SourceGenerationContext context ) {
        final MethodSource<JavaClassSource> entityType =
                producerClass
                .addMethod()
                .setName( "entityType" )
                .setPublic()
                .setBody( "return " + context.getEntityName() + ".class;" )
                .setReturnType( "java.lang.Class<" + context.getEntityName() + ">" );

        entityType.addAnnotation( Singleton.class );
        entityType.addAnnotation( Produces.class );
    }

    private static void addProducerMethodsForAllFlows( final JavaClassSource producerClass,
                                                       final SourceGenerationContext context ) {
        class MethodDef {
            final String name, returnType;
            MethodDef(final String name, final String returnType) {
                this.name = name;
                this.returnType = returnType;
            }
        }

        Arrays
        .asList(
                new MethodDef( "create",
                               "org.kie.appformer.flow.api.AppFlow<Unit,Command<FormOperation,$FORM_MODEL$>>" ),
                new MethodDef( "crud",
                               "org.kie.appformer.flow.api.AppFlow<Unit,Unit>" ),
                new MethodDef( "createAndReview",
                               "org.kie.appformer.flow.api.AppFlow<Unit,Unit>" ),
                new MethodDef( "view",
                               "org.kie.appformer.flow.api.AppFlow<Unit,Unit>" )
                )
        .stream()
        .map( def -> new MethodDef( def.name, def.returnType
                                                 .replace( "$FORM_MODEL$",
                                                           context.getSharedPackage().getPackageName() + "." + context.getFormModelName() ) ) )
        .forEach( def -> addFlowProducer( def.name, def.returnType, producerClass, context ) );
    }

    private static void addFlowProducer( final String methodName,
                                         final String parameterizedReturnType,
                                         final JavaClassSource producerClass,
                                         final SourceGenerationContext context ) {
        final MethodSource<JavaClassSource> producerMethod =
                producerClass
                .addMethod()
                .setName( methodName )
                .setPublic()
                // XXX Maybe allow for injected parameters?
                .setBody( "return super." + methodName + "();" )
                .setReturnType( parameterizedReturnType );

        producerMethod.addAnnotation( Override.class );
        producerMethod.addAnnotation( Produces.class );
        producerMethod.addAnnotation( Singleton.class );
        producerMethod.addAnnotation( ForEntity.class )
                      .setStringValue( context.getSharedPackage().getPackageName() + "." + context.getEntityName() );
        producerMethod.addAnnotation( Named.class )
                      .setStringValue( methodName );
    }

    private static void implementAbstractMethods( final JavaClassSource producerClass,
                                           final SourceGenerationContext context ) {
        modelToFormModel( producerClass, context );
        formModelToModel( producerClass, context );
        newModel( producerClass, context );
        getModelType( producerClass, context );
        getFormModelType( producerClass, context );
    }

    private static void getFormModelType( final JavaClassSource producerClass,
                                          final SourceGenerationContext context ) {
        producerClass
        .addMethod()
        .setName( "getFormModelType" )
        .setPublic()
        .setBody( "return " + context.getFormModelName() + ".class;" )
        .setReturnType( "Class<" + context.getFormModelName() + ">" )
        .addAnnotation( Override.class );
    }

    private static void getModelType( final JavaClassSource producerClass,
                                      final SourceGenerationContext context ) {
        producerClass
        .addMethod()
        .setName( "getModelType" )
        .setPublic()
        .setBody( "return " + context.getEntityName() + ".class;" )
        .setReturnType( "Class<" + context.getEntityName() + ">" )
        .addAnnotation( Override.class );
    }

    private static void newModel( final JavaClassSource producerClass,
                           final SourceGenerationContext context ) {
        producerClass
        .addMethod()
        .setName( "newModel" )
        .setPublic()
        .setBody( "return new " + context.getEntityName() + "();" )
        .setReturnType( context.getEntityName() )
        .addAnnotation( Override.class );
    }

    private static void formModelToModel( final JavaClassSource producerClass,
                                   final SourceGenerationContext context ) {
        final MethodSource<JavaClassSource> formModelToModel =
                producerClass
                .addMethod()
                .setName( "formModelToModel" )
                .setPublic()
                .setBody( "return formModel.get" + context.getEntityName() + "();" )
                .setReturnType( context.getEntityName() );
        formModelToModel.addParameter( context.getFormModelName(), "formModel" );
        formModelToModel.addAnnotation( Override.class );
    }

    private static void modelToFormModel( final JavaClassSource producerClass,
                                   final SourceGenerationContext context ) {
        final MethodSource<JavaClassSource> modelToFormModel =
                producerClass
                .addMethod()
                .setName( "modelToFormModel" )
                .setPublic()
                .setBody( "return new " + context.getFormModelName() + "( model );" )
                .setReturnType( context.getFormModelName() );
        modelToFormModel.addParameter( context.getEntityName(), "model" );
        modelToFormModel.addAnnotation( Override.class );
    }

}
