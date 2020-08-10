package org.appformer.kogito.bridge.client.pmmleditor.marshaller;

import org.appformer.kogito.bridge.client.pmmleditor.marshaller.model.PMMLDocumentData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PMMLEditorMarshallerServiceTest {

    @Mock
    private PMMLEditorMarshallerApiInteropWrapper wrapperMock;

    private PMMLEditorMarshallerService pmmlEditorMarshallerServiceSpy;

    @Before
    public void setup() {
        pmmlEditorMarshallerServiceSpy = spy(new PMMLEditorMarshallerService() {
            @Override
            PMMLEditorMarshallerApiInteropWrapper getWrapper() {
                return wrapperMock;
            }
        });
    }

    @Test
    public void getPMMLDocument() {
        final String xmlContent = "<PMML>content</PMML>";
        when(wrapperMock.getPMMLDocumentData(xmlContent)).thenReturn(new PMMLDocumentData());
        pmmlEditorMarshallerServiceSpy.getPMMLDocumentData(xmlContent);
        verify(wrapperMock, times(1)).getPMMLDocumentData(xmlContent);
    }
}
