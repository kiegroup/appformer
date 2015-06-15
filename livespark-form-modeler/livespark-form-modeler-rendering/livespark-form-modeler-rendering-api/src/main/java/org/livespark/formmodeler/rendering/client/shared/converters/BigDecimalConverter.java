package org.livespark.formmodeler.rendering.client.shared.converters;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

/**
 * Created by pefernan on 6/12/15.
 */

@DefaultConverter
public class BigDecimalConverter implements Converter<BigDecimal, String> {

    @Override
    public BigDecimal toModelValue( String s ) {
        if (s == null) return null;
        return new BigDecimal( s );
    }

    @Override
    public String toWidgetValue( BigDecimal decimal ) {
        if (decimal == null) return null;
        return decimal.toString();
    }
}
