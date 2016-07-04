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


package org.livespark.flow.cdi.api;

import org.livespark.flow.api.Step;

/**
 * An injectable type allowing {@link FlowComponent FlowComponents} to access input values of the
 * steps in which they are executing.
 */
public interface FlowInput<M> {

    /**
     * @return The input value of the {@link Step} in which this component is running.
     */
    M get();
}
