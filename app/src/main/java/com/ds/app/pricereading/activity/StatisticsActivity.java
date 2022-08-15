package com.ds.app.pricereading.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.app.pricereading.R;
import com.ds.app.pricereading.activity.support.ResultViewer;
import com.ds.app.pricereading.activity.shop.ShopListActivity;
import com.ds.app.pricereading.db.entity.ProductEntity;
import com.ds.app.pricereading.db.entity.ShopEntity;
import com.ds.app.pricereading.db.entity.statistics.ProductStatisticsEntity;
import com.ds.app.pricereading.db.entity.statistics.ProductStatisticsJoined;
import com.ds.app.pricereading.db.entity.statistics.ShopStatisticsJoined;
import com.ds.app.pricereading.db.entity.util.DateUtil;
import com.ds.app.pricereading.db.entity.util.ProductUtil;
import com.ds.app.pricereading.db.entity.util.ShopUtil;
import com.ds.app.pricereading.service.ProductService;
import com.ds.app.pricereading.service.ShopService;
import com.ds.app.pricereading.service.readingservice.ReadingService;
import com.ds.app.pricereading.service.util.Page;
import com.ds.app.pricereading.util.customasynctask.PrCallback;
import com.ds.app.pricereading.util.customasynctask.PrJobError;
import com.ds.app.pricereading.util.ReferenceHolder;
import com.ds.app.pricereading.db.entity.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;
    public static final int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_INPUT_PRODUCT_ID = "product_id";
    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.statistics_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.statistics_exit_menu:
                Intent intent = new Intent();
                intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Operazione annullata dall'utente");
                setResult(RESULT_CODE_MAIN_CANCELLED, intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_layout);

        productId = getIntent().getLongExtra(EXTRA_KEY_MAIN_INPUT_PRODUCT_ID, 0);

        if (productId == 0l) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Nessun prodotto selezionato");
            setResult(RESULT_CODE_MAIN_KO, intent);
            finish();
            return;
        }

        productNameView = findViewById(R.id.statistics_product_name_view);
        productBarcodeView = findViewById(R.id.statistics_product_barcode_view);
        shopCountView = findViewById(R.id.statistics_shop_count_view);
        shopAddButton = findViewById(R.id.statistics_shop_add_button);
        shopClearButton = findViewById(R.id.statistics_shop_clear_button);
        shopListButton = findViewById(R.id.statistics_shop_list_button);
        statisticsSwitch = findViewById(R.id.statistics_statistics_switch);
        priceLastView = findViewById(R.id.statistics_price_last_view);
        promoLastView = findViewById(R.id.statistics_promo_last_view);
        priceMeanView = findViewById(R.id.statistics_price_mean_view);
        promoMeanView = findViewById(R.id.statistics_promo_mean_view);
        priceMinView = findViewById(R.id.statistics_price_min_view);
        promoMinView = findViewById(R.id.statistics_promo_min_view);
        priceMaxView = findViewById(R.id.statistics_price_max_view);
        promoMaxView = findViewById(R.id.statistics_promo_max_view);
        resultViewer = findViewById(R.id.statistics_result_viewer);

        productService = ProductService.create(getApplicationContext());
        readingService = ReadingService.create(getApplicationContext());
        shopService = ShopService.create(getApplicationContext());

        shopEntityMap = new LinkedHashMap<>();
        productEntityReferenceHolder = new ReferenceHolder<>();

        updateShopListView(null);

        shopAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        StatisticsActivity.this,
                        ShopListActivity.class
                );
                intent.putExtra(ShopListActivity.EXTRA_KEY_PICK_INPUT_MODE, true);
                startActivityForResult(intent, REQUEST_CODE_SHOP_PICK);
            }
        });

        shopClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopEntityMap.clear();
                updateShopListView(null);
                resultViewer.forceRefresh();
            }
        });

        shopListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog
                        .Builder(StatisticsActivity.this)
                        .setAdapter(
                                createShopListAdapter(
                                        StatisticsActivity.this
                                ),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ShopEntity shopEntity = (ShopEntity) shopEntityMap.values().toArray()[which];
                                        shopEntityMap.remove(shopEntity.getId());
                                        resultViewer.forceRefresh();
                                        updateShopListView(null);
                                        dialog.dismiss();
                                    }
                                })
                        .create()
                        .show();
            }
        });

        statisticsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.statistics_statistics_container)
                        .setVisibility(b ? View.VISIBLE : View.GONE);
            }
        });

        readingService
                .getProductStatisticsList(productId)
                .execute(new PrCallback<List<ProductStatisticsJoined>>() {
                    @Override
                    public void call(List<ProductStatisticsJoined> result, PrJobError prJobError) {

                        if (prJobError != null) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, prJobError.getMessage());
                            setResult(RESULT_CODE_MAIN_KO, intent);
                            finish();
                            return;
                        }

                        Map<String, ProductStatisticsJoined> statisticsMap = ReadingService.toMap(result);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // priceLast
                                ProductStatisticsJoined priceLastStatistics = statisticsMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_LAST);
                                if (priceLastStatistics != null) {
                                    priceLastView.setText(
                                            String.format(
                                                    getString(R.string.pr_statistics_product_price_last),
                                                    priceLastStatistics.getPrice(),
                                                    DateUtil.tryFormatTime(priceLastStatistics.getLastUpdateDate()),
                                                    ShopUtil.getStringifiedShop(priceLastStatistics)
                                            )
                                    );
                                }

                                // promoLast
                                ProductStatisticsJoined promoLastStatistics = statisticsMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_LAST);
                                if (promoLastStatistics != null) {
                                    promoLastView.setText(
                                            String.format(
                                                    getString(R.string.pr_statistics_product_promo_last),
                                                    promoLastStatistics.getPrice(),
                                                    DateUtil.tryFormatTime(priceLastStatistics.getLastUpdateDate()),
                                                    ShopUtil.getStringifiedShop(promoLastStatistics)
                                            )
                                    );
                                }

                                // priceMean
                                ProductStatisticsJoined statistics = statisticsMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MEAN);
                                if (statistics != null) {
                                    priceMeanView.setText(
                                            String.format(
                                                    getString(R.string.pr_statistics_product_price_mean),
                                                    statistics.getPrice()
                                            )
                                    );
                                }

                                // promoMean
                                ProductStatisticsJoined promoMeanStatistics = statisticsMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MEAN);
                                if (promoMeanStatistics != null) {
                                    promoMeanView.setText(
                                            String.format(
                                                    getString(R.string.pr_statistics_product_promo_mean),
                                                    promoMeanStatistics.getPrice()
                                            )
                                    );
                                }

                                // priceMin
                                ProductStatisticsJoined priceMinStatistics = statisticsMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MIN);
                                if (priceMinStatistics != null) {
                                    priceMinView.setText(
                                            String.format(
                                                    getString(R.string.pr_statistics_product_price_min),
                                                    priceMinStatistics.getPrice(),
                                                    DateUtil.tryFormatTime(priceLastStatistics.getLastUpdateDate()),
                                                    ShopUtil.getStringifiedShop(priceMinStatistics)
                                            )
                                    );
                                }

                                // promoMin
                                ProductStatisticsJoined promoMinStatistics = statisticsMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MIN);
                                if (promoMinStatistics != null) {
                                    promoMinView.setText(
                                            String.format(
                                                    getString(R.string.pr_statistics_product_promo_min),
                                                    promoMinStatistics.getPrice(),
                                                    DateUtil.tryFormatTime(priceLastStatistics.getLastUpdateDate()),
                                                    ShopUtil.getStringifiedShop(promoMinStatistics)
                                            )
                                    );
                                }

                                // priceMax
                                ProductStatisticsJoined priceMaxStatistics = statisticsMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PRICE_MAX);
                                if (priceMaxStatistics != null) {
                                    priceMaxView.setText(
                                            String.format(
                                                    getString(R.string.pr_statistics_product_price_max),
                                                    priceMaxStatistics.getPrice(),
                                                    DateUtil.tryFormatTime(priceLastStatistics.getLastUpdateDate()),
                                                    ShopUtil.getStringifiedShop(priceMaxStatistics)
                                            )
                                    );
                                }

                                // promoMax
                                ProductStatisticsJoined promoMaxStatistics = statisticsMap.get(ProductStatisticsEntity.STATISTICS_TYPE_PROMO_MAX);
                                if (promoMaxStatistics != null) {
                                    promoMaxView.setText(
                                            String.format(
                                                    getString(R.string.pr_statistics_product_promo_max),
                                                    promoMaxStatistics.getPrice(),
                                                    DateUtil.tryFormatTime(priceLastStatistics.getLastUpdateDate()),
                                                    ShopUtil.getStringifiedShop(promoMaxStatistics)
                                            )
                                    );
                                }

                            }
                        });

                    }
                });

        resultViewer
                .setOnPageRequestedListener(new ResultViewer.OnPageChangedListener() {
                    @Override
                    public void invoke(
                            ResultViewer.ResultViewerPageTransaction pageTransaction,
                            int startIndex,
                            int endIndex
                    ) {

                        List<Long> shopIdList = new ArrayList<>();

                        for (ShopEntity shopEntity : shopEntityMap.values()) {
                            shopIdList.add(shopEntity.getId());
                        }

                        readingService
                                .getShopStatisticsPageByShopIdListAndProductId(
                                        shopIdList,
                                        productId,
                                        pageTransaction.getOffset(),
                                        pageTransaction.getCount()
                                )
                                .execute(new PrCallback<Page<ShopStatisticsJoined>>() {
                                    @Override
                                    public void call(Page<ShopStatisticsJoined> result, PrJobError prJobError) {

                                        if (prJobError != null) {
                                            Toast
                                                    .makeText(
                                                            StatisticsActivity.this,
                                                            prJobError.getMessage(),
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show();
                                            return;
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pageTransaction.commit(
                                                        result.getSize(),
                                                        R.layout.statistics_item_layout,
                                                        new ResultViewer.ItemViewBinderCallback() {
                                                            @Override
                                                            public void invoke(View view, int position) {

                                                                ShopStatisticsJoined statistics = result.getData().get(position);

                                                                TextView itemShopFullNameView = view.findViewById(R.id.statistics_item_shop_full_name_view);
                                                                TextView itemShopFullAddressView = view.findViewById(R.id.statistics_item_shop_full_address_view);
                                                                Switch itemStatisticsSwitch = view.findViewById(R.id.statistics_item_statistics_switch);
                                                                ImageButton itemReadingListButton = view.findViewById(R.id.statistics_item_reading_list_button);
                                                                TextView itemPriceLastView = view.findViewById(R.id.statistics_item_price_last_view);
                                                                TextView itemPromoLastView = view.findViewById(R.id.statistics_item_promo_last_view);
                                                                TextView itemPriceMeanView = view.findViewById(R.id.statistics_item_price_mean_view);
                                                                TextView itemPromoMeanView = view.findViewById(R.id.statistics_item_promo_mean_view);
                                                                TextView itemPriceMinView = view.findViewById(R.id.statistics_item_price_min_view);
                                                                TextView itemPromoMinView = view.findViewById(R.id.statistics_item_promo_min_view);
                                                                TextView itemPriceMaxView = view.findViewById(R.id.statistics_item_price_max_view);
                                                                TextView itemPromoMaxView = view.findViewById(R.id.statistics_item_promo_max_view);

                                                                Long itemShopId = statistics.getShopId();
                                                                Long itemProductId = statistics.getProductId();

                                                                String shopName = statistics.getShopName();
                                                                String shopAddress = statistics.getShopAddress();
                                                                String shopLocation = statistics.getShopLocation();
                                                                String shopPostalCode = statistics.getShopPostalCode();
                                                                String shopDistribution = statistics.getShopDistribution();

                                                                String shopFullName = ShopUtil.getFullName(shopName, shopDistribution);
                                                                String shopFullAddress = ShopUtil.getFullAddress(shopAddress, shopLocation, shopPostalCode);

                                                                if (!StringUtil.isNullOrEmpty(shopFullName)) {
                                                                    itemShopFullNameView.setText(shopFullName);
                                                                }

                                                                if (!StringUtil.isNullOrEmpty(shopFullAddress)) {
                                                                    itemShopFullAddressView.setText(shopFullAddress);
                                                                }

                                                                itemStatisticsSwitch
                                                                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                                            @Override
                                                                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                                                                view
                                                                                        .findViewById(R.id.statistics_item_statistics_container)
                                                                                        .setVisibility(b ? View.VISIBLE : View.GONE);
                                                                            }
                                                                        });

                                                                itemReadingListButton
                                                                        .setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                Intent intent = new Intent(
                                                                                        StatisticsActivity.this,
                                                                                        ReadingListActivity.class
                                                                                );
                                                                                intent.putExtra(ReadingListActivity.EXTRA_KEY_MAIN_INPUT_SHOP_ID, itemShopId);
                                                                                intent.putExtra(ReadingListActivity.EXTRA_KEY_MAIN_INPUT_STRINGIFIED_SHOP, ShopUtil.getStringifiedShop(statistics));
                                                                                intent.putExtra(ReadingListActivity.EXTRA_KEY_MAIN_INPUT_PRODUCT_ID, itemProductId);
                                                                                intent.putExtra(ReadingListActivity.EXTRA_KEY_MAIN_INPUT_STRINGIFIED_PRODUCT, ProductUtil.getStringifiedProduct(productEntityReferenceHolder.getReference()));
                                                                                startActivity(intent);
                                                                            }
                                                                        });

                                                                BigDecimal priceLast = statistics.getPriceLast();
                                                                BigDecimal promoLast = statistics.getPromoLast();
                                                                BigDecimal priceMean = statistics.getPriceMean();
                                                                BigDecimal promoMean = statistics.getPromoMean();
                                                                BigDecimal priceMin = statistics.getPriceMin();
                                                                BigDecimal promoMin = statistics.getPromoMin();
                                                                BigDecimal priceMax = statistics.getPriceMax();
                                                                BigDecimal promoMax = statistics.getPromoMax();

                                                                if (priceLast != null) {
                                                                    itemPriceLastView.setText(
                                                                            String.format(
                                                                                    getString(R.string.pr_statistics_shop_price_last),
                                                                                    priceLast,
                                                                                    DateUtil.tryFormatTime(statistics.getPriceLastLastUpdate())
                                                                            )
                                                                    );
                                                                }

                                                                if (promoLast != null) {
                                                                    itemPromoLastView.setText(
                                                                            String.format(
                                                                                    getString(R.string.pr_statistics_shop_promo_last),
                                                                                    promoLast,
                                                                                    DateUtil.tryFormatTime(statistics.getPromoLastLastUpdate())
                                                                            )
                                                                    );
                                                                }

                                                                if (priceMean != null) {
                                                                    itemPriceMeanView.setText(
                                                                            String.format(
                                                                                    getString(R.string.pr_statistics_shop_price_mean),
                                                                                    priceMean
                                                                            )
                                                                    );
                                                                }

                                                                if (promoMean != null) {
                                                                    itemPromoMeanView.setText(
                                                                            String.format(
                                                                                    getString(R.string.pr_statistics_shop_promo_mean),
                                                                                    promoMean
                                                                            )
                                                                    );
                                                                }

                                                                if (priceMin != null) {
                                                                    itemPriceMinView.setText(
                                                                            String.format(
                                                                                    getString(R.string.pr_statistics_shop_price_min),
                                                                                    priceMin,
                                                                                    DateUtil.tryFormatTime(statistics.getPriceMinLastUpdate())
                                                                            )
                                                                    );
                                                                }

                                                                if (promoMin != null) {
                                                                    itemPromoMinView.setText(
                                                                            String.format(
                                                                                    getString(R.string.pr_statistics_shop_promo_min),
                                                                                    promoMin,
                                                                                    DateUtil.tryFormatTime(statistics.getPromoMinLastUpdate())
                                                                            )
                                                                    );
                                                                }

                                                                if (priceMax != null) {
                                                                    itemPriceMaxView.setText(
                                                                            String.format(
                                                                                    getString(R.string.pr_statistics_shop_price_max),
                                                                                    priceMax,
                                                                                    DateUtil.tryFormatTime(statistics.getPriceMaxLastUpdate())
                                                                            )
                                                                    );
                                                                }

                                                                if (promoMax != null) {
                                                                    itemPromoMaxView.setText(
                                                                            String.format(
                                                                                    getString(R.string.pr_statistics_shop_promo_max),
                                                                                    promoMax,
                                                                                    DateUtil.tryFormatTime(statistics.getPromoMaxLastUpdate())
                                                                            )
                                                                    );
                                                                }


                                                            }
                                                        }
                                                );
                                            }
                                        });

                                    }
                                });

                    }
                });

        productService
                .getProductById(productId)
                .execute(new PrCallback<ProductEntity>() {
                    @Override
                    public void call(ProductEntity result, PrJobError prJobError) {

                        if (prJobError != null) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, prJobError.getMessage());
                            setResult(RESULT_CODE_MAIN_KO, intent);
                            finish();
                            return;
                        }

                        setProductDetails(result);

                    }
                });

    }

    private ListAdapter createShopListAdapter(StatisticsActivity statisticsActivity) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                statisticsActivity,
                R.layout.statistics_shop_list_spinner_layout,
                R.id.statistics_shop_list_spinner_label
        );
        adapter.addAll(createShopLabelList(shopEntityMap.values()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private static List<String> createShopLabelList(Collection<ShopEntity> values) {
        List<String> labelList = new ArrayList<>();
        for (ShopEntity shopEntity : values) {
            labelList.add(ShopUtil.getStringifiedShop(shopEntity));
        }
        return labelList;
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent databack
    ) {
        super.onActivityResult(requestCode, resultCode, databack);

        if (REQUEST_CODE_SHOP_PICK == requestCode) {

            if (ShopListActivity.RESULT_CODE_PICK_OK == resultCode) {

                Long shopId = databack.getLongExtra(ShopListActivity.EXTRA_KEY_PICK_OUTPUT_SHOP_ID, 0);

                shopService
                        .getShopById(shopId)
                        .execute(new PrCallback<ShopEntity>() {
                            @Override
                            public void call(ShopEntity result, PrJobError prJobError) {

                                if (prJobError != null) {
                                    Toast
                                            .makeText(
                                                    StatisticsActivity.this,
                                                    prJobError.getMessage(),
                                                    Toast.LENGTH_LONG
                                            )
                                            .show();
                                    return;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateShopListView(result);
                                        resultViewer.forceRefresh();
                                    }
                                });

                            }
                        });

                return;

            }

            return;

        }

    }

    private void updateShopListView(ShopEntity shopEntity) {
        if (shopEntity != null && !shopEntityMap.containsKey(shopEntity.getId())) {
            shopEntityMap.put(shopEntity.getId(), shopEntity);
        }
        int count = shopEntityMap.values().size();
        shopCountView.setText(
                getResources()
                        .getQuantityString(
                                R.plurals.pr_statistics_shop_count,
                                count,
                                count
                        )
        );
        shopListButton.setEnabled(count > 0);
    }

    private void setProductDetails(ProductEntity productEntity) {

        productEntityReferenceHolder.setReference(productEntity);

        String productName = productEntity.getName();
        String productBarcode = productEntity.getBarcode();

        if (!StringUtil.isNullOrEmpty(productName)) {
            productNameView.setText(productEntity.getName());
        }

        if (!StringUtil.isNullOrEmpty(productBarcode)) {
            productBarcodeView.setText(productEntity.getBarcode());
        }

    }

    private long productId;

    private TextView productNameView;
    private TextView productBarcodeView;
    private TextView shopCountView;
    private ImageButton shopAddButton;
    private ImageButton shopClearButton;
    private ImageButton shopListButton;
    private Switch statisticsSwitch;
    private TextView priceLastView;
    private TextView promoLastView;
    private TextView priceMeanView;
    private TextView promoMeanView;
    private TextView priceMinView;
    private TextView promoMinView;
    private TextView priceMaxView;
    private TextView promoMaxView;
    private ResultViewer resultViewer;

    private ProductService productService;
    private ReadingService readingService;
    private ShopService shopService;

    private LinkedHashMap<Long, ShopEntity> shopEntityMap;
    private ReferenceHolder<ProductEntity> productEntityReferenceHolder;

    private static final int REQUEST_CODE_SHOP_PICK = 1;

}