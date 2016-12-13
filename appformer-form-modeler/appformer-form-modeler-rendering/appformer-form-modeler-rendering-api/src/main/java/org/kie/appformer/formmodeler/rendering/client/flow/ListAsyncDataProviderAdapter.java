/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.formmodeler.rendering.client.flow;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.view.client.HasData;

public class ListAsyncDataProviderAdapter<MODEL> extends FlowDataProvider<MODEL> {

    private final List<MODEL> list;

    public ListAsyncDataProviderAdapter(final List<MODEL> list) {
        this.list = list;
    }

    @Override
    protected void onRangeChanged( final HasData<MODEL> display ) {
        if ( list != null ) {
            updateRowCount( list.size(), true );
            updateRowData( 0, list );
        } else {
            updateRowCount( 0, true );
            updateRowData( 0, new ArrayList<>() );
        }
    }

    @Override
    public MODEL getRowData( final int index ) {
        return list.get( index );
    }

    @Override
    public void clearCache() {
    }

}
