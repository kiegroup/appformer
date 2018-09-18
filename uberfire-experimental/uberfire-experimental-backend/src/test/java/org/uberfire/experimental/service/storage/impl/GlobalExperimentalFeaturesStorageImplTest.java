package org.uberfire.experimental.service.storage.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.experimental.service.events.PortableExperimentalFeatureModifiedEvent;
import org.uberfire.experimental.service.registry.impl.ExperimentalFeatureImpl;
import org.uberfire.java.nio.file.Path;
import org.uberfire.mocks.EventSourceMock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExperimentalFeaturesStorageImplTest extends AbstractExperimentalFeaturesStorageTest<GlobalExperimentalFeaturesStorageImpl> {

    @Mock
    private EventSourceMock<PortableExperimentalFeatureModifiedEvent> event;

    @Test
    public void testFirstLoad() {
        storage.init();

        verifyInit();

        verify(ioService, times(1)).exists(any());
        verify(ioService, times(1)).newOutputStream(any());
        verify(ioService, times(1)).startBatch(fileSystem);
        verify(ioService, times(1)).endBatch();

        verifyLoadedFeatures(new ArrayList<>(storage.getFeatures()), new ExperimentalFeatureImpl(GLOBAL_FEATURE_1, false), new ExperimentalFeatureImpl(GLOBAL_FEATURE_2, false), new ExperimentalFeatureImpl(GLOBAL_FEATURE_3, false));
    }

    @Test
    public void testRegularLoad() throws IOException {

        Path path = fileSystem.getPath(storage.getStoragePath());

        ioService.write(path, IOUtils.toString(getClass().getResourceAsStream("/test/global/regularFeatures.txt"), Charset.defaultCharset()));

        storage.init();

        verifyInit();

        verify(ioService, times(2)).exists(any());
        verify(ioService, times(1)).newInputStream(any());

        verify(ioService, never()).newOutputStream(any());
        verify(ioService, never()).startBatch(fileSystem);
        verify(ioService, never()).endBatch();

        verifyLoadedFeatures(new ArrayList<>(storage.getFeatures()), new ExperimentalFeatureImpl(GLOBAL_FEATURE_1, true), new ExperimentalFeatureImpl(GLOBAL_FEATURE_2, false), new ExperimentalFeatureImpl(GLOBAL_FEATURE_3, true));
    }

    @Test
    public void testExtraFeaturesLoad() throws IOException {
        Path path = fileSystem.getPath(storage.getStoragePath());

        ioService.write(path, IOUtils.toString(getClass().getResourceAsStream("/test/global/extraFeatures.txt"), Charset.defaultCharset()));

        storage.init();

        verifyInit();

        verify(ioService, times(2)).exists(any());
        verify(ioService, times(1)).newInputStream(any());

        verify(ioService, times(1)).newOutputStream(any());
        verify(ioService, times(1)).startBatch(fileSystem);
        verify(ioService, times(1)).endBatch();

        verifyLoadedFeatures(new ArrayList<>(storage.getFeatures()), new ExperimentalFeatureImpl(GLOBAL_FEATURE_1, true), new ExperimentalFeatureImpl(GLOBAL_FEATURE_2, false), new ExperimentalFeatureImpl(GLOBAL_FEATURE_3, true));
    }

    @Test
    public void testMissingFeaturesLoad() throws IOException {
        Path path = fileSystem.getPath(storage.getStoragePath());

        ioService.write(path, IOUtils.toString(getClass().getResourceAsStream("/test/global/missingFeatures.txt"), Charset.defaultCharset()));

        storage.init();

        verifyInit();

        verify(ioService, times(2)).exists(any());
        verify(ioService, times(1)).newInputStream(any());

        verify(ioService, times(1)).newOutputStream(any());
        verify(ioService, times(1)).startBatch(fileSystem);
        verify(ioService, times(1)).endBatch();

        verifyLoadedFeatures(new ArrayList<>(storage.getFeatures()), new ExperimentalFeatureImpl(GLOBAL_FEATURE_1, true), new ExperimentalFeatureImpl(GLOBAL_FEATURE_2, false), new ExperimentalFeatureImpl(GLOBAL_FEATURE_3, true));
    }

    @Test
    public void testStoreFeature() throws IOException {
        testRegularLoad();

        storage.store(new ExperimentalFeatureImpl(FEATURE_1, true));

        verify(ioService, never()).newOutputStream(any());
        verify(ioService, never()).startBatch(fileSystem);
        verify(ioService, never()).endBatch();
        verify(event, never()).fire(any());

        storage.store(new ExperimentalFeatureImpl(GLOBAL_FEATURE_1, false));

        verify(ioService, times(1)).newOutputStream(any());
        verify(ioService, times(1)).startBatch(fileSystem);
        verify(ioService, times(1)).endBatch();
        verify(event, times(1)).fire(any());

        verifyLoadedFeatures(new ArrayList<>(storage.getFeatures()), new ExperimentalFeatureImpl(GLOBAL_FEATURE_1, false), new ExperimentalFeatureImpl(GLOBAL_FEATURE_2, false), new ExperimentalFeatureImpl(GLOBAL_FEATURE_3, true));
    }

    private void verifyInit() {
        verify(spaces).resolveFileSystemURI(any(), any(), any());
        verify(ioService, times(1)).newFileSystem(any(), any());
    }

    @Override
    protected GlobalExperimentalFeaturesStorageImpl getStorageInstance() {
        return new GlobalExperimentalFeaturesStorageImpl(sessionInfo, spaces, ioService, defRegistry, event);
    }
}
