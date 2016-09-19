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


package org.livespark.flow.api.descriptor.common;

import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.DescriptorFactory;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;

/**
 * <p>
 * A part of an {@link AppFlowDescriptor} that has an identifier that can be used to look it up in
 * a {@link DescriptorRegistry}.
 */
public interface HasIdentifier {

    /**
     * @return A unique identifier for the given flow descriptor part. Never null.
     */
    String getDescriptorIdentifier();

    /**
     * @return A unique identifier for this instance (unique per {@link DescriptorFactory}).
     */
    int getInstanceId();

}
