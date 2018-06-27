/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.shared.experimental.demo;

import javax.enterprise.context.Dependent;

import org.uberfire.ext.experimental.service.def.ExperimentalFeatureDefinition;

@Dependent
public class DemoExperimentalFeatureDefinition2 implements ExperimentalFeatureDefinition {

    private static final String ID = "demo_feature2";
    private static final String DESCRIPTION = "demo_feature2_description";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getNameKey() {
        return ID;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }
}
