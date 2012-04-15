package com.jnispace.isll.services.nullobjects;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

public class SearchResultNullObject extends SearchResult {
    public SearchResultNullObject() {
        super(null, null, null);
    }

    @Override
    public Attributes getAttributes() {
        return new AttributesNullObject();
    }

    private static final long serialVersionUID = 1L;

}
