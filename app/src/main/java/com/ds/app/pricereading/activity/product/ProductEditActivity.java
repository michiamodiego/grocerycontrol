package com.ds.app.pricereading.activity.product;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ds.app.pricereading.R;
import com.ds.app.pricereading.activity.BarcodeReaderActivity;
import com.ds.app.pricereading.activity.support.AlertDialogFactory;
import com.ds.app.pricereading.db.entity.ProductEntity;
import com.ds.app.pricereading.service.ProductService;
import com.ds.app.pricereading.service.util.customasynctask.PrCallback;
import com.ds.app.pricereading.service.util.customasynctask.PrJobError;
import com.ds.app.pricereading.util.ReferenceHolder;
import com.ds.app.pricereading.db.entity.util.StringUtil;

import java.util.Date;

public class ProductEditActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;
    public static final int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_INPUT_PRODUCT_ID = "product_id";
    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_edit_layout);

        productId = getIntent().getLongExtra(EXTRA_KEY_MAIN_INPUT_PRODUCT_ID, 0);

        if (productId == 0l) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Nessun prodotto selezionato");
            setResult(RESULT_CODE_MAIN_KO, intent);
            finish();
            return;
        }

        nameInput = findViewById(R.id.product_edit_name_input);
        barcodeInput = findViewById(R.id.product_edit_barcode_input);
        barcodeButton = findViewById(R.id.product_edit_barcode_button);
        exitButton = findViewById(R.id.product_edit_exit_button);
        clearButton = findViewById(R.id.product_edit_clear_button);
        saveButton = findViewById(R.id.product_edit_save_button);

        productService = ProductService.create(getApplicationContext());

        productEntityReferenceHolder = new ReferenceHolder<>();

        barcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        ProductEditActivity.this,
                        BarcodeReaderActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_BARCODE_READ);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory.createExitDialog(
                        ProductEditActivity.this,
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
                        ProductEditActivity.this,
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

                String name = nameInput.getText().toString().toUpperCase();
                String barcode = barcodeInput.getText().toString().toUpperCase();

                ProductEntity productEntity = productEntityReferenceHolder.getReference().clone();

                productEntity.setName(name);
                productEntity.setBarcode(barcode);

                productService
                        .update(productEntity)
                        .execute(new PrCallback<Void>() {
                            @Override
                            public void call(Void result, PrJobError prJobError) {

                                if (prJobError != null) {
                                    Intent intent = new Intent();
                                    intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, prJobError.getMessage());
                                    setResult(RESULT_CODE_MAIN_OK);
                                    finish();
                                    return;
                                }

                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Prodotto aggiornato con successo");
                                setResult(RESULT_CODE_MAIN_OK, intent);
                                finish();

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

                        if (result == null) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Il prodotto selezionato non esiste");
                            setResult(RESULT_CODE_MAIN_KO, intent);
                            finish();
                            return;
                        }

                        productEntityReferenceHolder.setReference(result);

                        String name = result.getName();
                        String barcode = result.getBarcode();

                        if (!StringUtil.isNullOrEmpty(name)) {
                            nameInput.setText(name);
                        }

                        if (!StringUtil.isNullOrEmpty(barcode)) {
                            barcodeInput.setText(barcode);
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

        if (REQUEST_CODE_BARCODE_READ == requestCode) {

            if (BarcodeReaderActivity.RESULT_CODE_MAIN_KO == resultCode) {

                Toast
                        .makeText(
                                ProductEditActivity.this,
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

    private long productId;

    private EditText nameInput;
    private EditText barcodeInput;
    private ImageButton barcodeButton;
    private ImageButton exitButton;
    private ImageButton clearButton;
    private ImageButton saveButton;

    private ProductService productService;

    private ReferenceHolder<ProductEntity> productEntityReferenceHolder;

    private static final int REQUEST_CODE_BARCODE_READ = 1;

}