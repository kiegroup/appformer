package org.uberfire.ext.widgets.common.client.common;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GwtMockitoTestRunner.class)
public class NumericIntegerTextBoxTest {

    private NumericIntegerTextBox numericIntegerTextBox;

    @Before
    public void setUp() throws Exception {
        numericIntegerTextBox = new NumericIntegerTextBox();
    }

    @Test
    public void testDefaultValue() {
        Assertions.assertThat(numericIntegerTextBox.makeValidValue("")).isEqualTo("0");
        Assertions.assertThat(numericIntegerTextBox.makeValidValue("123")).isEqualTo("0");
        Assertions.assertThat(numericIntegerTextBox.makeValidValue("-123")).isEqualTo("0");
        Assertions.assertThat(numericIntegerTextBox.makeValidValue("abcd")).isEqualTo("0");
    }

    @Test
    public void testIsValidValue_ValidValues() {
        Assertions.assertThat(numericIntegerTextBox.isValidValue("123", false)).isTrue();
        Assertions.assertThat(numericIntegerTextBox.isValidValue("-123", false)).isTrue();
    }

    @Test
    public void testIsValidValue_InvalidValues() {
        Assertions.assertThat(numericIntegerTextBox.isValidValue("123.0", false)).isFalse();
        Assertions.assertThat(numericIntegerTextBox.isValidValue("abcd", false)).isFalse();
        Assertions.assertThat(numericIntegerTextBox.isValidValue("!", false)).isFalse();
    }
}
