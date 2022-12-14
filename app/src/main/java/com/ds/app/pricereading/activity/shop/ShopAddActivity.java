package com.ds.app.pricereading.activity.shop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ds.app.pricereading.R;
import com.ds.app.pricereading.activity.product.ProductAddActivity;
import com.ds.app.pricereading.activity.support.AlertDialogFactory;
import com.ds.app.pricereading.db.entity.ShopEntity;
import com.ds.app.pricereading.db.entity.util.StringUtil;
import com.ds.app.pricereading.service.ShopService;
import com.ds.app.pricereading.service.util.ValidationResult;
import com.ds.app.pricereading.util.customasynctask.PrCallback;
import com.ds.app.pricereading.util.customasynctask.PrJobError;

public class ShopAddActivity extends AppCompatActivity {

    public static int RESULT_CODE_MAIN_OK = 1;
    public static int RESULT_CODE_MAIN_KO = 2;
    public static int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_INPUT_NAME = "name";
    public static final String EXTRA_KEY_MAIN_INPUT_ADDRESS = "address";
    public static final String EXTRA_KEY_MAIN_INPUT_LOCATION = "location";
    public static final String EXTRA_KEY_MAIN_INPUT_POSTAL_CODE = "postal_code";
    public static final String EXTRA_KEY_MAIN_INPUT_DISTRIBUTION = "distribution";
    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";
    public static final String EXTRA_KEY_MAIN_OUTPUT_SHOP_ID = "shop_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_add_layout);

        nameInput = findViewById(R.id.shop_add_name_input);
        addressInput = findViewById(R.id.shop_add_address_input);
        locationInput = findViewById(R.id.shop_add_location_input);
        postalCodeInput = findViewById(R.id.shop_add_postal_code_input);
        distributionInput = findViewById(R.id.shop_add_distribution_input);
        exitButton = findViewById(R.id.shop_add_exit_button);
        clearButton = findViewById(R.id.shop_add_clear_button);
        saveButton = findViewById(R.id.shop_add_save_button);

        shopService = ShopService.create(getApplicationContext());

        Intent intent = getIntent();
        String name = intent.getStringExtra(EXTRA_KEY_MAIN_INPUT_NAME);
        String address = intent.getStringExtra(EXTRA_KEY_MAIN_INPUT_ADDRESS);
        String location = intent.getStringExtra(EXTRA_KEY_MAIN_INPUT_LOCATION);
        String postalCode = intent.getStringExtra(EXTRA_KEY_MAIN_INPUT_POSTAL_CODE);
        String distribution = intent.getStringExtra(EXTRA_KEY_MAIN_INPUT_DISTRIBUTION);

        if (!StringUtil.isNullOrEmpty(name)) {
            nameInput.setText(name);
        }

        if (!StringUtil.isNullOrEmpty(address)) {
            addressInput.setText(address);
        }

        if (!StringUtil.isNullOrEmpty(location)) {
            locationInput.setText(location);
        }

        if (!StringUtil.isNullOrEmpty(postalCode)) {
            postalCodeInput.setText(postalCode);
        }

        if (!StringUtil.isNullOrEmpty(distribution)) {
            distributionInput.setText(distribution);
        }

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory
                        .createExitDialog(
                                ShopAddActivity.this,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent();
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
                        .createAbortDialog(
                                ShopAddActivity.this,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        nameInput.setText("");
                                        addressInput.setText("");
                                        locationInput.setText("");
                                        postalCodeInput.setText("");
                                        distributionInput.setText("");
                                    }
                                }
                        );
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory.createSaveDialog(
                        ShopAddActivity.this,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String name = nameInput.getText().toString();
                                String address = addressInput.getText().toString();
                                String location = locationInput.getText().toString();
                                String postalCode = postalCodeInput.getText().toString();
                                String distribution = distributionInput.getText().toString();

                                ValidationResult validationResult = shopService.isValid(
                                        name,
                                        address,
                                        location,
                                        postalCode,
                                        distribution
                                );

                                if (validationResult.anyError()) {
                                    Toast
                                            .makeText(
                                                    ShopAddActivity.this,
                                                    validationResult.getCompleteMessage(),
                                                    Toast.LENGTH_LONG
                                            )
                                            .show();
                                    return;
                                }

                                shopService
                                        .create(
                                                name,
                                                address,
                                                location,
                                                postalCode,
                                                distribution
                                        )
                                        .execute(new PrCallback<ShopEntity>() {
                                            @Override
                                            public void call(ShopEntity result, PrJobError prJobError) {

                                                if (prJobError != null) {
                                                    Intent intent = new Intent();
                                                    intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, prJobError.getMessage());
                                                    setResult(RESULT_CODE_MAIN_OK, intent);
                                                    finish();
                                                    return;
                                                }

                                                Intent intent = new Intent();
                                                intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Punto vendita creato con successo");
                                                intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_SHOP_ID, result.getId());
                                                setResult(RESULT_CODE_MAIN_OK, intent);
                                                finish();
                                                return;

                                            }
                                        });

                            }
                        }
                );
            }
        });

    }

    private EditText nameInput;
    private EditText addressInput;
    private EditText locationInput;
    private EditText postalCodeInput;
    private EditText distributionInput;
    private ImageButton exitButton;
    private ImageButton clearButton;
    private ImageButton saveButton;

    private ShopService shopService;

}