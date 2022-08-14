package com.ds.app.pricereading.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ds.app.pricereading.db.entity.ReadingEntity;

import java.util.Date;
import java.util.List;

@Dao
public interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    Long insert(ReadingEntity readingEntity);

    @Query("SELECT r.id, r.shop_id, r.product_id, r.price, r.promo, r.read_at FROM reading r WHERE (:from IS NULL OR read_at >= :from) AND (:to IS NULL OR read_at <= :to) ORDER BY r.read_at DESC")
    List<ReadingEntity> findBy(Long from, Long to);

    @Query("SELECT count(1) FROM reading r WHERE (:from IS NULL OR read_at >= :from) AND (:to IS NULL OR read_at <= :to) ORDER BY r.read_at DESC")
    int countBy(Long from, Long to);

}
