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

import java.util.Objects;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class UIComponentDescriptorImpl extends IdentifiedFlowPart implements UIComponentDescriptor {

    private final Type componentType;

    public UIComponentDescriptorImpl( final @MapsTo( "id" ) String id,
                                      final @MapsTo( "instanceId" ) int instanceId,
                                      final @MapsTo( "inputType" ) Type inputType,
                                      final @MapsTo( "outputType" ) Type outputType,
                                      final @MapsTo("componentType") Type componentType ) {
        super( id, instanceId, inputType, outputType );
        this.componentType = componentType;
    }

    @Override
    public Type getComponentType() {
        return componentType;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ componentType.hashCode();
    }

    @Override
    public boolean equals( final Object obj ) {
        return super.equals( obj ) && obj instanceof UIComponentDescriptor && Objects.equals( getComponentType(),
                                                                                              ((UIComponentDescriptor) obj).getComponentType() );
    }

}
