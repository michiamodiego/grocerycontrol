package com.ds.app.pricereading.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ds.app.pricereading.db.entity.statistics.ProductStatisticsEntity;
import com.ds.app.pricereading.db.entity.statistics.ProductStatisticsJoined;
import com.ds.app.pricereading.db.entity.statistics.ShopStatisticsEntity;
import com.ds.app.pricereading.db.entity.statistics.ShopStatisticsJoined;

import java.util.List;

@Dao
public interface StatisticsDao {

    @Query("SELECT ps.id, ps.product_id, ps.price, ps.type, ps.iteration, ps.shop_id, ps.read_at, s.name as shop_name, s.address as shop_address, s.location as shop_location, s.postal_code as shop_postal_code, s.distribution as shop_distribution FROM product_statistics ps LEFT JOIN shop s ON ps.shop_id = s.id WHERE product_id = :productId")
    List<ProductStatisticsJoined> findProductStatisticsByProductId(Long productId);

    @Query("SELECT ss.id, ss.shop_id, ss.product_id, ss.price_min, ss.price_min_last_update, ss.promo_min, ss.promo_min_last_update, ss.price_max, ss.price_max_last_update, ss.promo_max, ss.promo_max_last_update, ss.price_last, ss.price_last_last_update, ss.promo_last, ss.promo_last_last_update, ss.price_mean, ss.price_mean_iteration, ss.price_mean_last_update, ss.promo_mean, ss.promo_mean_iteration, ss.promo_mean_last_update FROM shop_statistics ss WHERE ss.shop_id = :shopId AND ss.product_id = :productId")
    ShopStatisticsEntity findShopProductStatisticsByShopIdAndProductId(Long shopId, Long productId);

    @Query("SELECT ss.id, ss.shop_id, ss.product_id, ss.price_min, ss.price_min_last_update, ss.promo_min, ss.promo_min_last_update, ss.price_max, ss.price_max_last_update, ss.promo_max, ss.promo_max_last_update, ss.price_last, ss.price_last_last_update, ss.promo_last, ss.promo_last_last_update, ss.price_mean, ss.price_mean_iteration, ss.price_mean_last_update, ss.promo_mean, ss.promo_mean_iteration, ss.promo_mean_last_update, s.name as shop_name, s.address as shop_address, s.location as shop_location, s.postal_code as shop_postal_code, s.distribution as shop_distribution FROM shop_statistics ss INNER JOIN shop s ON ss.shop_id = s.id WHERE (-1 IN (:shopIdList) OR ss.shop_id IN (:shopIdList)) AND ss.product_id = :productId ORDER BY ss.shop_id LIMIT :offset, :count")
    List<ShopStatisticsJoined> findShopStatisticsListByShopIdAndProductId(List<Long> shopIdList, Long productId, int offset, int count);

    @Query("SELECT count(1) FROM shop_statistics ss INNER JOIN shop s ON ss.shop_id = s.id WHERE (-1 IN (:shopIdList) OR ss.shop_id IN (:shopIdList)) AND ss.product_id = :productId")
    int countBy(List<Long> shopIdList, Long productId);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    Long insert(ProductStatisticsEntity newStatistics);

    @Update
    void update(ProductStatisticsEntity statistics);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    Long insert(ShopStatisticsEntity statistics);

    @Update
    void update(ShopStatisticsEntity statistics);

}
