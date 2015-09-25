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
package org.livespark.formmodeler.editor.client.editor.service;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.service.AbstractFieldManager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import java.util.Collection;

/**
 * Created by pefernan on 9/25/15.
 */
@Dependent
public class ClientFieldManagerImpl extends AbstractFieldManager {

    @PostConstruct
    protected void init() {
        Collection<IOCBeanDef<FieldDefinition>> fields = IOC.getBeanManager().lookupBeans(FieldDefinition.class);
        for (IOCBeanDef<FieldDefinition> field : fields) {
            registerFieldDefinition( field.getInstance() );
        }
    }

    @Override
    protected FieldDefinition createNewInstance(FieldDefinition definition) throws Exception {
        return  IOC.getBeanManager().lookupBean(definition.getClass()).newInstance();
    }
}
