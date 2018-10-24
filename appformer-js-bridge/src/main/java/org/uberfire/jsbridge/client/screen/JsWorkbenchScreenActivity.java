/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.jsbridge.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.IsWidget;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import jsinterop.base.Any;
import jsinterop.base.Js;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.marshalling.client.Marshalling;
import org.uberfire.client.mvp.AbstractWorkbenchScreenActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.jsbridge.client.AppFormerJsBridge;
import org.uberfire.jsbridge.client.JsPlaceRequest;
import org.uberfire.jsbridge.client.loading.JsWorkbenchLazyActivity;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

public class JsWorkbenchScreenActivity extends AbstractWorkbenchScreenActivity implements JsWorkbenchLazyActivity {

    private InvocationPostponer invocationsPostponer;

    private PlaceRequest place;
    private JsNativeScreen screen;
    private List<Subscription> subscriptions;

    public JsWorkbenchScreenActivity(final JsNativeScreen screen,
                                     final PlaceManager placeManager) {

        super(placeManager);
        this.screen = screen;
        this.subscriptions = new ArrayList<>();
        this.invocationsPostponer = new InvocationPostponer();
    }

    @Override
    public void updateRealContent(final JavaScriptObject jsObject) {
        this.screen.updateRealContent(jsObject);
        this.invocationsPostponer.executeAll();
    }

    //
    //
    //LIFECYCLE

    @Override
    public void onStartup(final PlaceRequest place) {

        this.place = place;

        if (!this.screen.screenLoaded()) {
            this.invocationsPostponer.postpone(() -> this.onStartup(place));
            return;
        }

        this.registerSubscriptions();
        screen.onStartup(JsPlaceRequest.fromPlaceRequest(place));
    }

    @Override
    public void onOpen() {

        // render no matter if the script was loaded or not, even if the call results in a blank screen being rendered.
        screen.render();

        if (!this.screen.screenLoaded()) {
            this.invocationsPostponer.postpone(this::onOpen);
            return;
        }

        screen.onOpen();
        placeManager.executeOnOpenCallbacks(place);
    }

    @Override
    public void onClose() {

        if (this.screen.screenLoaded()) {
            screen.onClose();
        }

        placeManager.executeOnCloseCallbacks(place);
    }

    @Override
    public boolean onMayClose() {

        if (this.screen.screenLoaded()) {
            return screen.onMayClose();
        }

        return true;
    }

    @Override
    public void onShutdown() {

        this.invocationsPostponer.clear();

        if (this.screen.screenLoaded()) {
            this.unsubscribeFromAllEvents();
            screen.onShutdown();
        }
    }

    @Override
    public void onFocus() {
        if (this.screen.screenLoaded()) {
            screen.onFocus();
        }
    }

    @Override
    public void onLostFocus() {
        if (this.screen.screenLoaded()) {
            screen.onLostFocus();
        }
    }

    // PROPERTIES
    @Override
    public String getTitle() {
        return screen.componentTitle();
    }

    @Override
    public Position getDefaultPosition() {
        return CompassPosition.ROOT;
    }

    @Override
    public PlaceRequest getPlace() {
        return place;
    }

    @Override
    public String getIdentifier() {
        return screen.componentId();
    }

    @Override
    public IsWidget getTitleDecoration() {
        return null;
    }

    @Override
    public Menus getMenus() {
        return null;
    }

    @Override
    public ToolBar getToolBar() {
        return null;
    }

    @Override
    public PlaceRequest getOwningPlace() {
        return null;
    }

    @Override
    public IsWidget getWidget() {
        return ElementWrapperWidget.getWidget(screen.getElement());
    }

    @Override
    public String contextId() {
        return screen.componentContextId();
    }

    @Override
    public int preferredHeight() {
        return -1;
    }

    @Override
    public int preferredWidth() {
        return -1;
    }

    //
    //
    //CDI Events Subscriptions

    private void registerSubscriptions() {
        DomGlobal.console.info("Registering event subscriptions for " + this.getIdentifier() + "...");
        final JsObject subscriptions = this.screen.subscriptions();
        for (final String eventFqcn : JsObject.keys(subscriptions)) {
            if (subscriptions.hasOwnProperty(eventFqcn)) {
                final Any jsObject = Js.uncheckedCast(subscriptions);
                final Object callback = jsObject.asPropertyMap().get(eventFqcn);

                //TODO: Parent classes of "eventFqcn" should be subscribed to as well?
                //FIXME: Marshall/unmarshall is happening twice

                final Subscription subscription = CDI.subscribe(eventFqcn, new AbstractCDIEventCallback<Object>() {
                    public void fireEvent(final Object event) {
                        callNative(callback, Marshalling.toJSON(event));
                    }
                });

                //Subscribes to client-sent events.
                this.subscriptions.add(subscription);

                //TODO: Handle local-only events
                //Forwards server-sent events to the local subscription.
                ErraiBus.get().subscribe("cdi.event:" + eventFqcn, CDI.ROUTING_CALLBACK);
            }
        }
    }

    public native static void callNative(final Object func, final String jsonArg) /*-{
        func(JSON.parse(jsonArg)); //FIXME: Unmarshall!
    }-*/;

    private void unsubscribeFromAllEvents() {
        DomGlobal.console.info("Removing event subscriptions for " + this.getIdentifier() + "...");
        this.subscriptions.forEach(Subscription::remove);
        this.subscriptions = new ArrayList<>();
    }

    public class InvocationPostponer {

        private final Stack<Runnable> invocations;

        public InvocationPostponer() {
            this.invocations = new Stack<>();
        }

        public void postpone(final Runnable invocation) {
            this.invocations.push(invocation);
        }

        public void executeAll() {
            while (!this.invocations.isEmpty()) {
                this.invocations.pop().run();
            }
        }

        public void clear() {
            this.invocations.clear();
        }
    }
}
