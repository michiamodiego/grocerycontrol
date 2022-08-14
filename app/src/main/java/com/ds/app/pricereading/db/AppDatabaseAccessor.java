package com.ds.app.pricereading.db;

import android.content.Context;

import androidx.room.Room;

import com.ds.app.pricereading.db.AppDatabase;

public class AppDatabaseAccessor {

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context,
                    AppDatabase.class,
                    "price_reading_db_10"
            ).build();
        }
        return instance;
    }

    private static AppDatabase instance;

}
