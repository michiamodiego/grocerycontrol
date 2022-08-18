package com.ds.app.pricereading.service;

import android.content.Context;

import com.ds.app.pricereading.db.AppDatabase;
import com.ds.app.pricereading.db.dao.ShopDao;
import com.ds.app.pricereading.db.AppDatabaseAccessor;
import com.ds.app.pricereading.db.entity.ShopEntity;
import com.ds.app.pricereading.db.util.DbUtil;
import com.ds.app.pricereading.service.util.Page;
import com.ds.app.pricereading.service.util.ValidationResult;
import com.ds.app.pricereading.util.customasynctask.PrAsyncTask;
import com.ds.app.pricereading.util.customasynctask.PrJob;
import com.ds.app.pricereading.util.customasynctask.PrJobError;
import com.ds.app.pricereading.util.customasynctask.PrResult;
import com.ds.app.pricereading.db.entity.util.StringUtil;

import java.util.Date;

public class ShopService {

    public static final ShopService create(Context context) {
        AppDatabase database = AppDatabaseAccessor.getInstance(context);
        return new ShopService(database, database.getShopDao());
    }

    public ValidationResult isValid(
            String name,
            String address,
            String location,
            String postalCode,
            String distribution
    ) {

        ValidationResult validationResult = new ValidationResult();

        if (StringUtil.isNullOrEmpty(name)) {
            validationResult.add("Il nome del punto vendita non deve essere vuoto");
        }

        return validationResult;

    }

    public PrAsyncTask<ShopEntity> getShopById(Long id) {
        return PrAsyncTask.getInstance(new PrJob<ShopEntity>() {
            @Override
            public void run(PrResult<ShopEntity> prResult) {
                try {
                    prResult.resolve(shopDao.findById(id));
                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossbile recuperare il punto vendita"));
                }
            }
        });
    }

    public PrAsyncTask<Page<ShopEntity>> getShopListBy(
            String name,
            String address,
            String location,
            String postalCode,
            String distribution,
            int offset,
            int count
    ) {

        String nameLikeExpression = DbUtil.createLikeExpression(name);
        String addressLikeExpression = DbUtil.createLikeExpression(address);
        String locationLikeExpression = DbUtil.createLikeExpression(location);
        String distributionLikeExpression = DbUtil.createLikeExpression(distribution);

        return PrAsyncTask.getInstance(new PrJob<Page<ShopEntity>>() {
            @Override
            public void run(PrResult<Page<ShopEntity>> prResult) {
                try {
                    prResult.resolve(
                            new Page<ShopEntity>(
                                    shopDao.countBy(nameLikeExpression, addressLikeExpression, locationLikeExpression, StringUtil.isNullOrEmpty(postalCode) ? null : postalCode, distributionLikeExpression),
                                    shopDao.findShopListBy(nameLikeExpression, addressLikeExpression, locationLikeExpression, StringUtil.isNullOrEmpty(postalCode) ? null : postalCode, distributionLikeExpression, offset, count)
                            )
                    );
                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile recuperare l'elenco dei punti vendita"));
                }
            }
        });

    }

    public PrAsyncTask<ShopEntity> create(
            String name,
            String address,
            String location,
            String postalCode,
            String distribution
    ) {
        return PrAsyncTask.getInstance(new PrJob<ShopEntity>() {
            @Override
            public void run(PrResult<ShopEntity> prResult) {
                try {
                    ShopEntity shopEntity = new ShopEntity();
                    shopEntity.setName(name);
                    shopEntity.setAddress(address);
                    shopEntity.setLocation(location);
                    shopEntity.setPostalCode(postalCode);
                    shopEntity.setDistribution(distribution);
                    shopEntity.setCreatedAt(new Date().getTime());
                    shopEntity.setId(shopDao.create(shopEntity));
                    prResult.resolve(shopEntity);
                } catch (Throwable e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile salvare il punto vendita"));
                }
            }
        });
    }

    public PrAsyncTask<Void> update(ShopEntity shopEntity) {
        return PrAsyncTask.getInstance(new PrJob<Void>() {
            @Override
            public void run(PrResult<Void> prResult) {
                try {
                    shopDao.update(shopEntity);
                    prResult.resolve();
                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile aggiornare il punto vendita"));
                }
            }
        });
    }

    private ShopService(AppDatabase database, ShopDao shopDao) {
        this.database = database;
        this.shopDao = shopDao;
    }

    private final AppDatabase database;
    private final ShopDao shopDao;

}
