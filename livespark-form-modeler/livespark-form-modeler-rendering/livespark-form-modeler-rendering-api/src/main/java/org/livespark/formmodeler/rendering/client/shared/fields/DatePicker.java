package org.livespark.formmodeler.rendering.client.shared.fields;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Event;

/**
 * Created by pefernan on 10/9/15.
 */
public class DatePicker extends org.gwtbootstrap3.extras.datepicker.client.ui.DatePicker {

    @Override
    public void onChangeDate(Event e) {
        super.onChangeDate(e);
        ValueChangeEvent.fire(DatePicker.this, getValue());
    }
}
