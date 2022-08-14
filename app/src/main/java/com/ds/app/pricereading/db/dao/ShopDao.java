package com.ds.app.pricereading.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ds.app.pricereading.db.entity.ShopEntity;

import java.util.List;

@Dao
public interface ShopDao {

    @Query("SELECT id, name, address, location, postal_code, distribution, created_at, updated_at FROM shop s WHERE s.id = :id")
    ShopEntity findById(Long id);

    @Query("SELECT count(1) FROM shop s WHERE (:name IS NULL OR s.name LIKE :name) AND (:address IS NULL OR s.address LIKE :address) AND (:location IS NULL OR s.location LIKE :location) AND (:postalCode IS NULL OR s.postal_code = :postalCode) AND (:distribution IS NULL OR s.distribution LIKE :distribution)")
    int countBy(String name, String address, String location, String postalCode, String distribution);

    @Query("SELECT id, name, address, location, postal_code, distribution, created_at, updated_at FROM shop s WHERE (:name IS NULL OR s.name LIKE :name) AND (:address IS NULL OR s.address LIKE :address) AND (:location IS NULL OR s.location LIKE :location) AND (:postalCode IS NULL OR s.postal_code = :postalCode) AND (:distribution IS NULL OR s.distribution LIKE :distribution) ORDER BY s.name LIMIT :offset, :count")
    List<ShopEntity> findShopListBy(String name, String address, String location, String postalCode, String distribution, int offset, int count);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    Long create(ShopEntity shopEntity);

    @Update
    void update(ShopEntity shopEntity);

}
