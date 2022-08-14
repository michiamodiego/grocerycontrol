package com.ds.app.pricereading.db.entity.statistics;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.ds.app.pricereading.db.converter.BigDecimalTypeConverter;

import java.math.BigDecimal;

@Entity(
        tableName = "shop_statistics"
)
public class ShopStatisticsEntity {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "shop_id")
    private Long shopId;

    @ColumnInfo(name = "product_id")
    private Long productId;

    @ColumnInfo(name = "price_min")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal priceMin;

    @ColumnInfo(name = "price_min_last_update")
    private Long priceMinLastUpdate;

    @ColumnInfo(name = "promo_min")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal promoMin;

    @ColumnInfo(name = "promo_min_last_update")
    private Long promoMinLastUpdate;

    @ColumnInfo(name = "price_max")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal priceMax;

    @ColumnInfo(name = "price_max_last_update")
    private Long priceMaxLastUpdate;

    @ColumnInfo(name = "promo_max")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal promoMax;

    @ColumnInfo(name = "promo_max_last_update")
    private Long promoMaxLastUpdate;

    @ColumnInfo(name = "price_last")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal priceLast;

    @ColumnInfo(name = "price_last_last_update")
    private Long priceLastLastUpdate;

    @ColumnInfo(name = "promo_last")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal promoLast;

    @ColumnInfo(name = "promo_last_last_update")
    private Long promoLastLastUpdate;

    @ColumnInfo(name = "price_mean")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal priceMean;

    @ColumnInfo(name = "price_mean_iteration")
    private int priceMeanIteration;

    @ColumnInfo(name = "price_mean_last_update")
    private Long priceMeanLastUpdate;

    @ColumnInfo(name = "promo_mean")
    @TypeConverters(BigDecimalTypeConverter.class)
    private BigDecimal promoMean;

    @ColumnInfo(name = "promo_mean_iteration")
    private int promoMeanIteration;

    @ColumnInfo(name = "promo_mean_last_update")
    private Long promoMeanLastUpdate;

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

    public BigDecimal getPriceMin() {
        return priceMin;
    }

    public void setPriceMin(BigDecimal priceMin) {
        this.priceMin = priceMin;
    }

    public Long getPriceMinLastUpdate() {
        return priceMinLastUpdate;
    }

    public void setPriceMinLastUpdate(Long priceMinLastUpdate) {
        this.priceMinLastUpdate = priceMinLastUpdate;
    }

    public BigDecimal getPromoMin() {
        return promoMin;
    }

    public void setPromoMin(BigDecimal promoMin) {
        this.promoMin = promoMin;
    }

    public Long getPromoMinLastUpdate() {
        return promoMinLastUpdate;
    }

    public void setPromoMinLastUpdate(Long promoMinLastUpdate) {
        this.promoMinLastUpdate = promoMinLastUpdate;
    }

    public BigDecimal getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(BigDecimal priceMax) {
        this.priceMax = priceMax;
    }

    public Long getPriceMaxLastUpdate() {
        return priceMaxLastUpdate;
    }

    public void setPriceMaxLastUpdate(Long priceMaxLastUpdate) {
        this.priceMaxLastUpdate = priceMaxLastUpdate;
    }

    public BigDecimal getPromoMax() {
        return promoMax;
    }

    public void setPromoMax(BigDecimal promoMax) {
        this.promoMax = promoMax;
    }

    public Long getPromoMaxLastUpdate() {
        return promoMaxLastUpdate;
    }

    public void setPromoMaxLastUpdate(Long promoMaxLastUpdate) {
        this.promoMaxLastUpdate = promoMaxLastUpdate;
    }

    public BigDecimal getPriceLast() {
        return priceLast;
    }

    public void setPriceLast(BigDecimal priceLast) {
        this.priceLast = priceLast;
    }

    public Long getPriceLastLastUpdate() {
        return priceLastLastUpdate;
    }

    public void setPriceLastLastUpdate(Long priceLastLastUpdate) {
        this.priceLastLastUpdate = priceLastLastUpdate;
    }

    public BigDecimal getPromoLast() {
        return promoLast;
    }

    public void setPromoLast(BigDecimal promoLast) {
        this.promoLast = promoLast;
    }

    public Long getPromoLastLastUpdate() {
        return promoLastLastUpdate;
    }

    public void setPromoLastLastUpdate(Long promoLastLastUpdate) {
        this.promoLastLastUpdate = promoLastLastUpdate;
    }

    public BigDecimal getPriceMean() {
        return priceMean;
    }

    public void setPriceMean(BigDecimal priceMean) {
        this.priceMean = priceMean;
    }

    public int getPriceMeanIteration() {
        return priceMeanIteration;
    }

    public void setPriceMeanIteration(int priceMeanIteration) {
        this.priceMeanIteration = priceMeanIteration;
    }

    public Long getPriceMeanLastUpdate() {
        return priceMeanLastUpdate;
    }

    public void setPriceMeanLastUpdate(Long priceMeanLastUpdate) {
        this.priceMeanLastUpdate = priceMeanLastUpdate;
    }

    public BigDecimal getPromoMean() {
        return promoMean;
    }

    public void setPromoMean(BigDecimal promoMean) {
        this.promoMean = promoMean;
    }

    public int getPromoMeanIteration() {
        return promoMeanIteration;
    }

    public void setPromoMeanIteration(int promoMeanIteration) {
        this.promoMeanIteration = promoMeanIteration;
    }

    public Long getPromoMeanLastUpdate() {
        return promoMeanLastUpdate;
    }

    public void setPromoMeanLastUpdate(Long promoMeanLastUpdate) {
        this.promoMeanLastUpdate = promoMeanLastUpdate;
    }

}
