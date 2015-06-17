package org.livespark.formmodeler.rendering.client.shared.converters;

import java.util.Date;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

import com.google.gwt.i18n.client.DateTimeFormat;


@DefaultConverter
public class DateConverter implements Converter<Date, String> {

    private static final String FORMAT = "yyyy/MM/dd";

    @Override
    public Date toModelValue( String widgetValue ) {
        return DateTimeFormat.getFormat( FORMAT ).parse( widgetValue );
    }

    @Override
    public String toWidgetValue( Date modelValue ) {
        return DateTimeFormat.getFormat( FORMAT ).format( modelValue );
    }

}
