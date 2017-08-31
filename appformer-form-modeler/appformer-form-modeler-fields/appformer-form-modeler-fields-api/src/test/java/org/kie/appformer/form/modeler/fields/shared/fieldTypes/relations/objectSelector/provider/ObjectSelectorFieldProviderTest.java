/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.form.modeler.fields.shared.fieldTypes.relations.objectSelector.provider;

import com.google.gwtmockito.GwtMockitoTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.appformer.form.modeler.fields.shared.fieldTypes.relations.objectSelector.definition.ObjectSelectorFieldDefinition;
import org.kie.appformer.form.modeler.fields.shared.fieldTypes.relations.objectSelector.type.ObjectSelectorFieldType;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.EntityRelationField;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.TypeInfo;
import org.kie.workbench.common.forms.model.TypeKind;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class ObjectSelectorFieldProviderTest extends TestCase {

    private ObjectSelectorFieldProvider provider;

    @Mock
    private TypeInfo typeInfo;

    @Before
    public void initTest() {
        provider = new ObjectSelectorFieldProvider();
    }

    @Test
    public void testGetFieldType() {
        Class type = provider.getFieldType();
        assertSame(ObjectSelectorFieldType.class,
                   type);
    }

    @Test
    public void testGetFieldTypeName() {
        String typeName = provider.getFieldTypeName();
        assertSame(ObjectSelectorFieldDefinition.FIELD_TYPE.getTypeName(),
                   typeName);
    }

    @Test
    public void testGetDefaultField() {
        FieldDefinition defaultField = provider.getDefaultField();
        assertTrue(defaultField instanceof ObjectSelectorFieldDefinition);
    }

    @Test
    public void testGetFieldByTypeEnum() {
        testGetFieldByType(true);
    }

    @Test
    public void testGetFieldByTypeNotEnum() {
        testGetFieldByType(false);
    }

    private void testGetFieldByType(boolean isEnum) {
        when(typeInfo.getType()).thenReturn(isEnum ? TypeKind.ENUM : TypeKind.OBJECT);
        FieldDefinition field = provider.getFieldByType(typeInfo);
        assertEquals(isEnum,
                     field == null);
    }

    @Test
    public void testIsCompatible() {
        FieldDefinition field = mock(FieldDefinition.class,
                                     withSettings().extraInterfaces(EntityRelationField.class));
        boolean isCompatible = provider.isCompatible(field);
        assertTrue(isCompatible);
    }

    @Test
    public void testIsCompatibleNot() {
        FieldDefinition field = mock(FieldDefinition.class);
        boolean isCompatible = provider.isCompatible(field);
        assertFalse(isCompatible);
    }
}
