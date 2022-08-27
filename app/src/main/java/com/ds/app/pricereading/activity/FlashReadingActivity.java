package com.ds.app.pricereading.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ds.app.pricereading.R;
import com.ds.app.pricereading.activity.support.AlertDialogFactory;
import com.ds.app.pricereading.activity.shop.ShopListActivity;
import com.ds.app.pricereading.db.entity.ProductEntity;
import com.ds.app.pricereading.db.entity.ReadingEntity;
import com.ds.app.pricereading.db.entity.ShopEntity;
import com.ds.app.pricereading.service.ProductService;
import com.ds.app.pricereading.service.readingservice.ReadingService;
import com.ds.app.pricereading.service.ShopService;
import com.ds.app.pricereading.service.readingservice.dto.CreateDto;
import com.ds.app.pricereading.service.readingservice.dto.CreateWithProductDto;
import com.ds.app.pricereading.util.customasynctask.PrCallback;
import com.ds.app.pricereading.util.customasynctask.PrJobError;
import com.ds.app.pricereading.db.entity.util.DateUtil;
import com.ds.app.pricereading.util.ReferenceHolder;
import com.ds.app.pricereading.db.entity.util.StringUtil;
import com.ds.app.pricereading.util.preferredshop.SharedPrefsUtil;
import com.ds.app.pricereading.util.preferredshop.dto.PreferredShop;

import java.math.BigDecimal;
import java.util.Date;

