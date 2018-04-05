package com.kgaurav.kmem.model;

/**
 * Created by admin on 3/23/2018.
 */
public class InternalItem {
    private String id;
    private String key;
    private String value;

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
