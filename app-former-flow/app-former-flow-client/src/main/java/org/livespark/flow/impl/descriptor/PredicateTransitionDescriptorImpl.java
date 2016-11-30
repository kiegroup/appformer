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


package org.livespark.flow.impl.descriptor;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.function.PredicateDescriptor;
import org.livespark.flow.api.descriptor.transition.PredicateTransitionDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class PredicateTransitionDescriptorImpl extends BaseTransitionDescriptor implements PredicateTransitionDescriptor {

    private final PredicateDescriptor predicate;
    private final AppFlowDescriptor ifTrue;
    private final AppFlowDescriptor ifFalse;

    public PredicateTransitionDescriptorImpl( final @MapsTo( "predicate" ) PredicateDescriptor predicate,
                                              final @MapsTo( "ifTrue" ) AppFlowDescriptor ifTrue,
                                              final @MapsTo( "ifFalse" ) AppFlowDescriptor ifFalse ) {
        super( predicate.getInputType() );
        this.predicate = predicate;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public Type getOutputType() {
        return getMappingFor( true ).getOutputType();
    }

    @Override
    public PredicateDescriptor getPredicate() {
        return predicate;
    }

    @Override
    public AppFlowDescriptor getMappingFor( final boolean predicateResult ) {
        return predicateResult ? ifTrue : ifFalse;
    }

}
