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

import java.util.Collections;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.transition.CommandTransitionDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class CommandTransitionDescriptorImpl extends BaseTransitionDescriptor implements CommandTransitionDescriptor {

    private final Map<? extends Enum<?>, AppFlowDescriptor> mapping;

    public CommandTransitionDescriptorImpl( final @MapsTo("inputType") Type inputType, final @MapsTo("mapping") Map<? extends Enum<?>, AppFlowDescriptor> mapping ) {
        super( inputType );
        this.mapping = mapping;
        if ( mapping.isEmpty() ) {
            throw new IllegalArgumentException( "Cannot provide empty mapping." );
        }
        // TODO validate that all mapping values have same output type
    }

    @Override
    public Type getOutputType() {
        return mapping
                .values()
                .stream()
                .findFirst()
                .map( desc -> desc.getOutputType() )
                .orElseThrow( () -> new IllegalStateException() );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <E extends Enum<E>> Map<E, AppFlowDescriptor> getMapping() {
        return (Map<E, AppFlowDescriptor>) Collections.unmodifiableMap( mapping );
    }

}
