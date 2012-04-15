package com.jnispace.isll.services.nullobjects;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class AttributesNullObject implements Attributes {

    public boolean isCaseIgnored() {
        return false;
    }

    public int size() {
        return 0;
    }

    public Attribute get(String attrID) {
        return new AttributeNullObject();
    }

    public NamingEnumeration<? extends Attribute> getAll() {
        return null;
    }

    public NamingEnumeration<String> getIDs() {
        return null;
    }

    public Attribute put(String attrID, Object val) {
        return null;
    }

    public Attribute put(Attribute attr) {
        return null;
    }

    public Attribute remove(String attrID) {
        return null;
    }

    @Override
    public AttributesNullObject clone() {
        return new AttributesNullObject();
    }

    private static final long serialVersionUID = 1L;
}
