package com.ds.app.pricereading.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.ds.app.pricereading.db.dao.ProductDao;
import com.ds.app.pricereading.db.dao.ReadingDao;
import com.ds.app.pricereading.db.dao.ShopDao;
import com.ds.app.pricereading.db.dao.StatisticsDao;
import com.ds.app.pricereading.db.entity.ProductEntity;
import com.ds.app.pricereading.db.entity.ReadingEntity;
import com.ds.app.pricereading.db.entity.ShopEntity;
import com.ds.app.pricereading.db.entity.statistics.ProductStatisticsEntity;
import com.ds.app.pricereading.db.entity.statistics.ShopStatisticsEntity;

@Database(
        entities = {
                ShopEntity.class,
                ProductEntity.class,
                ReadingEntity.class,
                ProductStatisticsEntity.class,
                ShopStatisticsEntity.class
        },
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ShopDao getShopDao();

    public abstract ProductDao getProductDao();

    public abstract ReadingDao getReadingDao();

    public abstract StatisticsDao getStatisticsDao();
}
