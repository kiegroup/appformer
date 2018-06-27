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

package org.uberfire.ext.experimental.service.def;

/**
 * Definition of an experimental feature
 */
public interface ExperimentalFeatureDefinition {

    /**
     * Unique id for the feature definition
     * @return a String with the unique id
     */
    String getId();

    /**
     * I18n key for that contains the name for the feature
     * @return a String containing the I18n key
     */
    String getNameKey();

    /**
     * I18n key for that contains the description for the feature
     * @return a String containing the I18n key, can be null
     */
    String getDescriptionKey();
}
