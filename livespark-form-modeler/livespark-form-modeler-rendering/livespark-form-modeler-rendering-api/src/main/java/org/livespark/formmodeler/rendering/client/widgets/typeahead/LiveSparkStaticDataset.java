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

package org.livespark.formmodeler.rendering.client.widgets.typeahead;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Suggestion;
import org.gwtbootstrap3.extras.typeahead.client.base.SuggestionCallback;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.kie.workbench.common.forms.common.rendering.client.util.masks.ClientMaskInterpreter;
import org.kie.workbench.common.forms.commons.rendering.shared.util.masks.MaskInterpreter;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.shared.query.MaskQueryCriteria;

public class LiveSparkStaticDataset<T> extends Dataset<T> {

    protected Class<? extends LiveSparkRestService<T>> restServiceClass;

    protected MaskInterpreter<T> maskInterpreter;

    public LiveSparkStaticDataset( String mask, Class<? extends LiveSparkRestService<T>> restServiceClass ) {
        Assert.notNull( "Mask cannot be null", mask );
        Assert.notNull( "RestServiceClass cannot be null", restServiceClass );

        this.maskInterpreter = new ClientMaskInterpreter<T>( mask );
        this.restServiceClass = restServiceClass;;
    }

    @Override
    public void findMatches( final String query, final SuggestionCallback<T> callback ) {
        RestClient.create( restServiceClass,
                           (RemoteCallback<List<T>>) models -> {

                               final List<Suggestion<T>> result = new ArrayList<>();

                               for ( T model : models ) {
                                   String suggestion = maskInterpreter.render( model );

                                   result.add( Suggestion.create( suggestion, model, this ) );
                               }
                               callback.execute( result );
                           } ).list( new MaskQueryCriteria( maskInterpreter.getMask(), query ) );
    }
}
