package com.ds.app.pricereading.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.ds.app.pricereading.db.converter.BigDecimalTypeConverter;

import java.math.BigDecimal;

@Entity(
        tableName = "reading"
)
public class ReadingEntity {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "shop_id")
    private Long shopId;

    @ColumnInfo(name = "product_id")
    private Long productId;

    @ColumnInfo(name = "price")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal price;

    @ColumnInfo(name = "promo")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal promo;

    @ColumnInfo(name = "read_at")
    private Long readAt;

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPromo() {
        return promo;
    }

    public void setPromo(BigDecimal promo) {
        this.promo = promo;
    }

    public Long getReadAt() {
        return readAt;
    }

    public void setReadAt(Long readAt) {
        this.readAt = readAt;
    }

}
