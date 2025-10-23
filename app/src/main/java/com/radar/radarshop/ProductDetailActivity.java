package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView textName, textCategory, textPrice, textDescription;
    private Button buttonAddReview, buttonAddToWishlist, buttonViewWishlist;
    private RecyclerView listReviews;

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private int productId = -1;

    private final ArrayList<Review> reviewItems = new ArrayList<>();
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        dbHelper = new DatabaseHelper(this);
        session  = new SessionManager(this);

        // Read productId (or Product extra as fallback)
        productId = getIntent().getIntExtra("productId", -1);
        if (productId == -1) {
            Product p = (Product) getIntent().getSerializableExtra("product");
            if (p != null) productId = p.getId();
        }
        if (productId == -1) {
            Toast.makeText(this, "Missing productId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views
        textName         = findViewById(R.id.textProductName);
        textCategory     = findViewById(R.id.textProductCategory);
        textPrice        = findViewById(R.id.textProductPrice);
        textDescription  = findViewById(R.id.textProductDescription);
        buttonAddReview  = findViewById(R.id.buttonAddReview);
        buttonAddToWishlist = findViewById(R.id.buttonAddToWishlist);
        buttonViewWishlist  = findViewById(R.id.buttonViewWishlist);
        listReviews      = findViewById(R.id.listReviews);

        // Bind details
        bindProductDetails(productId);

        // Reviews list
        listReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewItems);
        listReviews.setAdapter(reviewAdapter);
        loadReviews();

        // Add Review
        buttonAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, ReviewsActivity.class);
            intent.putExtra("productId", productId);
            startActivity(intent);
        });

        // Add to Wishlist
        buttonAddToWishlist.setOnClickListener(v -> {
            String email = session.getEmail();
            if (email == null || email.trim().isEmpty()) email = "demo@example.com";
            boolean ok = dbHelper.addToWishlist(email, productId);
            Toast.makeText(this, ok ? "Added to wishlist" : "Already in wishlist", Toast.LENGTH_SHORT).show();
        });

        // View Wishlist
        buttonViewWishlist.setOnClickListener(v ->
                startActivity(new Intent(ProductDetailActivity.this, WishlistActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReviews();
    }

    private void bindProductDetails(int pid) {
        // fetch product by scanning (or add a getProductById helper to DB if you want)
        List<Product> all = dbHelper.getAllProducts();
        Product match = null;
        for (Product p : all) {
            if (p.getId() == pid) { match = p; break; }
        }
        if (match == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
        textName.setText(match.getName());
        textCategory.setText(match.getCategory());
        textPrice.setText(currency.format(match.getPrice()));
        textDescription.setText(match.getDescription());
    }

    private void loadReviews() {
        reviewItems.clear();
        reviewItems.addAll(dbHelper.getReviews(productId));
        reviewAdapter.notifyDataSetChanged();
    }
}
