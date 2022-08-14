package com.ds.app.pricereading.db.entity.statistics;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.ds.app.pricereading.db.converter.BigDecimalTypeConverter;

import java.math.BigDecimal;

@Entity(
        tableName = "product_statistics"
)
public class ProductStatisticsEntity {

    public static final String STATISTICS_TYPE_PRICE_MIN = "PRICE_MIN";
    public static final String STATISTICS_TYPE_PROMO_MIN = "PROMO_MIN";
    public static final String STATISTICS_TYPE_PRICE_MAX = "PRICE_MAX";
    public static final String STATISTICS_TYPE_PROMO_MAX = "PROMO_MAX";
    public static final String STATISTICS_TYPE_PRICE_MEAN = "PRICE_MEAN";
    public static final String STATISTICS_TYPE_PROMO_MEAN = "PROMO_MEAN";
    public static final String STATISTICS_TYPE_PRICE_LAST = "PRICE_LAST";
    public static final String STATISTICS_TYPE_PROMO_LAST = "PROMO_LAST";

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "product_id")
    private Long productId;

    @ColumnInfo(name = "price")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal price;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "iteration")
    private int iteration;

    @ColumnInfo(name = "shop_id")
    private Long shopId;

    @ColumnInfo(name = "read_at")
    private Long readAt;

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getReadAt() {
        return readAt;
    }

    public void setReadAt(Long readAt) {
        this.readAt = readAt;
    }

}
