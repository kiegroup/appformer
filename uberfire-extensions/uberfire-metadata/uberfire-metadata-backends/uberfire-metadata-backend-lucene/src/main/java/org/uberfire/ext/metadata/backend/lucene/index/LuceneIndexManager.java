/*
 * Copyright 2015 JBoss, by Red Hat, Inc
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

package org.uberfire.ext.metadata.backend.lucene.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.uberfire.ext.metadata.backend.lucene.model.KClusterImpl;
import org.uberfire.ext.metadata.engine.Index;
import org.uberfire.ext.metadata.engine.IndexManager;
import org.uberfire.ext.metadata.model.KCluster;
import org.uberfire.ext.metadata.model.KObjectKey;
import org.uberfire.ext.metadata.search.ClusterSegment;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

public class LuceneIndexManager implements IndexManager {

    private final LuceneIndexFactory factory;
    private final Map<KCluster, LuceneIndex> indexes = new ConcurrentHashMap<KCluster, LuceneIndex>();

    public LuceneIndexManager(final LuceneIndexFactory factory) {
        this.factory = checkNotNull("factory",
                                    factory);
        this.indexes.putAll(factory.getIndexes());
    }

    @Override
    public boolean contains(final KCluster cluster) {
        return indexes.containsKey(cluster);
    }

    @Override
    public synchronized LuceneIndex indexOf(final KObjectKey object) {
        final KCluster kcluster = kcluster(object);
        final LuceneIndex currentSetup = indexes.get(kcluster);
        if (currentSetup != null) {
            return currentSetup;
        }

        final LuceneIndex index = factory.newCluster(kcluster);
        indexes.put(kcluster,
                    index);
        return index;
    }

    @Override
    public KCluster kcluster(final KObjectKey object) {
        return new KClusterImpl(object.getClusterId());
    }

    @Override
    public void delete(KCluster cluster) {
        final LuceneIndex setup = indexes.remove(cluster);
        factory.remove(cluster);
        if (setup != null) {
            setup.delete();
        } else {
            deleteProjectIndexes(cluster);
        }
    }

    protected void deleteProjectIndexes(KCluster cluster) {
        final List<KCluster> clusters = indexes.keySet().stream().parallel()
                .filter(s -> s.getClusterId().startsWith(cluster.getClusterId()))
                .collect(Collectors.toList());
        clusters.forEach(cl -> {
            final LuceneIndex setup = indexes.remove(cl);
            factory.remove(cl);
            if (setup != null) {
                setup.delete();
            }
        });
    }

    @Override
    public void dispose() {
        for (final LuceneIndex index : indexes.values()) {
            index.dispose();
        }
        factory.dispose();
    }

    @Override
    public Index get(KCluster cluster) {
        return indexes.get(cluster);
    }

    public IndexSearcher getIndexSearcher(final ClusterSegment... clusterSegments) {
        final Set<KCluster> clusters;
        if (clusterSegments == null || clusterSegments.length == 0) {
            clusters = new HashSet<KCluster>(indexes.keySet());
        } else {
            clusters = new HashSet<KCluster>(clusterSegments.length);
            for (final ClusterSegment clusterSegment : clusterSegments) {
                clusters.add(new KClusterImpl(clusterSegment.getClusterId()));
            }
        }

        final Collection<IndexReader> readers = new ArrayList<IndexReader>(clusters.size());
        for (final KCluster cluster : clusters) {
            final LuceneIndex index = indexes.get(cluster);
            readers.add(index.nrtReader());
        }

        try {
            return new SearcherFactory().newSearcher(new MultiReader(readers.toArray(new IndexReader[readers.size()])),
                                                     null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void release(final IndexSearcher index) {
        try {
            index.getIndexReader().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getIndices() {
        return indexes.keySet().stream().map(kCluster -> kCluster.getClusterId()).collect(Collectors.toList());
    }
}
