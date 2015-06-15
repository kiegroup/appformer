package org.livespark.formmodeler.rendering.client.shared.converters;

import java.math.BigInteger;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

/**
 * Created by pefernan on 6/12/15.
 */

@DefaultConverter
public class BigIntegerConverter implements Converter<BigInteger, String> {

    @Override
    public BigInteger toModelValue( String s ) {
        if (s == null) return null;
        return new BigInteger( s );
    }

    @Override
    public String toWidgetValue( BigInteger integer ) {
        if (integer == null) return null;
        return integer.toString();
    }
}
