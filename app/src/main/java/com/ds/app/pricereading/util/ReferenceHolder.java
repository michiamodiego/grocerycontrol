package com.ds.app.pricereading.util;

public class ReferenceHolder<T> {

    public boolean isNull() {
        return reference == null;
    }

    public T getReference() {
        return reference;
    }

    public void setReference(T reference) {
        this.reference = reference;
    }

    public ReferenceHolder() {

    }

    public ReferenceHolder(T reference) {
        this.reference = reference;
    }

    private T reference;

}
