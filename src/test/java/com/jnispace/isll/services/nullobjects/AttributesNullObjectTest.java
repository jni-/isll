package com.jnispace.isll.services.nullobjects;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class AttributesNullObjectTest {

    @Test
    public void getByAttrIDReturnsAnAttributeNullObject() {
        String someAttrID = "attrID";
        assertThat(new AttributesNullObject().get(someAttrID), instanceOf(AttributeNullObject.class));
    }
}
