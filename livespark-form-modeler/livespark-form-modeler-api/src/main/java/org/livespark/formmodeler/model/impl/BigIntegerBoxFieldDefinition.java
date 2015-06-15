package org.livespark.formmodeler.model.impl;

import java.math.BigInteger;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by pefernan on 3/19/15.
 */
@Portable
public class BigIntegerBoxFieldDefinition extends AbstractIntputFieldDefinition {

    @Override
    public String getStandaloneClassName() {
        return BigInteger.class.getName();
    }
}
