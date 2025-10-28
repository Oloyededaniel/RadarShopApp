package com.radar.radarshop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity implements OrderSuccessFragment.OnOrderSuccessListener {
    
    private ScrollView scrollView;
    private LinearLayout checkoutContainer;
    private ProgressBar progressBar;
    
    // Order Summary Section
    private CardView orderSummaryCard;
    private TextView tvSubtotal, tvShipping, tvTax, tvTotal;
    private LinearLayout orderItemsContainer;
    
    // Shipping Address Section
    private CardView shippingCard;
    private TextInputEditText etFullName, etAddress, etCity, etState, etZipCode, etPhone;
    private TextInputLayout tilFullName, tilAddress, tilCity, tilState, tilZipCode, tilPhone;
    private RadioGroup rgShippingMethod;
    private RadioButton rbStandard, rbExpress, rbOvernight;
    
    // Payment Section
    private CardView paymentCard;
    private TextInputEditText etCardNumber, etExpiryDate, etCVV, etCardholderName;
    private TextInputLayout tilCardNumber, tilExpiryDate, tilCVV, tilCardholderName;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCreditCard, rbDebitCard;
    
    // Action Buttons
    private Button btnPlaceOrder;
    private ImageButton btnBack;
    
    // Save Notice
    private LinearLayout layoutSaveNotice;
    
    private List<CartItem> cartItems;
    private double subtotal = 0.0;
    private double shippingCost = 0.0;
    private double tax = 0.0;
    private double total = 0.0;
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "CheckoutActivity: onCreate started", Toast.LENGTH_SHORT).show();
        
        setContentView(R.layout.activity_checkout);
        Toast.makeText(this, "CheckoutActivity: setContentView completed", Toast.LENGTH_SHORT).show();
        
        // Initialize database and session
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        
        initializeViews();
        Toast.makeText(this, "CheckoutActivity: initializeViews completed", Toast.LENGTH_SHORT).show();
        
        setupAnimations();
        Toast.makeText(this, "CheckoutActivity: setupAnimations completed", Toast.LENGTH_SHORT).show();
        
        loadCartItems();
        Toast.makeText(this, "CheckoutActivity: loadCartItems completed", Toast.LENGTH_SHORT).show();
        
        loadSavedUserInfo();
        Toast.makeText(this, "CheckoutActivity: loadSavedUserInfo completed", Toast.LENGTH_SHORT).show();
        
        setupListeners();
        Toast.makeText(this, "CheckoutActivity: setupListeners completed", Toast.LENGTH_SHORT).show();
        
        calculateTotals();
        Toast.makeText(this, "CheckoutActivity: calculateTotals completed", Toast.LENGTH_SHORT).show();
        
        updateOrderSummary();
        Toast.makeText(this, "CheckoutActivity: updateOrderSummary completed", Toast.LENGTH_SHORT).show();
        
        Toast.makeText(this, "CheckoutActivity: onCreate completed successfully!", Toast.LENGTH_LONG).show();
    }
    
    private void initializeViews() {
        scrollView = findViewById(R.id.scrollView);
        checkoutContainer = findViewById(R.id.checkoutContainer);
        progressBar = findViewById(R.id.progressBar);
        
        // Order Summary
        orderSummaryCard = findViewById(R.id.orderSummaryCard);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        orderItemsContainer = findViewById(R.id.orderItemsContainer);
        
        // Shipping Address
        shippingCard = findViewById(R.id.shippingCard);
        etFullName = findViewById(R.id.etFullName);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etZipCode = findViewById(R.id.etZipCode);
        etPhone = findViewById(R.id.etPhone);
        
        // TextInputLayouts for error handling
        tilFullName = findViewById(R.id.tilFullName);
        tilAddress = findViewById(R.id.tilAddress);
        tilCity = findViewById(R.id.tilCity);
        tilState = findViewById(R.id.tilState);
        tilZipCode = findViewById(R.id.tilZipCode);
        tilPhone = findViewById(R.id.tilPhone);
        
        rgShippingMethod = findViewById(R.id.rgShippingMethod);
        rbStandard = findViewById(R.id.rbStandard);
        rbExpress = findViewById(R.id.rbExpress);
        rbOvernight = findViewById(R.id.rbOvernight);
        
        // Payment
        paymentCard = findViewById(R.id.paymentCard);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etCVV = findViewById(R.id.etCVV);
        etCardholderName = findViewById(R.id.etCardholderName);
        
        // Payment TextInputLayouts for error handling
        tilCardNumber = findViewById(R.id.tilCardNumber);
        tilExpiryDate = findViewById(R.id.tilExpiryDate);
        tilCVV = findViewById(R.id.tilCVV);
        tilCardholderName = findViewById(R.id.tilCardholderName);
        
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCreditCard = findViewById(R.id.rbCreditCard);
        rbDebitCard = findViewById(R.id.rbDebitCard);
        
        // Action Buttons
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnBack = findViewById(R.id.btnBack);
        
        // Save Notice
        layoutSaveNotice = findViewById(R.id.layoutSaveNotice);
        
        // Initialize cart items
        cartItems = new ArrayList<>();
    }
    
    private void setupAnimations() {
        // Animate cards appearing with stagger effect
        animateCardAppearance(orderSummaryCard, 0);
        animateCardAppearance(shippingCard, 100);
        animateCardAppearance(paymentCard, 200);
        
        // Animate buttons
        animateButtonAppearance(btnPlaceOrder, 300);
    }
    
    private void animateCardAppearance(CardView card, int delay) {
        card.setAlpha(0f);
        card.setTranslationY(50f);
        
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
        ObjectAnimator translationAnim = ObjectAnimator.ofFloat(card, "translationY", 50f, 0f);
        
        alphaAnim.setDuration(600);
        alphaAnim.setStartDelay(delay);
        alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        
        translationAnim.setDuration(600);
        translationAnim.setStartDelay(delay);
        translationAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        
        alphaAnim.start();
        translationAnim.start();
    }
    
    private void animateButtonAppearance(Button button, int delay) {
        button.setAlpha(0f);
        button.setScaleX(0.8f);
        button.setScaleY(0.8f);
        
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(button, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(button, "scaleY", 0.8f, 1f);
        
        alphaAnim.setDuration(500);
        alphaAnim.setStartDelay(delay);
        
        scaleXAnim.setDuration(500);
        scaleXAnim.setStartDelay(delay);
        
        scaleYAnim.setDuration(500);
        scaleYAnim.setStartDelay(delay);
        
        alphaAnim.start();
        scaleXAnim.start();
        scaleYAnim.start();
    }
    
    private void loadCartItems() {
        Toast.makeText(this, "loadCartItems: Starting", Toast.LENGTH_SHORT).show();
        
        // Get cart items from intent
        Intent intent = getIntent();
        Toast.makeText(this, "loadCartItems: Got intent", Toast.LENGTH_SHORT).show();
        
        if (intent != null && intent.hasExtra("cart_items")) {
            Toast.makeText(this, "loadCartItems: Intent has cart_items extra", Toast.LENGTH_SHORT).show();
            try {
                Object serializableExtra = intent.getSerializableExtra("cart_items");
                Toast.makeText(this, "loadCartItems: Got serializable extra", Toast.LENGTH_SHORT).show();
                
                if (serializableExtra instanceof List) {
                    Toast.makeText(this, "loadCartItems: Extra is a List", Toast.LENGTH_SHORT).show();
                    @SuppressWarnings("unchecked")
                    List<CartItem> receivedItems = (List<CartItem>) serializableExtra;
                    if (receivedItems != null && !receivedItems.isEmpty()) {
                        Toast.makeText(this, "loadCartItems: Adding " + receivedItems.size() + " items", Toast.LENGTH_SHORT).show();
                        cartItems.clear();
                        cartItems.addAll(receivedItems);
                        Toast.makeText(this, "loadCartItems: Items added successfully", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        Toast.makeText(this, "loadCartItems: Received items are null or empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "loadCartItems: Extra is not a List, type: " + (serializableExtra != null ? serializableExtra.getClass().getSimpleName() : "null"), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "loadCartItems: Exception occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "loadCartItems: No cart_items extra in intent", Toast.LENGTH_SHORT).show();
        }
        // Fallback to sample items if no cart items received or error occurred
        Toast.makeText(this, "loadCartItems: Creating sample items", Toast.LENGTH_SHORT).show();
        createSampleItems();
        Toast.makeText(this, "loadCartItems: Sample items created", Toast.LENGTH_SHORT).show();
    }
    
    private void createSampleItems() {
        cartItems.add(new CartItem(1, "Wireless Headphones", "", 99.99, 1, "TechBrand", "Electronics"));
        cartItems.add(new CartItem(2, "Smart Watch", "", 199.99, 1, "TechBrand", "Electronics"));
        cartItems.add(new CartItem(3, "Phone Case", "", 29.99, 2, "TechBrand", "Accessories"));
    }
    
    private void loadSavedUserInfo() {
        try {
            String userEmail = sessionManager.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                // Load saved shipping address from user profile
                DatabaseHelper.UserProfile profile = databaseHelper.getUserProfile(userEmail);
                if (profile != null) {
                    if (profile.fullName() != null && !profile.fullName().isEmpty()) {
                        etFullName.setText(profile.fullName());
                    }
                    if (profile.street != null && !profile.street.isEmpty()) {
                        etAddress.setText(profile.street);
                    }
                    if (profile.city != null && !profile.city.isEmpty()) {
                        etCity.setText(profile.city);
                    }
                    if (profile.state != null && !profile.state.isEmpty()) {
                        etState.setText(profile.state);
                    }
                    if (profile.zip != null && !profile.zip.isEmpty()) {
                        etZipCode.setText(profile.zip);
                    }
                    if (profile.phone != null && !profile.phone.isEmpty()) {
                        etPhone.setText(profile.phone);
                    }
                }
                
                // Load saved payment information
                DatabaseHelper.PaymentInfo paymentInfo = databaseHelper.getDefaultPaymentInfo(userEmail);
                if (paymentInfo != null) {
                    // Set payment method
                    if ("credit_card".equals(paymentInfo.paymentMethod)) {
                        rbCreditCard.setChecked(true);
                    } else if ("debit_card".equals(paymentInfo.paymentMethod)) {
                        rbDebitCard.setChecked(true);
                    }
                    
                    // Set payment details
                    etCardNumber.setText(paymentInfo.cardNumber);
                    etExpiryDate.setText(paymentInfo.expiryDate);
                    etCVV.setText(paymentInfo.cvv);
                    etCardholderName.setText(paymentInfo.cardholderName);
                    
                    // Update payment fields visibility
                    updatePaymentFieldsVisibility();
                }
                
                // Show save notice if user has saved information
                if (profile != null || paymentInfo != null) {
                    layoutSaveNotice.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading saved information", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupListeners() {
        // Shipping method selection
        rgShippingMethod.setOnCheckedChangeListener((group, checkedId) -> {
            updateShippingCost();
            calculateTotals();
            updateOrderSummary();
        });
        
        // Payment method selection
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            updatePaymentFieldsVisibility();
        });
        
        // Place order button
        btnPlaceOrder.setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (validateForm()) {
                processOrder();
            }
        });
        
        // Back arrow button
        btnBack.setOnClickListener(v -> {
            finish();
        });
        
        // Add text watchers for real-time validation
        addTextWatchers();
    }
    
    private void addTextWatchers() {
        // Card number formatting
        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String formatted = formatCardNumber(s.toString());
                if (!formatted.equals(s.toString())) {
                    etCardNumber.setText(formatted);
                    etCardNumber.setSelection(formatted.length());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Expiry date formatting
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String formatted = formatExpiryDate(s.toString());
                if (!formatted.equals(s.toString())) {
                    etExpiryDate.setText(formatted);
                    etExpiryDate.setSelection(formatted.length());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Postal code formatting
        etZipCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String formatted = formatPostalCode(s.toString());
                if (!formatted.equals(s.toString())) {
                    etZipCode.setText(formatted);
                    etZipCode.setSelection(formatted.length());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private String formatCardNumber(String input) {
        String cleaned = input.replaceAll("\\s", "");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < cleaned.length() && i < 16; i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(cleaned.charAt(i));
        }
        
        return formatted.toString();
    }
    
    private String formatExpiryDate(String input) {
        String cleaned = input.replaceAll("/", "");
        if (cleaned.length() >= 2) {
            return cleaned.substring(0, 2) + "/" + cleaned.substring(2, Math.min(4, cleaned.length()));
        }
        return cleaned;
    }
    
    private String formatPostalCode(String input) {
        String cleaned = input.replaceAll("\\s", "").toUpperCase();
        if (cleaned.length() >= 3) {
            return cleaned.substring(0, 3) + " " + cleaned.substring(3, Math.min(6, cleaned.length()));
        }
        return cleaned;
    }
    
    private void updateShippingCost() {
        int checkedId = rgShippingMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.rbStandard) {
            shippingCost = 5.99;
        } else if (checkedId == R.id.rbExpress) {
            shippingCost = 12.99;
        } else if (checkedId == R.id.rbOvernight) {
            shippingCost = 24.99;
        }
    }
    
    private void updatePaymentFieldsVisibility() {
        int checkedId = rgPaymentMethod.getCheckedRadioButtonId();
        boolean showCardFields = (checkedId == R.id.rbCreditCard || checkedId == R.id.rbDebitCard);
        
        etCardNumber.setVisibility(showCardFields ? View.VISIBLE : View.GONE);
        etExpiryDate.setVisibility(showCardFields ? View.VISIBLE : View.GONE);
        etCVV.setVisibility(showCardFields ? View.VISIBLE : View.GONE);
        etCardholderName.setVisibility(showCardFields ? View.VISIBLE : View.GONE);
    }
    
    private void calculateTotals() {
        subtotal = 0.0;
        if (cartItems != null && !cartItems.isEmpty()) {
            for (CartItem item : cartItems) {
                if (item != null) {
                    subtotal += item.getTotalPrice();
                }
            }
        }
        
        updateShippingCost();
        tax = subtotal * 0.08; // 8% tax
        total = subtotal + shippingCost + tax;
    }
    
    private void updateOrderSummary() {
        Toast.makeText(this, "updateOrderSummary: Starting", Toast.LENGTH_SHORT).show();
        
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
        Toast.makeText(this, "updateOrderSummary: Text views updated", Toast.LENGTH_SHORT).show();
        
        // Update order items display
        if (orderItemsContainer != null) {
            Toast.makeText(this, "updateOrderSummary: orderItemsContainer found", Toast.LENGTH_SHORT).show();
            orderItemsContainer.removeAllViews();
            if (cartItems != null && !cartItems.isEmpty()) {
                Toast.makeText(this, "updateOrderSummary: Processing " + cartItems.size() + " cart items", Toast.LENGTH_SHORT).show();
                for (CartItem item : cartItems) {
                    if (item != null) {
                        Toast.makeText(this, "updateOrderSummary: Processing item: " + item.getProductName(), Toast.LENGTH_SHORT).show();
                        View itemView = getLayoutInflater().inflate(R.layout.item_checkout_summary, orderItemsContainer, false);
                        Toast.makeText(this, "updateOrderSummary: Layout inflated successfully", Toast.LENGTH_SHORT).show();
                        
                        TextView tvItemName = itemView.findViewById(R.id.tvItemName);
                        TextView tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
                        TextView tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
                        Toast.makeText(this, "updateOrderSummary: TextViews found", Toast.LENGTH_SHORT).show();
                        
                        if (tvItemName != null) {
                            tvItemName.setText(item.getProductName() != null ? item.getProductName() : "Unknown Product");
                        }
                        if (tvItemQuantity != null) {
                            tvItemQuantity.setText("Qty: " + item.getQuantity());
                        }
                        if (tvItemPrice != null) {
                            tvItemPrice.setText(currencyFormat.format(item.getTotalPrice()));
                        }
                        Toast.makeText(this, "updateOrderSummary: TextViews populated", Toast.LENGTH_SHORT).show();
                        
                        orderItemsContainer.addView(itemView);
                        Toast.makeText(this, "updateOrderSummary: Item view added to container", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "updateOrderSummary: No cart items to display", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "updateOrderSummary: orderItemsContainer is null", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "updateOrderSummary: Completed successfully", Toast.LENGTH_SHORT).show();
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate shipping address
        if (etFullName.getText().toString().trim().isEmpty()) {
            tilFullName.setError("Full name is required");
            isValid = false;
        } else {
            tilFullName.setError(null);
        }
        
        if (etAddress.getText().toString().trim().isEmpty()) {
            tilAddress.setError("Address is required");
            isValid = false;
        } else {
            tilAddress.setError(null);
        }
        
        if (etCity.getText().toString().trim().isEmpty()) {
            tilCity.setError("City is required");
            isValid = false;
        } else {
            tilCity.setError(null);
        }
        
        if (etState.getText().toString().trim().isEmpty()) {
            tilState.setError("Province is required");
            isValid = false;
        } else {
            tilState.setError(null);
        }
        
        if (etZipCode.getText().toString().trim().isEmpty()) {
            tilZipCode.setError("Postal code is required");
            isValid = false;
        } else {
            tilZipCode.setError(null);
        }
        
        if (etPhone.getText().toString().trim().isEmpty()) {
            tilPhone.setError("Phone number is required");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }
        
        // Validate payment method
        int paymentMethod = rgPaymentMethod.getCheckedRadioButtonId();
        if (paymentMethod == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else if (paymentMethod == R.id.rbCreditCard || paymentMethod == R.id.rbDebitCard) {
            if (etCardNumber.getText().toString().trim().isEmpty()) {
                tilCardNumber.setError("Card number is required");
                isValid = false;
            } else {
                tilCardNumber.setError(null);
            }
            
            if (etExpiryDate.getText().toString().trim().isEmpty()) {
                tilExpiryDate.setError("Expiry date is required");
                isValid = false;
            } else {
                tilExpiryDate.setError(null);
            }
            
            if (etCVV.getText().toString().trim().isEmpty()) {
                tilCVV.setError("CVV is required");
                isValid = false;
            } else {
                tilCVV.setError(null);
            }
            
            if (etCardholderName.getText().toString().trim().isEmpty()) {
                tilCardholderName.setError("Cardholder name is required");
                isValid = false;
            } else {
                tilCardholderName.setError(null);
            }
        }
        
        return isValid;
    }
    
    private void processOrder() {
        try {
            // Show loading animation
            progressBar.setVisibility(View.VISIBLE);
            btnPlaceOrder.setEnabled(false);
            
            // Create order in database
            String userEmail = sessionManager.getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = "demo@example.com"; // Fallback for demo
            }
            
            // Get shipping method
            String shippingMethod = "standard";
            int checkedShippingId = rgShippingMethod.getCheckedRadioButtonId();
            if (checkedShippingId == R.id.rbExpress) {
                shippingMethod = "express";
            } else if (checkedShippingId == R.id.rbOvernight) {
                shippingMethod = "overnight";
            }
            
            // Get shipping address
            String fullName = etFullName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String state = etState.getText().toString().trim();
            String zipCode = etZipCode.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            
            // Create order in database
            long orderId = databaseHelper.createOrder(userEmail, cartItems, shippingMethod,
                    subtotal, shippingCost, tax, total, fullName, address, city, state, zipCode, phone);
            
            if (orderId == -1) {
                Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                return;
            }
            
            // Simulate order processing
            btnPlaceOrder.postDelayed(() -> {
                try {
                    progressBar.setVisibility(View.GONE);
                    btnPlaceOrder.setEnabled(true);
                    
                    // Clear cart after successful order processing
                    clearCartAfterOrder();
                    
                    // Save user information for future use
                    saveUserInfo();
                    
                    // Show success animation
                    showOrderSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing order", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnPlaceOrder.setEnabled(true);
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing order", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(true);
        }
    }
    
    private void clearCartAfterOrder() {
        try {
            String userEmail = sessionManager.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                boolean success = databaseHelper.clearCart(userEmail);
                if (success) {
                    Toast.makeText(this, "Cart cleared successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to clear cart", Toast.LENGTH_SHORT).show();
                }
            } else {
                // For demo purposes, just show a message
                Toast.makeText(this, "Cart cleared (demo mode)", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error clearing cart", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveUserInfo() {
        try {
            String userEmail = sessionManager.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                // Save shipping address to user profile
                String fullName = etFullName.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String city = etCity.getText().toString().trim();
                String state = etState.getText().toString().trim();
                String zipCode = etZipCode.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                
                // Split full name into first and last name
                String[] nameParts = fullName.split(" ", 2);
                String firstName = nameParts.length > 0 ? nameParts[0] : "";
                String lastName = nameParts.length > 1 ? nameParts[1] : "";
                
                // Update user profile with address information
                databaseHelper.updateProfile(userEmail, firstName, lastName, phone, address, city, state, zipCode, "US");
                
                // Save payment information
                int paymentMethod = rgPaymentMethod.getCheckedRadioButtonId();
                if (paymentMethod == R.id.rbCreditCard || paymentMethod == R.id.rbDebitCard) {
                    String paymentMethodStr = (paymentMethod == R.id.rbCreditCard) ? "credit_card" : "debit_card";
                    String cardNumber = etCardNumber.getText().toString().trim();
                    String expiryDate = etExpiryDate.getText().toString().trim();
                    String cvv = etCVV.getText().toString().trim();
                    String cardholderName = etCardholderName.getText().toString().trim();
                    
                    // Save payment info as default
                    boolean success = databaseHelper.savePaymentInfo(userEmail, paymentMethodStr, cardNumber, 
                            expiryDate, cvv, cardholderName, true);
                    
                    if (success) {
                        Toast.makeText(this, "Information saved for future use", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving information", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showOrderSuccess() {
        try {
            // Hide the checkout content and show success fragment
            scrollView.setVisibility(View.GONE);
            
            // Create and show the success fragment
            OrderSuccessFragment successFragment = OrderSuccessFragment.newInstance(total);
            successFragment.setOnOrderSuccessListener(this);
            
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, successFragment)
                    .commit();
                    
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Order placed successfully! 🎉", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    @Override
    public void onNavigateToOrders() {
        // Navigate to OrdersActivity
        Intent intent = new Intent(this, OrdersActivity.class);
        startActivity(intent);
        finish();
    }
}
