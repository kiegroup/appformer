/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.ala.wildfly.config;

import java.util.Map;

import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.runtime.providers.ProviderId;
import org.guvnor.ala.wildfly.config.WildflyRuntimeExecConfig;
import org.guvnor.ala.wildfly.config.impl.ContextAwareWildflyRuntimeExecConfig;

public class AppFormerWildflyRuntimeExecConfig
        extends ContextAwareWildflyRuntimeExecConfig {

    private String jndiDataSource;

    private String kieDataSource;

    private String kieDataSourceDeploymentId;

    private String realm;

    public AppFormerWildflyRuntimeExecConfig( ) {
    }

    public AppFormerWildflyRuntimeExecConfig( final ProviderId providerId,
                                              final String warPath,
                                              final String redeployStrategy,
                                              final String jndiDataSource,
                                              final String kieDataSource,
                                              final String kieDataSourceDeploymentId,
                                              final String realm ) {
        super( null, providerId, warPath, redeployStrategy );
        this.jndiDataSource = jndiDataSource;
        this.kieDataSource = kieDataSource;
        this.kieDataSourceDeploymentId = kieDataSourceDeploymentId;
        this.realm = realm;
    }

    @Override
    public WildflyRuntimeExecConfig asNewClone( final WildflyRuntimeExecConfig origin ) {
        return new AppFormerWildflyRuntimeExecConfig( origin.getProviderId( ),
                origin.getWarPath( ),
                getRedeployStrategy(),
                getJndiDataSource( ),
                getKieDataSource( ),
                getKieDataSourceDeploymentId( ),
                getRealm() );
    }

    @Override
    public void setContext( Map< String, ? > context ) {
        Input input = ( Input ) context.get( "input" );
        if ( input != null ) {
            jndiDataSource = input.get( "jndi-data-source" );
            kieDataSource = input.get( "kie-data-source" );
            kieDataSourceDeploymentId = input.get( "kie-data-source-deployment-id" );
            realm = input.get( "wildfly-realm" );
        }
        super.setContext( context );
    }

    public String getJndiDataSource( ) {
        return jndiDataSource;
    }

    public String getKieDataSource( ) {
        return kieDataSource;
    }

    public String getKieDataSourceDeploymentId( ) {
        return kieDataSourceDeploymentId;
    }

    public String getRealm( ) {
        return realm;
    }
}