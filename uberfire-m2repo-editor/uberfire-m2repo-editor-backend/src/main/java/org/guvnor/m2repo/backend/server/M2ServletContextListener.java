/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.guvnor.m2repo.backend.server;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.guvnor.common.services.project.model.GAV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It reads all the jars present in the WEB-INF/lib
 * to create a Map with entries of GAV and path of the dependency
 * */
public class M2ServletContextListener implements ServletContextListener {

    private final String JAR_EXT = ".jar";
    private final String WEB_INF_FOLDER = "WEB-INF";
    private final String LIB_FOLDER = "lib";
    private final String CTX_JARS = "CTX_JARS";
    private Logger logger = LoggerFactory.getLogger(M2ServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("M2ServletContextListener contextInitialized");
        ServletContext ctx = servletContextEvent.getServletContext();
        String jarsPath = ctx.getRealPath(getJarsFolder());
        ctx.setAttribute(CTX_JARS, searchJars(jarsPath));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().removeAttribute(CTX_JARS);
    }

    private String getJarsFolder(){
        StringBuilder sb = new StringBuilder();
        sb.append(File.separator).append(WEB_INF_FOLDER).append(File.separator).append(LIB_FOLDER);
        return sb.toString();
    }

    public Map<GAV, String> searchJars(String path) {
        Map<GAV, String> jars = new HashMap<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("file://" + path))) {
            GuvnorM2Repository guvnorM2Repository = new GuvnorM2Repository();
            for (Path p : ds) {
                if (!Files.isDirectory(p) && p.endsWith(JAR_EXT)) {
                    jars.put(guvnorM2Repository.loadGAVFromJar(p.toAbsolutePath().toString()), p.toAbsolutePath().toString());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("M2ServletContextListener, prepared WarRepo with {} jars dependencies:", jars.size());
        return jars;
    }
}
