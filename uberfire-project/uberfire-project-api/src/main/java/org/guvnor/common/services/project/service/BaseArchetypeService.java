/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.common.services.project.service;

import java.util.Optional;

import org.guvnor.structure.repositories.Repository;

public interface BaseArchetypeService {

    /**
     * Return the repository where the archetype is stored.
     *
     * @param alias archetype alias
     * @param spaceName archetype space
     * @return repository of the archetype
     */
    Repository getTemplateRepository(String alias, String spaceName);
}
