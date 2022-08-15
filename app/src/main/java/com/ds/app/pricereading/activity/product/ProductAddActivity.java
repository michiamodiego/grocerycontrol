package com.ds.app.pricereading.activity.product;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ds.app.pricereading.R;
import com.ds.app.pricereading.activity.BarcodeReaderActivity;
import com.ds.app.pricereading.activity.FlashReadingActivity;
import com.ds.app.pricereading.activity.support.AlertDialogFactory;
import com.ds.app.pricereading.db.entity.ProductEntity;
import com.ds.app.pricereading.db.entity.util.StringUtil;
import com.ds.app.pricereading.service.ProductService;
import com.ds.app.pricereading.service.util.customasynctask.PrCallback;
import com.ds.app.pricereading.service.util.customasynctask.PrJobError;

public class ProductAddActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;
    public static final int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_INPUT_NAME = "name";
    public static final String EXTRA_KEY_MAIN_INPUT_BARCODE = "barcode";
    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";
    public static final String EXTRA_KEY_MAIN_OUTPUT_PRODUCT_ID = "product_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_add_layout);

        nameInput = findViewById(R.id.product_add_name_input);
        barcodeInput = findViewById(R.id.product_add_barcode_input);
        barcodeButton = findViewById(R.id.product_add_barcode_button);
        exitButton = findViewById(R.id.product_add_exit_button);
        clearButton = findViewById(R.id.product_add_clear_button);
        saveButton = findViewById(R.id.product_add_save_button);

        productService = ProductService.create(getApplicationContext());

        Intent intent = getIntent();
        String name = intent.getStringExtra(EXTRA_KEY_MAIN_INPUT_NAME);
        String barcode = intent.getStringExtra(EXTRA_KEY_MAIN_INPUT_BARCODE);

        if (!StringUtil.isNullOrEmpty(name)) {
            nameInput.setText(name);
        }

        if (!StringUtil.isNullOrEmpty(barcode)) {
            barcodeInput.setText(barcode);
        }

        barcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        ProductAddActivity.this,
                        BarcodeReaderActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_BARCODE_READ);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory.createExitDialog(
                        ProductAddActivity.this,
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
                AlertDialogFactory.createAbortDialog(
                        ProductAddActivity.this,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                nameInput.setText("");
                                barcodeInput.setText("");
                            }
                        }
                );
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory.createSaveDialog(
                        ProductAddActivity.this,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String name = nameInput.getText().toString();
                                String barcode = barcodeInput.getText().toString();

                                productService
                                        .create(name, barcode)
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

                                                Intent intent = new Intent();
                                                intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Prodotto creato con successo");
                                                intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_PRODUCT_ID, result.getId());
                                                setResult(RESULT_CODE_MAIN_OK, intent);
                                                finish();

                                            }
                                        });

                            }
                        }
                );
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

        if (REQUEST_CODE_BARCODE_READ == requestCode) {

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
    private ImageButton exitButton;
    private ImageButton clearButton;
    private ImageButton saveButton;

    private ProductService productService;

    private static final int REQUEST_CODE_BARCODE_READ = 1;

}