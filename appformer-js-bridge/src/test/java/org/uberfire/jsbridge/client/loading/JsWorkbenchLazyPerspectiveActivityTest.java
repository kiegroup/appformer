package org.uberfire.jsbridge.client.loading;

import java.util.function.Consumer;

import com.google.gwt.place.shared.Place;
import org.junit.Before;
import org.junit.Test;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.jsbridge.client.perspective.JsWorkbenchPerspectiveActivity;
import org.uberfire.mvp.PlaceRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsWorkbenchLazyPerspectiveActivityTest {

    private JsWorkbenchLazyPerspectiveActivity jsWorkbenchLazyPerspectiveActivity;

    private PlaceManager placeManager;
    private AppFormerComponentsRegistry.Entry entry;
    private ActivityManager activityManager;
    private Consumer<String> lazyLoadingParentScript;

    @Before
    public void before() {
        placeManager = mock(PlaceManager.class);
        entry = mock(AppFormerComponentsRegistry.Entry.class);
        activityManager = mock(ActivityManager.class);
        lazyLoadingParentScript = s -> {
        };

        jsWorkbenchLazyPerspectiveActivity = spy(new JsWorkbenchLazyPerspectiveActivity(
                entry,
                placeManager,
                activityManager,
                lazyLoadingParentScript));
    }

    @Test
    public void getNotLoaded() {
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(mock(JsWorkbenchPerspectiveActivity.class));
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(false);
        assertEquals(jsWorkbenchLazyPerspectiveActivity, jsWorkbenchLazyPerspectiveActivity.get());
    }

    @Test
    public void getLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(true);
        assertEquals(backedPerspective, jsWorkbenchLazyPerspectiveActivity.get());
    }

    @Test
    public void onStartupLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(true);

        final PlaceRequest placeRequest = mock(PlaceRequest.class);
        jsWorkbenchLazyPerspectiveActivity.onStartup(placeRequest);

        verify(backedPerspective).onStartup(placeRequest);
    }

    @Test
    public void onStartupNotLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(false);

        final PlaceRequest placeRequest = mock(PlaceRequest.class);
        jsWorkbenchLazyPerspectiveActivity.onStartup(placeRequest);

        verify(backedPerspective, never()).onStartup(placeRequest);
    }

    @Test
    public void onOpenLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(true);

        jsWorkbenchLazyPerspectiveActivity.onOpen();

        verify(backedPerspective).onOpen();
        verify(placeManager).executeOnOpenCallbacks(any());
        verify(jsWorkbenchLazyPerspectiveActivity, never()).onLoaded();
    }

    @Test
    public void onOpenNotLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(false);

        jsWorkbenchLazyPerspectiveActivity.onStartup(mock(PlaceRequest.class));
        jsWorkbenchLazyPerspectiveActivity.onOpen();

        verify(backedPerspective, never()).onOpen();
        verify(placeManager, times(2)).executeOnOpenCallbacks(any());
        verify(jsWorkbenchLazyPerspectiveActivity).onLoaded();
    }

    @Test
    public void onCloseLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(true);

        jsWorkbenchLazyPerspectiveActivity.onStartup(mock(PlaceRequest.class));
        jsWorkbenchLazyPerspectiveActivity.onClose();

        verify(backedPerspective).onClose();
        verify(placeManager).executeOnCloseCallbacks(any());
    }

    @Test
    public void onCloseNotLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(false);

        jsWorkbenchLazyPerspectiveActivity.onStartup(mock(PlaceRequest.class));
        jsWorkbenchLazyPerspectiveActivity.onOpen();
        jsWorkbenchLazyPerspectiveActivity.onClose();

        verify(backedPerspective, never()).onClose();
        verify(placeManager, times(2)).executeOnCloseCallbacks(any());
    }

    @Test
    public void onShutdownLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(true);

        jsWorkbenchLazyPerspectiveActivity.onStartup(mock(PlaceRequest.class));
        jsWorkbenchLazyPerspectiveActivity.onOpen();
        jsWorkbenchLazyPerspectiveActivity.onShutdown();

        verify(backedPerspective).onShutdown();
    }

    @Test
    public void onShutdownNotLoaded() {
        JsWorkbenchPerspectiveActivity backedPerspective = mock(JsWorkbenchPerspectiveActivity.class);
        when(jsWorkbenchLazyPerspectiveActivity.getBackedPerspective()).thenReturn(backedPerspective);
        when(jsWorkbenchLazyPerspectiveActivity.isPerspectiveLoaded()).thenReturn(false);

        jsWorkbenchLazyPerspectiveActivity.onStartup(mock(PlaceRequest.class));
        jsWorkbenchLazyPerspectiveActivity.onOpen();
        jsWorkbenchLazyPerspectiveActivity.onClose();
        jsWorkbenchLazyPerspectiveActivity.onShutdown();

        verify(backedPerspective, never()).onShutdown();
    }
}
