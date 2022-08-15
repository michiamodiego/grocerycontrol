package com.ds.app.pricereading.service.readingservice;

import android.content.Context;

import com.ds.app.pricereading.db.AppDatabase;
import com.ds.app.pricereading.db.dao.ReadingDao;
import com.ds.app.pricereading.db.AppDatabaseAccessor;
import com.ds.app.pricereading.db.dao.StatisticsDao;
import com.ds.app.pricereading.db.entity.ProductEntity;
import com.ds.app.pricereading.db.entity.ReadingEntity;
import com.ds.app.pricereading.db.entity.statistics.ProductStatisticsEntity;
import com.ds.app.pricereading.db.entity.statistics.ProductStatisticsJoined;
import com.ds.app.pricereading.db.entity.statistics.ShopStatisticsEntity;
import com.ds.app.pricereading.db.entity.statistics.ShopStatisticsJoined;
import com.ds.app.pricereading.service.ProductService;
import com.ds.app.pricereading.service.ShopService;
import com.ds.app.pricereading.service.readingservice.dto.CreateDto;
import com.ds.app.pricereading.service.readingservice.dto.CreateWithProductDto;
import com.ds.app.pricereading.service.util.Page;
import com.ds.app.pricereading.util.customasynctask.PrAsyncTask;
import com.ds.app.pricereading.util.customasynctask.PrJob;
import com.ds.app.pricereading.util.customasynctask.PrJobError;
import com.ds.app.pricereading.util.customasynctask.PrResult;
import com.ds.app.pricereading.util.ReferenceHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReadingService {

    public static final ReadingService create(Context context) {
        AppDatabase database = AppDatabaseAccessor.getInstance(context);
        return new ReadingService(
                database,
                database.getReadingDao(),
                database.getStatisticsDao(),
                ShopService.create(context),
                ProductService.create(context)
        );
    }

    public PrAsyncTask<ReadingEntity> create(CreateDto createDto) {
        return create(
                createDto.getShopId(),
                createDto.getProductId(),
                createDtoFrom(createDto)
        );
    }

    private CreateWithProductDto createDtoFrom(CreateDto createDto) {
        return new CreateWithProductDto(
                createDto.getShopId(),
                null,
                null,
                createDto.getPrice(),
                createDto.getPromo(),
                createDto.getReadAt()
        );
    }

    public PrAsyncTask<ReadingEntity> create(CreateWithProductDto createWithProductDto) {
        return create(
                createWithProductDto.getShopId(),
                null,
                createWithProductDto
        );
    }

    public PrAsyncTask<Page<ReadingEntity>> getReadingList(Date from, Date to) {
        return PrAsyncTask.getInstance(new PrJob<Page<ReadingEntity>>() {
            @Override
            public void run(PrResult<Page<ReadingEntity>> prResult) {
                try {
                    Long longFrom = from != null ? from.getTime() : null;
                    Long longTo = to != null ? to.getTime() : null;
                    prResult.resolve(
                            new Page<ReadingEntity>(
                                    readingDao.countBy(longFrom, longTo),
                                    readingDao.findBy(longFrom, longTo)
                            )
                    );
                } catch (Exception e) {
                    prResult.resolve(new PrJobError(e));
                }
            }
        });
    }

    public PrAsyncTask<Page<ShopStatisticsJoined>> getShopStatisticsPageByShopIdListAndProductId(
            List<Long> shopIdList,
            Long productId,
            int offset,
            int count
    ) {
        return PrAsyncTask.getInstance(new PrJob<Page<ShopStatisticsJoined>>() {
            @Override
            public void run(PrResult<Page<ShopStatisticsJoined>> prResult) {

                List<Long> tmpShopIdList = shopIdList;
                if (tmpShopIdList == null || tmpShopIdList.isEmpty()) {
                    if (tmpShopIdList == null) {
                        tmpShopIdList = new ArrayList<>();
                    }
                    tmpShopIdList.add(-1l);
                }

                try {
                    prResult.resolve(
                            new Page(
                                    statisticsDao.countBy(tmpShopIdList, productId),
                                    statisticsDao.findShopStatisticsListByShopIdAndProductId(tmpShopIdList, productId, offset, count)
                            )
                    );
                } catch (Exception e) {
                    prResult.resolve(new PrJobError(e));
                }

            }
        });
    }

    public PrAsyncTask<List<ProductStatisticsJoined>> getProductStatisticsList(long productId) {
        return PrAsyncTask.getInstance(new PrJob<List<ProductStatisticsJoined>>() {
            @Override
            public void run(PrResult<List<ProductStatisticsJoined>> prResult) {
                try {
                    prResult.resolve(statisticsDao.findProductStatisticsByProductId(productId));
                } catch (Exception e) {
                    prResult.resolve(new PrJobError(e));
                }
            }
        });
    }

    private PrAsyncTask<ReadingEntity> create(
            Long shopId,
            Long productId,
            CreateWithProductDto createWithProductDto
    ) {
        return PrAsyncTask
                .getInstance(new PrJob<ReadingEntity>() {
                    @Override
                    public void run(PrResult<ReadingEntity> prResult) {

                        try {

                            ProductEntity productEntity = null;

                            if (productId == null) {
                                productEntity = productService
                                        .createOrUpdate(
                                                createWithProductDto.getProductName(),
                                                createWithProductDto.getProductBarcode()
                                        )
                                        .trySynchronize()
                                        .getDataOrThrowException();
                            }

                            Date readAt = createWithProductDto.getReadAt();

                            Long tmpProductId = productId != null ? productId : productEntity.getId();

                            ReferenceHolder<ReadingEntity> shopReferenceHolder = new ReferenceHolder<>();

                            database.runInTransaction(new Runnable() {
                                @Override
                                public void run() {

                                    ReadingEntity readingEntity = new ReadingEntity();
                                    readingEntity.setShopId(shopId);
                                    readingEntity.setProductId(tmpProductId);
                                    readingEntity.setPrice(createWithProductDto.getPrice());
                                    readingEntity.setPromo(createWithProductDto.getPromo());
                                    readingEntity.setReadAt(readAt != null ? readAt.getTime() : new Date().getTime());
                                    readingEntity.setId(readingDao.insert(readingEntity));

                                    shopReferenceHolder.setReference(readingEntity);

                                    createOrUpdateGeneralProductStatistics(
                                            shopId,
                                            tmpProductId,
                                            createWithProductDto.getPrice(),
                                            createWithProductDto.getPromo(),
                                            readAt
                                    );

                                    createOrUpdateShopProductStatistics(
                                            shopId,
                                            tmpProductId,
                                            createWithProductDto.getPrice(),
                                            createWithProductDto.getPromo(),
                                            readAt
                                    );

                                }
                            });

                            prResult.resolve(shopReferenceHolder.getReference());

                        } catch (Exception e) {

                            prResult.resolve(new PrJobError(e));

                        }

                    }
                });
    }

    public static Map<String, ProductStatisticsJoined> toMap(List<ProductStatisticsJoined> list) {
        Map<String, ProductStatisticsJoined> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (int i = 0; i < list.size(); i++) {
            ProductStatisticsJoined item = list.get(i);
            map.put(item.getType(), item);
        }
        return map;
    }

    private List<ProductStatisticsEntity> createOrUpdateGeneralProductStatistics(
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt
    ) {
        Map<String, ProductStatisticsJoined> statisticsJoinedMap = toMap(statisticsDao.findProductStatisticsByProductId(productId));
        Long updateTime = new Date().getTime();
        List<ProductStatisticsEntity> statisticsEntityList = new LinkedList<>();
        statisticsEntityList.add(updatePriceMinStatistics(statisticsJoinedMap, shopId, productId, price, promo, readAt, updateTime));
        statisticsEntityList.add(updatePromoMinStatistics(statisticsJoinedMap, shopId, productId, price, promo, readAt, updateTime));
        statisticsEntityList.add(updatePriceMaxStatistics(statisticsJoinedMap, shopId, productId, price, promo, readAt, updateTime));
        statisticsEntityList.add(updatePromoMaxStatistics(statisticsJoinedMap, shopId, productId, price, promo, readAt, updateTime));
        statisticsEntityList.add(updatePriceMeanStatistics(statisticsJoinedMap, shopId, productId, price, promo, readAt, updateTime));
        statisticsEntityList.add(updatePromoMeanStatistics(statisticsJoinedMap, shopId, productId, price, promo, readAt, updateTime));
        statisticsEntityList.add(updatePriceLastStatistics(statisticsJoinedMap, shopId, productId, price, promo, readAt, updateTime));
        statisticsEntityList.add(updatePromoLastStatistics(statisticsJoinedMap, shopId, productId, price, promo, readAt, updateTime));
        return statisticsEntityList;
    }

    private ShopStatisticsEntity createOrUpdateShopProductStatistics(
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt
    ) {
        ShopStatisticsEntity statistics = statisticsDao.findShopProductStatisticsByShopIdAndProductId(shopId, productId);
        Long updateTime = readAt.getTime();
        if (statistics == null) {
            statistics = new ShopStatisticsEntity();
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setPriceMin(price);
            statistics.setPriceMinLastUpdate(updateTime);
            statistics.setPromoMin(promo);
            statistics.setPromoMinLastUpdate(updateTime);
            statistics.setPriceMax(price);
            statistics.setPriceMaxLastUpdate(updateTime);
            statistics.setPromoMax(promo);
            statistics.setPromoMaxLastUpdate(updateTime);
            statistics.setPriceMean(price);
            statistics.setPriceMeanIteration(1);
            statistics.setPriceMeanLastUpdate(updateTime);
            statistics.setPromoMean(promo);
            statistics.setPromoMeanIteration(1);
            statistics.setPromoMeanLastUpdate(updateTime);
            statistics.setPriceLast(price);
            statistics.setPriceLastLastUpdate(updateTime);
            statistics.setPromoLast(promo);
            statistics.setPromoLastLastUpdate(updateTime);
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            // price min
            if (price != null && (statistics.getPriceMin() == null || price.compareTo(statistics.getPriceMin()) == -1)) {
                statistics.setPriceMin(price);
                statistics.setPriceMinLastUpdate(updateTime);
            }
            // promo min
            if (promo != null && (statistics.getPromoMin() == null || promo.compareTo(statistics.getPromoMin()) == -1)) {
                statistics.setPromoMin(promo);
                statistics.setPromoMinLastUpdate(updateTime);
            }
            // price max
            if (price != null && (statistics.getPriceMax() == null || price.compareTo(statistics.getPriceMax()) == 1)) {
                statistics.setPriceMax(price);
                statistics.setPriceMaxLastUpdate(updateTime);
            }
            // promo max
            if (promo != null && (statistics.getPromoMax() == null || promo.compareTo(statistics.getPromoMax()) == 1)) {
                statistics.setPromoMax(promo);
                statistics.setPromoMaxLastUpdate(updateTime);
            }
            // price last
            if (price != null) {
                statistics.setPriceLast(price);
                statistics.setPriceLastLastUpdate(updateTime);
            }
            // promo last
            if (promo != null) {
                statistics.setPromoLast(promo);
                statistics.setPromoLastLastUpdate(updateTime);
            }
            // price mean
            if (price != null && statistics.getPriceMean() != null) {
                statistics.setPriceMean(calculateMean(statistics.getPriceMean(), price, statistics.getPriceMeanIteration()));
                statistics.setPriceMeanIteration(statistics.getPriceMeanIteration() + 1);
                statistics.setPriceMeanLastUpdate(updateTime);
            } else if (price != null) {
                statistics.setPriceMean(price);
                statistics.setPriceMeanIteration(1);
                statistics.setPriceMeanLastUpdate(updateTime);
            }
            if (promo != null && statistics.getPromoMean() != null) {
                statistics.setPromoMean(calculateMean(statistics.getPromoMean(), promo, statistics.getPromoMeanIteration()));
                statistics.setPromoMeanIteration(statistics.getPromoMeanIteration() + 1);
                statistics.setPromoMeanLastUpdate(updateTime);
            } else if (promo != null) {
                statistics.setPromoMean(promo);
                statistics.setPromoMeanIteration(1);
                statistics.setPromoMeanLastUpdate(updateTime);
            }
            statisticsDao.update(statistics);
        }
        return statistics;
    }

    private ProductStatisticsEntity updatePromoLastStatistics(
            Map<String, ProductStatisticsJoined> statisticsEntityMap,
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt,
            Long updateTime
    ) {
        ProductStatisticsEntity statistics = statisticsEntityMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_LAST);
        if (statistics == null && promo != null) {
            statistics = new ProductStatisticsEntity();
            statistics.setType(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_LAST);
            statistics.setPrice(promo);
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setReadAt(readAt.getTime());
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            if (promo != null) {
                statistics.setPrice(promo);
                statistics.setShopId(shopId);
                statistics.setReadAt(readAt.getTime());
                statisticsDao.update(statistics);
            }
        }
        return statistics;
    }

    private ProductStatisticsEntity updatePriceLastStatistics(
            Map<String, ProductStatisticsJoined> statisticsEntityMap,
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt,
            Long updateTime
    ) {
        ProductStatisticsEntity statistics = statisticsEntityMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_LAST);
        if (statistics == null && price != null) {
            statistics = new ProductStatisticsEntity();
            statistics.setType(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_LAST);
            statistics.setPrice(price);
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setReadAt(readAt.getTime());
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            if (price != null) {
                statistics.setPrice(price);
                statistics.setShopId(shopId);
                statistics.setReadAt(readAt.getTime());
                statisticsDao.update(statistics);
            }
        }
        return statistics;
    }

    private ProductStatisticsEntity updatePromoMeanStatistics(
            Map<String, ProductStatisticsJoined> statisticsEntityMap,
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt,
            Long updateTime
    ) {
        ProductStatisticsEntity statistics = statisticsEntityMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MEAN);
        if (statistics == null && promo != null) {
            statistics = new ProductStatisticsEntity();
            statistics.setType(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MEAN);
            statistics.setPrice(promo);
            statistics.setIteration(1);
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setReadAt(readAt.getTime());
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            if (promo != null) {
                statistics.setPrice(calculateMean(statistics.getPrice(), promo, statistics.getIteration()));
                statistics.setIteration(statistics.getIteration() + 1);
                statistics.setShopId(shopId);
                statistics.setReadAt(readAt.getTime());
                statisticsDao.update(statistics);
            }
        }
        return statistics;
    }

    private ProductStatisticsEntity updatePriceMeanStatistics(
            Map<String, ProductStatisticsJoined> statisticsEntityMap,
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt,
            Long updateTime
    ) {
        ProductStatisticsEntity statistics = statisticsEntityMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MEAN);
        if (statistics == null && price != null) {
            statistics = new ProductStatisticsEntity();
            statistics.setType(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MEAN);
            statistics.setPrice(price);
            statistics.setIteration(1);
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setReadAt(readAt.getTime());
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            if (price != null) {
                statistics.setPrice(calculateMean(statistics.getPrice(), price, statistics.getIteration()));
                statistics.setIteration(statistics.getIteration() + 1);
                statistics.setShopId(shopId);
                statistics.setReadAt(readAt.getTime());
                statisticsDao.update(statistics);
            }
        }
        return statistics;
    }

    private BigDecimal calculateMean(
            BigDecimal currentMean,
            BigDecimal newValue,
            int iterazioneCorrente
    ) {
        return currentMean
                .multiply(BigDecimal.valueOf(iterazioneCorrente))
                .add(newValue)
                .divide(BigDecimal.valueOf(++iterazioneCorrente), RoundingMode.HALF_UP);
    }

    private ProductStatisticsEntity updatePromoMaxStatistics(
            Map<String, ProductStatisticsJoined> statisticsEntityMap,
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt,
            Long updateTime
    ) {
        ProductStatisticsEntity statistics = statisticsEntityMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MAX);
        if (statistics == null && promo != null) {
            statistics = new ProductStatisticsEntity();
            statistics.setType(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MAX);
            statistics.setPrice(promo);
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setReadAt(readAt.getTime());
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            if (promo != null && promo.compareTo(statistics.getPrice()) == 1) {
                statistics.setPrice(promo);
                statistics.setShopId(shopId);
                statistics.setReadAt(readAt.getTime());
                statisticsDao.update(statistics);
            }
        }
        return statistics;
    }

    private ProductStatisticsEntity updatePriceMaxStatistics(
            Map<String, ProductStatisticsJoined> statisticsEntityMap,
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt,
            Long updateTime
    ) {
        ProductStatisticsEntity statistics = statisticsEntityMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MAX);
        if (statistics == null && price != null) {
            statistics = new ProductStatisticsEntity();
            statistics.setType(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MAX);
            statistics.setPrice(price);
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setReadAt(readAt.getTime());
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            if (price != null && price.compareTo(statistics.getPrice()) == 1) {
                statistics.setPrice(price);
                statistics.setShopId(shopId);
                statistics.setReadAt(readAt.getTime());
                statisticsDao.update(statistics);
            }
        }
        return statistics;
    }

    private ProductStatisticsEntity updatePriceMinStatistics(
            Map<String, ProductStatisticsJoined> statisticsEntityMap,
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt,
            Long updateTime
    ) {
        ProductStatisticsEntity statistics = statisticsEntityMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MIN);
        if (statistics == null && price != null) {
            statistics = new ProductStatisticsEntity();
            statistics.setType(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MIN);
            statistics.setPrice(price);
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setReadAt(readAt.getTime());
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            if (price != null && price.compareTo(statistics.getPrice()) == -1) {
                statistics.setPrice(price);
                statistics.setShopId(shopId);
                statistics.setReadAt(readAt.getTime());
                statisticsDao.update(statistics);
            }
        }
        return statistics;
    }

    private ProductStatisticsEntity updatePromoMinStatistics(
            Map<String, ProductStatisticsJoined> statisticsEntityMap,
            Long shopId,
            Long productId,
            BigDecimal price,
            BigDecimal promo,
            Date readAt,
            Long updateTime
    ) {
        ProductStatisticsEntity statistics = statisticsEntityMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MIN);
        if (statistics == null && promo != null) {
            statistics = new ProductStatisticsEntity();
            statistics.setType(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MIN);
            statistics.setPrice(promo);
            statistics.setShopId(shopId);
            statistics.setProductId(productId);
            statistics.setReadAt(readAt.getTime());
            statistics.setId(statisticsDao.insert(statistics));
        } else {
            if (promo != null && promo.compareTo(statistics.getPrice()) == -1) {
                statistics.setPrice(promo);
                statistics.setShopId(shopId);
                statistics.setReadAt(readAt.getTime());
            }
        }
        return statistics;
    }

    private ReadingService(
            AppDatabase database,
            ReadingDao readingDao,
            StatisticsDao statisticsDao,
            ShopService shopService,
            ProductService productService
    ) {
        this.database = database;
        this.readingDao = readingDao;
        this.statisticsDao = statisticsDao;
        this.shopService = shopService;
        this.productService = productService;
    }

    private final AppDatabase database;
    private final ReadingDao readingDao;
    private final StatisticsDao statisticsDao;
    private final ShopService shopService;
    private final ProductService productService;

}
