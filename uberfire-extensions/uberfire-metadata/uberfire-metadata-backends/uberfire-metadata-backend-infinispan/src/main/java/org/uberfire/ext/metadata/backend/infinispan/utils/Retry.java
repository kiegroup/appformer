/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ext.metadata.backend.infinispan.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Retry {

    private int retries;
    private Runnable runnable;
    private boolean finished = false;
    private Logger logger = LoggerFactory.getLogger(Retry.class);

    public Retry(int retries, Runnable runnable) {

        this.retries = retries;
        this.runnable = runnable;
    }

    public void run() {
        while (retries > 0 && !finished) {
            try {
                runnable.run();
                finished = true;
            } catch (Exception e) {
                logger.error("Error found. Retrying", e);
                retries--;
            }
        }
    }

    public boolean isFinished() {
        return this.finished;
    }

    public int getRemainingRetries() {
        return this.retries;
    }
}
