package org.appformer.kogito.bridge.client.pmmleditor.marshaller;

import elemental2.dom.Console;
import elemental2.dom.DomGlobal;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PMMLEditorMarshallerServiceProducerTest {

    @Mock
    private Console console;

    private PMMLEditorMarshallerServiceProducer producer;

    @Before
    public void setup() {
        producer = spy(new PMMLEditorMarshallerServiceProducer());
        DomGlobal.console = console;
    }

    @Test
    public void produceWithEnvelopeAvailable() {
        doReturn(true).when(producer).isEnvelopeAvailable();
        Assertions.assertThat(producer.produce()).isNotNull().isInstanceOf(PMMLEditorMarshallerService.class);
    }

    @Test
    public void produceWithEnvelopeNotAvailable() {
        doReturn(false).when(producer).isEnvelopeAvailable();
        Assertions.assertThat(producer.produce()).isNotNull().isInstanceOf(UnavailablePMMLEditorMarshallerService.class);
        verify(console).warn("[PMMLEditorMarshallerApi] Envelope API is not available. Empty PMML models will be passed");
    }

}