public class FlashReadingActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;
    public static final int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";
    public static final String EXTRA_KEY_INPUT_PRODUCT_ID = "product_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash_reading_layout);

        productId = getIntent().getLongExtra(EXTRA_KEY_INPUT_PRODUCT_ID, 0);

        shopNameInput = findViewById(R.id.flash_reading_shop_name_input);
        shopSearchButton = findViewById(R.id.flash_reading_shop_search_button);
        shopAddressInput = findViewById(R.id.flash_reading_shop_address_input);
        shopLocationInput = findViewById(R.id.flash_reading_shop_location_input);
        shopPostalCodeInput = findViewById(R.id.flash_reading_shop_postal_code_input);
        shopDistributionInput = findViewById(R.id.flash_reading_shop_distribution_input);
        productNameInput = findViewById(R.id.flash_reading_product_name_input);
        productBarcodeInput = findViewById(R.id.flash_reading_product_barcode_input);
        productBarcodeButton = findViewById(R.id.flash_reading_product_barcode_button);
        priceInput = findViewById(R.id.flash_reading_price_input);
        promoInput = findViewById(R.id.flash_reading_promo_input);
        calendarInput = findViewById(R.id.flash_reading_calendar_input);
        calendarButton = findViewById(R.id.flash_reading_calendar_button);
        exitButton = findViewById(R.id.flash_reading_exit_button);
        clearButton = findViewById(R.id.flash_reading_clear_button);
        saveButton = findViewById(R.id.flash_reading_save_button);

        shopService = ShopService.create(getApplicationContext());
        productService = ProductService.create(getApplicationContext());
        readingService = ReadingService.create(getApplicationContext());

        shopEntityReferenceHolder = new ReferenceHolder<>();
        productEntityReferenceHolder = new ReferenceHolder<>();

        PreferredShop preferredShop = SharedPrefsUtil
                .getPreferredShop(getApplicationContext());

        if (preferredShop != null) {
            shopService
                    .getShopById(preferredShop.getId())
                    .execute(new PrCallback<ShopEntity>() {
                        @Override
                        public void call(ShopEntity result, PrJobError prJobError) {

                            if (prJobError != null) {
                                return;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setShopDetail(result);
                                }
                            });

                        }
                    });
        }

        if (productId != 0l) {
            productService.getProductById(productId)
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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setProductDetail(result);
                                }
                            });

                        }
                    });
        }

        shopSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        FlashReadingActivity.this,
                        ShopListActivity.class
                );
                intent.putExtra(ShopListActivity.EXTRA_KEY_PICK_INPUT_MODE, true);
                startActivityForResult(
                        intent, REQUEST_CODE_PICK_SHOP
                );
            }
        });

        productBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        FlashReadingActivity.this,
                        BarcodeReaderActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_BARCODE_READ);
            }
        });

        setCalendarToToday();

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        FlashReadingActivity.this,
                        CalendarActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_CALENDAR_PICK);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory
                        .createYesAlertDialog(
                                FlashReadingActivity.this,
                                "Sei sicuro di voler uscire?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        setResult(RESULT_CODE_MAIN_CANCELLED);
                                        finish();
                                    }
                                }
                        );
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory
                        .createYesAlertDialog(
                                FlashReadingActivity.this,
                                "Sei sicuro di voler uscire?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        shopNameInput.setText("");
                                        shopAddressInput.setText("");
                                        shopLocationInput.setText("");
                                        shopPostalCodeInput.setText("");
                                        shopDistributionInput.setText("");
                                        productNameInput.setText("");
                                        productBarcodeInput.setText("");
                                        priceInput.setText("");
                                        promoInput.setText("");
                                        calendarInput.setText("");
                                        shopEntityReferenceHolder.setReference(null);
                                        productEntityReferenceHolder.setReference(null);
                                        setCalendarToToday();
                                    }
                                }
                        );
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PrCallback<ReadingEntity> prCallback = new PrCallback<ReadingEntity>() {
                    @Override
                    public void call(ReadingEntity result, PrJobError prJobError) {

                        if (prJobError != null) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, prJobError.getMessage());
                            setResult(RESULT_CODE_MAIN_KO, intent);
                            finish();
                            return;
                        }

                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Rilevazione salvata con successo");
                        setResult(RESULT_CODE_MAIN_OK, intent);
                        finish();

                    }
                };

                String stringifiedPrice = priceInput.getText().toString();
                String stringifiedPromo = promoInput.getText().toString();

                if (StringUtil.isNullOrEmpty(stringifiedPrice) && StringUtil.isNullOrEmpty(stringifiedPromo)) {
                    Toast
                            .makeText(
                                    FlashReadingActivity.this,
                                    "Indicare il prezzo o l'offerta",
                                    Toast.LENGTH_LONG
                            )
                            .show();
                    return;
                }

                BigDecimal price = StringUtil.isNullOrEmpty(stringifiedPrice) ?
                        null :
                        BigDecimal.valueOf(Double.valueOf(stringifiedPrice));
                BigDecimal promo =
                        StringUtil.isNullOrEmpty(stringifiedPromo) ?
                                null :
                                BigDecimal.valueOf(Double.valueOf(stringifiedPromo));
                Date readAt = DateUtil.tryConvertDate(calendarInput.getText().toString());

                if (shopEntityReferenceHolder.isNull()) {
                    Toast
                            .makeText(
                                    FlashReadingActivity.this,
                                    "Selezionare il punto vendita",
                                    Toast.LENGTH_LONG
                            )
                            .show();
                    return;
                }

                if (productEntityReferenceHolder.isNull()) {

                    CreateWithProductDto createWithProductDto = new CreateWithProductDto(
                            shopEntityReferenceHolder.getReference().getId(),
                            productNameInput.getText().toString(),
                            productBarcodeInput.getText().toString(),
                            price,
                            promo,
                            readAt
                    );

                    readingService
                            .create(createWithProductDto)
                            .execute(prCallback);

                    return;

                }

                if (!productEntityReferenceHolder.isNull()) {

                    CreateDto createDto = new CreateDto(
                            shopEntityReferenceHolder.getReference().getId(),
                            productEntityReferenceHolder.getReference().getId(),
                            price,
                            promo,
                            readAt
                    );

                    readingService
                            .create(createDto)
                            .execute(prCallback);

                    return;

                }

            }
        });

    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent databack
    ) {
        super.onActivityResult(requestCode, resultCode, databack);

        if (REQUEST_CODE_PICK_SHOP == requestCode) {

            if (ShopListActivity.RESULT_CODE_PICK_OK == resultCode) {

                long shopId = databack.getLongExtra(ShopListActivity.EXTRA_KEY_PICK_OUTPUT_SHOP_ID, 0);

                shopService
                        .getShopById(shopId)
                        .execute(new PrCallback<ShopEntity>() {
                            @Override
                            public void call(ShopEntity result, PrJobError prJobError) {

                                if (prJobError != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast
                                                    .makeText(
                                                            FlashReadingActivity.this,
                                                            prJobError.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show();
                                        }
                                    });
                                    return;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setShopDetail(result);
                                    }
                                });

                            }
                        });

                return;

            }

            return;

        }

        if (REQUEST_CODE_BARCODE_READ == requestCode) {

            if (BarcodeReaderActivity.RESULT_CODE_MAIN_OK == resultCode) {

                String barcode = databack.getStringExtra(BarcodeReaderActivity.EXTRA_KEY_MAIN_OUTPUT_BARCODE);

                productService
                        .getProductByBarcode(barcode)
                        .execute(new PrCallback<ProductEntity>() {
                            @Override
                            public void call(ProductEntity result, PrJobError prJobError) {

                                if (prJobError != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast
                                                    .makeText(
                                                            FlashReadingActivity.this,
                                                            prJobError.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show();
                                        }
                                    });
                                    return;
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setProductDetail(result, barcode);
                                    }
                                });

                            }
                        });

                return;

            }

            return;

        }

        if (REQUEST_CODE_CALENDAR_PICK == requestCode) {

            if (CalendarActivity.RESULT_CODE_MAIN_OK == resultCode) {

                calendarInput.setText(DateUtil.tryFormatTime(databack.getLongExtra(CalendarActivity.EXTRA_KEY_MAIN_OUTPUT_DATE, 0l)));

                return;

            }

            return;

        }

    }

    private void setProductDetail(ProductEntity productEntity) {
        setProductDetail(productEntity, "");
    }

    private void setProductDetail(ProductEntity productEntity, String barcode) {

        if (productEntity == null) {

            productEntityReferenceHolder.setReference(null);

            productNameInput.setText("");
            productBarcodeInput.setText(barcode);

            return;

        }

        productEntityReferenceHolder.setReference(productEntity);

        productNameInput.setText(productEntity.getName());
        productBarcodeInput.setText(productEntity.getBarcode());

    }

    private void setShopDetail(ShopEntity shopEntity) {

        if (shopEntity == null) {

            shopEntityReferenceHolder.setReference(null);

            shopNameInput.setText("");
            shopAddressInput.setText("");
            shopLocationInput.setText("");
            shopPostalCodeInput.setText("");
            shopDistributionInput.setText("");

            return;

        }

        shopEntityReferenceHolder.setReference(shopEntity);

        shopNameInput.setText(shopEntity.getName());
        shopAddressInput.setText(shopEntity.getAddress());
        shopLocationInput.setText(shopEntity.getLocation());
        shopPostalCodeInput.setText(shopEntity.getPostalCode());
        shopDistributionInput.setText(shopEntity.getDistribution());

    }

    private void setCalendarToToday() {
        calendarInput.setText(DateUtil.tryFormatTime(new Date()));
    }

    private long productId;

    private EditText shopNameInput;
    private ImageButton shopSearchButton;
    private EditText shopAddressInput;
    private EditText shopLocationInput;
    private EditText shopPostalCodeInput;
    private EditText shopDistributionInput;
    private EditText productNameInput;
    private EditText productBarcodeInput;
    private ImageButton productBarcodeButton;
    private EditText priceInput;
    private EditText promoInput;
    private EditText calendarInput;
    private ImageButton calendarButton;
    private ImageButton exitButton;
    private ImageButton clearButton;
    private ImageButton saveButton;

    private ShopService shopService;
    private ProductService productService;
    private ReadingService readingService;

    private ReferenceHolder<ShopEntity> shopEntityReferenceHolder;
    private ReferenceHolder<ProductEntity> productEntityReferenceHolder;

    private static final int REQUEST_CODE_PICK_SHOP = 1;
    private static final int REQUEST_CODE_BARCODE_READ = 2;
    private static final int REQUEST_CODE_CALENDAR_PICK = 3;

}