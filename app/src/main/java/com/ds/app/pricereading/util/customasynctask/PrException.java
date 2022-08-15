package com.ds.app.pricereading.util.customasynctask;

public class PrException extends RuntimeException {

    public PrException(PrJobError prJobError) {
        super(prJobError.getMessage());
    }

}
