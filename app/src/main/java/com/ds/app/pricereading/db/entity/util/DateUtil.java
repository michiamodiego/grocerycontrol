package com.ds.app.pricereading.db.entity.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String tryFormatTime(long time) {
        if (time == 0l) {
            return null;
        }
        try {
            return simpleDateFormat.format(new Date(time));
        } catch (Exception e) {
            return null;
        }
    }

    public static String tryFormatTime(Date date) {
        return tryFormatTime(date.getTime());
    }

    public static Long tryConvertTime(String stringifiedDate) {
        try {
            return simpleDateFormat.parse(stringifiedDate).getTime();
        } catch (Exception e) {
            return null;
        }
    }

    public static Date tryConvertDate(String stringifiedDate) {
        try {
            return simpleDateFormat.parse(stringifiedDate);
        } catch (Exception e) {
            return null;
        }
    }

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

}
