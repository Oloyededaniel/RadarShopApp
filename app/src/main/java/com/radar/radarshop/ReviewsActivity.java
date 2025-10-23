package com.radar.radarshop;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ReviewsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;

    private int productId = -1;

    private TextView tvProductName, tvProductPrice;
    private RatingBar ratingBar;
    private EditText editComment;
    private Button btnSubmit, btnCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        productId = getIntent().getIntExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "Missing productId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views
        tvProductName = findViewById(R.id.tvReviewProductName);
        tvProductPrice = findViewById(R.id.tvReviewProductPrice);
        ratingBar = findViewById(R.id.ratingBar);
        editComment = findViewById(R.id.editReviewComment);
        btnSubmit = findViewById(R.id.buttonSubmitReview);
        btnCancel = findViewById(R.id.buttonCancelReview);

        bindHeader(productId);

        btnSubmit.setOnClickListener(v -> submitReview());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void bindHeader(int pid) {
        List<Product> all = db.getAllProducts();
        Product match = null;
        for (Product p : all) { if (p.getId() == pid) { match = p; break; } }
        if (match == null) return;

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
        tvProductName.setText(match.getName());
        tvProductPrice.setText(currency.format(match.getPrice()));
    }

    private void submitReview() {
        int rating = Math.round(ratingBar.getRating());
        String comment = editComment.getText().toString().trim();

        if (rating < 1) {
            Toast.makeText(this, "Please select a rating (1–5).", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = session.getEmail();
        if (email == null || email.trim().isEmpty()) email = "demo@example.com";

        boolean ok = db.addReview(email, productId, rating, comment);
        if (ok) {
            Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
            finish(); // ProductDetailActivity.onResume() will refresh the list
        } else {
            Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show();
        }
    }
}
