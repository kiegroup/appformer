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

package org.livespark.build;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Project;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.livespark.client.shared.AppReady;
import org.livespark.client.shared.GwtWarBuildService;
import org.livespark.test.BaseIntegrationTest;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.java.nio.file.Path;
import org.uberfire.java.nio.file.attribute.FileTime;

@RunWith( Arquillian.class )
public class BuildAndDeploymentTest extends BaseIntegrationTest {

    private static final String DATA_OBJECT_NAME = "Foobar";
    private static final String PACKAGE = "buildtest";

    private static final Queue<AppReady> observedEvents = new ConcurrentLinkedQueue<AppReady>();

    /*
     * If this method is non-static, it is invoked on a different instance than the one running the tests, regardless of scopes.
     */
    public static void observeAppReadyEvent( @Observes AppReady appReady ) {
        observedEvents.add( appReady );
    }

    @Deployment
    public static WebArchive createDeployment() {
        return BaseIntegrationTest.createLiveSparkDeployment( BuildAndDeploymentTest.class.getSimpleName().toLowerCase() );
    }

    private Project project;


    @Inject
    private GwtWarBuildService buildService;

    @Before
    public void prepareForTest() {
        prepareServiceTest();
        observedEvents.clear();
        project = getProject();

        final org.uberfire.java.nio.file.Path sharedPath = makePath( getSrcMainPackageHelper( project, "/" + PACKAGE + "/client/shared" ), "" );
        final Path dataObjectPath = makePath( sharedPath.toUri().toString(), DATA_OBJECT_NAME + ".java" );
        maybeCreateDataObject( Paths.convert( sharedPath ), DATA_OBJECT_NAME );

        final DataModel dataModel = dataModelerService.loadModel( (KieProject) project );
        final DataObject dataObject = dataModel.getDataObject( PACKAGE + ".client.shared." + DATA_OBJECT_NAME );
        dataObject.addProperty( "str", "java.lang.String" );
        dataObject.addProperty( "num", "java.lang.Integer" );
        dataObject.addProperty( "biggerNum", "java.lang.Long" );
        dataObject.addProperty( "date", "java.util.Date" );

        final FileTime lastModified = ioService.getLastModifiedTime( dataObjectPath );
        updateDataObject( dataObject, dataObjectPath );

        // Want to make sure the model has been written before we start a test
        runAssertions( new Runnable() {
            @Override
            public void run() {
                assertNotEquals( "Precondition failed: tests require the updated data object to be saved.", lastModified, ioService.getLastModifiedTime( dataObjectPath ) );
            }
        }, 20, 1000 );
    }

    @Test
    public void testProductionCompileAndDeploymentFiresAppReadyEvent() throws Exception {
        assertEquals( "Precondition failed: There should be no observed AppReady events before building.", 0, observedEvents.size() );

        buildService.buildAndDeploy( project );

        runAssertions( new Runnable() {
            @Override
            public void run() {
                assertEquals( "There should be exactly one AppReady event observed.", 1, observedEvents.size() );
            }
        }, 60, 2000, 5000 );
    }

}
