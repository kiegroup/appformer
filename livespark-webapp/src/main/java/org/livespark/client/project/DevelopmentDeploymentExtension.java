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

package org.livespark.client.project;

import java.util.Collection;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.screens.projecteditor.client.editor.extension.BuildOptionExtension;
import org.livespark.client.shared.GwtWarBuildService;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

@ApplicationScoped
public class DevelopmentDeploymentExtension implements BuildOptionExtension {

    private static final String LINK_NAME = "Dev Mode Deploy";

    @Inject
    private Caller<GwtWarBuildService> buildCaller;

    @Override
    public Collection<Widget> getBuildOptions( Project project ) {
        return Collections.singleton( createNavLink( project ) );
    }

    private Widget createNavLink( Project project ) {
        final NavLink link = new NavLink( LINK_NAME );

        link.addClickHandler( createClickHandler( project ) );

        return link;
    }

    private ClickHandler createClickHandler( final Project project ) {
        return new ClickHandler() {

            @Override
            public void onClick( ClickEvent event ) {
                buildCaller.call().buildAndDeployDevMode( project );
            }
        };
    }

}
