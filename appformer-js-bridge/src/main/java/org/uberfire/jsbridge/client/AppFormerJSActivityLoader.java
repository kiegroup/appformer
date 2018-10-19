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

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.mvp.*;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import javax.enterprise.context.Dependent;
import java.util.*;
import java.util.function.Consumer;

import static org.jboss.errai.ioc.client.QualifierUtil.DEFAULT_QUALIFIERS;

@EntryPoint
public class AppFormerJSActivityLoader {


    private Map<String, String> components = new HashMap<>();
    private String gwtModuleName;
    private Set<String> loadedScripts = new HashSet<>();

    public void init(String gwtModuleName) {
        this.gwtModuleName = gwtModuleName;
        extractComponents();
    }

    private void extractComponents() {
        Arrays.stream(AppFormerComponentsRegistry.keys())
                .filter(k -> AppFormerComponentsRegistry.get(k) != null)
                .forEach(k -> registryComponent(k, null));
    }

    public void registerScreen(Object js) {
        JavaScriptObject jsObject = (JavaScriptObject) js;
        String id = extractId(jsObject);
        if (components.containsKey(id)) {
            final SyncBeanManager beanManager = IOC.getBeanManager();
            final ActivityManager activityManager = beanManager.lookupBean(ActivityManager.class).getInstance();

            JsWorkbenchScreenActivity activity = (JsWorkbenchScreenActivity) activityManager.getActivity(new DefaultPlaceRequest(id));
            activity.updateRealContent(jsObject);
        } else {
            registryComponent(id, jsObject);

        }
    }

    //TODO this should be unified with JSWorkbenchScreenActivity getIdentifier
    public native String extractId(final JavaScriptObject object)  /*-{
        return object['af_componentId'];
    }-*/;


    private void lazyLoadParentScript(String component) {
        //TODO REMOVE THIS: ONLY FOR DEMO PURPOSES
        com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                String targetScript = components.get(component);
                if (!loadedScripts.contains(targetScript)) {
                    loadedScripts.add(targetScript);
                    ScriptInjector.fromUrl("/" + gwtModuleName + "/" + targetScript)
                            .setWindow(ScriptInjector.TOP_WINDOW)
                            .inject();
                }

            }
        };
        timer.schedule(1500);
    }

    private void registryComponent(String identifier, JavaScriptObject jsObject) {

        final SyncBeanManager beanManager = IOC.getBeanManager();
        JsNativeScreen newScreen = JsNativeScreen.build(identifier, jsObject, this::lazyLoadParentScript);

        final JsWorkbenchScreenActivity activity = new JsWorkbenchScreenActivity(newScreen,
                beanManager.lookupBean(PlaceManager.class).getInstance());
        final ActivityBeansCache activityBeansCache = beanManager.lookupBean(ActivityBeansCache.class).getInstance();

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

        components.put(identifier, AppFormerComponentsRegistry.get(identifier));
        activityBeansCache.addNewScreenActivity(beanManager.lookupBeans(activity.getIdentifier()).iterator().next());
    }


    public interface Success<T> extends Callback<T, Exception> {
        @Override
        default void onFailure(Exception o) {
        }
    }

    public static class AppFormerComponentsRegistry {

        public static native String[] keys() /*-{
            if (typeof $wnd.AppFormerComponentsRegistry === "undefined") {
                return [];
            }
            return Object.keys($wnd.AppFormerComponentsRegistry);
        }-*/;

        public static native String get(String key) /*-{
            if (typeof $wnd.AppFormerComponentsRegistry[key] === "undefined") {
                return null;
            }
            return $wnd.AppFormerComponentsRegistry[key];
        }-*/;

    }
}
