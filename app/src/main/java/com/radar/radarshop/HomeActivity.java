package com.radar.radarshop;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private EditText etSearch;
    private TextView avatar;
    private TextView welcome;
    private TextView userName;
    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private ImageView searchIcon;
    
    // Fragment management
    private Fragment currentFragment;
    private CartFragment cartFragment;
    
    // Responsive search fields
    private Handler searchHandler;
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY = 500; // 500ms delay for debouncing

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_stylish);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize search handler for responsive search
        searchHandler = new Handler(Looper.getMainLooper());
        
        // Initialize user interface elements
        avatar = findViewById(R.id.avatar);
        welcome = findViewById(R.id.welcome);
        userName = findViewById(R.id.userName);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        searchIcon = findViewById(R.id.searchIcon);
        
        // Set up personalized welcome message
        setupUserWelcome();
        
        // Set up category RecyclerView
        setupCategoryRecyclerView();
        
        // Add stylish animations
        addStylishAnimations();
        
        // Avatar click -> go directly to ProfileActivity
        if (avatar != null) {
            avatar.setOnClickListener(v -> {
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
            });
            
            // Avatar long press -> show detailed session info
            avatar.setOnLongClickListener(v -> {
                Toast.makeText(this, "Long press for session details", Toast.LENGTH_SHORT).show();
                showDetailedUserInfo();
                return true;
            });
        }

        // Search field -> responsive search with TextWatcher
        etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            setupResponsiveSearch();
        }

        // Search icon click -> trigger immediate search
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                String searchQuery = etSearch.getText().toString().trim();
                if (!searchQuery.isEmpty()) {
                    // Cancel any pending search and trigger immediate search
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    performSearch(searchQuery);
                } else {
                    Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
                    etSearch.requestFocus();
                }
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
                    showHomeFragment();
                    return true;
                } else if (id == R.id.nav_shop) {
                    openProducts(null);
                    return true;
                } else if (id == R.id.nav_orders) {
                    startActivity(new Intent(this, OrdersActivity.class));
                    return true;
                } else if (id == R.id.nav_cart) {
                    showCartFragment();
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
            String email = session.getEmail();
            
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
            
            // Debug: Log user info to verify it's working
            android.util.Log.d("HomeActivity", "User Info - Name: " + fullName + ", Email: " + email + ", Initials: " + initials);
        } else {
            // Fallback if not logged in (shouldn't happen)
            userName.setText("Guest");
            avatar.setText("G");
            welcome.setText("Welcome,");
        }
    }

    private void setupCategoryRecyclerView() {
        // Get all categories from database
        List<Category> categories = databaseHelper.getAllCategories();
        
        // Set up RecyclerView with scattered grid layout
        ScatteredGridLayoutManager layoutManager = new ScatteredGridLayoutManager(this);
        recyclerViewCategories.setLayoutManager(layoutManager);
        
        // Create and set adapter
        categoryAdapter = new CategoryAdapter(categories, databaseHelper, category -> {
            // Handle category click - open products filtered by category
            openProductsByCategory(category.getId());
        });
        
        recyclerViewCategories.setAdapter(categoryAdapter);
        
        // Enable scrolling based on content
        recyclerViewCategories.setNestedScrollingEnabled(true);
    }
    
    private void setupResponsiveSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String searchQuery = s.toString().trim();
                
                // Cancel previous search if user is still typing
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                if (searchQuery.isEmpty()) {
                    // Clear any search results when field is empty
                    return;
                }
                
                // Set up new search with delay (debouncing)
                searchRunnable = () -> performSearch(searchQuery);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
            }
        });
        
        // Also keep the Enter key functionality for immediate search
        etSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String searchQuery = v.getText().toString().trim();
                if (!searchQuery.isEmpty()) {
                    // Cancel any pending search and trigger immediate search
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    performSearch(searchQuery);
                    return true;
                } else {
                    Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        });
    }

    private void performSearch(String searchQuery) {
        // Update search icon to show searching state
        if (searchIcon != null) {
            searchIcon.setAlpha(0.5f); // Dim the icon to show searching
        }
        
        // Show loading indicator
        Toast.makeText(this, "Searching for products...", Toast.LENGTH_SHORT).show();
        
        // Search for products in background thread
        new Thread(() -> {
            List<Product> searchResults = databaseHelper.searchProducts(searchQuery);
            
            // Update UI on main thread
            runOnUiThread(() -> {
                // Restore search icon
                if (searchIcon != null) {
                    searchIcon.setAlpha(1.0f);
                }
                
                if (searchResults.isEmpty()) {
                    // No products found
                    showNoProductsFoundDialog(searchQuery);
                } else {
                    // Products found, navigate to shop with search query
                    Toast.makeText(this, "Found " + searchResults.size() + " product(s)", Toast.LENGTH_SHORT).show();
                    openProducts(searchQuery);
                }
            });
        }).start();
    }

    private void openProductsByCategory(int categoryId) {
        Intent i = new Intent(this, ShopActivity.class);
        i.putExtra("category_filter", categoryId);
        startActivity(i);
    }

    private void openProducts(@Nullable String initialQuery) {
        Intent i = new Intent(this, ShopActivity.class);
        if (initialQuery != null && !initialQuery.isEmpty()) {
            i.putExtra("initial_query", initialQuery);
        }
        startActivity(i);
    }

    private void showNoProductsFoundDialog(String searchQuery) {
        new AlertDialog.Builder(this)
                .setTitle("No Products Found")
                .setMessage("Sorry, no products were found for \"" + searchQuery + "\".\n\nTry searching with different keywords or browse our categories.")
                .setPositiveButton("Browse Categories", (dialog, which) -> {
                    // Open shop without search query to show all products
                    openProducts(null);
                })
                .setNegativeButton("Try Again", (dialog, which) -> {
                    // Clear search field and focus on it
                    etSearch.setText("");
                    etSearch.requestFocus();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void showDetailedUserInfo() {
        SessionManager session = new SessionManager(this);
        
        if (!session.isLoggedIn()) {
            return;
        }
        
        // Get detailed session information
        String sessionInfo = session.getSessionInfo();
        
        // Show detailed info in a simple dialog
        new AlertDialog.Builder(this)
                .setTitle("Session Details")
                .setMessage(sessionInfo)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up search handler to prevent memory leaks
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    private void addStylishAnimations() {
        // Animate header elements
        animateHeaderElements();
        
        // Animate promo cards
        animatePromoCards();
        
        // Add click animations to interactive elements
        addClickAnimations();
    }

    private void animateHeaderElements() {
        // Animate avatar
        if (avatar != null) {
            Animation slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            slideInUp.setStartOffset(100);
            avatar.startAnimation(slideInUp);
        }
        
        // Animate welcome text
        if (welcome != null) {
            Animation slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            slideInUp.setStartOffset(200);
            welcome.startAnimation(slideInUp);
        }
        
        // Animate user name
        if (userName != null) {
            Animation slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            slideInUp.setStartOffset(300);
            userName.startAnimation(slideInUp);
        }
        
        // Animate search bar
        if (etSearch != null) {
            Animation slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            slideInUp.setStartOffset(400);
            etSearch.startAnimation(slideInUp);
        }
    }

    private void animatePromoCards() {
        // Find promo cards and animate them
        View promo1 = findViewById(R.id.promoSuperSale);
        View promo2 = findViewById(R.id.promoNewArrivals);
        View promo3 = findViewById(R.id.promoFlashDeals);
        
        if (promo1 != null) {
            Animation slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            slideInUp.setStartOffset(500);
            promo1.startAnimation(slideInUp);
        }
        
        if (promo2 != null) {
            Animation slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            slideInUp.setStartOffset(600);
            promo2.startAnimation(slideInUp);
        }
        
        if (promo3 != null) {
            Animation slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
            slideInUp.setStartOffset(700);
            promo3.startAnimation(slideInUp);
        }
    }

    private void addClickAnimations() {
        // Add scale animations to clickable elements
        View[] clickableViews = {
            findViewById(R.id.avatar),
            findViewById(R.id.promoSuperSale),
            findViewById(R.id.promoNewArrivals),
            findViewById(R.id.promoFlashDeals),
            findViewById(R.id.seeAll)
        };
        
        for (View view : clickableViews) {
            if (view != null) {
                view.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case android.view.MotionEvent.ACTION_DOWN:
                            Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
                            v.startAnimation(scaleDown);
                            break;
                        case android.view.MotionEvent.ACTION_UP:
                        case android.view.MotionEvent.ACTION_CANCEL:
                            Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
                            v.startAnimation(scaleUp);
                            break;
                    }
                    return false;
                });
            }
        }
    }

    // Fragment management methods
    public void showHomeFragment() {
        // Hide any current fragment and show home content
        if (currentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .hide(currentFragment)
                    .commit();
            currentFragment = null;
        }
        
        // Show home content
        findViewById(R.id.scrollContent).setVisibility(View.VISIBLE);
        
        // Hide fragment container
        findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
    }

    private void showCartFragment() {
        // Hide home content
        findViewById(R.id.scrollContent).setVisibility(View.GONE);
        
        // Show fragment container
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        
        // Create cart fragment if not exists
        if (cartFragment == null) {
            cartFragment = new CartFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, cartFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .show(cartFragment)
                    .commit();
        }
        
        currentFragment = cartFragment;
    }

    // Method to update cart badge from fragments
    public void updateCartBadge() {
        // This method can be called by fragments to update cart badge
        // Implementation depends on where the cart badge is displayed
        if (cartFragment != null) {
            cartFragment.refreshCart();
        }
    }
    
    // Method to handle checkout button click from fragment layout
    public void onCheckout(View view) {
        if (cartFragment != null) {
            cartFragment.onCheckout(view);
        }
    }
}
