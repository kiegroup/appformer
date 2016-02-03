package org.livespark.formmodeler.codegen.layout.impl;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.codegen.layout.FormLayoutTemplateGenerator;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.FormLayoutComponent;
import org.livespark.formmodeler.model.impl.basic.CheckBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.DateBoxFieldDefinition;
import org.livespark.formmodeler.model.impl.basic.TextBoxFieldDefinition;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.layout.editor.api.editor.LayoutColumn;
import org.uberfire.ext.layout.editor.api.editor.LayoutComponent;
import org.uberfire.ext.layout.editor.api.editor.LayoutRow;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class StaticFormLayoutTemplateGeneratorTest extends FormLayoutTemplateGeneratorTest {

    @Override
    protected FormLayoutTemplateGenerator getTemplateGenerator() {
        return new StaticFormLayoutTemplateGenerator();
    }
}
