/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.renderer.backend.service.impl.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.workbench.models.datamodel.oracle.Annotation;
import org.livespark.formmodeler.metaModel.Option;
import org.livespark.formmodeler.metaModel.SelectorDataProvider;
import org.livespark.formmodeler.model.impl.basic.selectors.SelectorFieldBase;
import org.livespark.formmodeler.model.impl.basic.selectors.SelectorOption;
import org.livespark.formmodeler.model.impl.basic.selectors.StringSelectorOption;
import org.livespark.formmodeler.renderer.service.SelectorDataProviderManager;
import org.livespark.formmodeler.renderer.service.TransformerContext;
import org.livespark.formmodeler.service.impl.fieldProviders.SelectorFieldProvider;

public abstract class AbstractSelectorAnnotationProcessor<T extends SelectorFieldBase, P extends SelectorFieldProvider<T>>
        extends AbstractFieldAnnotationProcessor<T, P> {

    public AbstractSelectorAnnotationProcessor( P fieldProvider ) {
        super( fieldProvider );
    }

    @Override
    protected void initField( T field, Annotation annotation, TransformerContext context ) {
        Map<String, Object> params = annotation.getParameters();

        SelectorDataProvider provider = (SelectorDataProvider) params.get( "provider" );

        if ( !provider.className().isEmpty() ) {
            String providerId = provider.type().getCode()
                    + SelectorDataProviderManager.SEPARATOR
                    + provider.className();

            field.setDataProvider( providerId );
        } else {

            List<SelectorOption> options = new ArrayList<SelectorOption>();
            field.setOptions( options );

            Option[] configOptions = (Option[]) params.get( "options" );

            if ( configOptions != null ) {
                for ( Option option : configOptions ) {
                    StringSelectorOption selectorOption = new StringSelectorOption();
                    selectorOption.setValue( option.value() );
                    selectorOption.setText( option.text() );
                    selectorOption.setDefaultValue( option.isDefault() );
                    options.add( selectorOption );
                }

            }
        }
    }
}
