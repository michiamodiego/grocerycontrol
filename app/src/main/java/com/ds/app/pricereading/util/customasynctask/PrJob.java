package com.ds.app.pricereading.util.customasynctask;

public interface PrJob<T> {

    void run(PrResult<T> prResult);

}
