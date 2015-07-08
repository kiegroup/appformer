package org.livespark.test.mock;

import java.util.Collection;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;

public class MockQueueSession implements QueueSession {

    private final String queueSessionId;

    public MockQueueSession( final String queueSessionId ) {
        this.queueSessionId = queueSessionId;
    }

    @Override
    public void setAttribute( String attribute,
                              Object value ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object removeAttribute( String attribute ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttribute( String attribute ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSessionId() {
        return queueSessionId;
    }

    @Override
    public String getParentSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getAttributeNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getAttribute( Class<T> type,
                               String attribute ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean endSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addSessionEndListener( SessionEndListener listener ) {
        throw new UnsupportedOperationException();
    }
}