package org.livespark.formmodeler.codegen.layout.impl;

import org.junit.runner.RunWith;
import org.livespark.formmodeler.codegen.layout.FormLayoutTemplateGenerator;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StaticFormLayoutTemplateGeneratorTest extends FormLayoutTemplateGeneratorTest {

    @Override
    protected FormLayoutTemplateGenerator getTemplateGenerator() {
        return new StaticFormLayoutTemplateGenerator();
    }
}
