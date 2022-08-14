package com.ds.app.pricereading.db.entity.util;

import com.ds.app.pricereading.db.entity.ProductEntity;

public class ProductUtil {

    public static String getStringifiedProduct(ProductEntity productEntity) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(productEntity.getName());
        String barcode = productEntity.getBarcode();
        if (StringUtil.isNullOrEmpty(barcode)) {
            stringBuilder.append(" (");
            stringBuilder.append(barcode);
            stringBuilder.append(" )");
        }
        return stringBuilder.toString();
    }

}
