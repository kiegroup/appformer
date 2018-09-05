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
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.guvnor.common.services.project.model.GAV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It reads all the jars present in the WEB-INF/lib
 * to create a Map with entries of GAV and path of the dependency
 * */
@WebListener
public class M2ServletContextListener implements ServletContextListener {

    private final String JAR_EXT = ".jar";
    private final String WEB_INF_FOLDER = "WEB-INF";
    private final String LIB_FOLDER = "lib";
    private final String CTX_JARS = "CTX_JARS";
    private Logger logger = LoggerFactory.getLogger(M2ServletContextListener.class);

    @Inject
    private GuvnorM2Repository repository;


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext ctx = servletContextEvent.getServletContext();
        String jarsPath = ctx.getRealPath(getJarsFolder());
        deployJarFromWar(jarsPath, ctx);
        logger.info("M2ServletContextListener contextInitialized");
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

    public void deployJarFromWar(String path, ServletContext ctx) {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("file://" + path))) {
            for (Path p : ds) {
                if (!Files.isDirectory(p) && p.endsWith(JAR_EXT)) {
                    GAV gav = repository.loadGAVFromJar(p.toAbsolutePath().toString());
                    repository.deployPom(ctx.getResourceAsStream(p.toAbsolutePath().toString()), gav);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
