package com.ds.app.pricereading.service;

import android.content.Context;

import com.ds.app.pricereading.db.AppDatabase;
import com.ds.app.pricereading.db.dao.ProductDao;
import com.ds.app.pricereading.db.AppDatabaseAccessor;
import com.ds.app.pricereading.db.entity.ProductEntity;
import com.ds.app.pricereading.db.util.DbUtil;
import com.ds.app.pricereading.service.util.Page;
import com.ds.app.pricereading.service.util.customasynctask.PrAsyncTask;
import com.ds.app.pricereading.service.util.customasynctask.PrJob;
import com.ds.app.pricereading.service.util.customasynctask.PrJobError;
import com.ds.app.pricereading.service.util.customasynctask.PrResult;
import com.ds.app.pricereading.db.entity.util.StringUtil;

import java.util.Date;

public class ProductService {

    public static final ProductService create(Context context) {
        AppDatabase database = AppDatabaseAccessor.getInstance(context);
        return new ProductService(database, database.getProductDao());
    }

    public PrAsyncTask<ProductEntity> getProductById(long productId) {
        return PrAsyncTask.getInstance(new PrJob<ProductEntity>() {
            @Override
            public void run(PrResult<ProductEntity> prResult) {
                try {
                    prResult.resolve(productDao.findById(productId));
                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile recuperare il prodotto"));
                }
            }
        });
    }

    public PrAsyncTask<ProductEntity> getProductByBarcode(String barcode) {
        return PrAsyncTask.getInstance(new PrJob<ProductEntity>() {
            @Override
            public void run(PrResult<ProductEntity> prResult) {
                try {
                    if (StringUtil.isNullOrEmpty(barcode)) {
                        prResult.resolve();
                    }
                    prResult.resolve(productDao.findByBarcode(barcode));
                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile recuperare il prodotto"));
                }
            }
        });
    }

    public PrAsyncTask<Page<ProductEntity>> getProductListBy(String name, String barcode, int offset, int count) {
        return PrAsyncTask.getInstance(new PrJob<Page<ProductEntity>>() {
            @Override
            public void run(PrResult<Page<ProductEntity>> prResult) {
                try {

                    String nameLikeExpression = DbUtil.createLikeExpression(name);

                    prResult.resolve(
                            new Page(
                                    productDao.countBy(nameLikeExpression, StringUtil.isNullOrEmpty(barcode) ? null : barcode),
                                    productDao.findProductListBy(nameLikeExpression, StringUtil.isNullOrEmpty(barcode) ? null : barcode, offset, count)
                            )
                    );

                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile recuperare l'elenco dei prodotti"));
                }
            }
        });
    }

    public PrAsyncTask<ProductEntity> create(String name, String barcode) {
        return PrAsyncTask.getInstance(new PrJob<ProductEntity>() {
            @Override
            public void run(PrResult<ProductEntity> prResult) {
                try {
                    ProductEntity productEntity = new ProductEntity();
                    productEntity.setName(name);
                    productEntity.setBarcode(barcode);
                    productEntity.setCreatedAt(new Date().getTime());
                    productEntity.setId(productDao.create(productEntity));
                    prResult.resolve(productEntity);
                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile creare il prodotto"));
                }
            }
        });
    }

    public PrAsyncTask<Void> update(ProductEntity productEntity) {
        return PrAsyncTask.getInstance(new PrJob<Void>() {
            @Override
            public void run(PrResult<Void> prResult) {
                try {
                    productEntity.setUpdateAt(new Date().getTime());
                    productDao.update(productEntity);
                    prResult.resolve();
                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile aggiornare il prodotto"));
                }
            }
        });
    }

    public PrAsyncTask<ProductEntity> createOrUpdate(String name, String barcode) {
        return PrAsyncTask.getInstance(new PrJob<ProductEntity>() {
            @Override
            public void run(PrResult<ProductEntity> prResult) {
                try {
                    ProductEntity productEntity = getProductByBarcode(barcode)
                            .trySynchronize()
                            .getDataOrThrowException();
                    if (productEntity != null) {
                        update(productEntity)
                                .trySynchronize()
                                .getDataOrThrowException();
                        prResult.resolve(productEntity);
                    } else {
                        prResult.resolve(
                                create(name, barcode)
                                        .trySynchronize()
                                        .getDataOrThrowException()
                        );
                    }
                } catch (Exception e) {
                    prResult.resolve(new PrJobError("Errore tecnico: impossibile creare/aggiornare il prodotto"));
                }
            }
        });
    }

    private ProductService(AppDatabase database, ProductDao productDao) {
        this.database = database;
        this.productDao = productDao;
    }

    private final AppDatabase database;
    private final ProductDao productDao;

}
