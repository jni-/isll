package com.jnispace.isll.services.nullobjects;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;

public class AttributeNullObject implements Attribute {

    public NamingEnumeration<?> getAll() throws NamingException {
        return null;
    }

    public Object get() throws NamingException {
        return "";
    }

    public int size() {
        return 0;
    }

    public String getID() {
        return null;
    }

    public boolean contains(Object attrVal) {
        return false;
    }

    public boolean add(Object attrVal) {
        return false;
    }

    public boolean remove(Object attrval) {
        return false;
    }

    public void clear() {
    }

    public DirContext getAttributeSyntaxDefinition() throws NamingException {
        return null;
    }

    public DirContext getAttributeDefinition() throws NamingException {
        return null;
    }

    public boolean isOrdered() {
        return false;
    }

    public Object get(int ix) throws NamingException {
        return null;
    }

    public Object remove(int ix) {
        return null;
    }

    public void add(int ix, Object attrVal) {
    }

    public Object set(int ix, Object attrVal) {
        return null;
    }

    @Override
    public AttributeNullObject clone() {
        return new AttributeNullObject();
    }

    private static final long serialVersionUID = 1L;
}
