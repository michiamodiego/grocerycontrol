package com.ds.app.pricereading.db.converter;

import androidx.room.TypeConverter;

import java.math.BigDecimal;

public class BigDecimalTypeConverter {

    @TypeConverter
    public static BigDecimal fromString(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    @TypeConverter
    public static String toString(BigDecimal bigDecimal) {
        return bigDecimal == null ? null : bigDecimal.toString();
    }

}
