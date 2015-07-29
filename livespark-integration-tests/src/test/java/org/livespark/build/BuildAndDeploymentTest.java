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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.guvnor.common.services.project.model.Project;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.livespark.backend.server.service.build.BuildAndDeployWithCodeServerCallable;
import org.livespark.backend.server.service.build.BuildCallableFactory;
import org.livespark.client.shared.AppReady;
import org.livespark.client.shared.GwtWarBuildService;
import org.livespark.formmodeler.codegen.SourceGenerationContext;
import org.livespark.test.BaseIntegrationTest;
import org.livespark.test.mock.MockHttpSession;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.java.nio.file.Path;

@RunWith( Arquillian.class )
public class BuildAndDeploymentTest extends BaseIntegrationTest {

    private static final String DATA_OBJECT_NAME = "Foobar";
    // Currently this is the only package where building will work because it is where the App.gwt.xml lives.
    private static final String PACKAGE = "demo";
    private static final String PACKAGE_PATH = PACKAGE.replace( '.', '/' );
    private static final File DEPLOY_DIR = new File( "target/wildfly-8.1.0.Final/standalone/deployments/" );

    private static final Queue<AppReady> observedEvents = new ConcurrentLinkedQueue<AppReady>();
    private static Message message;

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


    private void removeDoDeployedMarkerFiles() {
        for ( final File deployMarker : getWithSuffix( DEPLOY_DIR, ".deployed" ) ) {
            deployMarker.delete();
        }
    }

    private void prepareDataObject() {
        final org.uberfire.java.nio.file.Path sharedPath = makePath( getSrcMainPackageHelper( project, "/" + PACKAGE_PATH + "/client/shared" ), "" );
        final Path dataObjectPath = makePath( sharedPath.toUri().toString(), DATA_OBJECT_NAME + ".java" );
        final Path dataModelPath = makePath( sharedPath.toUri().toString(), DATA_OBJECT_NAME + SourceGenerationContext.FORM_MODEL_SUFFIX + ".java" );
        maybeCreateDataObject( Paths.convert( sharedPath ), DATA_OBJECT_NAME );

        final DataModel dataModel = dataModelerService.loadModel( (KieProject) project );
        final DataObject dataObject = dataModel.getDataObject( PACKAGE + ".client.shared." + DATA_OBJECT_NAME );
        dataObject.addProperty( "strVal", String.class.getCanonicalName() );
        dataObject.addProperty( "intVal", int.class.getCanonicalName() );
        dataObject.addProperty( "dateVal", Date.class.getCanonicalName() );
        dataObject.addProperty( "bigIntVal", BigInteger.class.getCanonicalName() );
        dataObject.addProperty( "byteVal", byte.class.getCanonicalName() );
        dataObject.addProperty( "charVal", char.class.getCanonicalName() );
        dataObject.addProperty( "boolVal", boolean.class.getCanonicalName() );
        dataObject.addProperty( "doubleVal", double.class.getCanonicalName() );
        dataObject.addProperty( "floatVal", float.class.getCanonicalName() );
        dataObject.addProperty( "longVal", long.class.getCanonicalName() );
        dataObject.addProperty( "shortVal", short.class.getCanonicalName() );
        dataObject.addProperty( "bigDecVal", BigDecimal.class.getCanonicalName() );

        updateDataObject( dataObject, dataObjectPath );

        // Want to make sure the model has been written before we start a test
        runAssertions( new Runnable() {
            @Override
            public void run() {
                assertTrue( "Precondition failed: tests require generated source from updated data object.", ioService.exists( dataModelPath ) );
            }
        }, 20, 1000 );
    }

    private void prepareFields() {
        observedEvents.clear();
        project = getProject();
    }

