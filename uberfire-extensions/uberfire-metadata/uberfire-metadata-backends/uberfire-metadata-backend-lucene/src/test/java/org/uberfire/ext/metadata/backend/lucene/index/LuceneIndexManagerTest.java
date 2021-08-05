package org.uberfire.ext.metadata.backend.lucene.index;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.ext.metadata.model.KCluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(MockitoJUnitRunner.class)
public class LuceneIndexManagerTest {

    @Mock
    LuceneIndexFactory factory;

    LuceneIndexManager luceneIndexManager;

    @Before
    public void setup() {
        luceneIndexManager = Mockito.spy(new LuceneIndexManager(factory));
    }

    @Test
    public void testDelete() {
        Map<KCluster, LuceneIndex> mockMap = Mockito.mock(ConcurrentHashMap.class);
        LuceneIndex index = Mockito.mock(LuceneIndex.class);
        luceneIndexManager.delete(Mockito.mock(KCluster.class));
        Mockito.verify(factory).remove(Mockito.any(KCluster.class));
    }

    @Test
    public void testProjectDelete() {
        Map<KCluster, LuceneIndex> mockMap = Mockito.mock(ConcurrentHashMap.class);
        luceneIndexManager.delete(Mockito.mock(KCluster.class));
        Mockito.verify(factory).remove(Mockito.any(KCluster.class));
        Mockito.verify(luceneIndexManager).deleteProjectIndexes(Mockito.any(KCluster.class));
    }
}
