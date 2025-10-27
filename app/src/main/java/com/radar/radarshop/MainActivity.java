package com.radar.radarshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Spinner spinnerCategory;
    private EditText editTextSearch;
    private SeekBar seekBarMin, seekBarMax;
    private TextView textMinPrice, textMaxPrice;
    private ListView listViewProducts;
    private Button buttonViewWishlist, buttonSearch;
    private ProductAdapter productAdapter;

    private static final int PRICE_MAX_DEFAULT = 2000; // > 999 seed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        dbHelper.ensureSeedProducts(12);   // top up demo data BEFORE loading the list

        // UI
        spinnerCategory   = findViewById(R.id.spinnerCategory);
        editTextSearch    = findViewById(R.id.editTextSearch);
        seekBarMin        = findViewById(R.id.seekBarMin);
        seekBarMax        = findViewById(R.id.seekBarMax);
        textMinPrice      = findViewById(R.id.textMinPrice);
        textMaxPrice      = findViewById(R.id.textMaxPrice);
        listViewProducts  = findViewById(R.id.listViewProducts);
        buttonViewWishlist= findViewById(R.id.buttonViewWishlist);
        buttonSearch      = findViewById(R.id.buttonSearch);

        // Categories
        List<String> categories = new ArrayList<>();
        categories.add("All");
        List<Category> categoryList = dbHelper.getAllCategories();
        for (Category cat : categoryList) {
            categories.add(cat.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);
        spinnerCategory.setSelection(0);

        seekBarMin.setMax(PRICE_MAX_DEFAULT);
        seekBarMax.setMax(PRICE_MAX_DEFAULT);
        seekBarMin.setProgress(0);
        seekBarMax.setProgress(PRICE_MAX_DEFAULT);
        textMinPrice.setText("Min: 0");
        textMaxPrice.setText("Max: " + PRICE_MAX_DEFAULT);

        seekBarMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textMinPrice.setText("Min: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textMaxPrice.setText("Max: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Optional: prefill search
        String initial = getIntent().getStringExtra("initial_query");
        if (initial != null && !initial.trim().isEmpty()) {
            editTextSearch.setText(initial.trim());
        }
        
        // Handle category filter from HomeActivity
        int categoryFilter = getIntent().getIntExtra("category_filter", -1);
        if (categoryFilter != -1) {
            // Find the category name and set spinner selection
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getId() == categoryFilter) {
                    spinnerCategory.setSelection(i + 1); // +1 because "All" is at index 0
                    break;
                }
            }
        }

        // Actions
        buttonSearch.setOnClickListener(v -> applyFilters());
        buttonViewWishlist.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WishlistActivity.class))
        );

        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = (Product) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product", selectedProduct);
            intent.putExtra("productId", selectedProduct.getId());
            startActivity(intent);
        });

        // First load
        applyFilters();

        // Re-apply when category changes
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyFilters() {
        String selectedCategory = spinnerCategory.getSelectedItem() == null
                ? "All" : spinnerCategory.getSelectedItem().toString();
        String searchQuery = editTextSearch.getText().toString().trim();

        int min = seekBarMin.getProgress();
        int max = seekBarMax.getProgress();
        if (max < min) { int t = min; min = max; max = t; }

        List<Product> products = dbHelper.getFilteredProducts(selectedCategory, searchQuery, min, max);

        if (productAdapter == null) {
            productAdapter = new ProductAdapter(this, products);
            listViewProducts.setAdapter(productAdapter);
        } else {
            productAdapter.updateProducts(products);
        }

        // (Optional) quick sanity
        // Toast.makeText(this, "Found " + products.size() + " products", Toast.LENGTH_SHORT).show();
    }
}
