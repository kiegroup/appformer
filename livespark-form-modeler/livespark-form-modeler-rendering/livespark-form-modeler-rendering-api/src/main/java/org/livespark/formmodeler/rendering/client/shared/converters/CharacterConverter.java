package org.livespark.formmodeler.rendering.client.shared.converters;

import java.math.BigDecimal;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

/**
 * Created by pefernan on 6/12/15.
 */

@DefaultConverter
public class CharacterConverter implements Converter<Character, String> {

    @Override
    public Character toModelValue( String s ) {
        if (s == null) return null;
        return new Character( s.charAt( 0 ) );
    }

    @Override
    public String toWidgetValue( Character character ) {
        if (character == null) return null;
        return character.toString();
    }
}
