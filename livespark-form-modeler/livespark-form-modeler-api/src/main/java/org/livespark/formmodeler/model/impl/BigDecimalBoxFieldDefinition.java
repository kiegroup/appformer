package org.livespark.formmodeler.model.impl;

import java.math.BigDecimal;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
public class BigDecimalBoxFieldDefinition extends AbstractIntputFieldDefinition {

    @Override
    public String getStandaloneClassName() {
        return BigDecimal.class.getName();
    }
}
