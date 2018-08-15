/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.appformer.jsbridge.client;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.exporter.SingletonBeanDef;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.WorkbenchScreenActivity;
import org.uberfire.client.workbench.Workbench;

import static org.jboss.errai.ioc.client.QualifierUtil.DEFAULT_QUALIFIERS;

@EntryPoint
public class AppFormerGwtBridge {

    @Inject
    private Workbench workbench;

    @PostConstruct
    public void init() {

        workbench.addStartupBlocker(AppFormerGwtBridge.class);

        exposeBridge();

        //FIXME: Not ideal to load scripts here. Make it lazy.
        //FIXME: Load React from local instead of CDN.

        ScriptInjector.fromUrl("https://unpkg.com/react@16/umd/react.production.min.js")
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback((Success<Void>) i1 -> ScriptInjector.fromUrl("https://unpkg.com/react-dom@16/umd/react-dom.production.min.js")
                        .setWindow(ScriptInjector.TOP_WINDOW)
                        .setCallback((Success<Void>) i2 -> ScriptInjector.fromUrl("/org.uberfire.UberfireShowcase/core-screens/screens.bundle.js")
                                .setWindow(ScriptInjector.TOP_WINDOW)
                                .setCallback((Success<Void>) i3 -> workbench.removeStartupBlocker(AppFormerGwtBridge.class))
                                .inject())
                        .inject())
                .inject();
    }

    private native void exposeBridge() /*-{
        $wnd.appformerGwtBridge = {
            registerScreen: this.@org.appformer.jsbridge.client.AppFormerGwtBridge::registerScreen(Ljava/lang/Object;),
            registerPerspective: this.@org.appformer.jsbridge.client.AppFormerGwtBridge::registerPerspective(Ljava/lang/Object;),
            goTo: $wnd.$goToPlace, //This window.$goToPlace method is bound in PlaceManagerJSExporter.publish()
            RPC: this.@org.appformer.jsbridge.client.AppFormerGwtBridge::RPC(Ljava/lang/String;[Ljava/lang/Object;),
            subscribe: this.@org.appformer.jsbridge.client.AppFormerGwtBridge::subscribe(Ljava/lang/String;Ljava/lang/Object;)
        };
    }-*/;

    public Promise<Object> RPC(final String path, final Object[] params) {
        return new Promise<>((res, rej) -> {

            final String[] parts = path.split("\\|");
            final String serviceFqcn = parts[0];
            final String method = parts[1];
            final Annotation[] qualifiers = {};

            MessageBuilder.createCall()
                    .call(serviceFqcn)
                    .endpoint(method + ":", qualifiers, params)
                    .respondTo(Object.class, res::onInvoke)
                    .errorsHandledBy((e, a) -> true)
                    .sendNowWith(ErraiBus.get());
        });
    }

    @SuppressWarnings("unchecked")
    public void registerPerspective(final Object jsObject) {
        //TODO: Actually register perspectives
        DomGlobal.console.info(jsObject + " registered as perspective.");
    }

    @SuppressWarnings("unchecked")
    public void registerScreen(final Object jsObject) {

        final SyncBeanManager beanManager = IOC.getBeanManager();
        final ActivityBeansCache activityBeansCache = beanManager.lookupBean(ActivityBeansCache.class).getInstance();
        final JsNativeScreen newScreen = new JsNativeScreen((JavaScriptObject) jsObject);
        final JsWorkbenchScreenActivity activity = new JsWorkbenchScreenActivity(newScreen,
                                                                                 beanManager.lookupBean(PlaceManager.class).getInstance());

        final SingletonBeanDef<JsWorkbenchScreenActivity, JsWorkbenchScreenActivity> activityBean = new SingletonBeanDef<>(
                activity,
                JsWorkbenchScreenActivity.class,
                new HashSet<>(Arrays.asList(DEFAULT_QUALIFIERS)),
                activity.getIdentifier(),
                true,
                WorkbenchScreenActivity.class,
                Activity.class);

        beanManager.registerBean(activityBean);
        beanManager.registerBeanTypeAlias(activityBean, WorkbenchScreenActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, Activity.class);

        activityBeansCache.addNewScreenActivity(beanManager.lookupBeans(activity.getIdentifier()).iterator().next());
    }

    public void subscribe(final String eventFqcn, final Object consumer) {

        //TODO: Parent classes of "eventFqcn" should be subscribed to as well?

        //Subscribes to client-sent events.
        CDI.subscribe(eventFqcn, new AbstractCDIEventCallback<Object>() {
            public void fireEvent(final Object event) {
                AppFormerGwtBridge.callNative(consumer, event);
            }

            public String toString() {
                return "JS Component subscription to " + eventFqcn;
            }
        });

        //TODO: Handle local-only events
        //Subscribes to server-sent events.
        ErraiBus.get().subscribe("cdi.event:" + eventFqcn, CDI.ROUTING_CALLBACK);
    }

    public native static void callNative(Object func, Object arg) /*-{
        func(arg);
    }-*/;

    interface Success<T> extends Callback<T, Exception> {

        @Override
        default void onFailure(Exception o) {
        }
    }
}






