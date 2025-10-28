package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemInteractionListener {
    
    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private TextView tvTotalAmount;
    private TextView tvCartEmpty;
    private Button btnCheckout;
    private LinearLayout layoutCartSummary;
    private LinearLayout layoutEmptyCart;
    
    private List<CartItem> cartItems;
    private double totalAmount = 0.0;
    private int cartItemCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize components
        initializeViews(view);
        initializeDatabase();
        setupRecyclerView();
        loadCartItems();
    }

    private void initializeViews(View view) {
        rvCartItems = view.findViewById(R.id.rvCartItems);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvCartEmpty = view.findViewById(R.id.tvCartEmpty);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        layoutCartSummary = view.findViewById(R.id.layoutCartSummary);
        layoutEmptyCart = view.findViewById(R.id.layoutEmptyCart);
    }

    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(getContext());
        sessionManager = new SessionManager(getContext());
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        cartItems = new java.util.ArrayList<>();
        cartAdapter = new CartAdapter(getContext(), cartItems, databaseHelper);
        cartAdapter.setOnCartItemInteractionListener(this);
        rvCartItems.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        String userEmail = sessionManager.getEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            cartItems = databaseHelper.getCartItems(userEmail);
            cartAdapter.updateCartItems(cartItems);
            updateCartSummary();
            updateEmptyState();
        } else {
            showEmptyCart();
        }
    }

    private void updateCartSummary() {
        totalAmount = 0.0;
        cartItemCount = 0;
        
        for (CartItem item : cartItems) {
            totalAmount += item.getPrice() * item.getQuantity();
            cartItemCount += item.getQuantity();
        }
        
        tvTotalAmount.setText(String.format("$%.2f", totalAmount));
        
        if (cartItemCount > 0) {
            btnCheckout.setText("Checkout (" + cartItemCount + " items)");
            btnCheckout.setEnabled(true);
        } else {
            btnCheckout.setText("Checkout");
            btnCheckout.setEnabled(false);
        }
    }

    private void updateEmptyState() {
        if (cartItems.isEmpty()) {
            showEmptyCart();
        } else {
            showCartContent();
        }
    }

    private void showEmptyCart() {
        layoutEmptyCart.setVisibility(View.VISIBLE);
        layoutCartSummary.setVisibility(View.GONE);
        rvCartItems.setVisibility(View.GONE);
        
        // Animate empty cart appearance
        Animation slideInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.cart_slide_in);
        layoutEmptyCart.startAnimation(slideInAnimation);
    }

    private void showCartContent() {
        layoutEmptyCart.setVisibility(View.GONE);
        layoutCartSummary.setVisibility(View.VISIBLE);
        rvCartItems.setVisibility(View.VISIBLE);
        
        // Animate cart content appearance
        Animation slideInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.cart_slide_in);
        rvCartItems.startAnimation(slideInAnimation);
        layoutCartSummary.startAnimation(slideInAnimation);
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            // Remove item from cart
            databaseHelper.removeFromCart(sessionManager.getEmail(), item.getProductId());
            cartItems.remove(item);
            cartAdapter.updateCartItems(cartItems);
            Toast.makeText(getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
        } else {
            // Update quantity
            databaseHelper.updateCartItemQuantity(sessionManager.getEmail(), item.getProductId(), newQuantity);
            item.setQuantity(newQuantity);
            cartAdapter.updateCartItems(cartItems);
        }
        
        updateCartSummary();
        updateEmptyState();
        
        // Notify parent activity to update cart badge
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).updateCartBadge();
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        databaseHelper.removeFromCart(sessionManager.getEmail(), item.getProductId());
        cartItems.remove(item);
        cartAdapter.updateCartItems(cartItems);
        updateCartSummary();
        updateEmptyState();
        
        Toast.makeText(getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
        
        // Notify parent activity to update cart badge
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).updateCartBadge();
        }
    }

    @Override
    public void onCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Start checkout process
        Intent intent = new Intent(getContext(), CheckoutActivity.class);
        intent.putExtra("cart_items", (java.io.Serializable) cartItems);
        intent.putExtra("total_amount", totalAmount);
        intent.putExtra("item_count", cartItemCount);
        startActivity(intent);
    }

    @Override
    public void onProductClick(CartItem item) {
        // Navigate to product detail
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("productId", item.getProductId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh cart when returning to fragment
        loadCartItems();
    }

    // Method to refresh cart from external calls
    public void refreshCart() {
        loadCartItems();
    }

    // Handle button clicks from layout
    public void startShopping(View view) {
        // Navigate to shop
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showHomeFragment();
        } else if (getActivity() instanceof ShopActivity) {
            ((ShopActivity) getActivity()).showShopFragment();
        }
    }

    public void onCheckout(View view) {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Start checkout process
        Intent intent = new Intent(getContext(), CheckoutActivity.class);
        intent.putExtra("cart_items", (java.io.Serializable) cartItems);
        intent.putExtra("total_amount", totalAmount);
        intent.putExtra("item_count", cartItemCount);
        startActivity(intent);
    }
}
