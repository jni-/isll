package com.jnispace.isll.services.nullobjects;

import static org.junit.Assert.*;

import javax.naming.NamingException;

import org.junit.Test;

public class AttributeNullObjectTest {

    @Test
    public void getReturnsAnEmptyString() throws NamingException {
        assertEquals(0, ((String) new AttributeNullObject().get()).length());
    }

}
