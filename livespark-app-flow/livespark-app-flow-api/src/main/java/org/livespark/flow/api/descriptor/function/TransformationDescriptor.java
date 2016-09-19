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


package org.livespark.flow.api.descriptor.function;

import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.DescriptorFactory;
import org.livespark.flow.api.descriptor.common.FlowPartDescriptor;
import org.livespark.flow.api.descriptor.common.HasIdentifier;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;

/**
 * <p>
 * A unique identifier for a {@link AppFlow#andThen(java.util.function.Function) tranformation}.
 *
 * @see DescriptorFactory
 * @see DescriptorRegistry
 * @see AppFlowDescriptor
 * @see AppFlow
 */
public interface TransformationDescriptor extends HasIdentifier, FlowPartDescriptor {

}
