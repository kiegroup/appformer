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

package org.livespark.formmodeler.codegen.view.impl.html.util;

import java.io.StringWriter;

import javax.enterprise.context.Dependent;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.HTMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class HTMLTemplateFormatter {
    private static transient Logger log = LoggerFactory.getLogger( HTMLTemplateFormatter.class );

    public String formatHTMLCode( String htmlTemplate ) {
        try {
            Document document = DocumentHelper.parseText( htmlTemplate );
            StringWriter sw = new StringWriter();
            HTMLWriter writer = new HTMLWriter( sw );
            writer.write( document );
            return sw.toString();
        } catch ( Exception e ) {
            log.warn( "Error formatting html template: ", e );
        }
        return null;
    }
}
