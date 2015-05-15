package org.livespark.formmodeler.codegen.view.impl.html;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.formmodeler.codegen.view.FormHTMLTemplateSourceGenerator;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.SimpleTemplateRegistry;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRegistry;
import org.mvel2.templates.TemplateRuntime;

/**
 * Created by pefernan on 4/29/15.
 */
@ApplicationScoped
public class MVELFormHTMLTemplateSourceGenerator implements FormHTMLTemplateSourceGenerator {
    TemplateRegistry registry = new SimpleTemplateRegistry();
    
    @Inject
    protected Instance<InputTemplateProvider> providers;

    private String formTemplatePath = "templates/form.mv";
    private CompiledTemplate formTemplate;

    @PostConstruct
    protected void init() {
        formTemplate = TemplateCompiler.compileTemplate( getClass().getResourceAsStream( formTemplatePath ) );
        
        for ( InputTemplateProvider provider : providers ) {
            registry.addNamedTemplate( provider.getSupportedFieldType(), TemplateCompiler.compileTemplate( provider.getTemplateInputStream() ) );
        }
        
    }

    @Override
    public String generateHTMLTemplateSource( SourceGenerationContext context ) {
        Map params = new HashMap(  );
        params.put( "formDefinition", context.getFormDefinition() );
        return ( String ) TemplateRuntime.execute(formTemplate, null, params, registry);
    }
}
