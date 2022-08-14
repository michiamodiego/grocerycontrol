package com.ds.app.pricereading.service.readingservice.dto;

import java.math.BigDecimal;
import java.util.Date;

public class CreateDto {

    public CreateDto(Long shopId, Long productId, BigDecimal price, BigDecimal promo, Date readAt) {
        this.shopId = shopId;
        this.productId = productId;
        this.price = price;
        this.promo = promo;
        this.readAt = readAt;
    }

    public Long getShopId() {
        return shopId;
    }

    public Long getProductId() {
        return productId;
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
    private Long productId;
    private BigDecimal price;
    private BigDecimal promo;
    private Date readAt;

}
