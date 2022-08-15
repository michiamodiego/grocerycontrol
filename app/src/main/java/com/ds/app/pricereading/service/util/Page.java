package com.ds.app.pricereading.service.util;

import java.util.List;

public class Page<T> {

    public boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> list) {
        this.data = list;
    }

    public Page(int size, List<T> data) {
        this.size = size;
        this.data = data;
    }

    private int size;
    private List<T> data;

}
