/*
 * Copyright 2015 JBoss Inc
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
package org.livespark.backend.server.service.build;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMonitorHandle implements HttpSessionBindingListener {

    private static final Logger logger = LoggerFactory.getLogger( FileMonitorHandle.class );

    private final FileAlterationMonitor monitor;
    private final String fileName;

    public FileMonitorHandle( final FileAlterationMonitor monitor, final String fileName ) {
        this.monitor = monitor;
        this.fileName = fileName;
    }

    @Override
    public void valueBound( HttpSessionBindingEvent event ) {
    }

    @Override
    public void valueUnbound( HttpSessionBindingEvent event ) {
        try {
            monitor.stop();
        } catch ( Exception e ) {
            logger.error( "An error occurred while stopping the FileAlterationMonitor for " + fileName, e );
        }
    }

}
