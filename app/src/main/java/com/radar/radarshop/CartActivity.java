package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemInteractionListener {

    private Button btnCheckout;
    private RecyclerView rvCart;
    private DatabaseHelper db;
    private SessionManager session;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private BottomNavigationView bottomNavigationView;
    
    // UI Elements
    private TextView tvItemCount;
    private TextView tvSubtotal;
    private TextView tvShipping;
    private TextView tvTax;
    private TextView tvTotal;
    private LinearLayout layoutOrderSummary;
    private LinearLayout layoutEmptyCart;
    
    // Calculations
    private double subtotal = 0.0;
    private double shippingCost = 5.99;
    private double tax = 0.0;
    private double total = 0.0;
    private int itemCount = 0;
    
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initializeViews();
        initializeDatabase();
        setupRecyclerView();
        setupBottomNavigation();
        loadCartItems();
        setupListeners();
    }

    private void initializeViews() {
        btnCheckout = findViewById(R.id.btnCheckout);
        rvCart = findViewById(R.id.rvCart);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        layoutOrderSummary = findViewById(R.id.layoutOrderSummary);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        bottomNavigationView = findViewById(R.id.bottomNav);
        
        // Setup back button click listener as backup
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                android.util.Log.d("CartActivity", "Back button clicked programmatically");
                handleBackNavigation();
            });
        }
    }

    private void initializeDatabase() {
        db = new DatabaseHelper(this);
        session = new SessionManager(this);
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_shop) {
                    startActivity(new Intent(this, ShopActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_orders) {
                    startActivity(new Intent(this, OrdersActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_cart) {
                    return true; // Already in cart
                } else if (id == R.id.nav_wishlist) {
                    startActivity(new Intent(this, WishlistActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        }
    }

    private void setupRecyclerView() {
        try {
            rvCart.setLayoutManager(new LinearLayoutManager(this));
            cartItems = new ArrayList<>();
            cartAdapter = new CartAdapter(this, cartItems, db);
            cartAdapter.setOnCartItemInteractionListener(this);
            rvCart.setAdapter(cartAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            // Initialize with empty list if there's an error
            cartItems = new ArrayList<>();
            cartAdapter = new CartAdapter(this, cartItems, db);
            cartAdapter.setOnCartItemInteractionListener(this);
            rvCart.setAdapter(cartAdapter);
        }
    }

    private void setupListeners() {
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            animateCheckoutButton();
        });
    }

    private void loadCartItems() {
        try {
            String userEmail = session.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                cartItems = db.getCartItems(userEmail);
                if (cartItems == null) {
                    cartItems = new ArrayList<>();
                }
                cartAdapter.updateCartItems(cartItems);
                updateCartSummary();
                updateEmptyState();
            } else {
                // Show sample items for demo
                createSampleItems();
                cartAdapter.updateCartItems(cartItems);
                updateCartSummary();
                updateEmptyState();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to sample items if there's an error
            createSampleItems();
            cartAdapter.updateCartItems(cartItems);
            updateCartSummary();
            updateEmptyState();
        }
    }

    private void createSampleItems() {
        cartItems.clear();
        cartItems.add(new CartItem(1, "Wireless Headphones", "", 99.99, 1, "TechBrand", "Electronics"));
        cartItems.add(new CartItem(2, "Smart Watch", "", 249.99, 2, "TechBrand", "Electronics"));
        cartItems.add(new CartItem(3, "Phone Case", "", 19.99, 1, "TechBrand", "Accessories"));
    }

    private void updateCartSummary() {
        try {
            subtotal = 0.0;
            itemCount = 0;
            
            if (cartItems != null) {
                for (CartItem item : cartItems) {
                    if (item != null) {
                        subtotal += item.getTotalPrice();
                        itemCount += item.getQuantity();
                    }
                }
            }
            
            tax = subtotal * 0.08; // 8% tax
            total = subtotal + shippingCost + tax;
            
            // Update UI
            if (tvItemCount != null) {
                tvItemCount.setText(itemCount + " items");
            }
            if (tvSubtotal != null) {
                tvSubtotal.setText(currencyFormat.format(subtotal));
            }
            if (tvShipping != null) {
                tvShipping.setText(currencyFormat.format(shippingCost));
            }
            if (tvTax != null) {
                tvTax.setText(currencyFormat.format(tax));
            }
            if (tvTotal != null) {
                tvTotal.setText(currencyFormat.format(total));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateEmptyState() {
        try {
            if (cartItems == null || cartItems.isEmpty()) {
                showEmptyCart();
            } else {
                showCartContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showEmptyCart();
        }
    }

    private void showEmptyCart() {
        layoutEmptyCart.setVisibility(View.VISIBLE);
        layoutOrderSummary.setVisibility(View.GONE);
        rvCart.setVisibility(View.GONE);
    }

    private void showCartContent() {
        layoutEmptyCart.setVisibility(View.GONE);
        layoutOrderSummary.setVisibility(View.VISIBLE);
        rvCart.setVisibility(View.VISIBLE);
    }

    private void animateCheckoutButton() {
        // Disable button to prevent multiple clicks
        btnCheckout.setEnabled(false);
        
        // Add haptic feedback for better UX
        android.view.View view = btnCheckout;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);
        } else {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
        }
        
        // Create scale animation for button press effect
        android.view.animation.ScaleAnimation scaleDown = new android.view.animation.ScaleAnimation(
                1.0f, 0.95f, 1.0f, 0.95f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleDown.setDuration(100);
        scaleDown.setFillAfter(true);
        
        // Create scale animation for button release effect
        android.view.animation.ScaleAnimation scaleUp = new android.view.animation.ScaleAnimation(
                0.95f, 1.0f, 0.95f, 1.0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(100);
        scaleUp.setStartOffset(100);
        
        // Create fade animation for text change
        android.view.animation.AlphaAnimation fadeOut = new android.view.animation.AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(200);
        fadeOut.setStartOffset(200);
        
        android.view.animation.AlphaAnimation fadeIn = new android.view.animation.AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(200);
        fadeIn.setStartOffset(400);
        
        // Set up animation listener
        scaleUp.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {}
            
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                // Change button appearance to processing state with pulsing effect
                btnCheckout.setBackgroundResource(R.drawable.pulsing_processing_button);
                btnCheckout.setText("Processing...");
                
                // Start the pulsing animation
                android.graphics.drawable.AnimationDrawable animDrawable = 
                    (android.graphics.drawable.AnimationDrawable) btnCheckout.getBackground();
                animDrawable.start();
                
                // Start fade animations
                btnCheckout.startAnimation(fadeOut);
            }
            
            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {}
        });
        
        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {}
            
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                btnCheckout.startAnimation(fadeIn);
            }
            
            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {}
        });
        
        fadeIn.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {}
            
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                // Wait a bit then proceed to checkout
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    proceedToCheckout();
                }, 800); // Wait 800ms to show processing state
            }
            
            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {}
        });
        
        // Start the animation sequence
        btnCheckout.startAnimation(scaleDown);
        btnCheckout.startAnimation(scaleUp);
    }

    private void proceedToCheckout() {
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("cart_items", (java.io.Serializable) cartItems);
        intent.putExtra("total_amount", total);
        intent.putExtra("item_count", itemCount);
        startActivity(intent);
    }

    public void handleBackClick(View view) {
        android.util.Log.d("CartActivity", "handleBackClick called");
        handleBackNavigation();
    }

    public void onBackPressed(View view) {
        android.util.Log.d("CartActivity", "Back arrow clicked");
        handleBackNavigation();
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }

    private void handleBackNavigation() {
        android.util.Log.d("CartActivity", "handleBackNavigation called");
        
        // Check if we came from a specific activity
        Intent intent = getIntent();
        if (intent != null) {
            String fromActivity = intent.getStringExtra("from_activity");
            android.util.Log.d("CartActivity", "From activity: " + fromActivity);
            
            if (fromActivity != null) {
                switch (fromActivity) {
                    case "HomeActivity":
                        android.util.Log.d("CartActivity", "Navigating back to HomeActivity");
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                        return;
                    case "ShopActivity":
                        android.util.Log.d("CartActivity", "Navigating back to ShopActivity");
                        startActivity(new Intent(this, ShopActivity.class));
                        finish();
                        return;
                    case "OrdersActivity":
                        android.util.Log.d("CartActivity", "Navigating back to OrdersActivity");
                        startActivity(new Intent(this, OrdersActivity.class));
                        finish();
                        return;
                    case "ProfileActivity":
                        android.util.Log.d("CartActivity", "Navigating back to ProfileActivity");
                        startActivity(new Intent(this, ProfileActivity.class));
                        finish();
                        return;
                    case "ShopFragment":
                    case "ProductDetailActivity":
                        android.util.Log.d("CartActivity", "Navigating back to ShopActivity");
                        startActivity(new Intent(this, ShopActivity.class));
                        finish();
                        return;
                }
            }
        }
        
        // Default behavior - go back to HomeActivity
        android.util.Log.d("CartActivity", "Using default navigation to HomeActivity");
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    public void startShopping(View view) {
        // Navigate to ShopActivity
        Intent intent = new Intent(this, ShopActivity.class);
        startActivity(intent);
        finish(); // Close the cart activity
    }

    // CartAdapter.OnCartItemInteractionListener implementation
    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            cartItems.remove(item);
            cartAdapter.updateCartItems(cartItems);
            Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show();
        } else {
            item.setQuantity(newQuantity);
            cartAdapter.updateCartItems(cartItems);
        }
        
        updateCartSummary();
        updateEmptyState();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartItems.remove(item);
        cartAdapter.updateCartItems(cartItems);
        updateCartSummary();
        updateEmptyState();
        Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckout() {
        proceedToCheckout();
    }

    @Override
    public void onProductClick(CartItem item) {
        // Navigate to product detail
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("productId", item.getProductId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset button state when returning to activity
        resetCheckoutButton();
        // Refresh cart when returning to activity (e.g., from checkout)
        loadCartItems();
    }

    private void resetCheckoutButton() {
        btnCheckout.setEnabled(true);
        
        // Stop any running pulsing animation
        if (btnCheckout.getBackground() instanceof android.graphics.drawable.AnimationDrawable) {
            android.graphics.drawable.AnimationDrawable animDrawable = 
                (android.graphics.drawable.AnimationDrawable) btnCheckout.getBackground();
            animDrawable.stop();
        }
        
        btnCheckout.setBackgroundResource(R.drawable.rounded_checkout_button);
        btnCheckout.setText("Proceed to Checkout");
        btnCheckout.clearAnimation();
    }
}
