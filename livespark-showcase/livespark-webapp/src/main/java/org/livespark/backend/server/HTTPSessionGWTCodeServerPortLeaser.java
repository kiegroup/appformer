/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.backend.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.servlet.http.HttpSession;
import org.guvnor.ala.build.maven.executor.gwt.GWTCodeServerPortLeaserImpl;
import org.jboss.errai.bus.server.api.RpcContext;

@ApplicationScoped
@Specializes
public class HTTPSessionGWTCodeServerPortLeaser extends GWTCodeServerPortLeaserImpl {

  
    private final HttpSession session;
    private final Map<String, HttpSession> codeServerBySession = new ConcurrentHashMap<>();
    
    public HTTPSessionGWTCodeServerPortLeaser() {
        session = RpcContext.getHttpSession();
    }

    @Override
    public boolean isCodeServerRunning(String projectName) {
        return codeServerBySession.containsKey(projectName);
    }

    @Override
    public void setCodeServerForProject(String projectName, Integer portNumber) {
        codeServerByProject.put(projectName, portNumber);
        codeServerBySession.put(projectName, session);
        
    }
    
    
}
