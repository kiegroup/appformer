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

package org.livespark.formmodeler.rendering.server.rest.query.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.kie.workbench.common.forms.commons.rendering.server.util.masks.BackendMaskInterpreter;
import org.kie.workbench.common.forms.commons.rendering.shared.util.masks.MaskInterpreter;
import org.kie.workbench.common.forms.commons.rendering.shared.util.masks.MaskSection;
import org.kie.workbench.common.forms.commons.rendering.shared.util.masks.MaskSectionType;
import org.livespark.formmodeler.rendering.client.shared.query.MaskQueryCriteria;
import org.livespark.formmodeler.rendering.server.rest.query.QueryCriteriaGenerator;

@ApplicationScoped
public class MaskQueryCriteriaGenerator implements QueryCriteriaGenerator<MaskQueryCriteria> {

    @Override
    public Class getSupportedType() {
        return MaskQueryCriteria.class;
    }

    @Override
    public Expression buildCriteriaExpression( MaskQueryCriteria criteria, CriteriaBuilder builder, Root rootEntity ) {
        MaskInterpreter<?> interpreter = new BackendMaskInterpreter<>( (String)criteria.getMask() );

        String text =  criteria.getValue();
        if ( (text != null && !text.isEmpty()) && !interpreter.getSections().isEmpty() ) {
            String token = null;
            Expression concatExpression = null;
            for ( MaskSection section : interpreter.getSections() ) {

                if ( MaskSectionType.LITERAL.equals( section.getType() ) ) {
                    token = section.getText();
                } else if ( MaskSectionType.PROPERTY.equals( section.getType() ) ) {
                    Expression<String> expression = rootEntity.get( section.getText() ).as( String.class );
                    if ( token != null ) {
                        expression = builder.concat( token, expression );
                        token = null;
                    }

                    if ( concatExpression == null ) {
                        concatExpression = expression;
                    } else {
                        concatExpression = builder.concat( concatExpression, expression );
                    }
                }
            }

            concatExpression = builder.like( builder.lower( concatExpression ), "%" + text.toLowerCase() + "%" );

            return concatExpression;
        }
        return null;
    }
}
