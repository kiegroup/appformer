package org.livespark.formmodeler.rendering.client.shared.converters;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

/**
 * Created by pefernan on 6/12/15.
 */

@DefaultConverter
public class FloatConverter implements Converter<Float, String> {

    @Override
    public Float toModelValue( String s ) {
        if (s == null) return null;
        return Float.parseFloat( s );
    }

    @Override
    public String toWidgetValue( Float aFloat ) {
        if (aFloat == null) return null;
        return aFloat.toString();
    }
}
