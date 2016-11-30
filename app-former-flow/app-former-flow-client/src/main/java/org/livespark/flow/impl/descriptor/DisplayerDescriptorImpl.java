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
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.type.Type;

@Portable
public class DisplayerDescriptorImpl implements DisplayerDescriptor {

    private final String identifier;
    private final Type componentType;
    private final int instanceId;

    public DisplayerDescriptorImpl( final @MapsTo( "identifier" ) String identifier,
                                    final @MapsTo( "instanceId" ) int instanceId,
                                    final @MapsTo( "componentType" ) Type componentType ) {
        this.identifier = identifier;
        this.componentType = componentType;
        this.instanceId = instanceId;
    }

    @Override
    public String getDescriptorIdentifier() {
        return identifier;
    }

    @Override
    public int getInstanceId() {
        return instanceId;
    }

    @Override
    public Type getComponentType() {
        return componentType;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode() ^ componentType.hashCode();
    }

    @Override
    public boolean equals( final Object obj ) {
        return obj instanceof DisplayerDescriptor
               && ((DisplayerDescriptor) obj).getDescriptorIdentifier().equals( getDescriptorIdentifier() )
               && ((DisplayerDescriptor) obj).getComponentType().equals( getComponentType() );
    }

}
