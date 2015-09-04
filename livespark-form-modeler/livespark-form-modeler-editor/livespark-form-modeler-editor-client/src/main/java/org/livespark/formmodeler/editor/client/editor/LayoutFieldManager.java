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
package org.livespark.formmodeler.editor.client.editor;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.livespark.formmodeler.editor.client.editor.fields.FieldLayoutComponent;
import org.livespark.formmodeler.editor.model.FieldDefinition;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pefernan on 8/28/15.
 */
@Dependent
public class LayoutFieldManager {
    Map<String, FieldLayoutComponent> fields = new HashMap<String, FieldLayoutComponent>( );

    @PostConstruct
    protected void init() {
        Collection<IOCBeanDef<FieldLayoutComponent>> descs = IOC.getBeanManager().lookupBeans( FieldLayoutComponent.class );

        if (descs != null) {
            for (IOCBeanDef<FieldLayoutComponent> desc : descs) {
                fields.put( desc.getInstance().getSupportedFieldDefinition(), desc.getInstance() );
            }
        }
    }

    public FieldLayoutComponent<FieldDefinition> getComponentForFieldDefinition( String formId, FieldDefinition definition ) {
        FieldLayoutComponent<FieldDefinition> component = fields.get( definition.getCode() );
        if (component == null) return null;
        return component.newInstance( formId, definition );
    }
}
