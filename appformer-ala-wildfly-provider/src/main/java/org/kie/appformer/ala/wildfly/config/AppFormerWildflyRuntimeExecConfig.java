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

    public AppFormerWildflyRuntimeExecConfig( ) {
    }

    public AppFormerWildflyRuntimeExecConfig( ProviderId providerId,
                                              String warPath,
                                              String jndiDataSource,
                                              String kieDataSource ) {
        super( providerId, warPath );
        this.jndiDataSource = jndiDataSource;
        this.kieDataSource = kieDataSource;
    }

    @Override
    public WildflyRuntimeExecConfig asNewClone( WildflyRuntimeExecConfig origin ) {
        return new AppFormerWildflyRuntimeExecConfig( origin.getProviderId(),
                origin.getWarPath(), getJndiDataSource(), getKieDataSource() );
    }

    @Override
    public void setContext( Map< String, ? > context ) {
        Input input = ( Input ) context.get( "input" );
        if ( input != null ) {
            jndiDataSource = input.get( "jndi-data-source" );
            kieDataSource = input.get( "kie-data-source" );
        }
        super.setContext( context );
    }

    public String getJndiDataSource( ) {
        return jndiDataSource;
    }

    public String getKieDataSource( ) {
        return kieDataSource;
    }
}