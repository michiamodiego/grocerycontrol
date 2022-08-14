package com.ds.app.pricereading.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.app.pricereading.R;
import com.ds.app.pricereading.activity.product.ProductListActivity;
import com.ds.app.pricereading.activity.shop.ShopListActivity;
import com.ds.app.pricereading.util.preferredshop.SharedPrefsUtil;
import com.ds.app.pricereading.util.preferredshop.dto.PreferredShop;

public class MainActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_exit_menu:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        shopButton = findViewById(R.id.main_shop_button);
        productButton = findViewById(R.id.main_product_button);
        flashButton = findViewById(R.id.main_flash_button);
        syncButton = findViewById(R.id.main_sync_button);

        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        MainActivity.this,
                        ShopListActivity.class
                );
                startActivityForResult(
                        intent,
                        REQUEST_CODE_SHOP_LIST
                );
            }
        });

        productButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        MainActivity.this,
                        ProductListActivity.class
                );
                startActivityForResult(
                        intent,
                        REQUEST_CODE_SHOP_LIST
                );
            }
        });

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        MainActivity.this,
                        FlashReadingActivity.class
                );
                startActivityForResult(
                        intent,
                        REQUEST_CODE_PRICE_READ
                );
            }
        });

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast
                        .makeText(
                                MainActivity.this,
                                "Funzionalità non ancora implementata",
                                Toast.LENGTH_LONG
                        )
                        .show();
            }
        });

    }

    private ImageButton shopButton;
    private ImageButton productButton;
    private ImageButton flashButton;
    private ImageButton syncButton;

    private static final int REQUEST_CODE_SHOP_LIST = 1;
    private static final int REQUEST_CODE_PRICE_READ = 2;

}