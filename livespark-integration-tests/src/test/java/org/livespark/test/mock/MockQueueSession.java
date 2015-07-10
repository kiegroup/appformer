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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;

public class MockQueueSession implements QueueSession {

    private final String queueSessionId;

    private Map<String, Object> attrs = new ConcurrentHashMap<String, Object>();

    public MockQueueSession( final String queueSessionId, final HttpSession httpSession ) {
        this.queueSessionId = queueSessionId;
        attrs.put( HttpSession.class.getName(), httpSession );
    }

    @Override
    public void setAttribute( String attribute,
                              Object value ) {
        attrs.put( attribute, value );
    }

    @Override
    public Object removeAttribute( String attribute ) {
        return attrs.remove( attribute );
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttribute( String attribute ) {
        return attrs.containsKey( attribute );
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
        return attrs.keySet();
    }

    @Override
    public <T> T getAttribute( Class<T> type,
                               String attribute ) {
        return type.cast( attrs.get( attribute ) );
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