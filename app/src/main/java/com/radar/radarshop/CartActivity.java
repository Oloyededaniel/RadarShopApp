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
    }

    private void initializeDatabase() {
        db = new DatabaseHelper(this);
        session = new SessionManager(this);
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
            proceedToCheckout();
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

    private void proceedToCheckout() {
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("cart_items", (java.io.Serializable) cartItems);
        intent.putExtra("total_amount", total);
        intent.putExtra("item_count", itemCount);
        startActivity(intent);
    }

    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    public void startShopping(View view) {
        // Navigate back to shop or home
        finish();
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
}
