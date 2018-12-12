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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Qualifier;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.PlaceManagerImpl;
import org.uberfire.client.mvp.WorkbenchEditorActivity;
import org.uberfire.client.mvp.WorkbenchScreenActivity;
import org.uberfire.client.mvp.jsbridge.JsWorkbenchLazyActivity;
import org.uberfire.client.promise.Promises;
import org.uberfire.jsbridge.client.cdi.EditorActivityBeanDefinition;
import org.uberfire.jsbridge.client.cdi.SingletonBeanDefinition;
import org.uberfire.jsbridge.client.editor.JsNativeEditor;
import org.uberfire.jsbridge.client.editor.JsWorkbenchEditorActivity;
import org.uberfire.jsbridge.client.screen.JsNativeScreen;
import org.uberfire.jsbridge.client.screen.JsWorkbenchScreenActivity;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.jboss.errai.ioc.client.QualifierUtil.DEFAULT_QUALIFIERS;

@EntryPoint
public class AppFormerJsActivityLoader implements PlaceManagerImpl.AppFormerActivityLoader {

    @Inject
    private Promises promises;

    @Inject
    private ActivityManager activityManager;

    @Inject
    private ActivityBeansCache activityBeansCache;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private LazyLoadingScreen lazyLoadingScreen;

    private final Map<String, String> components = new HashMap<>();
    private final Set<String> loadedScripts = new HashSet<>();
    private final Map<String, AppFormerComponentsRegistry.Entry> editors = new HashMap<>();

    private String gwtModuleName;

    @Inject
    private Event<ActivityLazyLoaded> activityLazyLoadedEvent;

    public void init(final String gwtModuleName) {
        this.gwtModuleName = gwtModuleName;

        stream(AppFormerComponentsRegistry.keys())
                .map(componentId -> new AppFormerComponentsRegistry.Entry(componentId, AppFormerComponentsRegistry.get(componentId)))
                .forEach(this::registerComponent);
    }

    public void onComponentLoaded(final Object jsObject) {

        final String componentId = extractId(jsObject);

        if (this.editors.containsKey(componentId)) {
            registerEditor(jsObject, componentId);
            return;
        }

        if (!components.containsKey(componentId)) {
            throw new IllegalArgumentException("Cannot find component " + componentId);
        }

        //FIXME: Get activity bean from BeanManager to prevent onStartup to be invoked.
        final Activity activity = activityManager.getActivity(new DefaultPlaceRequest(componentId));

        JsWorkbenchLazyActivity lazyActivity = (JsWorkbenchLazyActivity) activity;
        lazyActivity.updateRealContent((JavaScriptObject) jsObject);

        activityLazyLoadedEvent.fire(new ActivityLazyLoaded(componentId, activity));
    }

    //TODO this should be unified with JSWorkbenchScreenActivity getIdentifier
    public native String extractId(final Object object)  /*-{
        return object['af_componentId'];
    }-*/;

