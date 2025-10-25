package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private EditText etSearch;
    private TextView avatar;
    private TextView welcome;
    private TextView userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize user interface elements
        avatar = findViewById(R.id.avatar);
        welcome = findViewById(R.id.welcome);
        userName = findViewById(R.id.userName);
        
        // Set up personalized welcome message
        setupUserWelcome();
        
        // Avatar click -> open profile
        if (avatar != null) {
            avatar.setOnClickListener(v -> {
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
            });
        }

        // Search field -> open products with initial query
        etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    openProducts(v.getText().toString().trim());
                    return true;
                }
                return false;
            });
        }

        // "See All" -> open products
        View seeAll = findViewById(R.id.seeAll);
        if (seeAll != null) {
            seeAll.setOnClickListener(v -> openProducts(null));
        }

        // Promo cards -> open products
        View promo1 = findViewById(R.id.promoSuperSale);
        View promo2 = findViewById(R.id.promoNewArrivals);
        View promo3 = findViewById(R.id.promoFlashDeals);
        View.OnClickListener toShop = v -> openProducts(null);
        if (promo1 != null) promo1.setOnClickListener(toShop);
        if (promo2 != null) promo2.setOnClickListener(toShop);
        if (promo3 != null) promo3.setOnClickListener(toShop);

        // Bottom nav
        BottomNavigationView bottom = findViewById(R.id.bottomNav);
        if (bottom != null) {
            bottom.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true; // already here
                } else if (id == R.id.nav_shop) {
                    openProducts(null);
                    return true;
                } else if (id == R.id.nav_orders) {
                    startActivity(new Intent(this, OrdersActivity.class));
                    return true;
                } else if (id == R.id.nav_cart) {
                    startActivity(new Intent(this, CartActivity.class));
                    return true;
                } else if (id == R.id.nav_wishlist) {
                    startActivity(new Intent(this, WishlistActivity.class));
                    return true;
                }
                return false;
            });
            bottom.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupUserWelcome() {
        SessionManager session = new SessionManager(this);
        
        if (session.isLoggedIn()) {
            String fullName = session.getFullName();
            String initials = session.getUserInitials();
            
            // Set user name
            if (!fullName.isEmpty()) {
                userName.setText(fullName);
            } else {
                userName.setText("User");
            }
            
            // Set user initials in avatar
            if (!initials.isEmpty()) {
                avatar.setText(initials);
            } else {
                avatar.setText("U");
            }
            
            // Set welcome message
            welcome.setText("Welcome back,");
        } else {
            // Fallback if not logged in (shouldn't happen)
            userName.setText("Guest");
            avatar.setText("G");
            welcome.setText("Welcome,");
        }
    }

    private void openProducts(@Nullable String initialQuery) {
        Intent i = new Intent(this, MainActivity.class);
        if (initialQuery != null && !initialQuery.isEmpty()) {
            i.putExtra("initial_query", initialQuery);
        }
        startActivity(i);
    }
}
