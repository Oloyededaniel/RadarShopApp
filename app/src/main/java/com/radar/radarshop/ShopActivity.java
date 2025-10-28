package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ShopActivity extends AppCompatActivity {
    
    private ShopFragment shopFragment;
    private CartFragment cartFragment;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        
        // Initialize fragment
        shopFragment = new ShopFragment();
        
        // Load the shop fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, shopFragment)
                .commit();
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Handle intent extras after fragment is loaded
        getSupportFragmentManager().executePendingTransactions();
        handleIntentExtras();
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNav);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_shop) {
                    return true; // Already in shop
                } else if (id == R.id.nav_orders) {
                    startActivity(new Intent(this, OrdersActivity.class));
                    return true;
                } else if (id == R.id.nav_cart) {
                    Intent cartIntent = new Intent(this, CartActivity.class);
                    cartIntent.putExtra("from_activity", "ShopActivity");
                    startActivity(cartIntent);
                    return true;
                } else if (id == R.id.nav_wishlist) {
                    startActivity(new Intent(this, WishlistActivity.class));
                    return true;
                }
                return false;
            });
            bottomNavigationView.setSelectedItemId(R.id.nav_shop);
        }
    }

    private void handleIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            // Handle category filter
            int categoryFilter = intent.getIntExtra("category_filter", -1);
            if (categoryFilter != -1 && shopFragment != null) {
                shopFragment.setCategoryFilter(categoryFilter);
            }
            
            // Handle search query
            String searchQuery = intent.getStringExtra("initial_query");
            if (searchQuery != null && !searchQuery.isEmpty() && shopFragment != null) {
                shopFragment.setSearchQuery(searchQuery);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCartFragment() {
        // Hide shop fragment
        if (shopFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .hide(shopFragment)
                    .commit();
        }
        
        // Create cart fragment if not exists
        if (cartFragment == null) {
            cartFragment = new CartFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, cartFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .show(cartFragment)
                    .commit();
        }
    }

    // Method to show shop fragment (public for external access)
    public void showShopFragment() {
        // Hide cart fragment
        if (cartFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .hide(cartFragment)
                    .commit();
        }
        
        // Show shop fragment
        if (shopFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .show(shopFragment)
                    .commit();
        }
    }

    // Method to update cart badge from fragments
    public void updateCartBadge() {
        if (shopFragment != null) {
            shopFragment.updateCartBadge();
        }
        if (cartFragment != null) {
            cartFragment.refreshCart();
        }
    }
    
    // Method to handle start shopping button click from fragment layout
    public void startShopping(View view) {
        if (cartFragment != null) {
            cartFragment.startShopping(view);
        }
    }
}