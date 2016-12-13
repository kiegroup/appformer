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

package org.kie.appformer.formmodeler.codegen.view.impl.html;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.kie.appformer.formmodeler.codegen.SourceGenerationContext;
import org.kie.appformer.formmodeler.codegen.view.FormView;
import org.kie.appformer.formmodeler.codegen.view.HTMLTemplateGenerator;
import org.kie.appformer.formmodeler.codegen.view.impl.html.util.HTMLTemplateFormatter;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.SimpleTemplateRegistry;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRegistry;
import org.mvel2.templates.TemplateRuntime;

@FormView
@ApplicationScoped
public class MVELFormHTMLTemplateSourceGenerator implements HTMLTemplateGenerator {
    TemplateRegistry registry = new SimpleTemplateRegistry();

    @Inject
    protected Instance<InputTemplateProvider> providers;

    @Inject
    private HTMLTemplateFormatter formatter;

    private final String formTemplatePath = "templates/form.mv";
    private CompiledTemplate formTemplate;

    @PostConstruct
    protected void init() {
        formTemplate = TemplateCompiler.compileTemplate( getClass().getResourceAsStream( formTemplatePath ) );

        for ( final InputTemplateProvider provider : providers ) {
            provider.registerTemplates( registry );
        }

    }

    @Override
    public String generateHTMLTemplate( final SourceGenerationContext context ) {
        final Map<String, FormDefinition> params = new HashMap<>(  );
        params.put( "formDefinition", context.getFormDefinition() );
        return formatter.formatHTMLCode( ( String ) TemplateRuntime.execute( formTemplate, null, params, registry ) );
    }
}
