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
package org.livespark.client.docks;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.common.services.shared.security.KieWorkbenchACL;
import org.kie.workbench.common.screens.datamodeller.client.DataModelerContext;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchContext;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchContextChangeEvent;
import org.kie.workbench.common.screens.datamodeller.client.context.DataModelerWorkbenchFocusEvent;
import org.uberfire.client.workbench.docks.UberfireDock;
import org.uberfire.client.workbench.docks.UberfireDockPosition;
import org.uberfire.client.workbench.docks.UberfireDockReadyEvent;
import org.uberfire.client.workbench.docks.UberfireDocks;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.rpc.SessionInfo;

@Dependent
public class AuthoringWorkbenchDocks {

    @Inject
    private UberfireDocks uberfireDocks;

    @Inject
    protected DataModelerWorkbenchContext dataModelerWBContext;

    private String authoringPerspectiveIdentifier;

    private UberfireDock projectExplorerDock;

    private boolean dataModelerIsHidden;

    private DataModelerContext lastActiveContext;

    @Inject
    private KieWorkbenchACL kieACL;

    @Inject
    private SessionInfo sessionInfo;

    private String currentPerspectiveIdentifier = null;

    private boolean dataModelerDocksEnabled = true;


    public void perspectiveChangeEvent( @Observes UberfireDockReadyEvent dockReadyEvent ) {
        currentPerspectiveIdentifier = dockReadyEvent.getCurrentPerspective();
        if ( authoringPerspectiveIdentifier != null && dockReadyEvent.getCurrentPerspective().equals( authoringPerspectiveIdentifier ) ) {
            if ( projectExplorerDock != null ) {
                uberfireDocks.expand( projectExplorerDock );
            }
        }
    }

    public void setup( String authoringPerspectiveIdentifier, PlaceRequest projectExplorerPlaceRequest ) {
                this.authoringPerspectiveIdentifier = authoringPerspectiveIdentifier;
        projectExplorerDock = new UberfireDock( UberfireDockPosition.WEST, "ADJUST", projectExplorerPlaceRequest, authoringPerspectiveIdentifier ).withSize( 400 ).withLabel( "Project Explorer" );
        uberfireDocks.add(
                projectExplorerDock,
                new UberfireDock( UberfireDockPosition.EAST, "RANDOM", new DefaultPlaceRequest( "DroolsDomainScreen" ), authoringPerspectiveIdentifier ).withSize( 450 ).withLabel( "Drools & jBPM" ),
                new UberfireDock( UberfireDockPosition.EAST, "BRIEFCASE", new DefaultPlaceRequest( "JPADomainScreen" ), authoringPerspectiveIdentifier ).withSize( 450 ).withLabel( "Persistence" ),
                new UberfireDock( UberfireDockPosition.EAST, "COG", new DefaultPlaceRequest( "AdvancedDomainScreen" ), authoringPerspectiveIdentifier ).withSize( 450 ).withLabel( "Advanced" )

        );
        uberfireDocks.disable( UberfireDockPosition.EAST, authoringPerspectiveIdentifier );
        dataModelerDocksEnabled = false;
    }

    public void onContextChange( @Observes DataModelerWorkbenchContextChangeEvent contextEvent ) {
        if ( isAuthoringActive() ) {
            handleDocks();
        }
    }

    private void handleDocks() {
        DataModelerContext context = dataModelerWBContext.getActiveContext();
        if ( !dataModelerIsHidden && shouldDisplayWestDocks( context ) && lastActiveContext != context ) {
            enableDocks( true );
            lastActiveContext = context;
        } else if ( dataModelerIsHidden || !shouldDisplayWestDocks( context ) ) {
            enableDocks( false );
            lastActiveContext = null;
        }
    }

    public void onDataModelerWorkbenchFocusEvent( @Observes DataModelerWorkbenchFocusEvent event ) {
        if ( isAuthoringActive() ) {
            if ( !event.isFocused() ) {
                this.dataModelerIsHidden = true;
                enableDocks( false );
            } else {
                this.dataModelerIsHidden = false;
                handleDocks();
            }
        }
    }

    private boolean isAuthoringActive() {
        return authoringPerspectiveIdentifier != null &&
                authoringPerspectiveIdentifier.equals( currentPerspectiveIdentifier );
    }

    private void enableDocks( boolean enabled ) {
        if ( enabled != dataModelerDocksEnabled ) {
            dataModelerDocksEnabled = enabled;
            if ( enabled ) {
                uberfireDocks.enable( UberfireDockPosition.EAST, authoringPerspectiveIdentifier );
            } else {
                uberfireDocks.disable( UberfireDockPosition.EAST, authoringPerspectiveIdentifier );
            }
        }
    }

    private boolean shouldDisplayWestDocks( DataModelerContext context ) {
        return context != null && context.getEditionMode() == DataModelerContext.EditionMode.GRAPHICAL_MODE;
    }
}
