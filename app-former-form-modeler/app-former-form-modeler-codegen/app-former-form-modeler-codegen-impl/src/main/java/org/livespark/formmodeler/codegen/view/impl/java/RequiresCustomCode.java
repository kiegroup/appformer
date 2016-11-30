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

package org.livespark.formmodeler.codegen.view.impl.java;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.kie.workbench.common.forms.model.FieldDefinition;

/**
 * Created by pefernan on 7/2/15.
 */
public interface RequiresCustomCode<F extends FieldDefinition> {

    public void addCustomCode(F definition, SourceGenerationContext context, JavaClassSource parentClass);

}