    private File[] getWithSuffix( final File parent, final String suffix ) {
        assertTrue( parent.isDirectory() );
        return parent.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.endsWith( suffix );
            }
        } );
    }

    /**
     * This method calls unbound on all {@link HttpSessionBindingListener HttpSessionBindingListeners} in the session returned by {@link RpcContext#getHttpSession()}.
     */
    private void simulateSessionExpiration() {
        final MockHttpSession httpSession = (MockHttpSession) RpcContext.getHttpSession();
        for ( final Entry<String, Object> entry : httpSession.getAllAttributes() ) {
            if ( entry.getValue() instanceof HttpSessionBindingListener ) {
                final HttpSessionBindingListener listener = (HttpSessionBindingListener) entry.getValue();
                final HttpSessionBindingEvent event = new HttpSessionBindingEvent( httpSession, entry.getKey(), entry.getValue() );
                listener.valueUnbound( event );
            }
        }
        httpSession.clearAllAttributes();
    }

    private void assertCodeServerIsDown( final URL codeServerUrl ) throws AssertionError {
        final HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) codeServerUrl.openConnection();
        } catch ( IOException e ) {
            throw new AssertionError( "Opening connection failed (this should succeed even if the codeserver is down).", e );
        }

        try {
            connection.connect();
            throw new AssertionError( "The codeserver is still running after session expiration." );
        } catch ( IOException ignore ) {
        } finally {
            try {
                connection.disconnect();
            } catch ( Exception ignore ) {
            }
        }
    }

    private void assertCodeServerIsActive( final URL codeServerUrl ) throws AssertionError {
        try {
            final HttpURLConnection connection = (HttpURLConnection) codeServerUrl.openConnection();
            connection.connect();
            connection.disconnect();
        } catch ( IOException e ) {
            throw new AssertionError( "Code server was not properly launched.", e );
        }
    }

    /**
     * This should only be run before the first test in the sequence.
     */
    private void prepareInitialTest() {
        prepareServiceTest();
        message = RpcContext.getMessage();
        prepareFields();
        removeDoDeployedMarkerFiles();
        prepareDataObject();
    }

    @Before
    public void setup() {
        prepareFields();
        /*
         * The message stored in the RpcContext is stored in a thread-local, and tests are not guaranteed to run on the same thread.
         */
        if ( message != null ) {
            RpcContext.set( message );
        }
    }

    @Test
    @InSequence(1)
    public void productionCompileAndDeploymentFiresAppReadyEvent() throws Exception {
        prepareInitialTest();
        assertEquals( "Precondition failed: There should be no observed AppReady events before building.", 0, observedEvents.size() );

        buildService.buildAndDeploy( project );

        runAssertions( new Runnable() {
            @Override
            public void run() {
                /*
                 * FIXME Should check that exactly one event is fired. Currently file change observers are not cleaned up so multiple events may be fired.
                 */
                assertNotEquals( "There should be exactly one AppReady event observed.", 0, observedEvents.size() );
            }
        }, 60, 2000, 5000 );
    }

    @Test
    @InSequence(2)
    public void sessionExpirationRemovesWarFileFromProductionCompile() throws Exception {
        try {
            assertEquals( 1, getWithSuffix( DEPLOY_DIR, ".war" ).length );
        } catch ( AssertionError e ) {
            throw new AssertionError( "Precondition failed.", e );
        }

        simulateSessionExpiration();
        assertEquals( 0, getWithSuffix( DEPLOY_DIR, ".war" ).length );
    }

    @Test
    @InSequence(3)
    public void devModeDeploymentFiresAppReadyEvent() throws Exception {
        assertEquals( "Precondition failed: There should be no observed AppReady events before building.", 0, observedEvents.size() );

        buildService.buildAndDeployDevMode( project );

        runAssertions( new Runnable() {
            @Override
            public void run() {
                /*
                 * FIXME Should check that exactly one event is fired. Currently file change observers are not cleaned up so multiple events may be fired.
                 */
                assertNotEquals( "There should be exactly one AppReady event observed.", 0, observedEvents.size() );
            }
        }, 60, 2000, 5000 );
    }

    @Test
    @InSequence(4)
    public void sessionExpirationAfterDevModeCompileRemovesWarAndShutsDownCodeServer() throws Exception {
        final URL codeServerUrl;
        try {
            final MockHttpSession httpSession = (MockHttpSession) RpcContext.getHttpSession();
            final BuildAndDeployWithCodeServerCallable callable =
                    (BuildAndDeployWithCodeServerCallable) httpSession.getAttribute( BuildCallableFactory.CODE_SERVER_CALLABLE_ATTR_KEY );
            codeServerUrl = new URL( "http", "localhost", callable.getCodeServerPort(), "" );

            assertEquals( 1, getWithSuffix( DEPLOY_DIR, ".war" ).length );
            assertCodeServerIsActive( codeServerUrl );
        } catch ( AssertionError e ) {
            throw new AssertionError( "Precondition failed.", e );
        }

        simulateSessionExpiration();

        runAssertions( new Runnable() {
            @Override
            public void run() {
                assertEquals( 0, getWithSuffix( DEPLOY_DIR, ".war" ).length );
                assertCodeServerIsDown( codeServerUrl );
            }
        }, 10, 500 );

    }

}
