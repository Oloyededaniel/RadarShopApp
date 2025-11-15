package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WriteReviewActivity extends AppCompatActivity {
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    
    private int orderId;
    private String orderNumber;
    private List<DatabaseHelper.OrderItem> orderItems;
    private List<Product> products;
    private int currentProductIndex = 0;
    
    // Views
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvOrderNumber;
    private TextView tvProgress;
    private TextView tvOrderDate;
    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvRateProduct;
    private RatingBar ratingBar;
    private TextView tvShareExperience;
    private EditText etReviewComment;
    private TextView tvCharacterCount;
    private Button btnNextProduct;
    private TextView tvDoLater;
    private ProgressBar progressBar;
    
    private static final int MAX_CHARACTERS = 500;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        
        // Get order information from intent
        orderId = getIntent().getIntExtra("orderId", -1);
        orderNumber = getIntent().getStringExtra("orderNumber");
        
        if (orderId == -1 || orderNumber == null) {
            Toast.makeText(this, "Invalid order information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeDatabase();
        initializeViews();
        loadOrderItems();
        setupClickListeners();
        displayCurrentProduct();
    }
    
    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
    }
    
    private void initializeViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvProgress = findViewById(R.id.tvProgress);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvRateProduct = findViewById(R.id.tvRateProduct);
        ratingBar = findViewById(R.id.ratingBar);
        tvShareExperience = findViewById(R.id.tvShareExperience);
        etReviewComment = findViewById(R.id.etReviewComment);
        tvCharacterCount = findViewById(R.id.tvCharacterCount);
        btnNextProduct = findViewById(R.id.btnNextProduct);
        tvDoLater = findViewById(R.id.tvDoLater);
        progressBar = findViewById(R.id.progressBar);
        
        // Set order number
        tvOrderNumber.setText("Order " + orderNumber);
        
        // Setup character counter
        etReviewComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvCharacterCount.setText(length + "/" + MAX_CHARACTERS + " characters");
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Set initial character count
        tvCharacterCount.setText("0/" + MAX_CHARACTERS + " characters");
    }
    
    private void loadOrderItems() {
        orderItems = databaseHelper.getOrderItems(orderId);
        if (orderItems == null || orderItems.isEmpty()) {
            Toast.makeText(this, "No items found in this order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Load product details for each order item
        products = new ArrayList<>();
        List<Product> allProducts = databaseHelper.getAllProducts();
        
        for (DatabaseHelper.OrderItem item : orderItems) {
            for (Product product : allProducts) {
                if (product.getId() == item.productId) {
                    products.add(product);
                    break;
                }
            }
        }
        
        if (products.isEmpty()) {
            Toast.makeText(this, "Product information not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Get order date
        DatabaseHelper.Order order = getOrderById(orderId);
        if (order != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                Date date = inputFormat.parse(order.orderDate);
                tvOrderDate.setText("Ordered on " + outputFormat.format(date));
            } catch (Exception e) {
                tvOrderDate.setText("Ordered on " + order.orderDate);
            }
        }
    }
    
    private DatabaseHelper.Order getOrderById(int orderId) {
        String userEmail = sessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "demo@example.com";
        }
        
        List<DatabaseHelper.Order> orders = databaseHelper.getUserOrders(userEmail);
        for (DatabaseHelper.Order order : orders) {
            if (order.id == orderId) {
                return order;
            }
        }
        return null;
    }
    
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        btnNextProduct.setOnClickListener(v -> submitCurrentReviewAndMoveNext());
        
        tvDoLater.setOnClickListener(v -> finish());
    }
    
    private void displayCurrentProduct() {
        if (currentProductIndex >= products.size()) {
            // All products reviewed
            Toast.makeText(this, "Thank you for your reviews!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        Product product = products.get(currentProductIndex);
        
        // Update progress
        tvProgress.setText("Product " + (currentProductIndex + 1) + " of " + products.size());
        progressBar.setMax(products.size());
        progressBar.setProgress(currentProductIndex + 1);
        
        // Update button text
        if (currentProductIndex == products.size() - 1) {
            btnNextProduct.setText("Submit Review");
        } else {
            btnNextProduct.setText("Next Product");
        }
        
        // Load product image
        List<ProductImage> images = databaseHelper.getProductImages(product.getId());
        if (images != null && !images.isEmpty()) {
            String imageUrl = images.get(0).getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_shopping_bag)
                    .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_shopping_bag);
            }
        } else {
            ivProductImage.setImageResource(R.drawable.ic_shopping_bag);
        }
        
        // Set product name and price
        tvProductName.setText(product.getName());
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
        tvProductPrice.setText(currency.format(product.getPrice()));
        
        // Reset rating and comment
        ratingBar.setRating(0);
        etReviewComment.setText("");
        tvCharacterCount.setText("0/" + MAX_CHARACTERS + " characters");
    }
    
    private void submitCurrentReviewAndMoveNext() {
        Product product = products.get(currentProductIndex);
        int rating = Math.round(ratingBar.getRating());
        String comment = etReviewComment.getText().toString().trim();
        
        if (rating < 1) {
            Toast.makeText(this, "Please select a rating (1–5 stars)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Submit review
        String email = sessionManager.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = "demo@example.com";
        }
        
        boolean success = databaseHelper.addReview(email, product.getId(), rating, comment);
        if (success) {
            // Move to next product
            currentProductIndex++;
            displayCurrentProduct();
            
            if (currentProductIndex >= products.size()) {
                // All reviews submitted
                Toast.makeText(this, "Thank you for your reviews!", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Failed to submit review. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}

