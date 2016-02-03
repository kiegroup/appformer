/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.formmodeler.codegen.view.impl.java.tableColumns.impl;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.livespark.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGenerator;
import org.livespark.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGeneratorManager;

@ApplicationScoped
public class ColumnMetaGeneratorManagerImpl implements ColumnMetaGeneratorManager {

    @Inject
    private Instance<ColumnMetaGenerator> columnMetaGeneratorInstances;

    private Map<String, ColumnMetaGenerator> columnMetaGenerators = new HashMap<>();

    private ColumnMetaGenerator defaultColumnGenerator;

    @PostConstruct
    protected void init() {
        for ( ColumnMetaGenerator generator : columnMetaGeneratorInstances ) {
            if ( generator.isDefault() ) {
                defaultColumnGenerator = generator;
            } else {
                columnMetaGenerators.put( generator.getSupportedType(), generator );
            }
        }
    }

    @Override
    public ColumnMetaGenerator getColumnMetaGeneratorForType( String typeName ) {
        ColumnMetaGenerator generator = columnMetaGenerators.get( typeName );
        if ( generator != null ) {
            return generator;
        }
        return defaultColumnGenerator;
    }
}
