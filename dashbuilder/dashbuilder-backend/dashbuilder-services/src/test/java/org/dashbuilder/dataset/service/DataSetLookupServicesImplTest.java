/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
 *
 */

package org.dashbuilder.dataset.service;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetDefDeployerCDI;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetManagerCDI;
import org.dashbuilder.dataset.uuid.ActiveBranchUUID;
import org.dashbuilder.dataset.uuid.UUIDGenerator;
import org.dashbuilder.exception.ExceptionManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataSetLookupServicesImplTest {

    @Mock
    DataSetManagerCDI dataSetManager;
    @Mock
    UUIDGenerator uuidGenerator;
    @Mock
    DataSetDefDeployerCDI dataSetDefDeployer;
    @Mock
    ExceptionManager exceptionManager;

    @InjectMocks
    DataSetLookupServicesImpl dataSetLookupServices;

    @Test
    public void testLookupDataSet() throws Exception {
        final String uuid = "uuid";
        final DataSet _d = DataSetFactory.newEmptyDataSet();
        final DataSetLookup lookup = Mockito.mock(DataSetLookup.class);
        Mockito.when(lookup.getMetadata(Mockito.eq("activeBranch"))).thenReturn("test");
        Mockito.when(dataSetManager.lookupDataSet(lookup)).thenReturn(_d);
        DataSet result = dataSetLookupServices.lookupDataSet(lookup);
        Assert.assertEquals(_d, result);
        Mockito.verify(dataSetManager).activeBranchChanged(Mockito.any(ActiveBranchUUID.class));
        Mockito.verify(dataSetManager).lookupDataSet(lookup);
    }

    @Test
    public void testLookupDataSetMetadata() throws Exception {
        final ActiveBranchUUID activeBranchUUID = new ActiveBranchUUID("uuid", "test");
        dataSetLookupServices.lookupDataSetMetadata(activeBranchUUID);
        Mockito.verify(dataSetManager).activeBranchChanged(Mockito.eq(activeBranchUUID));
        Mockito.verify(dataSetManager).getDataSetMetadata(Mockito.eq(activeBranchUUID.getUuid()));
    }
}
