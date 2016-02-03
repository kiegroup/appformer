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

package org.livespark.formmodeler.codegen.view.impl.html;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.livespark.formmodeler.codegen.view.ListView;
import org.livespark.formmodeler.codegen.view.impl.html.util.HTMLTemplateFormatter;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

@ListView
@ApplicationScoped
public class ListHTMLTemplateSourceGenerator implements FormHTMLTemplateSourceGenerator {

    private String listViewTemplatePath = "templates/listview.mv";
    private CompiledTemplate listViewTemplate;

    @PostConstruct
    protected void init() {
        listViewTemplate = TemplateCompiler.compileTemplate( getClass().getResourceAsStream( listViewTemplatePath ) );
    }

    @Inject
    private HTMLTemplateFormatter formatter;

    @Override
    public String generateHTMLTemplateSource( SourceGenerationContext context ) {
        return formatter.formatHTMLCode( (String) TemplateRuntime.execute( listViewTemplate ) );
    }
}
