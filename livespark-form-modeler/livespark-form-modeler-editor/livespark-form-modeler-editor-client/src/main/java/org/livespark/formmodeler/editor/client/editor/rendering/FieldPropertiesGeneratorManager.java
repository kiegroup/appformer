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
package org.livespark.formmodeler.editor.client.editor.rendering;

import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.livespark.formmodeler.editor.client.editor.rendering.renderers.FieldPropertiesGenerator;
import org.livespark.formmodeler.model.FieldDefinition;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pefernan on 9/22/15.
 */
@ApplicationScoped
public class FieldPropertiesGeneratorManager {

    @Inject
    private SyncBeanManager iocBeanManager;

    private Map<String, FieldPropertiesGenerator> availableGenerators = new HashMap<String, FieldPropertiesGenerator>();

    @PostConstruct
    protected void init() {
        Collection<SyncBeanDef<FieldPropertiesGenerator>> generators = iocBeanManager.lookupBeans(FieldPropertiesGenerator.class);
        for (SyncBeanDef<FieldPropertiesGenerator> generatorDef : generators) {
            FieldPropertiesGenerator generator = generatorDef.getInstance();
            if ( generator != null ) {
                availableGenerators.put( generator.getSupportedFieldDefinitionCode(), generator );
            }
        }
    }

    public FieldPropertiesGenerator getRendererForField( FieldDefinition fieldDefinition ) {
        FieldPropertiesGenerator def = availableGenerators.get( fieldDefinition.getCode() );

        if ( def == null ) return null;

        FieldPropertiesGenerator generator = iocBeanManager.lookupBean( def.getClass() ).getInstance();

        generator.setField( fieldDefinition );

        return generator;
    }
}
