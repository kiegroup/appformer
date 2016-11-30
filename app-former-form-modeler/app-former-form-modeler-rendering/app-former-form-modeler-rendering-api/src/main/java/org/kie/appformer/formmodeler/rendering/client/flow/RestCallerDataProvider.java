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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.jboss.errai.common.client.api.Caller;
import org.kie.appformer.formmodeler.rendering.client.shared.AppFormerRestService;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class RestCallerDataProvider<M> extends FlowDataProvider<M> {

    private final Caller<? extends AppFormerRestService<M>> caller;
    SortedMap<Integer, M> cache = new TreeMap<>();
    Set<Range> inFlight = new HashSet<>();

    public RestCallerDataProvider(final Caller<? extends AppFormerRestService<M>> caller) {
        this.caller = caller;
    }

    @Override
    public M getRowData( final int index ) {
        final M data = cache.get( index );
        if ( data != null ) {
            return data;
        }
        else {
            throw new RuntimeException( "No cached value at index " + index );
        }
    }

    @Override
    protected void onRangeChanged( final HasData<M> display ) {
        final Range visibleRange = display.getVisibleRange();
        final SortedMap<Integer, M> rangeCache = getCacheForRange( visibleRange );
        if ( rangeCache.size() == visibleRange.getLength() ) {
            update(rangeCache);
        }
        else if ( !inFlight.contains( visibleRange ) ) {
            final int start = visibleRange.getStart();
            final int endExclusive = visibleRange.getStart() + visibleRange.getLength();
            caller
                .call( (final List<M> result) -> {
                    for (int i = 0; i < result.size(); i++) {
                        cache.put( start + i, result.get( i ) );
                    }
                    update( cache.subMap( start, endExclusive - 1 ) );
                    inFlight.remove( visibleRange );
                } )
                .load( start, endExclusive );
            inFlight.add( visibleRange );
        }
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    private void update(final SortedMap<Integer, M> cachedRange) {
        final List<M> list = cachedRange.values().stream().collect( Collectors.toList() );
        updateRowCount( estimateSize(), cache.isEmpty() );
        updateRowData( cachedRange.isEmpty() ? 0 : cachedRange.firstKey(), list );
    }

    private int estimateSize() {
        return cache.isEmpty() ? 0 : cache.lastKey() + 1;
    }

    private SortedMap<Integer, M> getCacheForRange( final Range range ) {
        return cache.subMap( range.getStart(), range.getStart() + range.getLength() );
    }

}
