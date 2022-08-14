package com.ds.app.pricereading.service.util.customasynctask;

public interface PrCallback<T> {

    void call(T result, PrJobError prJobError);

}
