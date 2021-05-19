package org.dashbuilder.client.marshalling;

import java.util.Arrays;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderRegistry;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.json.DataSetDefJSONMarshaller;
import org.dashbuilder.dataset.json.DataSetMetadataJSONMarshaller;

@ApplicationScoped
public class ClientDataSetMetadataJSONMarshaller extends DataSetMetadataJSONMarshaller {

    public ClientDataSetMetadataJSONMarshaller() {
        super(new DataSetDefJSONMarshaller(new ClientDataSetProviderRegistry()));
    }

    public static class ClientDataSetProviderRegistry implements DataSetProviderRegistry {

        DataSetProviderType<?>[] PROVIDERS = {
                                           DataSetProviderType.STATIC,
                                           DataSetProviderType.BEAN,
                                           DataSetProviderType.SQL,
                                           DataSetProviderType.CSV,
                                           DataSetProviderType.ELASTICSEARCH,
                                           DataSetProviderType.PROMETHEUS,
                                           DataSetProviderType.KAFKA
        };

        @Override
        public void registerDataProvider(DataSetProvider dataProvider) {
            // not used
        }

        @Override
        public DataSetProvider getDataSetProvider(DataSetProviderType type) {
            // not used
            return null;
        }

        @Override
        public Set<DataSetProviderType> getAvailableTypes() {
            // not used
            return null;
        }

        @Override
        public DataSetProviderType getProviderTypeByName(String name) {
            return Arrays.stream(PROVIDERS)
                         .filter(p -> p.getName().equalsIgnoreCase(name))
                         .findFirst()
                         .orElseThrow(() -> new RuntimeException("Provider not found: " + name));
        }
    }

}