    private Promise<Void> loadScriptFor(final String componentId) {

        final Optional<String> editorScriptUrl = ofNullable(editors.get(componentId))
                .map(AppFormerComponentsRegistry.Entry::getSource);

        final Optional<String> scriptFilename = editorScriptUrl.isPresent()
                ? editorScriptUrl
                : Optional.ofNullable(components.get(componentId));

        if (!scriptFilename.isPresent()) {
            throw new RuntimeException("No script found for " + componentId);
        }

        if (loadedScripts.contains(scriptFilename.get())) {
            return promises.resolve();
        }

        loadedScripts.add(scriptFilename.get());
        final String scriptUrl = "/" + gwtModuleName + "/" + scriptFilename.get();

        return promises.resolve().<Void>then(l -> new Promise<>((res, rej) -> {
            //FIXME: Timer is here for demo purposes only
            com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
                @Override
                public void run() {
                    ScriptInjector.fromUrl(scriptUrl)
                            .setWindow(ScriptInjector.TOP_WINDOW)
                            .setCallback(new Callback<Void, Exception>() {
                                @Override
                                public void onFailure(final Exception e1) {
                                    rej.onInvoke(e1);
                                }

                                @Override
                                public void onSuccess(final Void v) {
                                    res.onInvoke(v);
                                }
                            })
                            .inject();
                }
            };
            timer.schedule(1500);
        })).catch_(e -> {
            DomGlobal.console.info("Error loading script for " + componentId);
            return promises.reject(e);
        });
    }

    private void registerComponent(final AppFormerComponentsRegistry.Entry registryEntry) {
        switch (registryEntry.getType()) {
            case PERSPECTIVE:
                registerPerspective(registryEntry);
                break;
            case SCREEN:
                registerScreen(registryEntry);
                break;
            case EDITOR:
                registerEditor(registryEntry);
                break;
            default:
                throw new IllegalArgumentException("Don't know how to register component " + registryEntry.getComponentId());
        }
    }

    public boolean triggerLoadOfMatchingEditors(final Path path,
                                                final Runnable successCallback) {

        if (path == null) {
            return false;
        }

        final List<Promise<Void>> matchingEditors = this.editors.values().stream()
                .filter(e -> {
                    final String matches = e.getParams().get("matches");
                    final String regex = matches.substring(1, matches.length() - 1); //FIXME: Temporary workaround to remove extra quotes
                    return path.toURI().matches(regex);
                })
                .filter(e -> !this.loadedScripts.contains(e.getSource()))
                .map(e -> this.loadScriptFor(e.getComponentId()))
                .collect(toList());

        if (matchingEditors.size() <= 0) {
            return false;
        }

        this.promises.resolve().then(i -> promises.all(matchingEditors, identity()).then(s -> {
            successCallback.run();
            return this.promises.resolve();
        })).catch_(e -> {
            //If something goes wrong, it's a no-op.
            return this.promises.resolve();
        });

        return true;
    }

    private void registerEditor(final AppFormerComponentsRegistry.Entry registryEntry) {
        this.editors.put(registryEntry.getComponentId(), registryEntry);
    }

    @SuppressWarnings("unchecked")
    private void registerScreen(final AppFormerComponentsRegistry.Entry registryEntry) {

        final JsNativeScreen newScreen = new JsNativeScreen(registryEntry.getComponentId(), this::loadScriptFor, lazyLoadingScreen);
        final JsWorkbenchScreenActivity activity = new JsWorkbenchScreenActivity(newScreen, placeManager);

        //FIXME: Check if this bean is being registered correctly. Startup/Shutdown is begin called as if they were Open/Close.
        final SingletonBeanDefinition activityBean = new SingletonBeanDefinition<>(
                activity,
                JsWorkbenchScreenActivity.class,
                new HashSet<>(Arrays.asList(DEFAULT_QUALIFIERS)),
                activity.getIdentifier(),
                true,
                WorkbenchScreenActivity.class,
                JsWorkbenchLazyActivity.class,
                Activity.class);

        components.put(registryEntry.getComponentId(), registryEntry.getSource());
        activityBeansCache.addNewScreenActivity(activityBean);

        final SyncBeanManager beanManager = IOC.getBeanManager();
        beanManager.registerBean(activityBean);
        beanManager.registerBeanTypeAlias(activityBean, WorkbenchScreenActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, JsWorkbenchLazyActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, Activity.class);
    }

    @SuppressWarnings("unchecked")
    private void registerPerspective(final AppFormerComponentsRegistry.Entry registryEntry) {
        final String componentId = registryEntry.getComponentId();

        final SyncBeanManager beanManager = IOC.getBeanManager();
        final ActivityBeansCache activityBeansCache = beanManager.lookupBean(ActivityBeansCache.class).getInstance();

        final PlaceManager placeManager = beanManager.lookupBean(PlaceManager.class).getInstance();
        final ActivityManager activityManager = beanManager.lookupBean(ActivityManager.class).getInstance();

        final PerspectiveActivity activity = registerPerspectiveActivity(registryEntry, placeManager, activityManager, beanManager);

        components.put(componentId, registryEntry.getSource());
        activityBeansCache.addNewPerspectiveActivity(beanManager.lookupBeans(activity.getIdentifier()).iterator().next());
    }

    private PerspectiveActivity registerPerspectiveActivity(final AppFormerComponentsRegistry.Entry registryEntry,
                                                            final PlaceManager placeManager,
                                                            final ActivityManager activityManager,
                                                            final SyncBeanManager beanManager) {

        final JsWorkbenchLazyPerspectiveActivity activity = new JsWorkbenchLazyPerspectiveActivity(registryEntry,
                                                                                                   placeManager,
                                                                                                   activityManager,
                                                                                                   this::loadScriptFor);

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

        //FIXME: For some reason, using the injected activityBeansCache yields an error saying that the perspective has already been registered.
        beanManager.registerBean(activityBean);
        beanManager.registerBeanTypeAlias(activityBean, PerspectiveActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, JsWorkbenchLazyActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, Activity.class);

        return activity;
    }

    @Qualifier
    public @interface Shadowed {

    }

    @Inject
    @Shadowed
    private Instance<JsWorkbenchEditorActivity> jsWorkbenchEditorActivityInstance;

    @SuppressWarnings("unchecked")
    private void registerEditor(final Object jsObject,
                                final String componentId) {

        final JsNativeEditor editor = new JsNativeEditor(componentId, jsObject);

        final SyncBeanManager beanManager = IOC.getBeanManager();
        final EditorActivityBeanDefinition activityBean = new EditorActivityBeanDefinition<>(
                () -> this.jsWorkbenchEditorActivityInstance.get().withEditor(new JsNativeEditor(componentId, jsObject))
        );

        beanManager.registerBean(activityBean);
        beanManager.registerBeanTypeAlias(activityBean, WorkbenchEditorActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, Activity.class);

        activityBeansCache.addNewEditorActivity(activityBean,
                                                editor.getPriority(),
                                                Arrays.asList(editor.getResourceTypes()));
    }
}
