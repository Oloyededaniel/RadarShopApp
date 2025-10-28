package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    // Views
    private ImageView ivProductImage;
    private TextView textName, textBrand, textCategory, textPrice, textDescription, textDetailedDescription;
    private TextView textSku, textWeight, textDimensions, tvStockStatus, tvRatingCount, tvQuantity;
    private RatingBar ratingBar;
    private ImageButton btnWishlist, btnBack, btnDecrease, btnIncrease;
    private Button buttonAddReview, buttonViewWishlist, btnAddToCart, btnBuyNow;
    private RecyclerView listReviews;
    private TextView tvEmptyReviews;
    private LinearLayout layoutQuantityControls, layoutSpecifications;

    // Data
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private int productId = -1;
    private Product currentProduct;
    private boolean isInWishlist = false;
    private boolean isInCart = false;
    private int cartQuantity = 0;

    // Reviews
    private final ArrayList<Review> reviewItems = new ArrayList<>();
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

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

        initializeViews();
        setupClickListeners();
        bindProductDetails(productId);
        setupReviews();
        checkWishlistStatus();
        checkCartStatus();
        updateUI();
    }

    private void initializeViews() {
        // Image and main views
        ivProductImage = findViewById(R.id.ivProductImage);
        textName = findViewById(R.id.textProductName);
        textBrand = findViewById(R.id.textBrand);
        textCategory = findViewById(R.id.textProductCategory);
        textPrice = findViewById(R.id.textProductPrice);
        textDescription = findViewById(R.id.textProductDescription);
        textDetailedDescription = findViewById(R.id.textDetailedDescription);
        
        // Specifications
        textSku = findViewById(R.id.textSku);
        textWeight = findViewById(R.id.textWeight);
        textDimensions = findViewById(R.id.textDimensions);
        layoutSpecifications = findViewById(R.id.layoutSpecifications);
        
        // Status and rating
        tvStockStatus = findViewById(R.id.tvStockStatus);
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingCount = findViewById(R.id.tvRatingCount);
        
        // Buttons
        btnWishlist = findViewById(R.id.btnWishlist);
        btnBack = findViewById(R.id.btnBack);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        buttonAddReview = findViewById(R.id.buttonAddReview);
        buttonViewWishlist = findViewById(R.id.buttonViewWishlist);
        
        // Quantity controls
        layoutQuantityControls = findViewById(R.id.layoutQuantityControls);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        tvQuantity = findViewById(R.id.tvQuantity);
        
        // Reviews
        listReviews = findViewById(R.id.listReviews);
        tvEmptyReviews = findViewById(R.id.tvEmptyReviews);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Wishlist button
        btnWishlist.setOnClickListener(v -> toggleWishlist());

        // Add to cart button
        btnAddToCart.setOnClickListener(v -> addToCart());

        // Buy now button
        btnBuyNow.setOnClickListener(v -> buyNow());

        // Quantity controls
        btnIncrease.setOnClickListener(v -> increaseQuantity());
        btnDecrease.setOnClickListener(v -> decreaseQuantity());

        // Other buttons
        buttonAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, ReviewsActivity.class);
            intent.putExtra("productId", productId);
            startActivity(intent);
        });

        buttonViewWishlist.setOnClickListener(v ->
                startActivity(new Intent(ProductDetailActivity.this, WishlistActivity.class)));
    }

    private void setupReviews() {
        listReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewItems);
        listReviews.setAdapter(reviewAdapter);
        loadReviews();
    }

    private void bindProductDetails(int pid) {
        // Get product details
        List<Product> all = dbHelper.getAllProducts();
        Product match = null;
        for (Product p : all) {
            if (p.getId() == pid) { 
                match = p; 
                break; 
            }
        }
        
        if (match == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentProduct = match;

        // Format currency
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
        
        // Set basic product info
        textName.setText(match.getName());
        textCategory.setText(match.getCategoryName());
        textPrice.setText(currency.format(match.getPrice()));
        textDescription.setText(match.getDescription());
        
        // Set brand if available
        if (match.getBrand() != null && !match.getBrand().isEmpty()) {
            textBrand.setText(match.getBrand());
            textBrand.setVisibility(View.VISIBLE);
        } else {
            textBrand.setVisibility(View.GONE);
        }
        
        // Set detailed description if available
        if (match.getDetailedDescription() != null && !match.getDetailedDescription().isEmpty()) {
            textDetailedDescription.setText(match.getDetailedDescription());
            textDetailedDescription.setVisibility(View.VISIBLE);
        } else {
            textDetailedDescription.setVisibility(View.GONE);
        }
        
        // Set rating
        ratingBar.setRating((float) match.getAverageRating());
        tvRatingCount.setText("(" + match.getTotalReviews() + " reviews)");
        
        // Set stock status
        if (match.isInStock()) {
            tvStockStatus.setText("In Stock");
            tvStockStatus.setBackgroundResource(R.drawable.stock_badge_background);
            tvStockStatus.setVisibility(View.VISIBLE);
        } else {
            tvStockStatus.setText("Out of Stock");
            tvStockStatus.setBackgroundResource(R.drawable.stock_badge_background);
            tvStockStatus.setVisibility(View.VISIBLE);
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Out of Stock");
        }
        
        // Set specifications if available
        boolean hasSpecs = false;
        if (match.getSku() != null && !match.getSku().isEmpty()) {
            textSku.setText(match.getSku());
            hasSpecs = true;
        }
        if (match.getWeight() > 0) {
            textWeight.setText(match.getWeight() + " kg");
            hasSpecs = true;
        }
        if (match.getDimensions() != null && !match.getDimensions().isEmpty()) {
            textDimensions.setText(match.getDimensions());
            hasSpecs = true;
        }
        
        if (hasSpecs) {
            layoutSpecifications.setVisibility(View.VISIBLE);
        } else {
            layoutSpecifications.setVisibility(View.GONE);
        }
        
        // Load product image
        loadProductImage(match);
    }

    private void loadProductImage(Product product) {
        // Get main product image
        List<ProductImage> images = dbHelper.getProductImages(product.getId());
        String imageUrl = null;
        
        for (ProductImage image : images) {
            if ("main".equals(image.getImageType()) || "thumbnail".equals(image.getImageType())) {
                imageUrl = image.getImageUrl();
                break;
            }
        }
        
        // If no main image, get first available image
        if (imageUrl == null && !images.isEmpty()) {
            imageUrl = images.get(0).getImageUrl();
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                .placeholder(R.drawable.product_placeholder)
                .error(R.drawable.product_placeholder)
                .into(ivProductImage);
        } else {
            ivProductImage.setImageResource(R.drawable.product_placeholder);
        }
    }

    private void checkWishlistStatus() {
        String userEmail = session.getEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            isInWishlist = dbHelper.isInWishlist(userEmail, productId);
            updateWishlistIcon();
        }
    }

    private void checkCartStatus() {
        String userEmail = session.getEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            cartQuantity = dbHelper.getCartQuantity(userEmail, productId);
            isInCart = cartQuantity > 0;
        }
    }

    private void updateWishlistIcon() {
        if (isInWishlist) {
            btnWishlist.setImageResource(R.drawable.ic_favorite_filled);
            btnWishlist.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
        } else {
            btnWishlist.setImageResource(R.drawable.ic_favorite_border);
            btnWishlist.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void updateUI() {
        if (isInCart && cartQuantity > 0) {
            btnAddToCart.setVisibility(View.GONE);
            layoutQuantityControls.setVisibility(View.VISIBLE);
            tvQuantity.setText(String.valueOf(cartQuantity));
        } else {
            btnAddToCart.setVisibility(View.VISIBLE);
            layoutQuantityControls.setVisibility(View.GONE);
        }
    }

    private void toggleWishlist() {
        String userEmail = session.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Please log in to use wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isInWishlist) {
            dbHelper.removeFromWishlist(userEmail, productId);
            isInWishlist = false;
            Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addToWishlist(userEmail, productId);
            isInWishlist = true;
            Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show();
        }
        updateWishlistIcon();
    }

    private void addToCart() {
        String userEmail = session.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentProduct.isInStock()) {
            Toast.makeText(this, "Product is out of stock", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.addToCart(userEmail, productId, 1);
        isInCart = true;
        cartQuantity = 1;
        updateUI();
        
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
    }

    private void buyNow() {
        String userEmail = session.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Please log in to buy items", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentProduct.isInStock()) {
            Toast.makeText(this, "Product is out of stock", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to cart first
        dbHelper.addToCart(userEmail, productId, 1);
        
        // Navigate to checkout/cart
        Intent intent = new Intent(this, CartActivity.class);
        intent.putExtra("checkout_mode", true);
        intent.putExtra("from_activity", "ProductDetailActivity");
        startActivity(intent);
    }

    private void increaseQuantity() {
        String userEmail = session.getEmail();
        if (userEmail == null || userEmail.isEmpty()) return;

        if (cartQuantity < currentProduct.getStockQuantity()) {
            cartQuantity++;
            dbHelper.updateCartQuantity(userEmail, productId, cartQuantity);
            tvQuantity.setText(String.valueOf(cartQuantity));
        } else {
            Toast.makeText(this, "Maximum stock reached", Toast.LENGTH_SHORT).show();
        }
    }

    private void decreaseQuantity() {
        String userEmail = session.getEmail();
        if (userEmail == null || userEmail.isEmpty()) return;

        if (cartQuantity > 1) {
            cartQuantity--;
            dbHelper.updateCartQuantity(userEmail, productId, cartQuantity);
            tvQuantity.setText(String.valueOf(cartQuantity));
        } else {
            // Remove from cart completely
            dbHelper.removeFromCart(userEmail, productId);
            isInCart = false;
            cartQuantity = 0;
            updateUI();
        }
    }

    private void loadReviews() {
        reviewItems.clear();
        List<Review> reviews = dbHelper.getReviews(productId);
        reviewItems.addAll(reviews);
        reviewAdapter.notifyDataSetChanged();
        
        if (reviews.isEmpty()) {
            tvEmptyReviews.setVisibility(View.VISIBLE);
            listReviews.setVisibility(View.GONE);
        } else {
            tvEmptyReviews.setVisibility(View.GONE);
            listReviews.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReviews();
        checkWishlistStatus();
        checkCartStatus();
        updateUI();
    }
}