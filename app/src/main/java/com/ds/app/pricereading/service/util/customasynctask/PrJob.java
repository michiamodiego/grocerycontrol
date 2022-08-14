package com.ds.app.pricereading.service.util.customasynctask;

public interface PrJob<T> {

    void run(PrResult<T> prResult);

}
