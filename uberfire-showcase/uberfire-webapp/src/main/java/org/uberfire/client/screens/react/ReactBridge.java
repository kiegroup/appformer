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

package org.uberfire.client.screens.react;

import java.util.Arrays;
import java.util.HashSet;

import javax.annotation.PostConstruct;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.uberfire.client.exporter.SingletonBeanDef;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityBeansCache;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.WorkbenchScreenActivity;

import static org.jboss.errai.ioc.client.QualifierUtil.DEFAULT_QUALIFIERS;

@EntryPoint
public class ReactBridge {

    @PostConstruct
    public void init() {

        exposeBridgeRegistrar();

        //FIXME: Not ideal to load scripts here. Make it lazy.
        if (!appformerJsIsAvailable()) {
            ScriptInjector.fromString(ReactJsExamplesBundle.INSTANCE.source().getText())
                    .setWindow(ScriptInjector.TOP_WINDOW)
                    .inject();
        }
    }

    private native void exposeBridgeRegistrar() /*-{
        $wnd.appformerBridge = {
            registerScreen: this.@org.uberfire.client.screens.react.ReactBridge::registerScreen(Ljava/lang/Object;)
        };
    }-*/;

    private native boolean appformerJsIsAvailable() /*-{
        return $wnd.appformer !== undefined;
    }-*/;

    @SuppressWarnings("unchecked")
    public void registerScreen(final Object jsObject) {

        final SyncBeanManager beanManager = IOC.getBeanManager();
        final ActivityBeansCache activityBeansCache = beanManager.lookupBean(ActivityBeansCache.class).getInstance();
        final JsNativeScreen newScreen = new JsNativeScreen((JavaScriptObject) jsObject);
        final String screenId = newScreen.get("id");

        final SingletonBeanDef<JsWorkbenchScreenActivity, JsWorkbenchScreenActivity> activityBean = new SingletonBeanDef<>(
                new JsWorkbenchScreenActivity(newScreen, beanManager.lookupBean(PlaceManager.class).getInstance()),
                JsWorkbenchScreenActivity.class,
                new HashSet<>(Arrays.asList(DEFAULT_QUALIFIERS)),
                screenId,
                true,
                WorkbenchScreenActivity.class,
                Activity.class);

        beanManager.registerBean(activityBean);
        beanManager.registerBeanTypeAlias(activityBean, WorkbenchScreenActivity.class);
        beanManager.registerBeanTypeAlias(activityBean, Activity.class);

        activityBeansCache.addNewScreenActivity(beanManager.lookupBeans(screenId).iterator().next());
    }
}
