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

public class DefaultColumnMetaGenerator extends AbstractColumnMetaGenerator {

    @Override
    protected String generateNewColumnSource( String property, String modelTypeName, SourceGenerationContext context ) {
        StringBuffer result = new StringBuffer();

        result.append( "new TextColumn<" )
                .append( modelTypeName )
                .append( ">() { " )
                .append( "@Override\n")
                .append( "public String getValue( " )
                .append( modelTypeName )
                .append( " model ) {" )
                .append( "Object value = model.get" )
                .append( StringUtils.capitalize( property ) )
                .append( "();" )
                .append( "if ( value == null ) { return \"\"; }"  )
                .append( "return String.valueOf( value );" )
                .append( "}}" );

        return result.toString();
    }

    @Override
    protected String getInitializerCode( String property, SourceGenerationContext context ) {
        return "";
    }

    @Override
    public String getSupportedType() {
        return null;
    }

    @Override
    public String[] getImports() {
        return new String[]{
            "com.google.gwt.user.cellview.client.TextColumn",
            Override.class.getName()
        };
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
