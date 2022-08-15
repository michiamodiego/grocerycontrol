package com.ds.app.pricereading.util.customasynctask;

public class PrJobError {

    public String getMessage() {
        return message;
    }

    public PrJobError(String message) {
        this.message = message;
    }

    public PrJobError(Throwable message) {
        this.message = String.format("Errore tecnico: %s", message.getMessage());
    }

    private String message;

}
