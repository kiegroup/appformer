/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
 *
 */

package org.uberfire.commons.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.async.DescriptiveThreadFactory;

/**
 * ExecutorService Producer. It produces managed and unmanaged executor services. For now the implementation is the same
 * but it could change if any other container gets under support. They are in different variables on purpose.
 */
public class ExecutorServiceProducer {

    private Logger logger = LoggerFactory.getLogger(ExecutorServiceProducer.class);

    private final ExecutorService executorService;
    private final ExecutorService unmanagedExecutorService;
    private final ExecutorService indexingExecutorService;
    private final ExecutorService fsWatchExecutorService;
    private final ExecutorService restApiExecutorService;

    protected static final String MANAGED_LIMIT_PROPERTY = "org.appformer.concurrent.managed.thread.limit";
    protected static final String UNMANAGED_LIMIT_PROPERTY = "org.appformer.concurrent.unmanaged.thread.limit";
    protected static final String INDEXING_LIMIT_PROPERTY = "org.appformer.concurrent.indexing.thread.limit";
    protected static final String FS_WATCH_LIMIT_PROPERTY = "org.appformer.concurrent.fs.watch.thread.limit";
    protected static final String REST_API_LIMIT_PROPERTY = "org.appformer.concurrent.rest.api.thread.limit";

    public ExecutorServiceProducer() {
        this.executorService = this.buildFixedThreadPoolExecutorService(MANAGED_LIMIT_PROPERTY);
        this.unmanagedExecutorService = this.buildFixedThreadPoolExecutorService(UNMANAGED_LIMIT_PROPERTY);
        this.indexingExecutorService = this.buildFixedThreadPoolExecutorService(INDEXING_LIMIT_PROPERTY, 20);
        this.fsWatchExecutorService = this.buildFixedThreadPoolExecutorService(FS_WATCH_LIMIT_PROPERTY);
        this.restApiExecutorService = this.buildFixedThreadPoolExecutorService(REST_API_LIMIT_PROPERTY, 20);
    }

    protected ExecutorService buildFixedThreadPoolExecutorService(String key) {
        return this.buildFixedThreadPoolExecutorService(key, 0);
    }

    protected ExecutorService buildFixedThreadPoolExecutorService(String key, int defaultLimit) {
        String stringProperty = System.getProperty(key);
        int threadLimit = stringProperty == null ? defaultLimit : toInteger(stringProperty);
        if (threadLimit > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating FixedThreadPool Executor Service for: {} with a limit of {}", key, threadLimit);
            }
            return Executors.newFixedThreadPool(threadLimit,
                                                new DescriptiveThreadFactory());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating CachedThreadPool Executor Service for: {}", key);
            }
            return Executors.newCachedThreadPool(new DescriptiveThreadFactory());
        }
    }

    private Integer toInteger(String stringProperty) {
        try {
            return Integer.valueOf(stringProperty);
        } catch (NumberFormatException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Property {} is invalid, defaulting to 0",
                             stringProperty);
            }
            return 0;
        }
    }

    @Produces
    @ApplicationScoped
    @Managed
    public ExecutorService produceExecutorService() {
        return this.getManagedExecutorService();
    }

    @Produces
    @ApplicationScoped
    @Unmanaged
    public ExecutorService produceUnmanagedExecutorService() {
        return this.getUnmanagedExecutorService();
    }

    @Produces
    @ApplicationScoped
    @Indexing
    public ExecutorService produceIndexingExecutorService() {
        return this.getIndexingExecutorService();
    }

    @Produces
    @ApplicationScoped
    @FSWatch
    public ExecutorService produceFsWatchExecutorService() {
        return this.getFsWatchExecutorService();
    }

    @Produces
    @ApplicationScoped
    @RestApi
    public ExecutorService produceRestAPiExecutorService() {
        return this.getRestApiExecutorService();
    }

    protected ExecutorService getManagedExecutorService() {
        return this.executorService;
    }

    protected ExecutorService getUnmanagedExecutorService() {
        return this.unmanagedExecutorService;
    }

    protected ExecutorService getIndexingExecutorService() {
        return this.indexingExecutorService;
    }

    protected ExecutorService getFsWatchExecutorService() {
        return this.fsWatchExecutorService;
    }

    protected ExecutorService getRestApiExecutorService() {
        return this.restApiExecutorService;
    }
}
