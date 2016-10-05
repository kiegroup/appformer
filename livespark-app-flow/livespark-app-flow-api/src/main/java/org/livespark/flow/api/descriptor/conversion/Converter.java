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


package org.livespark.flow.api.descriptor.conversion;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.AppFlowReferenceDescriptor;
import org.livespark.flow.api.descriptor.StepReferenceDescriptor;
import org.livespark.flow.api.descriptor.common.HasIdentifier;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;

/**
 * Converts {@link AppFlowDescriptor AppFlowDescriptors} to executable {@link AppFlow AppFlows}.
 */
public interface Converter {

    /**
     * Convert an {@link AppFlowDescriptor} to an exectuable {@link AppFlow}.
     *
     * @param registry
     *            Used for looking up parts of the given {@link AppFlowDescriptor} that implement
     *            {@link HasIdentifier} (i.e. {@link StepReferenceDescriptor},
     *            {@link TransformationDescriptor}, {@link AppFlowReferenceDescriptor}). Must not be
     *            null.
     * @param descriptor
     *            The descriptor to be converted. Must not be null.
     * @return An executable {@link AppFlow}. Never null.
     * @throws IllegalArgumentException
     *             Thrown if any part of the given {@link AppFlowDescriptor} cannot be found in the
     *             given {@link DescriptorRegistry}.
     */
    AppFlow<?, ?> convert( DescriptorRegistry registry, AppFlowDescriptor descriptor );

}
