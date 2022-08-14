package com.ds.app.pricereading.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ds.app.pricereading.db.entity.ProductEntity;

import java.util.List;

@Dao
public interface ProductDao {

    @Query("SELECT p.id, p.name, p.barcode, p.created_at, p.updated_at FROM product p WHERE p.id = :id")
    ProductEntity findById(long id);

    @Query("SELECT count(1) FROM product p WHERE (:name IS NULL OR p.name LIKE :name) AND (:barcode IS NULL OR p.barcode = :barcode)")
    int countBy(String name, String barcode);

    @Query("SELECT p.id, p.name, p.barcode, p.created_at, p.updated_at FROM product p WHERE (:name IS NULL OR p.name LIKE :name) AND (:barcode IS NULL OR p.barcode = :barcode) ORDER BY p.name LIMIT :offset, :count")
    List<ProductEntity> findProductListBy(String name, String barcode, int offset, int count);

    @Query("SELECT p.id, p.name, p.barcode, p.created_at, p.updated_at FROM product p WHERE p.barcode = :barcode")
    ProductEntity findByBarcode(String barcode);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    Long create(ProductEntity productEntity);

    @Update
    void update(ProductEntity productEntity);

}
