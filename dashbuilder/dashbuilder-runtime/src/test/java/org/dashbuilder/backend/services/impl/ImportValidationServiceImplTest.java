package org.dashbuilder.backend.services.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImportValidationServiceImplTest {

    ImportValidationServiceImpl importValidationService;

    @Before
    public void init() {
        importValidationService = new ImportValidationServiceImpl();
    }

    @Test
    public void validFileTest() {
        String file = this.getClass().getResource("/valid_import.zip").getFile();
        assertTrue(importValidationService.validate(file));
    }
    
    @Test
    public void invalidFileTest() {
        String file = this.getClass().getResource("/not_valid.zip").getFile();
        assertFalse(importValidationService.validate(file));
    }

}