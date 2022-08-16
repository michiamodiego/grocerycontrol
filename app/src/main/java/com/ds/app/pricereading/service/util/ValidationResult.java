package com.ds.app.pricereading.service.util;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    public boolean anyError() {
        return !errorMessageList.isEmpty();
    }

    public void add(String message) {
        errorMessageList.add(message);
    }

    public ValidationResult() {
        this.errorMessageList = new ArrayList<>();
    }

    private List<String> errorMessageList;

    public String getCompleteMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Si sono verificati i seguenti errori: ");
        for (int i = 0; i < errorMessageList.size(); i++) {
            stringBuilder.append(errorMessageList.get(i));
            stringBuilder.append("; ");
        }
        return stringBuilder.toString();
    }

}
