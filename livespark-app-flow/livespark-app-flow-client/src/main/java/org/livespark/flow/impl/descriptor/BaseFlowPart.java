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
import org.livespark.flow.api.descriptor.type.Type;

public abstract class BaseFlowPart implements FlowPartDescriptor {

    private final Type inputType;
    private final Type outputType;

    public BaseFlowPart( final Type inputType, final Type outputType ) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

    @Override
    public Type getInputType() {
        return inputType;
    }

    @Override
    public Type getOutputType() {
        return outputType;
    }

    @Override
    public int hashCode() {
        return inputType.hashCode() ^ outputType.hashCode();
    }

    @Override
    public boolean equals( final Object obj ) {
        return obj instanceof FlowPartDescriptor && Objects.equals( ((FlowPartDescriptor) obj).getInputType(),
                                                                    getInputType() )
               && Objects.equals( ((FlowPartDescriptor) obj).getOutputType(),
                                  getOutputType() );
    }

}