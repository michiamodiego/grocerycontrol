package com.ds.app.pricereading.db.util;

import com.ds.app.pricereading.db.entity.util.StringUtil;

public class DbUtil {

    public static String createLikeExpression(String value) {
        return StringUtil.isNullOrEmpty(value) ? null : "%" +  value.toUpperCase() + "%";
    }

}
