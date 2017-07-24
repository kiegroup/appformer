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

package org.kie.appformer.formmodeler.codegen.view.impl.java.inputs;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.util.SourceGenerationValueConvertersFactory;
import org.kie.workbench.common.forms.model.FieldDefinition;

/**
 * Specifies that the form widget for a {@link FieldDefinition} might require a {@link Converter} on the generated
 * {@link Bound} annotation
 * @param <F> Any class extending FieldDefinition
 */
public interface RequiresValueConverter<F extends FieldDefinition> {

    /**
     * Retrieves the right {@link Converter} for the given {@link FieldDefinition}
     * @param field a {@link FieldDefinition}
     * @return a String containing the ClassName of the {@link Converter} to use on the {@link Bound} annotation or
     * null if it is not required.
     */
    default String getConverterClassName(F field) {
        return SourceGenerationValueConvertersFactory.getConverterClassName(field);
    }
}
