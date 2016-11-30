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

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.tableColumns.ColumnMetaGenerator;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public abstract class AbstractColumnMetaGenerator implements ColumnMetaGenerator {

    @Override
    public String generateColumnMeta( String property, String label, String modelTypeName, SourceGenerationContext context ) {
        StringBuffer out = new StringBuffer();

        out.append( getInitializerCode( property, context ) );

        out.append( "ColumnMeta< " )
                .append( modelTypeName )
                .append( ">" )
                .append( property + COLUMN_META_SUFFIX )
                .append( " = new ColumnMeta<" )
                .append( modelTypeName )
                .append( ">(" )
                .append( generateNewColumnSource( property, modelTypeName, context ) )
                .append( ", \"" )
                .append( label )
                .append( "\" );" );

        out.append( COLUMN_METAS_VAR_NAME )
                .append( ".add( " )
                .append( property + COLUMN_META_SUFFIX )
                .append( " );" );

        return out.toString();
    }

    protected abstract String generateNewColumnSource( String property, String modelTypeName, SourceGenerationContext context );

    protected abstract String getInitializerCode( String property, SourceGenerationContext context );

    @Override
    public boolean isDefault() {
        return false;
    }
}
