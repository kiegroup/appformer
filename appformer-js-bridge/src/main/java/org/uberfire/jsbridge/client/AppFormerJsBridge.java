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

package org.uberfire.jsbridge.client;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.WorkbenchScreenActivity;
import org.uberfire.client.workbench.Workbench;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.jboss.errai.ioc.client.QualifierUtil.DEFAULT_QUALIFIERS;

@Dependent
public class AppFormerJsBridge {

    @Inject
    private Workbench workbench;

    @Inject
    private TranslationService translationService;

    public void init(final String gwtModuleName) {

        workbench.addStartupBlocker(AppFormerJsBridge.class);

        exposeBridge();

        //FIXME: Not ideal to load scripts here. Make it lazy.
        //FIXME: Load React from local instead of CDN.

        ScriptInjector.fromUrl("https://unpkg.com/react@16/umd/react.production.min.js")
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback((Success<Void>) i1 -> ScriptInjector.fromUrl("https://unpkg.com/react-dom@16/umd/react-dom.production.min.js")
                        .setWindow(ScriptInjector.TOP_WINDOW)
                        .setCallback((Success<Void>) i2 -> ScriptInjector.fromUrl("/" + gwtModuleName + "/showcase/showcase-components.bundle.js")
                                .setWindow(ScriptInjector.TOP_WINDOW)
                                .setCallback((Success<Void>) i3 -> workbench.removeStartupBlocker(AppFormerJsBridge.class))
                                .inject())
                        .inject())
                .inject();
    }

    private native void exposeBridge() /*-{
        $wnd.appformerGwtBridge = {
            registerScreen: this.@org.uberfire.jsbridge.client.AppFormerJsBridge::registerScreen(Ljava/lang/Object;),
            registerPerspective: this.@org.uberfire.jsbridge.client.AppFormerJsBridge::registerPerspective(Ljava/lang/Object;),
            goTo: this.@org.uberfire.jsbridge.client.AppFormerJsBridge::goTo(Ljava/lang/String;),
            rpc: this.@org.uberfire.jsbridge.client.AppFormerJsBridge::rpc(Ljava/lang/String;[Ljava/lang/Object;),
            translate: this.@org.uberfire.jsbridge.client.AppFormerJsBridge::translate(Ljava/lang/String;[Ljava/lang/Object;)
        };
    }-*/;

    public void goTo(final String place) {
        final SyncBeanManager beanManager = IOC.getBeanManager();
        final PlaceManager placeManager = beanManager.lookupBean(PlaceManager.class).getInstance();
        placeManager.goTo(new DefaultPlaceRequest(place));
    }

    public String translate(final String key, final Object[] args) {
        return translationService != null ? translationService.format(key, args) : "GWT-Translated (" + key + ")";
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

        //FIXME: Check if this bean is being registered correctly. Startup/Shutdown is begin called as if they were Open/Close.
        final SingletonBeanDefinition<JsWorkbenchScreenActivity, JsWorkbenchScreenActivity> activityBean = new SingletonBeanDefinition<>(
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

    public Promise<Object> rpc(final String path, final Object[] params) {

        //FIXME: Marshall/unmarshall is happening twice
        return new Promise<>((res, rej) -> {

            final String[] parts = path.split("\\|");
            final String serviceFqcn = parts[0];
            final String method = parts[1];
            final Annotation[] qualifiers = {};

            final Function<Object, Object> jsonToGwt = object -> {
                final String json = (String) object;

                if (!json.startsWith("{")) {
                    return json;
                }

                try {
                    return Marshalling.fromJSON(json);
                } catch (final Exception e) {
                    DomGlobal.console.info("Error converting JS obj to GWT obj", e);
                    throw e;
                }
            };

            final Function<Object, Object> gwtToJson = value -> value != null
                    ? Marshalling.toJSON(value)
                    : null;

            Object[] objects = stream(((String[]) params[0])).map(jsonToGwt).toArray();

            MessageBuilder.createCall()
                    .call(serviceFqcn)
                    .endpoint(method, qualifiers, objects)
                    .respondTo(Object.class, value -> res.onInvoke(gwtToJson.apply(value)))
                    .errorsHandledBy((e, a) -> true)
                    .sendNowWith(ErraiBus.get());
        });
    }

    public native static void callNative(final Object func, final Object arg) /*-{
        func(JSON.parse(arg));
    }-*/;

    interface Success<T> extends Callback<T, Exception> {

        @Override
        default void onFailure(Exception o) {
        }
    }
}






