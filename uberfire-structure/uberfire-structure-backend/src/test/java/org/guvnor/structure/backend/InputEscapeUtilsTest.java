package org.guvnor.structure.backend;

import org.apache.commons.text.StringEscapeUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.guvnor.structure.backend.InputEscapeUtils.escapeHtmlInput;

public class InputEscapeUtilsTest {

    @Test
    public void testEscapeHtmlInput() {
        final String xssString = "<img/src/onerror=alert(\"XSS\")>";
        final String expectedResult = StringEscapeUtils.escapeHtml4(xssString).replace("'", "");
        final String result = escapeHtmlInput(xssString);
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void testEscapeHtmlInputWithSingleQoutes() {
        final String xssString = "<img/src/onerror=alert('XSS')>";
        final String expectedResult = StringEscapeUtils.escapeHtml4(xssString).replace("'", "");
        final String result = escapeHtmlInput(xssString);
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void testEscapeHtmlInputNull() {
        final String xssString = null;
        final String result = escapeHtmlInput(xssString);
        Assertions.assertThat(result).isNull();
    }
}
