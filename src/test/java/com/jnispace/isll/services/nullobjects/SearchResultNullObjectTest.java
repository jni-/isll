package com.jnispace.isll.services.nullobjects;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class SearchResultNullObjectTest {
    @Test
    public void getAttributesReturnsAnAttributesNullObject() {
        assertThat(new SearchResultNullObject().getAttributes(), instanceOf(AttributesNullObject.class));
    }
}
