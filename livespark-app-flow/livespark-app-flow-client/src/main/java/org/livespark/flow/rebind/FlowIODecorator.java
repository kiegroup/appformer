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


package org.livespark.flow.rebind;

import static org.jboss.errai.codegen.util.Stmt.castTo;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.InjectionPoint;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.inject.Qualifier;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;
import org.livespark.flow.cdi.api.FlowInput;
import org.livespark.flow.cdi.api.FlowOutput;
import org.livespark.flow.cdi.api.FlowComponent;
import org.livespark.flow.client.local.CDIFlowComponentFactory.BaseFlowIO;

/**
 * <p>
 * Decorates types with {@link FlowComponent} so that injected instances of {@link FlowInput} and
 * {@link FlowOutput} are properly configured up.
 *
 * <p>
 * This is a workaround. Ideally we would want injected {@link FlowInput} and {@link FlowOutput}
 * instances to configure themselves but there is no mechanism in Errai IOC to get access to
 * injection site info.
 */
@CodeDecorator
public class FlowIODecorator extends IOCDecoratorExtension<FlowComponent> {

    public FlowIODecorator( final Class<FlowComponent> decoratesWith ) {
        super( decoratesWith );
    }

    @Override
    public void generateDecorator( final Decorable decorable, final FactoryController controller ) {
        final Statement instanceStmt = decorable.getAccessStatement();
        final MetaClass flowInputClass = MetaClassFactory.get( FlowInput.class );
        final MetaClass flowOutputClass = MetaClassFactory.get( FlowOutput.class );
        final Collection<Class<? extends Annotation>> injAnnotations = decorable.getInjectionContext().getAnnotationsForElementType( InjectionPoint );

        final List<Statement> flowIOStmts =
                injAnnotations
                     .stream()
                    .flatMap( anno -> decorable.getType().getFieldsAnnotatedWith( anno ).stream() )
                    .filter( field -> field.getType().isAssignableTo( flowInputClass ) || field.getType().isAssignableTo( flowOutputClass ) )
                    .filter( field -> isUnqualified( field ) )
                    .map( field -> {
                        final ContextualStatementBuilder fieldAccessStmt = controller.exposedFieldStmt( field );
                        return castTo( BaseFlowIO.class, castTo( Object.class, fieldAccessStmt ) ).invoke( "setKey", instanceStmt );
                    } )
                    .collect( Collectors.toList() );

        controller.addInitializationStatements( flowIOStmts );
    }

    private boolean isUnqualified( final MetaField field ) {
        return !Arrays
                .stream( field.getAnnotations() )
                .map( anno -> anno.annotationType() )
                .filter( type -> !type.equals( Default.class ) && !type.equals( Any.class ) && type.isAnnotationPresent( Qualifier.class ) )
                .findAny()
                .isPresent();
    }
}
