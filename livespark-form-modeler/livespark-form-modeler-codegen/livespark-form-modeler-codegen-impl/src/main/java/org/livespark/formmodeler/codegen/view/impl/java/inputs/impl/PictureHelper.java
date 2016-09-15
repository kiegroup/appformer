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

package org.livespark.formmodeler.codegen.view.impl.java.inputs.impl;


import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.kie.workbench.common.forms.model.impl.basic.image.PictureFieldDefinition;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.impl.java.RequiresCustomCode;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

public class PictureHelper extends AbstractInputCreatorHelper<PictureFieldDefinition> implements RequiresCustomCode<PictureFieldDefinition> {

    @Override
    public String getSupportedFieldTypeCode() {
        return PictureFieldDefinition.CODE;
    }

    @Override
    public String getInputWidget( PictureFieldDefinition fieldDefinition ) {
        return "org.kie.workbench.common.forms.common.rendering.client.widgets.picture.PictureInput";
    }

    @Override
    public void addCustomCode( PictureFieldDefinition field, SourceGenerationContext context, JavaClassSource viewClass ) {
        MethodSource<JavaClassSource> beforeDisplayMethod = viewClass.getMethod( BEFORE_DISPLAY_METHOD, void.class );
        StringBuffer body = new StringBuffer( beforeDisplayMethod.getBody() == null ? "" : beforeDisplayMethod.getBody() );

        body.append( getWidgetInitialization( field ) );

        beforeDisplayMethod.setBody( body.toString() );
    }

    public String getWidgetInitialization( PictureFieldDefinition field ) {
        StringBuffer body = new StringBuffer();
        body.append( field.getName() )
                .append( ".init( " )
                .append( field.getSize().getWidth() )
                .append( ", " )
                .append( field.getSize().getHeight() )
                .append( " );" );
        return body.toString();
    }
}
