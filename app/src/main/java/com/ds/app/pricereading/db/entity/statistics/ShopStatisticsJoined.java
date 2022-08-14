package com.ds.app.pricereading.db.entity.statistics;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class ShopStatisticsJoined extends ShopStatisticsEntity {

    @ColumnInfo(name = "shop_name")
    private String shopName;

    @ColumnInfo(name = "shop_address")
    private String ShopAddress;

    @ColumnInfo(name = "shop_location")
    private String shopLocation;

    @ColumnInfo(name = "shop_postal_code")
    private String shopPostalCode;

    @ColumnInfo(name = "shop_distribution")
    private String shopDistribution;

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return ShopAddress;
    }

    public void setShopAddress(String shopAddress) {
        ShopAddress = shopAddress;
    }

    public String getShopLocation() {
        return shopLocation;
    }

    public void setShopLocation(String shopLocation) {
        this.shopLocation = shopLocation;
    }

    public String getShopPostalCode() {
        return shopPostalCode;
    }

    public void setShopPostalCode(String shopPostalCode) {
        this.shopPostalCode = shopPostalCode;
    }

    public String getShopDistribution() {
        return shopDistribution;
    }

    public void setShopDistribution(String shopDistribution) {
        this.shopDistribution = shopDistribution;
    }

}
