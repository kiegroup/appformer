package org.livespark.formmodeler.rendering.client.shared.converters;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

/**
 * Created by pefernan on 6/12/15.
 */

@DefaultConverter
public class ShortConverter implements Converter<Short, String> {

    @Override
    public Short toModelValue( String s ) {
        if (s == null) return null;
        return Short.parseShort( s );
    }

    @Override
    public String toWidgetValue( Short aShort ) {
        if (aShort == null) return null;
        return aShort.toString();
    }
}
