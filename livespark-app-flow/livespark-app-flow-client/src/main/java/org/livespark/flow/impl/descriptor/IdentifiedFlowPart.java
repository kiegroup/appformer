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

import org.livespark.flow.api.descriptor.common.FlowPartDescriptor;
import org.livespark.flow.api.descriptor.common.HasIdentifier;
import org.livespark.flow.api.descriptor.type.Type;

public abstract class IdentifiedFlowPart extends BaseFlowPart implements FlowPartDescriptor, HasIdentifier {

    private final String id;
    private final int instanceId;

    public IdentifiedFlowPart( final String id, final int instanceId, final Type inputType, final Type outputType ) {
        super( inputType, outputType );
        this.id = id;
        this.instanceId = instanceId;
    }

    @Override
    public String getDescriptorIdentifier() {
        return id;
    }

    @Override
    public int getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + getDescriptorIdentifier() + ", input=" + getInputType() + ", output=" + getOutputType() + "}";
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ id.hashCode();
    }

    @Override
    public boolean equals( final Object obj ) {
        return super.equals( obj ) && obj instanceof HasIdentifier
               && Objects.equals( ((HasIdentifier) obj).getDescriptorIdentifier(),
                                  getDescriptorIdentifier() );
    }

}
