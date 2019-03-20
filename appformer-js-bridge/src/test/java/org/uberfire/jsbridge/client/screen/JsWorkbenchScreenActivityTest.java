package org.uberfire.jsbridge.client.screen;

import elemental2.core.Map;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.client.mvp.PlaceManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JsWorkbenchScreenActivityTest {

    private JsWorkbenchScreenActivity jsWorkbenchScreenActivity;

    @Mock
    private JsNativeScreen jsNativeScreen;

    @Before
    public void before() {
        jsWorkbenchScreenActivity = spy(new JsWorkbenchScreenActivity(jsNativeScreen,
                                                                      mock(PlaceManager.class)));
    }

    @Test
    public void registerSubscription() {
        final Object callback = mock(Object.class);
        final String eventFqcn = "foo";

        doReturn(mock(Subscription.class)).when(jsWorkbenchScreenActivity).getSubscription(callback, eventFqcn);
        doNothing().when(jsWorkbenchScreenActivity).subscribeOnErraiBus(eventFqcn);

        final Void nullReturn = jsWorkbenchScreenActivity.registerSubscription(callback, eventFqcn, null);
        assertNull(nullReturn);
        assertEquals(1, jsWorkbenchScreenActivity.subscriptions.size());
        verify(jsWorkbenchScreenActivity).subscribeOnErraiBus(eventFqcn);
    }
}