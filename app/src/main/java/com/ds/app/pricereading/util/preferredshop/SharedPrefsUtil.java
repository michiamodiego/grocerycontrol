package com.ds.app.pricereading.util.preferredshop;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ds.app.pricereading.util.preferredshop.dto.PreferredShop;

public class SharedPrefsUtil {

    public static final String PREF_KEY_SHOP_ID = "shop_id";
    public static final String PREF_KEY_SHOP_DESCRIPTION = "shop_description";

    public static PreferredShop getPreferredShop(Context context) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            Long id = sharedPreferences.getLong(PREF_KEY_SHOP_ID, 0);
            String description = sharedPreferences.getString(PREF_KEY_SHOP_DESCRIPTION, null);
            if (id == 0l || description == null) {
                return null;
            }
            return new PreferredShop(id, description);
        } catch (Exception e) {
            return null;
        }
    }

    public static PreferredShop setPreferredShop(
            Context context,
            Long id,
            String description
    ) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_KEY_SHOP_ID, id);
        editor.putString(PREF_KEY_SHOP_DESCRIPTION, description);
        editor.apply();
        return new PreferredShop(id, description);
    }

    public static void clearPreferredShop(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_KEY_SHOP_ID, 0);
        editor.putString(PREF_KEY_SHOP_DESCRIPTION, null);
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
