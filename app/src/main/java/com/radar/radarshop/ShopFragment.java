package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.RecognizerIntent;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment implements ProductCardAdapter.OnProductInteractionListener {
    
    private RecyclerView rvProducts;
    private ProductCardAdapter productAdapter;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private TextInputEditText etSearch;
    private TextInputLayout textInputLayout;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipElectronics, chipClothing, chipHome;
    private ProgressBar progressBar;
    private TextView tvCartBadge;
    private FloatingActionButton fabCart;
    private View layoutEmptyState;
    
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private String currentSearchQuery = "";
    private int currentCategoryFilter = -1; // -1 means all categories
    private int cartItemCount = 0;
    
    // Speech recognition
    private static final int SPEECH_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_shop, container, false);
        } catch (Exception e) {
            android.util.Log.e("ShopFragment", "Error inflating layout", e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Check if fragment is still attached
        if (getContext() == null || getActivity() == null || getActivity().isFinishing()) {
            android.util.Log.w("ShopFragment", "Fragment not attached, skipping initialization");
            return;
        }
        
        try {
            // Initialize components in order
            initializeViews(view);
            
            // Initialize database first - this is critical
            initializeDatabase();
            
            // Setup UI components only if database is initialized
            if (databaseHelper != null && getContext() != null) {
                setupRecyclerView();
                setupSearch();
                setupFilters();
                setupCartFab();
                
                // Load products and update UI
                loadProducts();
                
                // Update cart badge only if session manager is ready
                if (sessionManager != null) {
                    updateCartBadge();
                }
                
                // Apply any pending category filter
                applyPendingCategoryFilter();
            } else {
                android.util.Log.w("ShopFragment", "Database helper not initialized, retrying...");
                // Retry after a short delay if database isn't ready
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (databaseHelper != null && getContext() != null && !isDetached()) {
                        setupRecyclerView();
                        setupSearch();
                        setupFilters();
                        setupCartFab();
                        loadProducts();
                        if (sessionManager != null) {
                            updateCartBadge();
                        }
                        applyPendingCategoryFilter();
                    }
                }, 100);
            }
        } catch (Exception e) {
            // Log error and show user-friendly message
            android.util.Log.e("ShopFragment", "Error initializing fragment", e);
            e.printStackTrace();
            if (getContext() != null && !isDetached()) {
                Toast.makeText(getContext(), "Error loading shop. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeViews(View view) {
        rvProducts = view.findViewById(R.id.rvProducts);
        etSearch = view.findViewById(R.id.etSearch);
        textInputLayout = view.findViewById(R.id.textInputLayoutSearch);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        chipAll = view.findViewById(R.id.chipAll);
        chipElectronics = view.findViewById(R.id.chipElectronics);
        chipClothing = view.findViewById(R.id.chipClothing);
        chipHome = view.findViewById(R.id.chipHome);
        progressBar = view.findViewById(R.id.progressBar);
        tvCartBadge = view.findViewById(R.id.tvCartBadge);
        fabCart = view.findViewById(R.id.fabCart);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
    }

    private void initializeDatabase() {
        if (getContext() == null) return;
        try {
            databaseHelper = new DatabaseHelper(getContext());
            sessionManager = new SessionManager(getContext());
            
            // Ensure we have demo data - do this in background to avoid blocking UI
            if (databaseHelper != null) {
                new Thread(() -> {
                    try {
                        databaseHelper.ensureSeedProducts(20);
                    } catch (Exception e) {
                        android.util.Log.e("ShopFragment", "Error seeding products", e);
                    }
                }).start();
            }
        } catch (Exception e) {
            android.util.Log.e("ShopFragment", "Error initializing database", e);
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        try {
            if (rvProducts == null || getContext() == null || databaseHelper == null) {
                android.util.Log.w("ShopFragment", "Cannot setup RecyclerView - missing dependencies");
                return;
            }
            
            // Use GridLayoutManager for 2 columns
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            rvProducts.setLayoutManager(layoutManager);
            
            // Initialize adapter
            if (filteredProducts == null) {
                filteredProducts = new ArrayList<>();
            }
            productAdapter = new ProductCardAdapter(getContext(), filteredProducts, databaseHelper);
            productAdapter.setOnProductInteractionListener(this);
            rvProducts.setAdapter(productAdapter);
            
            // Add spacing between items
            rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, 16, true));
        } catch (Exception e) {
            android.util.Log.e("ShopFragment", "Error setting up RecyclerView", e);
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                if (allProducts != null && productAdapter != null) {
                    filterProducts();
                }
            }
        });
        
        // Set up speech recognition for the end icon
        if (textInputLayout != null) {
            textInputLayout.setEndIconOnClickListener(v -> startSpeechRecognition());
        }
    }

    private void setupFilters() {
        // Only set up listeners if views are initialized
        if (chipAll == null || chipElectronics == null || chipClothing == null || chipHome == null) {
            return;
        }
        
        // Set up chip listeners
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = -1;
                if (allProducts != null && productAdapter != null) {
                    filterProducts();
                }
            }
        });

        chipElectronics.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = 1; // Electronics category ID
                if (allProducts != null && productAdapter != null) {
                    filterProducts();
                }
            }
        });

        chipClothing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = 3; // Sportswear category ID
                if (allProducts != null && productAdapter != null) {
                    filterProducts();
                }
            }
        });

        chipHome.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = 6; // Home category ID
                if (allProducts != null && productAdapter != null) {
                    filterProducts();
                }
            }
        });
    }

    private void setupCartFab() {
        if (fabCart == null) return;
        fabCart.setOnClickListener(v -> {
            if (getContext() == null) return;
            Intent intent = new Intent(getContext(), CartActivity.class);
            intent.putExtra("from_activity", "ShopFragment");
            startActivity(intent);
        });
    }

    private void loadProducts() {
        if (databaseHelper == null) return;
        
        showLoading(true);
        
        // Load all products in background
        new Thread(() -> {
            if (databaseHelper == null) return;
            
            try {
                allProducts = databaseHelper.getAllProducts();
                
                // Update UI on main thread
                if (getActivity() != null && !getActivity().isFinishing()) {
                    getActivity().runOnUiThread(() -> {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        showLoading(false);
                        // Apply any pending category filter before filtering
                        applyPendingCategoryFilter();
                        filterProducts();
                    });
                }
            } catch (Exception e) {
                // Handle any database errors
                if (getActivity() != null && !getActivity().isFinishing()) {
                    getActivity().runOnUiThread(() -> {
                        if (getActivity() == null || getActivity().isFinishing()) return;
                        showLoading(false);
                        Toast.makeText(getContext(), "Error loading products", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void filterProducts() {
        if (allProducts == null || productAdapter == null) return;
        if (filteredProducts == null) {
            filteredProducts = new ArrayList<>();
        }

        filteredProducts.clear();
        
        for (Product product : allProducts) {
            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                product.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                product.getBrand().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                product.getDescription().toLowerCase().contains(currentSearchQuery.toLowerCase());
            
            boolean matchesCategory = currentCategoryFilter == -1 || 
                product.getCategoryId() == currentCategoryFilter;
            
            if (matchesSearch && matchesCategory) {
                filteredProducts.add(product);
            }
        }
        
        productAdapter.updateProducts(filteredProducts);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (layoutEmptyState == null || rvProducts == null) return;
        if (filteredProducts == null) {
            filteredProducts = new ArrayList<>();
        }
        
        if (filteredProducts.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar == null || rvProducts == null) return;
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void updateCartBadge() {
        if (sessionManager == null || databaseHelper == null || tvCartBadge == null) return;
        
        try {
            String userEmail = sessionManager.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                cartItemCount = databaseHelper.getCartItemCount(userEmail);
            } else {
                cartItemCount = 0;
            }
            
            if (cartItemCount > 0) {
                tvCartBadge.setText(String.valueOf(cartItemCount));
                tvCartBadge.setVisibility(View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            android.util.Log.e("ShopFragment", "Error updating cart badge", e);
            if (tvCartBadge != null) {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onProductClick(Product product) {
        if (getContext() == null || product == null) return;
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product", product);
        intent.putExtra("productId", product.getId());
        startActivity(intent);
    }

    @Override
    public void onAddToCart(Product product, int quantity) {
        updateCartBadge();
        showCartAnimation();
        
        // Notify parent activity to update cart badge
        if (getActivity() instanceof ShopActivity) {
            ((ShopActivity) getActivity()).updateCartBadge();
        }
    }

    @Override
    public void onRemoveFromCart(Product product) {
        updateCartBadge();
        
        // Notify parent activity to update cart badge
        if (getActivity() instanceof ShopActivity) {
            ((ShopActivity) getActivity()).updateCartBadge();
        }
    }

    @Override
    public void onAddToWishlist(Product product) {
        // Optional: Show wishlist animation
    }

    @Override
    public void onRemoveFromWishlist(Product product) {
        // Optional: Show removal animation
    }

    @Override
    public void onCartCountChanged(int newCount) {
        updateCartBadge();
    }

    private void showCartAnimation() {
        // Simple cart animation - could be enhanced with more sophisticated animations
        if (fabCart == null) return;
        fabCart.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction(() -> {
                if (fabCart != null) {
                    fabCart.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150);
                }
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh cart count when returning to fragment
        updateCartBadge();
    }

    public void setCategoryFilter(int categoryId) {
        currentCategoryFilter = categoryId;
        
        // Only update UI if views are initialized
        if (chipAll != null && chipElectronics != null && chipClothing != null && chipHome != null) {
            // Temporarily disable listeners to avoid recursive calls
            chipAll.setOnCheckedChangeListener(null);
            chipElectronics.setOnCheckedChangeListener(null);
            chipClothing.setOnCheckedChangeListener(null);
            chipHome.setOnCheckedChangeListener(null);
            
            // Uncheck all chips first
            chipAll.setChecked(false);
            chipElectronics.setChecked(false);
            chipClothing.setChecked(false);
            chipHome.setChecked(false);
            
            // Update chip selection based on category ID
            if (categoryId == -1) {
                chipAll.setChecked(true);
            } else if (categoryId == 1) {
                chipElectronics.setChecked(true); // Electronics
            } else if (categoryId == 3) {
                chipClothing.setChecked(true); // Sportswear
            } else if (categoryId == 6) {
                chipHome.setChecked(true); // Home
            }
            // If categoryId doesn't match any chip, no chip will be selected
            // but the filter will still work via currentCategoryFilter
            
            // Re-enable listeners
            setupFilters();
        }
        
        // Always filter products if we have data, or wait for products to load
        if (allProducts != null && productAdapter != null) {
            filterProducts();
        }
    }

    public void setSearchQuery(String query) {
        currentSearchQuery = query;
        if (etSearch != null) {
            etSearch.setText(query);
        }
        if (allProducts != null && productAdapter != null) {
            filterProducts();
        }
    }

    private void applyPendingCategoryFilter() {
        // Apply the category filter if it was set before views were initialized
        if (currentCategoryFilter != -1) {
            if (chipAll != null && chipElectronics != null && chipClothing != null && chipHome != null) {
                // Temporarily disable listeners to avoid recursive calls
                chipAll.setOnCheckedChangeListener(null);
                chipElectronics.setOnCheckedChangeListener(null);
                chipClothing.setOnCheckedChangeListener(null);
                chipHome.setOnCheckedChangeListener(null);
                
                // Uncheck all chips first
                chipAll.setChecked(false);
                chipElectronics.setChecked(false);
                chipClothing.setChecked(false);
                chipHome.setChecked(false);
                
                // Update chip selection based on category ID
                if (currentCategoryFilter == -1) {
                    chipAll.setChecked(true);
                } else if (currentCategoryFilter == 1) {
                    chipElectronics.setChecked(true); // Electronics
                } else if (currentCategoryFilter == 3) {
                    chipClothing.setChecked(true); // Sportswear
                } else if (currentCategoryFilter == 6) {
                    chipHome.setChecked(true); // Home
                }
                // If categoryId doesn't match any chip, no chip will be selected
                // but the filter will still work via currentCategoryFilter
                
                // Re-enable listeners
                setupFilters();
            }
            
            // Filter products
            if (allProducts != null && productAdapter != null) {
                filterProducts();
            }
        }
    }

    // Grid spacing decorator class
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            
            int column = position % spanCount; // item column
            float spacingPx = spacing * view.getResources().getDisplayMetrics().density; // Convert dp to px

            if (includeEdge) {
                outRect.left = (int) (spacingPx - column * spacingPx / spanCount);
                outRect.right = (int) ((column + 1) * spacingPx / spanCount);

                if (position < spanCount) { // top edge
                    outRect.top = (int) spacingPx;
                }
                outRect.bottom = (int) spacingPx; // item bottom
            } else {
                outRect.left = (int) (column * spacingPx / spanCount);
                outRect.right = (int) (spacingPx - (column + 1) * spacingPx / spanCount);
                if (position >= spanCount) {
                    outRect.top = (int) spacingPx; // item top
                }
            }
        }
    }

    private void startSpeechRecognition() {
        // Check if speech recognition is available
        if (!isSpeechRecognitionAvailable()) {
            Toast.makeText(getContext(), "Speech recognition is not available on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for RECORD_AUDIO permission
        if (getContext() == null) return;
        
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 
                    PERMISSION_REQUEST_CODE);
            return;
        }

        // Start speech recognition intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search for products");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error starting speech recognition: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSpeechRecognitionAvailable() {
        if (getContext() == null) return false;
        PackageManager pm = getContext().getPackageManager();
        return pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0).size() > 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK && data != null) {
            java.util.ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                // Set the spoken text in the search field
                if (etSearch != null) {
                    etSearch.setText(spokenText);
                    currentSearchQuery = spokenText;
                    // Trigger search immediately
                    if (allProducts != null && productAdapter != null) {
                        filterProducts();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start speech recognition
                startSpeechRecognition();
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Microphone permission is required for voice search", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
