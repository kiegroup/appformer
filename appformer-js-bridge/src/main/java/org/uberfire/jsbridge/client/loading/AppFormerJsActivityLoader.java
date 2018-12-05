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
package org.uberfire.jsbridge.client.loading;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.WorkbenchScreenActivity;
import org.uberfire.client.mvp.jsbridge.JsWorkbenchLazyActivity;
import org.uberfire.jsbridge.client.SingletonBeanDefinition;
import org.uberfire.jsbridge.client.screen.JsNativeScreen;
import org.uberfire.jsbridge.client.screen.JsWorkbenchScreenActivity;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import static org.jboss.errai.ioc.client.QualifierUtil.DEFAULT_QUALIFIERS;

@EntryPoint
public class AppFormerJsActivityLoader {

    private Map<String, String> components = new HashMap<>();
    private String gwtModuleName;
    private Set<String> loadedScripts = new HashSet<>();

    @Inject
    private Event<ActivityLazyLoaded> activityLazyLoadedEvent;

    public void init(String gwtModuleName) {
        this.gwtModuleName = gwtModuleName;
        extractComponentsFromRegistry();
    }

    private void extractComponentsFromRegistry() {
        Arrays.stream(AppFormerComponentsRegistry.keys())
                .map(k -> new AppFormerComponentConfiguration(k, AppFormerComponentsRegistry.get(k)))
                .forEach(this::registerComponent);
    }

    public void onActivityLoaded(final Object jsInput) {

        final JavaScriptObject jsObject = (JavaScriptObject) jsInput;
        final String id = extractId(jsObject);

        if (!components.containsKey(id)) {
            throw new IllegalArgumentException("Cannot find component " + id);
        }

        final SyncBeanManager beanManager = IOC.getBeanManager();
        final ActivityManager activityManager = beanManager.lookupBean(ActivityManager.class).getInstance();

        final Activity activity = activityManager.getActivity(new DefaultPlaceRequest(id));

        JsWorkbenchLazyActivity lazyActivity = (JsWorkbenchLazyActivity) activity;
        lazyActivity.updateRealContent((JavaScriptObject) jsInput);

        activityLazyLoadedEvent.fire(new ActivityLazyLoaded(id, activity));
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

    private void registerComponent(final AppFormerComponentConfiguration component) {

        switch (component.getType()) {
            case PERSPECTIVE:
                registerPerspective(component);
                break;
            case SCREEN:
                registerScreen(component);
                break;
            default:
                throw new IllegalArgumentException("Don't know how to register component " + component.getId());
        }
    }

    @SuppressWarnings("unchecked")
    private void registerScreen(final AppFormerComponentConfiguration component) {

        final String identifier = component.getId();

        final SyncBeanManager beanManager = IOC.getBeanManager();
        final JsNativeScreen newScreen = new JsNativeScreen(identifier,
                                                            this::lazyLoadParentScript,
                                                            beanManager.lookupBean(LazyLoadingScreen.class).getInstance());

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
                JsWorkbenchLazyActivity.class,
                Activity.class);

        beanManager.registerBean(activityBean);
        beanManager.registerBeanTypeAlias(activityBean, WorkbenchScreenActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, JsWorkbenchLazyActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, Activity.class);

        components.put(identifier, component.getSource());
        activityBeansCache.addNewScreenActivity(beanManager.lookupBeans(activity.getIdentifier()).iterator().next());
    }

    @SuppressWarnings("unchecked")
    private void registerPerspective(final AppFormerComponentConfiguration component) {

        final String componentId = component.getId();

        final SyncBeanManager beanManager = IOC.getBeanManager();
        final ActivityBeansCache activityBeansCache = beanManager.lookupBean(ActivityBeansCache.class).getInstance();

        final PlaceManager placeManager = beanManager.lookupBean(PlaceManager.class).getInstance();
        final ActivityManager activityManager = beanManager.lookupBean(ActivityManager.class).getInstance();

        final PerspectiveActivity activity = registerPerspectiveActivity(component, placeManager, activityManager, beanManager);

        components.put(componentId, component.getSource());
        activityBeansCache.addNewPerspectiveActivity(beanManager.lookupBeans(activity.getIdentifier()).iterator().next());
    }

    private PerspectiveActivity registerPerspectiveActivity(final AppFormerComponentConfiguration component,
                                                            final PlaceManager placeManager,
                                                            final ActivityManager activityManager,
                                                            final SyncBeanManager beanManager) {

        final JsWorkbenchLazyPerspectiveActivity activity = new JsWorkbenchLazyPerspectiveActivity(component,
                                                                                                   placeManager,
                                                                                                   activityManager,
                                                                                                   this::lazyLoadParentScript);

        //FIXME: Check if this bean is being registered correctly. Startup/Shutdown is begin called as if they were Open/Close.
        final SingletonBeanDefinition<JsWorkbenchLazyPerspectiveActivity, JsWorkbenchLazyPerspectiveActivity> activityBean = new SingletonBeanDefinition<>(
                activity,
                JsWorkbenchLazyPerspectiveActivity.class,
                new HashSet<>(Arrays.asList(DEFAULT_QUALIFIERS)),
                activity.getIdentifier(),
                true,
                PerspectiveActivity.class,
                JsWorkbenchLazyActivity.class,
                Activity.class);

        beanManager.registerBean(activityBean);
        beanManager.registerBeanTypeAlias(activityBean, PerspectiveActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, JsWorkbenchLazyActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, Activity.class);

        return activity;
    }
}
