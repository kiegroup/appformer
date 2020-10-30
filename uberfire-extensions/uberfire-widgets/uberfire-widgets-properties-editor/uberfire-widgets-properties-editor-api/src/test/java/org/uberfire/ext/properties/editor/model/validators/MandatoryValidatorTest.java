package org.uberfire.ext.properties.editor.model.validators;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MandatoryValidatorTest {
    
    private MandatoryValidator validator;

    @Before
    public void setup() {
        validator = new MandatoryValidator();
    }
    
    @Test
    public void testValid() {
        assertTrue(validator.validate("valid"));
    }
    
    @Test
    public void testNotValid() {
        assertFalse(validator.validate(""));
        assertFalse(validator.validate("          "));
        assertFalse(validator.validate(null));
    }

}