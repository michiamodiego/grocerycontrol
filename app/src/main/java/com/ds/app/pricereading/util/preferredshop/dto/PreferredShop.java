package com.ds.app.pricereading.util.preferredshop.dto;

public class PreferredShop {

    private final Long id;
    private final String description;

    public PreferredShop(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

}
