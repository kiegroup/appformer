/*
 * Copyright 2012 JBoss Inc
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

package org.livespark.backend.server.startup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.server.config.ConfigurationService;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;
import org.uberfire.io.IOService;

//This is a temporary solution when running in PROD-MODE as /webapp/.niogit/system.git folder
//is not deployed to the Application Servers /bin folder. This will be remedied when an
//installer is written to create the system.git repository in the correct location.
@Startup(StartupType.BOOTSTRAP)
@ApplicationScoped
public class AppSetup {

    private static final Logger logger = LoggerFactory.getLogger( AppSetup.class );

    private static final String DROOLS_WB_ORGANIZATIONAL_UNIT1 = "demo";
    private static final String DROOLS_WB_ORGANIZATIONAL_UNIT1_OWNER = "demo@drools.org";

    private static final String LIVE_SPARK_PLAYGROUND_SCHEME = "git";
    private static final String LIVE_SPARK_PLAYGROUND_ALIAS = "ls-playground";
    private static final String LIVE_SPARK_PLAYGROUND_ORIGIN = "https://github.com/csadilek/test-playground";
    private static final String LIVE_SPARK_PLAYGROUND_UID = "";
    private static final String LIVE_SPARK_PLAYGROUND_PWD = "";

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private OrganizationalUnitService organizationalUnitService;

    @Inject
    private RepositoryService repositoryService;

    @Inject
    private KieProjectService projectService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
	private DataModelerService dataModelerService;

    @PostConstruct
    public void assertPlayground() {
        try {
            configurationService.startBatch();

            loadLiveSparkExamples();

        } catch ( final Exception e ) {
            logger.error( "Error during live spark demo repositories configuration", e );
            throw new RuntimeException( e );
        } finally {
            configurationService.endBatch();
        }

    }

    private void loadLiveSparkExamples() {

        Repository repository = createRepository(
                LIVE_SPARK_PLAYGROUND_ALIAS,
                LIVE_SPARK_PLAYGROUND_SCHEME,
                LIVE_SPARK_PLAYGROUND_ORIGIN,
                LIVE_SPARK_PLAYGROUND_UID,
                LIVE_SPARK_PLAYGROUND_PWD );

        OrganizationalUnit defaultOU = organizationalUnitService.getOrganizationalUnit( DROOLS_WB_ORGANIZATIONAL_UNIT1 );
        if ( defaultOU == null ) {
            createOU( repository,
                    DROOLS_WB_ORGANIZATIONAL_UNIT1,
                    DROOLS_WB_ORGANIZATIONAL_UNIT1_OWNER );
        } else {
            organizationalUnitService.addRepository( defaultOU, repository );
        }

        Set<Project> projects = projectService.getProjects(repository, "master");

        for (Project p : projects) {
        	dataModelerService.loadModel((KieProject) p);
        }
    }

    private Repository createRepository( final String alias,
                                         final String scheme,
                                         final String origin,
                                         final String user,
                                         final String password ) {
        Repository repository = repositoryService.getRepository( alias );
        if ( repository == null ) {
            repository = repositoryService.createRepository( scheme,
                                                             alias,
                                                             new HashMap<String, Object>() {{
                                                                 if ( origin != null ) {
                                                                     put( "origin", origin );
                                                                 }
                                                                 put( "username", user );
                                                                 put( "crypt:password", password );
                                                             }} );
        }
        return repository;
    }

    private OrganizationalUnit createOU( final Repository repository,
                                         final String ouName,
                                         final String ouOwner ) {
        OrganizationalUnit ou = organizationalUnitService.getOrganizationalUnit( ouName );
        if ( ou == null ) {
            List<Repository> repositories = new ArrayList<Repository>();
            repositories.add( repository );
            organizationalUnitService.createOrganizationalUnit( ouName,
                                                                ouOwner,
                                                                null,
                                                                repositories );
        }
        return ou;
    }

}
