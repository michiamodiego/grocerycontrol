package com.ds.app.pricereading.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ds.app.pricereading.R;
import com.ds.app.pricereading.activity.support.ResultViewer;
import com.ds.app.pricereading.db.entity.ReadingEntity;
import com.ds.app.pricereading.service.readingservice.ReadingService;
import com.ds.app.pricereading.service.util.Page;
import com.ds.app.pricereading.util.customasynctask.PrCallback;
import com.ds.app.pricereading.util.customasynctask.PrJobError;
import com.ds.app.pricereading.db.entity.util.DateUtil;
import com.ds.app.pricereading.db.entity.util.StringUtil;

import java.math.BigDecimal;
import java.util.Date;

public class ReadingListActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;
    public static final int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_INPUT_SHOP_ID = "shop_id";
    public static final String EXTRA_KEY_MAIN_INPUT_STRINGIFIED_SHOP = "stringified_shop";
    public static final String EXTRA_KEY_MAIN_INPUT_PRODUCT_ID = "product_id";
    public static final String EXTRA_KEY_MAIN_INPUT_STRINGIFIED_PRODUCT = "stringified_product";
    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reading_list_layout);

        shopId = getIntent().getLongExtra(EXTRA_KEY_MAIN_INPUT_SHOP_ID, 0);
        stringifiedShop = getIntent().getStringExtra(EXTRA_KEY_MAIN_INPUT_STRINGIFIED_SHOP);
        productId = getIntent().getLongExtra(EXTRA_KEY_MAIN_INPUT_PRODUCT_ID, 0);
        stringifiedProduct = getIntent().getStringExtra(EXTRA_KEY_MAIN_INPUT_STRINGIFIED_PRODUCT);

        shopView = findViewById(R.id.reading_list_shop_view);
        productView = findViewById(R.id.reading_list_product_view);
        fromInput = findViewById(R.id.reading_list_from_input);
        fromButton = findViewById(R.id.reading_list_from_button);
        toInput = findViewById(R.id.reading_list_to_input);
        toButton = findViewById(R.id.reading_list_to_button);
        clearButton = findViewById(R.id.reading_list_clear_button);
        searchButton = findViewById(R.id.reading_list_search_button);
        resultViewer = findViewById(R.id.reading_list_result_viewer);

        readingService = ReadingService.create(ReadingListActivity.this);

        shopView.setText(stringifiedShop);
        productView.setText(stringifiedProduct);

        fromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        ReadingListActivity.this,
                        CalendarActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_CALENDAR_PICK_FROM);
            }
        });

        toButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        ReadingListActivity.this,
                        CalendarActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_CALENDAR_PICK_TO);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromInput.setText("");
                toInput.setText("");
                resultViewer.forceRefresh();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultViewer.forceRefresh();
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

                        String stringifiedFrom = fromInput.getText().toString();
                        String stringifiedTo = toInput.getText().toString();

                        Date from = null;
                        Date to = null;

                        if (!StringUtil.isNullOrEmpty(stringifiedFrom)) {
                            from = DateUtil.tryConvertDate(stringifiedFrom);
                        }

                        if (!StringUtil.isNullOrEmpty(stringifiedTo)) {
                            to = DateUtil.tryConvertDate(stringifiedTo);
                        }

                        readingService
                                .getReadingList(from, to)
                                .execute(new PrCallback<Page<ReadingEntity>>() {
                                    @Override
                                    public void call(Page<ReadingEntity> result, PrJobError prJobError) {

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
                                                pageTransaction.commit(
                                                        result.getSize(),
                                                        R.layout.reading_list_item_layout,
                                                        new ResultViewer.ItemViewBinderCallback() {
                                                            @Override
                                                            public void invoke(View view, int position) {

                                                                ReadingEntity readingEntity = result.getData().get(position);

                                                                TextView itemPriceView = view.findViewById(R.id.reading_list_item_price_view);
                                                                TextView itemPromoView = view.findViewById(R.id.reading_list_item_promo_view);
                                                                TextView itemReadAtView = view.findViewById(R.id.reading_list_item_read_at_view);

                                                                BigDecimal itemPrice = readingEntity.getPrice();
                                                                BigDecimal itemPromo = readingEntity.getPromo();
                                                                Long itemReadAt = readingEntity.getReadAt();

                                                                if (itemPrice != null) {
                                                                    itemPriceView.setText(String.format(getString(R.string.pr_price), itemPrice.toString()));
                                                                }

                                                                if (itemPromo != null) {
                                                                    itemPromoView.setText(String.format(getString(R.string.pr_price), itemPromo.toString()));
                                                                }

                                                                if (itemReadAt != null) {
                                                                    itemReadAtView.setText(DateUtil.tryFormatTime(itemReadAt));
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

    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent databack
    ) {
        super.onActivityResult(requestCode, resultCode, databack);

        if (REQUEST_CODE_CALENDAR_PICK_FROM == requestCode) {

            if (CalendarActivity.RESULT_CODE_MAIN_OK == resultCode) {

                fromInput.setText(DateUtil.tryFormatTime(databack.getLongExtra(CalendarActivity.EXTRA_KEY_MAIN_OUTPUT_DATE, 0l)));

                return;

            }

            return;

        }

        if (REQUEST_CODE_CALENDAR_PICK_TO == requestCode) {

            if (CalendarActivity.RESULT_CODE_MAIN_OK == resultCode) {

                toInput.setText(DateUtil.tryFormatTime(databack.getLongExtra(CalendarActivity.EXTRA_KEY_MAIN_OUTPUT_DATE, 0l)));

                return;

            }

            return;

        }

    }

    private Long shopId;
    private String stringifiedShop;
    private Long productId;
    private String stringifiedProduct;

    private TextView shopView;
    private TextView productView;
    private EditText fromInput;
    private ImageButton fromButton;
    private EditText toInput;
    private ImageButton toButton;
    private ImageButton clearButton;
    private ImageButton searchButton;
    private ResultViewer resultViewer;

    private ReadingService readingService;

    private static final int REQUEST_CODE_CALENDAR_PICK_FROM = 1;
    private static final int REQUEST_CODE_CALENDAR_PICK_TO = 2;

}