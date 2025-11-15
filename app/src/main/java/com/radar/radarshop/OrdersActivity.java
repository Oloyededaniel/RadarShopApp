package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity implements OrderAdapter.OnOrderActionListener {
    
    private RecyclerView recyclerViewOrders;
    private LinearLayout layoutEmptyState;
    private TextView tvOrdersTitle;
    private BottomNavigationView bottomNavigationView;
    private View fragmentContainer;
    
    private OrderAdapter orderAdapter;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    
    private List<DatabaseHelper.Order> orders = new ArrayList<>();
    private Handler statusUpdateHandler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        
        initializeViews();
        initializeDatabase();
        setupRecyclerView();
        setupBottomNavigation();
        loadOrders();
        startStatusSimulation();
    }
    
    private void initializeViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvOrdersTitle = findViewById(R.id.tvOrdersTitle);
        bottomNavigationView = findViewById(R.id.bottomNav);
        fragmentContainer = findViewById(R.id.fragmentContainer);
    }
    
    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
    }
    
    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(orders, this, databaseHelper, sessionManager);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);
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
                    return true; // Already in orders
                } else if (id == R.id.nav_cart) {
                    Intent cartIntent = new Intent(this, CartActivity.class);
                    cartIntent.putExtra("from_activity", "OrdersActivity");
                    startActivity(cartIntent);
                    finish();
                    return true;
                } else if (id == R.id.nav_wishlist) {
                    startActivity(new Intent(this, WishlistActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            bottomNavigationView.setSelectedItemId(R.id.nav_orders);
        }
    }
    
    private void loadOrders() {
        String userEmail = sessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "demo@example.com"; // Fallback for demo
        }
        
        List<DatabaseHelper.Order> userOrders = databaseHelper.getUserOrders(userEmail);
        
        // If no orders exist, create sample orders for demo
        if (userOrders.isEmpty()) {
            //databaseHelper.createSampleOrders(userEmail);
            userOrders = databaseHelper.getUserOrders(userEmail);
        }
        
        orders.clear();
        orders.addAll(userOrders);
        
        updateUI();
    }
    
    private void updateUI() {
        if (orders.isEmpty()) {
            recyclerViewOrders.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            orderAdapter.updateOrders(orders);
        }
    }
    
    private void startStatusSimulation() {
        // Simulate order status updates based on shipping method
        statusUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateOrderStatuses();
                statusUpdateHandler.postDelayed(this, 30000); // Check every 30 seconds
            }
        }, 1000); // Start after 1 second
    }
    
    private void updateOrderStatuses() {
        boolean hasUpdates = false;
        
        for (DatabaseHelper.Order order : orders) {
            if ("processing".equals(order.status)) {
                // Check if enough time has passed to change status
                if (shouldUpdateToShipped(order)) {
                    databaseHelper.updateOrderStatus(order.id, "shipped");
                    order = getUpdatedOrder(order.id);
                    hasUpdates = true;
                }
            } else if ("shipped".equals(order.status)) {
                // Check if enough time has passed to change to delivered
                if (shouldUpdateToDelivered(order)) {
                    databaseHelper.updateOrderStatus(order.id, "delivered");
                    order = getUpdatedOrder(order.id);
                    hasUpdates = true;
                }
            }
        }
        
        if (hasUpdates) {
            loadOrders(); // Reload orders to reflect changes
        }
    }
    
    private boolean shouldUpdateToShipped(DatabaseHelper.Order order) {
        // For demo purposes, update to shipped after 1 minute for overnight, 2 minutes for express, 3 minutes for standard
        long timeSinceCreated = System.currentTimeMillis() - Long.parseLong(order.orderNumber.substring(4));
        long thresholdMinutes = getShippingThresholdMinutes(order.shippingMethod, "shipped");
        
        return timeSinceCreated > (thresholdMinutes * 60 * 1000);
    }
    
    private boolean shouldUpdateToDelivered(DatabaseHelper.Order order) {
        // For demo purposes, update to delivered after additional time
        long timeSinceCreated = System.currentTimeMillis() - Long.parseLong(order.orderNumber.substring(4));
        long thresholdMinutes = getShippingThresholdMinutes(order.shippingMethod, "delivered");
        
        return timeSinceCreated > (thresholdMinutes * 60 * 1000);
    }
    
    private long getShippingThresholdMinutes(String shippingMethod, String targetStatus) {
        if ("overnight".equals(shippingMethod)) {
            return targetStatus.equals("shipped") ? 1 : 3; // 1 min to shipped, 3 min to delivered
        } else if ("express".equals(shippingMethod)) {
            return targetStatus.equals("shipped") ? 2 : 5; // 2 min to shipped, 5 min to delivered
        } else { // standard
            return targetStatus.equals("shipped") ? 3 : 7; // 3 min to shipped, 7 min to delivered
        }
    }
    
    private DatabaseHelper.Order getUpdatedOrder(int orderId) {
        // This is a simplified version - in a real app, you'd fetch the updated order from DB
        for (DatabaseHelper.Order order : orders) {
            if (order.id == orderId) {
                return order;
            }
        }
        return null;
    }
    
    @Override
    public void onOrderCardClicked(DatabaseHelper.Order order) {
        // Show order details dialog when card is tapped
        showOrderDetailsDialog(order);
    }
    
    @Override
    public void onViewDetails(DatabaseHelper.Order order) {
        // Show order details dialog
        showOrderDetailsDialog(order);
    }
    
    @Override
    public void onBuyAgain(DatabaseHelper.Order order) {
        // Add order items back to cart
        List<DatabaseHelper.OrderItem> orderItems = databaseHelper.getOrderItems(order.id);
        String userEmail = sessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "demo@example.com";
        }
        
        boolean success = true;
        for (DatabaseHelper.OrderItem item : orderItems) {
            boolean added = databaseHelper.addToCart(userEmail, item.productId, item.quantity);
            if (!added) {
                success = false;
            }
        }
        
        if (success) {
            // Show order details dialog with success message
            showOrderDetailsDialog(order);
        } else {
            Toast.makeText(this, "Failed to add some items to cart", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onWriteReview(DatabaseHelper.Order order) {
        // Show WriteReviewFragment for reviewing products from this order
        showWriteReviewFragment(order);
    }
    
    private void showWriteReviewFragment(DatabaseHelper.Order order) {
        // Hide orders list and show fragment container
        recyclerViewOrders.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // Create and show the fragment
        WriteReviewFragment fragment = WriteReviewFragment.newInstance(order.id, order.orderNumber);
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
    
    public void hideWriteReviewFragment() {
        // Show orders list and hide fragment container
        fragmentContainer.setVisibility(View.GONE);
        recyclerViewOrders.setVisibility(View.VISIBLE);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        // Reload orders to update review status
        loadOrders();
    }
    
    @Override
    public void onTrackOrder(DatabaseHelper.Order order) {
        // Show order details dialog with tracking info
        showOrderDetailsDialog(order);
    }
    
    private void showOrderDetailsDialog(DatabaseHelper.Order order) {
        List<DatabaseHelper.OrderItem> orderItems = databaseHelper.getOrderItems(order.id);
        
        OrderDetailsDialog.OnOrderActionListener dialogListener = new OrderDetailsDialog.OnOrderActionListener() {
            @Override
            public void onBuyAgain(DatabaseHelper.Order order) {
                OrdersActivity.this.onBuyAgain(order);
            }
            
            @Override
            public void onWriteReview(DatabaseHelper.Order order) {
                OrdersActivity.this.onWriteReview(order);
            }
            
            @Override
            public void onTrackOrder(DatabaseHelper.Order order) {
                OrdersActivity.this.onTrackOrder(order);
            }
            
            @Override
            public void onViewDetails(DatabaseHelper.Order order) {
                OrdersActivity.this.onViewDetails(order);
            }
        };
        
        OrderDetailsDialog dialog = new OrderDetailsDialog(
            this, 
            order, 
            orderItems, 
            databaseHelper, 
            sessionManager,
            dialogListener
        );
        
        // Set dialog to be dismissible when clicking outside
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        
        dialog.show();
    }
    
    @Override
    public void onBackPressed() {
        // If fragment is visible, hide it instead of finishing activity
        if (fragmentContainer != null && fragmentContainer.getVisibility() == View.VISIBLE) {
            hideWriteReviewFragment();
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusUpdateHandler != null) {
            statusUpdateHandler.removeCallbacksAndMessages(null);
        }
    }
}
