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

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.transition.OptionalTransitionDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class OptionalTransitionDescriptorImpl extends BaseTransitionDescriptor implements OptionalTransitionDescriptor {

    private final AppFlowDescriptor ifPresent;
    private final AppFlowDescriptor ifAbsent;

    public OptionalTransitionDescriptorImpl( final @MapsTo("ifPresent") AppFlowDescriptor ifPresent,
                                             final @MapsTo("ifAbsent") AppFlowDescriptor ifAbsent ) {
        super( optionalOf( ifPresent.getInputType() ) );
        this.ifPresent = ifPresent;
        this.ifAbsent = ifAbsent;
    }

    @Override
    public Type getOutputType() {
        return getMappingFor( true ).getOutputType();
    }

    @Override
    public AppFlowDescriptor getMappingFor( final boolean present ) {
        return present ? ifPresent : ifAbsent;
    }

    private static Type optionalOf( final Type type ) {
        return new ParameterizedTypeImpl( new SimpleTypeImpl( Optional.class.getName(),
                                                              Optional.class.getSimpleName() ),
                                          Arrays.asList( new TypeVariableImpl( "T",
                                                                                         Collections.emptyList(),
                                                                                         Collections.emptyList() ) ),
                                          Arrays.asList( type ) );
    }

}
