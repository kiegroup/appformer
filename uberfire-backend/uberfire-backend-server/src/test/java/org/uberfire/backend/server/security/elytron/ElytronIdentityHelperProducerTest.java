/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.backend.server.security.elytron;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.security.WorkbenchUserManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ElytronIdentityHelperProducerTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    private ElytronIdentityHelperProducer producer;

    @Before
    public void init() {
        producer = new ElytronIdentityHelperProducer();
    }

    @Test
    public void testProduce() {

        ElytronIdentityHelper helper = producer.getDefaultElytronIdentityHelper();

        assertNotNull(helper);
        assertTrue(helper instanceof DefaultElytronIdentityHelper);
    }
}
