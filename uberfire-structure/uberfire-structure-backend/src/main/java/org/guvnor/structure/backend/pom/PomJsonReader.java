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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.guvnor.structure.pom.DependencyType;
import org.guvnor.structure.pom.DynamicPomDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Paths;

public class PomJsonReader {

    private final Logger logger = LoggerFactory.getLogger(PomJsonReader.class);
    private String kieVersion;
    private JsonObject pomObject;

    public PomJsonReader(InputStream in) {
        try (JsonReader reader = Json.createReader(in)) {
            pomObject = reader.readObject();
        } catch (Exception e) {
            logger.error(e.getMessage(),
                         e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public PomJsonReader(String path,
                         String jsonName) {
        String jsonPath = path+jsonName;
        if (!Files.exists(Paths.get(jsonPath))) {
            throw new RuntimeException("no " + jsonName + " in the provided path :" + path);
        }
        InputStream fis = null;
        JsonReader reader = null;
        try {
            fis = new FileInputStream(jsonPath);
            reader = Json.createReader(fis);
            pomObject = reader.readObject();
            reader.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    logger.warn(ex.getMessage(),
                                ex);
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    public ConfigurationMap readConfiguration() {
        kieVersion = pomObject.get("kieVersion").toString();
        JsonArray dependencies = pomObject.getJsonArray("dependencies");
        Map<DependencyType, List<DynamicPomDependency>> mapping = new HashMap<>(dependencies.size());
        for (int i = 0; i < dependencies.size(); i++) {
            JsonObject depType = dependencies.getJsonObject(i);
            String type = depType.getString("type");
            JsonArray deps = depType.getJsonArray("deps");
            ArrayList<DynamicPomDependency> dynamic = new ArrayList<>(deps.size());
            for (int k = 0; k < deps.size(); k++) {
                JsonObject dep = deps.getJsonObject(i);
                DynamicPomDependency dynamicDep = new DynamicPomDependency(
                        dep.getString("groupId"),
                        dep.getString("artifactId"),
                        dep.getString("version"),
                        dep.getString("scope")
                );
                dynamic.add(dynamicDep);
            }

            mapping.put(DependencyType.valueOf(type),
                        dynamic);
        }
        return new ConfigurationMap(mapping, kieVersion);
    }

}
