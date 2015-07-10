/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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