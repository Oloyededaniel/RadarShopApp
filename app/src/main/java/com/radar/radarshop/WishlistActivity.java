package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class WishlistActivity extends AppCompatActivity implements WishlistCardAdapter.OnWishlistItemListener {

    private RecyclerView rvWishlist;
    private TextView tvWishlistCount;
    private BottomNavigationView bottomNavigationView;
    private Button btnShareWishlist;
    private LinearLayout layoutEmptyWishlist;
    private DatabaseHelper db;
    private SessionManager session;
    private WishlistCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        rvWishlist = findViewById(R.id.rvWishlist);
        tvWishlistCount = findViewById(R.id.tvWishlistCount);
        bottomNavigationView = findViewById(R.id.bottomNav);
        btnShareWishlist = findViewById(R.id.btnShareWishlist);
        layoutEmptyWishlist = findViewById(R.id.layoutEmptyWishlist);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        setupRecyclerView();
        setupBottomNavigation();
        setupShareButton();
        loadWishlist();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist();
    }

    private void setupRecyclerView() {
        // Use GridLayoutManager for 2 columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvWishlist.setLayoutManager(layoutManager);
        
        // Initialize adapter
        adapter = new WishlistCardAdapter(this, null, db);
        adapter.setOnWishlistItemListener(this);
        rvWishlist.setAdapter(adapter);
        
        // Add spacing between items
        rvWishlist.addItemDecoration(new GridSpacingItemDecoration(2, 16, true));
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
                    Intent cartIntent = new Intent(this, CartActivity.class);
                    cartIntent.putExtra("from_activity", "WishlistActivity");
                    startActivity(cartIntent);
                    finish();
                    return true;
                } else if (id == R.id.nav_wishlist) {
                    return true; // Already in wishlist
                }
                return false;
            });
            bottomNavigationView.setSelectedItemId(R.id.nav_wishlist);
        }
    }

    private void setupShareButton() {
        if (btnShareWishlist != null) {
            btnShareWishlist.setOnClickListener(v -> {
                shareWishlist();
            });
        }
    }

    private void shareWishlist() {
        String email = session.getEmail();
        if (email == null || email.isEmpty()) {
            email = "demo@example.com";
        }
        
        List<Product> wishlist = db.getWishlist(email);
        
        if (wishlist.isEmpty()) {
            Toast.makeText(this, "Your wishlist is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build formatted wishlist text
        StringBuilder shareText = new StringBuilder();
        shareText.append("My Wishlist (").append(wishlist.size()).append(" items)\n\n");
        
        for (int i = 0; i < wishlist.size(); i++) {
            Product product = wishlist.get(i);
            shareText.append(i + 1).append(". ").append(product.getName());
            shareText.append(" - ").append(product.getFormattedPrice());
            
            // Add original price if on sale (simulate 20% discount)
            double originalPrice = product.getPrice() * 1.2;
            if (originalPrice > product.getPrice()) {
                shareText.append(" (was $").append(String.format("%.2f", originalPrice)).append(")");
            }
            
            if (!product.isInStock()) {
                shareText.append(" - Out of Stock");
            }
            
            shareText.append("\n");
        }
        
        shareText.append("\nShared from RadarShop");

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Wishlist from RadarShop");

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Wishlist via"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share wishlist", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWishlist() {
        String email = session.getEmail();
        if (email == null || email.isEmpty()) email = "demo@example.com";
        List<Product> wishlist = db.getWishlist(email);

        // Update badge count
        if (tvWishlistCount != null) {
            tvWishlistCount.setText(String.valueOf(wishlist.size()));
        }

        // Update adapter
        if (adapter != null) {
            adapter.updateProducts(wishlist);
        }

        // Show/hide empty state
        if (layoutEmptyWishlist != null) {
            if (wishlist.isEmpty()) {
                layoutEmptyWishlist.setVisibility(View.VISIBLE);
                if (btnShareWishlist != null) {
                    btnShareWishlist.setVisibility(View.GONE);
                }
            } else {
                layoutEmptyWishlist.setVisibility(View.GONE);
                if (btnShareWishlist != null) {
                    btnShareWishlist.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onItemRemoved(int newCount) {
        if (tvWishlistCount != null) {
            tvWishlistCount.setText(String.valueOf(newCount));
        }
        if (newCount == 0 && layoutEmptyWishlist != null) {
            layoutEmptyWishlist.setVisibility(View.VISIBLE);
            if (btnShareWishlist != null) {
                btnShareWishlist.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("productId", product.getId());
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onAddToCart(Product product) {
        String email = session.getEmail();
        if (email == null || email.isEmpty()) {
            email = "demo@example.com";
        }

        // Check if item is in wishlist and remove it
        boolean wasInWishlist = db.isInWishlist(email, product.getId());
        
        // Add to cart
        boolean success = db.addToCart(email, product.getId(), 1);
        
        // Verify the item is actually in cart
        if (success) {
            boolean isActuallyInCart = db.isInCart(email, product.getId());
            if (isActuallyInCart) {
                // Remove from wishlist if it was there
                if (wasInWishlist) {
                    db.removeFromWishlist(email, product.getId());
                }
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
                // Reload wishlist to reflect the removal
                loadWishlist();
            } else {
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
        }
    }

    // Grid spacing decorator class (same as ShopFragment)
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
            int position = parent.getChildAdapterPosition(view);
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            
            int column = position % spanCount;
            float spacingPx = spacing * view.getResources().getDisplayMetrics().density;

            if (includeEdge) {
                outRect.left = (int) (spacingPx - column * spacingPx / spanCount);
                outRect.right = (int) ((column + 1) * spacingPx / spanCount);

                if (position < spanCount) {
                    outRect.top = (int) spacingPx;
                }
                outRect.bottom = (int) spacingPx;
            } else {
                outRect.left = (int) (column * spacingPx / spanCount);
                outRect.right = (int) (spacingPx - (column + 1) * spacingPx / spanCount);
                if (position >= spanCount) {
                    outRect.top = (int) spacingPx;
                }
            }
        }
    }
}
