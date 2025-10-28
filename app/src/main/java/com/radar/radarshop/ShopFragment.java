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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment implements ProductCardAdapter.OnProductInteractionListener {
    
    private RecyclerView rvProducts;
    private ProductCardAdapter productAdapter;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private TextInputEditText etSearch;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize components
        initializeViews(view);
        initializeDatabase();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupCartFab();
        loadProducts();
        updateCartBadge();
        
        // Apply any pending category filter
        applyPendingCategoryFilter();
    }

    private void initializeViews(View view) {
        rvProducts = view.findViewById(R.id.rvProducts);
        etSearch = view.findViewById(R.id.etSearch);
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
        databaseHelper = new DatabaseHelper(getContext());
        sessionManager = new SessionManager(getContext());
        
        // Ensure we have demo data
        databaseHelper.ensureSeedProducts(20);
    }

    private void setupRecyclerView() {
        // Use GridLayoutManager for 2 columns
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(layoutManager);
        
        // Initialize adapter
        filteredProducts = new ArrayList<>();
        productAdapter = new ProductCardAdapter(getContext(), filteredProducts, databaseHelper);
        productAdapter.setOnProductInteractionListener(this);
        rvProducts.setAdapter(productAdapter);
        
        // Add spacing between items
        rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, 16, true));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                filterProducts();
            }
        });
    }

    private void setupFilters() {
        // Set up chip listeners
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = -1;
                filterProducts();
            }
        });

        chipElectronics.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = 1; // Electronics category ID
                filterProducts();
            }
        });

        chipClothing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = 3; // Sportswear category ID
                filterProducts();
            }
        });

        chipHome.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = 6; // Home category ID
                filterProducts();
            }
        });
    }

    private void setupCartFab() {
        fabCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CartActivity.class);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        showLoading(true);
        
        // Load all products in background
        new Thread(() -> {
            allProducts = databaseHelper.getAllProducts();
            
            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    filterProducts();
                });
            }
        }).start();
    }

    private void filterProducts() {
        if (allProducts == null) return;

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
        if (filteredProducts.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void updateCartBadge() {
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
    }

    @Override
    public void onProductClick(Product product) {
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
        fabCart.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction(() -> {
                fabCart.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150);
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
            // Update chip selection
            chipAll.setChecked(categoryId == -1);
            chipElectronics.setChecked(categoryId == 1); // Electronics
            chipClothing.setChecked(categoryId == 3); // Sportswear (was Clothing)
            chipHome.setChecked(categoryId == 6); // Home
        }
        
        // Always filter products if we have data
        if (allProducts != null) {
            filterProducts();
        }
    }

    public void setSearchQuery(String query) {
        currentSearchQuery = query;
        if (etSearch != null) {
            etSearch.setText(query);
        }
        filterProducts();
    }

    private void applyPendingCategoryFilter() {
        // Apply the category filter if it was set before views were initialized
        if (currentCategoryFilter != -1) {
            if (chipAll != null && chipElectronics != null && chipClothing != null && chipHome != null) {
                // Update chip selection
                chipAll.setChecked(currentCategoryFilter == -1);
                chipElectronics.setChecked(currentCategoryFilter == 1); // Electronics
                chipClothing.setChecked(currentCategoryFilter == 3); // Sportswear (was Clothing)
                chipHome.setChecked(currentCategoryFilter == 6); // Home
            }
            
            // Filter products
            if (allProducts != null) {
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
    }
}
