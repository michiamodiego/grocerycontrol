package com.ds.app.pricereading.service.readingservice.dto;

import java.math.BigDecimal;
import java.util.Date;

public class CreateWithProductDto {

    public CreateWithProductDto(Long shopId, String productName, String productBarcode, BigDecimal price, BigDecimal promo, Date readAt) {
        this.shopId = shopId;
        this.productName = productName;
        this.productBarcode = productBarcode;
        this.price = price;
        this.promo = promo;
        this.readAt = readAt;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getPromo() {
        return promo;
    }

    public Date getReadAt() {
        return readAt;
    }

    private Long shopId;
    private String productName;
    private String productBarcode;
    private BigDecimal price;
    private BigDecimal promo;
    private Date readAt;

}
