package com.ds.app.pricereading.activity.product;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.app.pricereading.R;
import com.ds.app.pricereading.activity.BarcodeReaderActivity;
import com.ds.app.pricereading.activity.FlashReadingActivity;
import com.ds.app.pricereading.activity.StatisticsActivity;
import com.ds.app.pricereading.activity.support.ResultViewer;
import com.ds.app.pricereading.db.entity.ProductEntity;
import com.ds.app.pricereading.service.ProductService;
import com.ds.app.pricereading.service.util.Page;
import com.ds.app.pricereading.util.customasynctask.PrCallback;
import com.ds.app.pricereading.util.customasynctask.PrJobError;
import com.ds.app.pricereading.db.entity.util.StringUtil;
import com.ds.app.pricereading.util.ReferenceHolder;

public class ProductListActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;
    public static final int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.product_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.product_list_add_menu:
                Intent intent = new Intent(
                        ProductListActivity.this,
                        ProductAddActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_PRODUCT_ADD);
                return true;

            case R.id.product_list_exit_menu:
                setResult(RESULT_CODE_MAIN_CANCELLED);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_list_layout);

        nameInput = findViewById(R.id.product_list_name_input);
        barcodeInput = findViewById(R.id.product_list_barcode_input);
        barcodeButton = findViewById(R.id.product_list_barcode_button);
        clearButton = findViewById(R.id.product_list_clear_button);
        searchButton = findViewById(R.id.product_list_search_button);
        addButton = findViewById(R.id.product_list_add_button);
        resultViewer = findViewById(R.id.product_list_result_viewer);

        productService = ProductService.create(getApplicationContext());

        barcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        ProductListActivity.this,
                        BarcodeReaderActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_BARCODE_READ);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameInput.setText("");
                barcodeInput.setText("");
                resultViewer.forceRefresh();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultViewer.forceRefresh();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = nameInput.getText().toString();
                String barcode = barcodeInput.getText().toString();

                Intent intent = new Intent(
                        ProductListActivity.this,
                        ProductAddActivity.class
                );

                if (pageReferenceHolder.isNull() || pageReferenceHolder.getReference().isEmpty()) {
                    intent.putExtra(ProductAddActivity.EXTRA_KEY_MAIN_INPUT_NAME, name);
                    intent.putExtra(ProductAddActivity.EXTRA_KEY_MAIN_INPUT_BARCODE, barcode);
                }

                startActivityForResult(intent, REQUEST_CODE_PRODUCT_ADD);

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

                        String name = nameInput.getText().toString();
                        String barcode = barcodeInput.getText().toString();

                        productService
                                .getProductListBy(
                                        name,
                                        barcode,
                                        pageTransaction.getOffset(),
                                        pageTransaction.getCount()
                                )
                                .execute(new PrCallback<Page<ProductEntity>>() {
                                    @Override
                                    public void call(Page<ProductEntity> result, PrJobError prJobError) {

                                        if (prJobError != null) {
                                            Intent intent = new Intent();
                                            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, prJobError.getMessage());
                                            setResult(RESULT_CODE_MAIN_KO, intent);
                                            finish();
                                            return;
                                        }

                                        pageReferenceHolder.setReference(result);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pageTransaction
                                                        .commit(
                                                                result.getSize(),
                                                                R.layout.product_list_item_layout,
                                                                new ResultViewer.ItemViewBinderCallback() {
                                                                    @Override
                                                                    public void invoke(View view, int position) {

                                                                        ProductEntity productEntity = result.getData().get(position);

                                                                        TextView itemNameView = view.findViewById(R.id.product_list_item_name_view);
                                                                        TextView itemBarcodeView = view.findViewById(R.id.product_list_item_barcode_view);
                                                                        ImageButton itemEditButton = view.findViewById(R.id.product_list_item_edit_button);
                                                                        ImageButton itemReadButton = view.findViewById(R.id.product_list_item_read_button);
                                                                        ImageButton itemStatisticsButton = view.findViewById(R.id.product_list_item_statistics_button);

                                                                        Long itemId = productEntity.getId();
                                                                        String itemName = productEntity.getName();
                                                                        String itemBarcode = productEntity.getBarcode();

                                                                        if (!StringUtil.isNullOrEmpty(itemName)) {
                                                                            itemNameView.setText(itemName);
                                                                        }

                                                                        if (!StringUtil.isNullOrEmpty(itemBarcode)) {
                                                                            itemBarcodeView.setText(itemBarcode);
                                                                        }

                                                                        itemEditButton.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                Intent intent = new Intent(
                                                                                        ProductListActivity.this,
                                                                                        ProductEditActivity.class
                                                                                );
                                                                                intent.putExtra(ProductEditActivity.EXTRA_KEY_MAIN_INPUT_PRODUCT_ID, productEntity.getId());
                                                                                startActivityForResult(intent, REQUEST_CODE_PRODUCT_EDIT);
                                                                            }
                                                                        });

                                                                        itemReadButton.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                Intent intent = new Intent(
                                                                                        ProductListActivity.this,
                                                                                        FlashReadingActivity.class
                                                                                );
                                                                                intent.putExtra(FlashReadingActivity.EXTRA_KEY_INPUT_PRODUCT_ID, itemId);
                                                                                startActivityForResult(intent, REQUEST_CODE_PRICE_READ);
                                                                            }
                                                                        });

                                                                        itemStatisticsButton.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                Intent intent = new Intent(
                                                                                        ProductListActivity.this,
                                                                                        StatisticsActivity.class
                                                                                );
                                                                                intent.putExtra(StatisticsActivity.EXTRA_KEY_MAIN_INPUT_PRODUCT_ID, itemId);
                                                                                startActivity(intent);
                                                                            }
                                                                        });

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

        if (REQUEST_CODE_PRODUCT_ADD == requestCode) {

            if (ProductAddActivity.RESULT_CODE_MAIN_OK == resultCode) {

                resultViewer.forceRefresh();

                return;

            }

            if (ProductAddActivity.RESULT_CODE_MAIN_KO == resultCode) {

                Toast
                        .makeText(
                                ProductListActivity.this,
                                databack.getStringExtra(ProductAddActivity.EXTRA_KEY_MAIN_OUTPUT_MESSAGE),
                                Toast.LENGTH_LONG
                        ).show();

                return;

            }

            return;

        }

        if (REQUEST_CODE_PRODUCT_EDIT == requestCode) {

            if (ProductEditActivity.RESULT_CODE_MAIN_OK == resultCode) {

                resultViewer.forceRefresh();

                return;

            }

            if (ProductEditActivity.RESULT_CODE_MAIN_KO == resultCode) {

                Toast
                        .makeText(
                                ProductListActivity.this,
                                databack.getStringExtra(ProductEditActivity.EXTRA_KEY_MAIN_OUTPUT_MESSAGE),
                                Toast.LENGTH_LONG
                        ).show();

                return;

            }

            return;

        }

        if (REQUEST_CODE_BARCODE_READ == requestCode) {

            if (BarcodeReaderActivity.RESULT_CODE_MAIN_KO == resultCode) {

                Toast
                        .makeText(
                                ProductListActivity.this,
                                databack.getStringExtra(BarcodeReaderActivity.EXTRA_KEY_MAIN_OUTPUT_MESSAGE),
                                Toast.LENGTH_LONG
                        )
                        .show();

                return;

            }

            if (BarcodeReaderActivity.RESULT_CODE_MAIN_OK == resultCode) {

                barcodeInput.setText(databack.getStringExtra(BarcodeReaderActivity.EXTRA_KEY_MAIN_OUTPUT_BARCODE));

                return;

            }

            return;

        }

    }

    private EditText nameInput;
    private EditText barcodeInput;
    private ImageButton barcodeButton;
    private ImageButton clearButton;
    private ImageButton searchButton;
    private ImageButton addButton;
    private ResultViewer resultViewer;

    private ProductService productService;

    private ReferenceHolder<Page<ProductEntity>> pageReferenceHolder = new ReferenceHolder<>();

    private static final int REQUEST_CODE_PRODUCT_ADD = 1;
    private static final int REQUEST_CODE_BARCODE_READ = 2;
    private static final int REQUEST_CODE_PRODUCT_EDIT = 3;
    private static final int REQUEST_CODE_PRICE_READ = 4;

}