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

import org.apache.commons.lang3.StringUtils;
import org.livespark.formmodeler.codegen.SourceGenerationContext;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public class BooleanBasicTypeColumnMetaGenerator extends AbstractColumnMetaGenerator {

    public static final String CHECKBOX_COLUMN_SUFFIX = COLUMN_META_SUFFIX + "_checkbox";

    @Override
    protected String getInitializerCode( String property, SourceGenerationContext context ) {
        return "CheckboxCellImpl " + property + CHECKBOX_COLUMN_SUFFIX + " = new CheckboxCellImpl( true );";
    }

    @Override
    protected String generateNewColumnSource( String property, String modelTypeName, SourceGenerationContext context ) {
        StringBuffer out = new StringBuffer();

        out.append( "new Column<" )
                .append( context.getEntityName() )
                .append( ", Boolean>(" )
                .append( property + CHECKBOX_COLUMN_SUFFIX )
                .append( " ) { " )
                .append( "@Override\n")
                .append( "public Boolean getValue( " )
                .append( modelTypeName )
                .append( " model ) {" )
                .append( "return model.is" )
                .append( StringUtils.capitalize( property ) )
                .append( "();" )
                .append( "}}" );

        return out.toString();
    }

    @Override
    public String getSupportedType() {
        return boolean.class.getName();
    }

    @Override
    public String[] getImports() {
        return new String[]{
            "com.google.gwt.user.cellview.client.Column",
            "org.uberfire.ext.widgets.table.client.CheckboxCellImpl"
        };
    }
}
