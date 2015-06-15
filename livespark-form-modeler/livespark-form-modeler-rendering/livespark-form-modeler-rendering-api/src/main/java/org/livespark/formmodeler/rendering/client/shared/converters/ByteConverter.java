package org.livespark.formmodeler.rendering.client.shared.converters;

import java.math.BigDecimal;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

/**
 * Created by pefernan on 6/12/15.
 */

@DefaultConverter
public class ByteConverter implements Converter<Byte, String> {

    @Override
    public Byte toModelValue( String s ) {
        if (s == null) return null;
        return Byte.parseByte( s );
    }

    @Override
    public String toWidgetValue( Byte aByte ) {
        if (aByte == null) return null;
        return aByte.toString();
    }
}
