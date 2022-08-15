package com.ds.app.pricereading.util.customasynctask;

public class PrResult<T> {

    public T getDataOrThrowException() {
        if (prJobError != null) {
            throw new PrException(prJobError);
        }
        return data;
    }

    public T getData() {
        return data;
    }

    public PrJobError getPrJobError() {
        return prJobError;
    }

    public boolean resolved() {
        return resolved;
    }

    public void resolve() {
        if (!resolved()) {
            resolved = true;
        }
        this.data = null;
    }

    public void resolve(T data) {
        if (!resolved()) {
            resolved = true;
            this.data = data;
        }
    }

    public void resolve(PrJobError prJobError) {
        if (!resolved()) {
            resolved = true;
            this.prJobError = prJobError;
        }
    }

    public void notify(PrCallback<T> callback) {
        if (callback == null) {
            return;
        }
        try {
            callback.call(data, prJobError);
        } catch (Exception e) {
            int a = 0;
        }
    }

    public boolean isError() {
        return prJobError != null;
    }

    private boolean resolved = false;
    private T data;
    private PrJobError prJobError;

}
