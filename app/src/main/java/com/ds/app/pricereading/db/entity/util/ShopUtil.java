package com.ds.app.pricereading.db.entity.util;

import com.ds.app.pricereading.db.entity.ShopEntity;
import com.ds.app.pricereading.db.entity.statistics.ProductStatisticsJoined;
import com.ds.app.pricereading.db.entity.statistics.ShopStatisticsJoined;

public class ShopUtil {

    public static String getFullName(String name, String distribution) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        if (!StringUtil.isNullOrEmpty(distribution)) {
            stringBuilder.append(" (");
            stringBuilder.append(distribution);
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

    public static String getFullName(ShopEntity shopEntity) {
        return getFullName(
                shopEntity.getName(),
                shopEntity.getDistribution()
        );
    }

    public static String getFullAddress(ShopEntity shopEntity) {
        return getFullAddress(
                shopEntity.getAddress(),
                shopEntity.getLocation(),
                shopEntity.getPostalCode()
        );
    }

    public static String getFullAddress(String address, String location, String postalCode) {
        StringBuilder fullAddress = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(address)) {
            fullAddress.append(address);
        }
        if (!StringUtil.isNullOrEmpty(location)) {
            if (!StringUtil.isNullOrEmpty(address)) {
                fullAddress.append(", ");
            }
            fullAddress.append(location);
        }
        if (!StringUtil.isNullOrEmpty(postalCode)) {
            if (!StringUtil.isNullOrEmpty(address) || !StringUtil.isNullOrEmpty(location)) {
                fullAddress.append(" ");
            }
            fullAddress.append("(");
            fullAddress.append(postalCode);
            fullAddress.append(")");
        }
        return fullAddress.toString();
    }

    public static String getStringifiedShop(
            String name,
            String address,
            String location,
            String postalCode,
            String distribution
    ) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getFullName(name, distribution));
        String fullAddress = getFullAddress(address, location, postalCode);
        if (!StringUtil.isNullOrEmpty(fullAddress)) {
            stringBuilder.append(" - ");
            stringBuilder.append(fullAddress);
        }
        return stringBuilder.toString();
    }

    public static String getStringifiedShop(ShopEntity shopEntity) {
        return getStringifiedShop(
                shopEntity.getName(),
                shopEntity.getAddress(),
                shopEntity.getLocation(),
                shopEntity.getPostalCode(),
                shopEntity.getDistribution()
        );
    }

    public static String getStringifiedShop(ProductStatisticsJoined statistics) {
        return getStringifiedShop(
                statistics.getShopName(),
                statistics.getShopAddress(),
                statistics.getShopLocation(),
                statistics.getShopPostalCode(),
                statistics.getShopDistribution()
        );
    }

    public static String getStringifiedShop(ShopStatisticsJoined statistics) {
        return getStringifiedShop(
                statistics.getShopName(),
                statistics.getShopAddress(),
                statistics.getShopLocation(),
                statistics.getShopPostalCode(),
                statistics.getShopDistribution()
        );
    }

}
