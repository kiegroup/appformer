/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.guvnor.structure.backend.pom;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapperDependencyTypes {

    private static final Logger logger = LoggerFactory.getLogger(MapperDependencyTypes.class);
    private final String MAPPING_TYPE_FILE = "MappingDependencies.properties";
    private Properties properties;
    private Map<DependencyType, DynamicPomDependency> mapping;

    public MapperDependencyTypes() {
        properties = loadProperties(MAPPING_TYPE_FILE);
        mapping = loadMapping(properties);
    }

    public Properties getMapperProperties() {
        return loadProperties(MAPPING_TYPE_FILE);
    }

    public Map<DependencyType, DynamicPomDependency> loadMapping(Properties props) {
        Map<DependencyType, DynamicPomDependency> mapping = new HashMap<>(props.size());
        for (Object key : props.keySet()) {
            String dep = props.getProperty(key.toString());
            StringTokenizer st = new StringTokenizer(dep, ":");
            int tokens = st.countTokens();
            while (st.hasMoreElements()) {
                String groupID = st.nextElement().toString();
                String artifactID = st.nextElement().toString();
                String versionID = st.nextElement().toString();
                String scope = "";
                if(tokens == 4) {
                    scope = st.nextElement().toString();
                }
                mapping.put(DependencyType.valueOf(key.toString()),
                            new DynamicPomDependency(groupID,
                                                     artifactID,
                                                     versionID, scope));
            }
        }
        return mapping;
    }

    public Map<DependencyType, DynamicPomDependency> getMapping() {
        return mapping;
    }

    public DynamicPomDependency getDependency(DependencyType key) {
        return mapping.get(key);
    }

    private Properties loadProperties(String propName) {
        Properties prop = new Properties();
        try (InputStream in = MapperDependencyTypes.class.getClassLoader().getResourceAsStream(propName)) {
            if (in == null) {
                logger.info("{} not available with the classloader no Mapping dependencies typeFound . \n",
                            propName);
            } else {
                prop.load(in);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return prop;
    }
}
