package com.ds.app.pricereading.util.customasynctask;

public interface PrCallback<T> {

    void call(T result, PrJobError prJobError);

}
