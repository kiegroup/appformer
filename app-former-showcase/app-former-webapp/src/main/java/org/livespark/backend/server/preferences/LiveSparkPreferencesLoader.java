/*
 * Copyright 2016 JBoss Inc
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

package org.livespark.backend.server.preferences;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import org.guvnor.common.services.backend.preferences.ApplicationPreferencesLoader;

@ApplicationScoped
public class LiveSparkPreferencesLoader
        implements ApplicationPreferencesLoader {

    @Override
    public Map<String, String> load() {
        //enables some client side features that we currently don't won't to enable in all workbenches.
        Map<String, String> preferences = new HashMap<>(  );
        preferences.put( "persistence-descriptor-editor-options.integrate-datasources", "true" );
        preferences.put( "data-modeler-options.enable-data-object-audit", "true" );
        return preferences;
    }
}
