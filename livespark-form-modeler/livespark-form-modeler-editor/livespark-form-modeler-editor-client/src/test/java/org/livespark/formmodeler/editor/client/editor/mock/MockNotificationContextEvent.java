package org.livespark.formmodeler.editor.client.editor.mock;

import org.livespark.formmodeler.editor.client.editor.events.FormContextResponse;

import javax.enterprise.event.Event;
import java.lang.annotation.Annotation;

/**
 * Created by pefernan on 10/14/15.
 */
public class MockNotificationContextEvent implements Event<FormContextResponse> {

    @Override public void fire( FormContextResponse notificationEvent ) {

    }

    @Override public Event<FormContextResponse> select( Annotation... annotations ) {
        return null;
    }

    @Override public <U extends FormContextResponse> Event<U> select( Class<U> aClass, Annotation... annotations ) {
        return null;
    }
}
