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
import com.ds.app.pricereading.activity.support.AlertDialogFactory;
import com.ds.app.pricereading.db.entity.ShopEntity;
import com.ds.app.pricereading.service.ShopService;
import com.ds.app.pricereading.service.util.ValidationResult;
import com.ds.app.pricereading.util.customasynctask.PrCallback;
import com.ds.app.pricereading.util.customasynctask.PrJobError;
import com.ds.app.pricereading.util.ReferenceHolder;
import com.ds.app.pricereading.db.entity.util.ShopUtil;
import com.ds.app.pricereading.db.entity.util.StringUtil;
import com.ds.app.pricereading.util.preferredshop.SharedPrefsUtil;
import com.ds.app.pricereading.util.preferredshop.dto.PreferredShop;

import java.util.Date;

public class ShopEditActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;
    public static final int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_INPUT_SHOP_ID = "shop_id";
    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_edit_layout);

        shopId = getIntent().getLongExtra(EXTRA_KEY_MAIN_INPUT_SHOP_ID, 0);

        if (shopId == 0l) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Nessun punto vendita selezionato");
            setResult(RESULT_CODE_MAIN_KO, intent);
            finish();
            return;
        }

        nameInput = findViewById(R.id.shop_edit_name_input);
        addressInput = findViewById(R.id.shop_edit_address_input);
        locationInput = findViewById(R.id.shop_edit_location_input);
        postalCodeInput = findViewById(R.id.shop_edit_postal_code_input);
        distributionInput = findViewById(R.id.shop_edit_distribution_input);
        exitButton = findViewById(R.id.shop_edit_exit_button);
        clearButton = findViewById(R.id.shop_edit_clear_button);
        saveButton = findViewById(R.id.shop_edit_save_button);

        shopService = ShopService.create(getApplicationContext());

        shopEntityReferenceHolder = new ReferenceHolder<>();

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory.createExitDialog(
                        ShopEditActivity.this,
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
                        ShopEditActivity.this,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ShopEntity shopEntity = shopEntityReferenceHolder.getReference();
                                nameInput.setText(shopEntity.getName());
                                addressInput.setText(shopEntity.getAddress());
                                locationInput.setText(shopEntity.getLocation());
                                postalCodeInput.setText(shopEntity.getPostalCode());
                                distributionInput.setText(shopEntity.getDistribution());
                            }
                        }
                );
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialogFactory.createSaveDialog(
                        ShopEditActivity.this,
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
                                                    ShopEditActivity.this,
                                                    validationResult.getCompleteMessage(),
                                                    Toast.LENGTH_LONG
                                            )
                                            .show();
                                    return;
                                }

                                ShopEntity shopEntity = shopEntityReferenceHolder
                                        .getReference()
                                        .clone();

                                shopEntity.setName(name);
                                shopEntity.setAddress(address);
                                shopEntity.setLocation(location);
                                shopEntity.setPostalCode(postalCode);
                                shopEntity.setDistribution(distribution);
                                shopEntity.setUpdatedAt(new Date().getTime());

                                shopService
                                        .update(shopEntity)
                                        .execute(new PrCallback<Void>() {
                                            @Override
                                            public void call(Void data, PrJobError prJobError) {

                                                if (prJobError != null) {
                                                    Intent intent = new Intent();
                                                    intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, prJobError.getMessage());
                                                    setResult(RESULT_CODE_MAIN_KO, intent);
                                                    finish();
                                                    return;
                                                }

                                                PreferredShop preferredShop = SharedPrefsUtil.getPreferredShop(getApplicationContext());

                                                if (shopId == preferredShop.getId()) {
                                                    SharedPrefsUtil.setPreferredShop(getApplicationContext(), shopId, ShopUtil.getStringifiedShop(shopEntity));
                                                }

                                                Intent intent = new Intent();
                                                intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Punto vendita aggiornato con successo");
                                                setResult(RESULT_CODE_MAIN_OK, intent);
                                                finish();
                                                return;

                                            }
                                        });

                            }
                        });
            }
        });

        shopService
                .getShopById(shopId)
                .execute(new PrCallback<ShopEntity>() {
                    @Override
                    public void call(ShopEntity result, PrJobError prJobError) {

                        if (prJobError != null) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, prJobError.getMessage());
                            setResult(RESULT_CODE_MAIN_KO, intent);
                            finish();
                            return;
                        }

                        if (result == null) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Nessun punto vendita trovato");
                            setResult(RESULT_CODE_MAIN_KO, intent);
                            finish();
                            return;
                        }

                        shopEntityReferenceHolder.setReference(result);

                        String name = result.getName();
                        String address = result.getAddress();
                        String location = result.getLocation();
                        String postalCode = result.getPostalCode();
                        String distribution = result.getDistribution();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (!StringUtil.isNullOrEmpty(result.getName())) {
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

                            }
                        });

                    }
                });

    }

    private long shopId;

    private EditText nameInput;
    private EditText addressInput;
    private EditText locationInput;
    private EditText postalCodeInput;
    private EditText distributionInput;
    private ImageButton exitButton;
    private ImageButton clearButton;
    private ImageButton saveButton;

    private ShopService shopService;

    private ReferenceHolder<ShopEntity> shopEntityReferenceHolder;

}