package com.jnispace.isll.config;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jnispace.isll.config.ConfigFieldValidator;

public class ConfigFieldValidatorTest {
    private static final String SOME_STRING = "asdkf";

    @Test
    public void returnsFalseIfInputIsNull() {
        assertFalse(ConfigFieldValidator.shouldInclude(null));
    }

    @Test
    public void returnsFalseIfInputIsAnEmptyString() {
        assertFalse(ConfigFieldValidator.shouldInclude(""));
    }

    @Test
    public void returnsTrueOtherwise() {
        assertTrue(ConfigFieldValidator.shouldInclude(SOME_STRING));
    }
}
