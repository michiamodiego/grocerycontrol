package com.ds.app.pricereading.activity.shop;

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
import com.ds.app.pricereading.activity.support.ResultViewer;
import com.ds.app.pricereading.db.entity.ShopEntity;
import com.ds.app.pricereading.service.util.Page;
import com.ds.app.pricereading.service.ShopService;
import com.ds.app.pricereading.service.util.customasynctask.PrCallback;
import com.ds.app.pricereading.service.util.customasynctask.PrJobError;
import com.ds.app.pricereading.db.entity.util.ShopUtil;
import com.ds.app.pricereading.db.entity.util.StringUtil;
import com.ds.app.pricereading.util.preferredshop.SharedPrefsUtil;

public class ShopListActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;
    public static final int RESULT_CODE_MAIN_CANCELLED = 3;

    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";

    public static final int RESULT_CODE_PICK_OK = 4;

    public static final String EXTRA_KEY_PICK_INPUT_MODE = "mode";
    public static final String EXTRA_KEY_PICK_OUTPUT_SHOP_ID = "shop_id";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shop_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shop_list_add_menu:
                Intent intent = new Intent(
                        ShopListActivity.this,
                        ShopAddActivity.class
                );
                startActivityForResult(intent, REQUEST_CODE_SHOP_ADD);
                return true;

            case R.id.shop_list_exit_menu:
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
        setContentView(R.layout.shop_list_layout);

        nameInput = findViewById(R.id.shop_list_name_input);
        addressInput = findViewById(R.id.shop_list_address_input);
        locationInput = findViewById(R.id.shop_list_location_input);
        postalCodeInput = findViewById(R.id.shop_list_postal_code_input);
        distributionInput = findViewById(R.id.shop_list_distribution_input);
        clearButton = findViewById(R.id.shop_list_clear_button);
        searchButton = findViewById(R.id.shop_list_search_button);
        resultViewer = findViewById(R.id.shop_list_result_viewer);

        shopService = ShopService.create(getApplicationContext());

        pickMode = getIntent().getBooleanExtra(EXTRA_KEY_PICK_INPUT_MODE, false);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameInput.setText("");
                addressInput.setText("");
                locationInput.setText("");
                postalCodeInput.setText("");
                distributionInput.setText("");
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

                        String name = nameInput.getText().toString();
                        String address = addressInput.getText().toString();
                        String location = locationInput.getText().toString();
                        String postalCode = postalCodeInput.getText().toString();
                        String distribution = distributionInput.getText().toString();

                        shopService
                                .getShopListBy(
                                        name,
                                        address,
                                        location,
                                        postalCode,
                                        distribution,
                                        pageTransaction.getOffset(),
                                        pageTransaction.getCount()
                                )
                                .execute(new PrCallback<Page<ShopEntity>>() {
                                    @Override
                                    public void call(Page<ShopEntity> result, PrJobError prJobError) {

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
                                                pageTransaction
                                                        .commit(
                                                                result.getSize(),
                                                                R.layout.shop_list_item_layout,
                                                                new ResultViewer.ItemViewBinderCallback() {
                                                                    @Override
                                                                    public void invoke(View view, int position) {

                                                                        ShopEntity shopEntity = result.getData().get(position);

                                                                        TextView itemNameView = view.findViewById(R.id.shop_list_item_name_view);
                                                                        TextView itemAddressView = view.findViewById(R.id.shop_list_item_address_view);
                                                                        TextView itemLocationView = view.findViewById(R.id.shop_list_item_location_view);
                                                                        TextView itemPostalCodeView = view.findViewById(R.id.shop_list_item_postal_code_view);
                                                                        TextView itemDistributionView = view.findViewById(R.id.shop_list_item_distribution_view);
                                                                        ImageButton itemEditButton = view.findViewById(R.id.shop_list_item_edit_button);
                                                                        ImageButton itemPinButton = view.findViewById(R.id.shop_list_item_pin_button);
                                                                        ImageButton itemPickButton = view.findViewById(R.id.shop_list_item_pick_button);

                                                                        if (!pickMode) {
                                                                            itemPickButton.setVisibility(View.GONE);
                                                                        } else {
                                                                            itemEditButton.setVisibility(View.GONE);
                                                                            itemPinButton.setVisibility(View.GONE);
                                                                        }

                                                                        Long itemId = shopEntity.getId();
                                                                        String itemName = shopEntity.getName();
                                                                        String itemAddress = shopEntity.getAddress();
                                                                        String itemLocation = shopEntity.getLocation();
                                                                        String itemPostalCode = shopEntity.getPostalCode();
                                                                        String itemDistribution = shopEntity.getDistribution();

                                                                        if (!StringUtil.isNullOrEmpty(itemName)) {
                                                                            itemNameView.setText(shopEntity.getName());
                                                                        }

                                                                        if (!StringUtil.isNullOrEmpty(itemAddress)) {
                                                                            itemAddressView.setText(shopEntity.getAddress());
                                                                        }

                                                                        if (!StringUtil.isNullOrEmpty(itemLocation)) {
                                                                            itemLocationView.setText(shopEntity.getLocation());
                                                                        }

                                                                        if (!StringUtil.isNullOrEmpty(itemPostalCode)) {
                                                                            itemPostalCodeView.setText(shopEntity.getPostalCode());
                                                                        }

                                                                        if (!StringUtil.isNullOrEmpty(itemDistribution)) {
                                                                            itemDistributionView.setText(shopEntity.getDistribution());
                                                                        }

                                                                        itemEditButton.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                Intent intent = new Intent(
                                                                                        ShopListActivity.this,
                                                                                        ShopEditActivity.class
                                                                                );
                                                                                intent.putExtra(ShopEditActivity.EXTRA_KEY_MAIN_INPUT_SHOP_ID, shopEntity.getId());
                                                                                startActivityForResult(intent, REQUEST_CODE_SHOP_EDIT);
                                                                            }
                                                                        });

                                                                        itemPinButton.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                SharedPrefsUtil.setPreferredShop(
                                                                                        ShopListActivity.this,
                                                                                        itemId,
                                                                                        ShopUtil.getStringifiedShop(shopEntity)
                                                                                );
                                                                                Toast
                                                                                        .makeText(
                                                                                                ShopListActivity.this,
                                                                                                "Punto vendita impostato come predefinito",
                                                                                                Toast.LENGTH_LONG
                                                                                        )
                                                                                        .show();
                                                                            }
                                                                        });

                                                                        itemPickButton.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                Intent intent = new Intent();
                                                                                intent.putExtra(EXTRA_KEY_PICK_OUTPUT_SHOP_ID, itemId);
                                                                                setResult(RESULT_CODE_PICK_OK, intent);
                                                                                finish();
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

        if (REQUEST_CODE_SHOP_ADD == requestCode) {

            if (ShopAddActivity.RESULT_CODE_MAIN_KO == resultCode) {

                Toast
                        .makeText(
                                ShopListActivity.this,
                                databack.getStringExtra(ShopAddActivity.EXTRA_KEY_MAIN_OUTPUT_MESSAGE),
                                Toast.LENGTH_LONG
                        )
                        .show();

                return;

            }

            if (ShopAddActivity.RESULT_CODE_MAIN_OK == resultCode) {

                Toast
                        .makeText(
                                ShopListActivity.this,
                                databack.getStringExtra(ShopAddActivity.EXTRA_KEY_MAIN_OUTPUT_MESSAGE),
                                Toast.LENGTH_LONG
                        )
                        .show();

                resultViewer.forceRefresh();

                return;

            }

            return;

        }

        if (REQUEST_CODE_SHOP_EDIT == requestCode) {

            if (ShopEditActivity.RESULT_CODE_MAIN_OK == resultCode) {

                Toast
                        .makeText(
                                ShopListActivity.this,
                                databack.getStringExtra(ShopEditActivity.EXTRA_KEY_MAIN_OUTPUT_MESSAGE),
                                Toast.LENGTH_LONG
                        )
                        .show();

                resultViewer.forceRefresh();

                return;

            }

            if (ShopEditActivity.RESULT_CODE_MAIN_KO == resultCode) {

                Toast
                        .makeText(
                                ShopListActivity.this,
                                databack.getStringExtra(ShopEditActivity.EXTRA_KEY_MAIN_OUTPUT_MESSAGE),
                                Toast.LENGTH_LONG
                        )
                        .show();

                return;

            }

            return;

        }

    }

    private EditText nameInput;
    private EditText addressInput;
    private EditText locationInput;
    private EditText postalCodeInput;
    private EditText distributionInput;
    private ImageButton clearButton;
    private ImageButton searchButton;
    private ResultViewer resultViewer;

    private ShopService shopService;

    private boolean pickMode;

    private static final int REQUEST_CODE_SHOP_ADD = 1;
    private static final int REQUEST_CODE_SHOP_EDIT = 2;

}