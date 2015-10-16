/*
 * Copyright 2015 JBoss Inc
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

package org.livespark.formmodeler.editor.backend.service.impl;

import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.service.AbstractFieldManager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by pefernan on 4/29/15.
 */
@ApplicationScoped
public class BackendFieldManagerImpl extends AbstractFieldManager {

    @Inject
    private Instance<FieldDefinition> definitions;

    @PostConstruct
    protected void init() {
        for (FieldDefinition definition : definitions ) {
            registerFieldDefinition( definition );
        }
    }

    @Override
    protected FieldDefinition createNewInstance(FieldDefinition definition) throws Exception {
        if ( definition == null ) return null;
        return definition.getClass().newInstance();
    }
}
