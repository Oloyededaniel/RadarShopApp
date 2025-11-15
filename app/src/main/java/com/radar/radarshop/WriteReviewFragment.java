package com.radar.radarshop;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WriteReviewFragment extends Fragment {
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    
    private int orderId;
    private String orderNumber;
    private List<DatabaseHelper.OrderItem> orderItems;
    private List<Product> products;
    private int currentProductIndex = 0;
    
    // Views
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvOrderNumber;
    private TextView tvProgress;
    private TextView tvOrderDate;
    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvRateProduct;
    private TextView tvRatingHint;
    private TextView tvRatingDescription;
    private RatingBar ratingBar;
    private TextView tvShareExperience;
    private EditText etReviewComment;
    private TextView tvCharacterCount;
    private Button btnNextProduct;
    private TextView tvDoLater;
    private ProgressBar progressBar;
    
    private static final int MAX_CHARACTERS = 500;
    
    public static WriteReviewFragment newInstance(int orderId, String orderNumber) {
        WriteReviewFragment fragment = new WriteReviewFragment();
        Bundle args = new Bundle();
        args.putInt("orderId", orderId);
        args.putString("orderNumber", orderNumber);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getInt("orderId", -1);
            orderNumber = getArguments().getString("orderNumber");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_write_review, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (orderId == -1 || orderNumber == null) {
            Toast.makeText(getContext(), "Invalid order information", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }
        
        initializeDatabase();
        initializeViews(view);
        loadOrderItems();
        setupClickListeners();
        displayCurrentProduct();
    }
    
    private void initializeDatabase() {
        if (getContext() != null) {
            databaseHelper = new DatabaseHelper(getContext());
            sessionManager = new SessionManager(getContext());
        }
    }
    
    private void initializeViews(View view) {
        ivBack = view.findViewById(R.id.ivBack);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvOrderNumber = view.findViewById(R.id.tvOrderNumber);
        tvProgress = view.findViewById(R.id.tvProgress);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        ivProductImage = view.findViewById(R.id.ivProductImage);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvProductPrice = view.findViewById(R.id.tvProductPrice);
        tvRateProduct = view.findViewById(R.id.tvRateProduct);
        tvRatingHint = view.findViewById(R.id.tvRatingHint);
        tvRatingDescription = view.findViewById(R.id.tvRatingDescription);
        ratingBar = view.findViewById(R.id.ratingBar);
        tvShareExperience = view.findViewById(R.id.tvShareExperience);
        etReviewComment = view.findViewById(R.id.etReviewComment);
        tvCharacterCount = view.findViewById(R.id.tvCharacterCount);
        btnNextProduct = view.findViewById(R.id.btnNextProduct);
        tvDoLater = view.findViewById(R.id.tvDoLater);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Set order number
        tvOrderNumber.setText("Order " + orderNumber);
        
        // Setup character counter with color changes
        etReviewComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvCharacterCount.setText(length + "/" + MAX_CHARACTERS + " characters");
                
                // Change color based on character count
                if (length > MAX_CHARACTERS * 0.9) {
                    // Near limit - show warning (orange/red)
                    tvCharacterCount.setTextColor(getResources().getColor(R.color.error_red, null));
                } else if (length > MAX_CHARACTERS * 0.7) {
                    // Getting close - show caution (orange)
                    tvCharacterCount.setTextColor(getResources().getColor(R.color.warning_yellow, null));
                } else {
                    // Normal - show secondary color
                    tvCharacterCount.setTextColor(getResources().getColor(R.color.text_secondary, null));
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // Limit character count
                if (s.length() > MAX_CHARACTERS) {
                    s.delete(MAX_CHARACTERS, s.length());
                }
            }
        });
        
        // Set initial character count
        tvCharacterCount.setText("0/" + MAX_CHARACTERS + " characters");
        
        // Setup rating bar listener for descriptions and button state
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            updateRatingDescription(rating);
            updateButtonState();
        });
        
        // Initial button state (disabled)
        updateButtonState();
    }
    
    private void loadOrderItems() {
        orderItems = databaseHelper.getOrderItems(orderId);
        if (orderItems == null || orderItems.isEmpty()) {
            Toast.makeText(getContext(), "No items found in this order", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }
        
        // Load product details for each order item
        products = new ArrayList<>();
        List<Product> allProducts = databaseHelper.getAllProducts();
        
        for (DatabaseHelper.OrderItem item : orderItems) {
            for (Product product : allProducts) {
                if (product.getId() == item.productId) {
                    products.add(product);
                    break;
                }
            }
        }
        
        if (products.isEmpty()) {
            Toast.makeText(getContext(), "Product information not found", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }
        
        // Get order date
        DatabaseHelper.Order order = getOrderById(orderId);
        if (order != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                Date date = inputFormat.parse(order.orderDate);
                tvOrderDate.setText("Ordered on " + outputFormat.format(date));
            } catch (Exception e) {
                tvOrderDate.setText("Ordered on " + order.orderDate);
            }
        }
    }
    
    private DatabaseHelper.Order getOrderById(int orderId) {
        String userEmail = sessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "demo@example.com";
        }
        
        List<DatabaseHelper.Order> orders = databaseHelper.getUserOrders(userEmail);
        for (DatabaseHelper.Order order : orders) {
            if (order.id == orderId) {
                return order;
            }
        }
        return null;
    }
    
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                if (getActivity() instanceof OrdersActivity) {
                    ((OrdersActivity) getActivity()).hideWriteReviewFragment();
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        btnNextProduct.setOnClickListener(v -> submitCurrentReviewAndMoveNext());
        
        tvDoLater.setOnClickListener(v -> {
            if (getActivity() != null) {
                if (getActivity() instanceof OrdersActivity) {
                    ((OrdersActivity) getActivity()).hideWriteReviewFragment();
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
    
    private void displayCurrentProduct() {
        if (currentProductIndex >= products.size()) {
            // All products reviewed
            Toast.makeText(getContext(), "Thank you for your reviews!", Toast.LENGTH_LONG).show();
            if (getActivity() != null) {
                if (getActivity() instanceof OrdersActivity) {
                    ((OrdersActivity) getActivity()).hideWriteReviewFragment();
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }
        
        Product product = products.get(currentProductIndex);
        
        // Update progress
        tvProgress.setText("Product " + (currentProductIndex + 1) + " of " + products.size());
        progressBar.setMax(products.size());
        progressBar.setProgress(currentProductIndex + 1);
        
        // Update button text
        if (currentProductIndex == products.size() - 1) {
            btnNextProduct.setText("Submit Review");
        } else {
            btnNextProduct.setText("Next Product");
        }
        
        // Load product image
        List<ProductImage> images = databaseHelper.getProductImages(product.getId());
        if (images != null && !images.isEmpty()) {
            String imageUrl = images.get(0).getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_shopping_bag)
                    .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_shopping_bag);
            }
        } else {
            ivProductImage.setImageResource(R.drawable.ic_shopping_bag);
        }
        
        // Set product name and price
        tvProductName.setText(product.getName());
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
        tvProductPrice.setText(currency.format(product.getPrice()));
        
        // Reset rating and comment
        ratingBar.setRating(0);
        etReviewComment.setText("");
        tvCharacterCount.setText("0/" + MAX_CHARACTERS + " characters");
        tvCharacterCount.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tvRatingDescription.setText("");
        updateButtonState();
    }
    
    private void updateRatingDescription(float rating) {
        int ratingInt = Math.round(rating);
        String description = "";
        
        switch (ratingInt) {
            case 1:
                description = "Poor - We're sorry to hear that";
                break;
            case 2:
                description = "Fair - We appreciate your feedback";
                break;
            case 3:
                description = "Good - Thank you for your review";
                break;
            case 4:
                description = "Very Good - We're glad you liked it";
                break;
            case 5:
                description = "Excellent - We're thrilled!";
                break;
            default:
                description = "";
                break;
        }
        
        tvRatingDescription.setText(description);
    }
    
    private void updateButtonState() {
        int rating = Math.round(ratingBar.getRating());
        boolean isEnabled = rating >= 1;
        
        btnNextProduct.setEnabled(isEnabled);
        if (isEnabled) {
            btnNextProduct.setAlpha(1.0f);
        } else {
            btnNextProduct.setAlpha(0.6f);
        }
    }
    
    private void submitCurrentReviewAndMoveNext() {
        Product product = products.get(currentProductIndex);
        int rating = Math.round(ratingBar.getRating());
        String comment = etReviewComment.getText().toString().trim();
        
        if (rating < 1) {
            // Show friendly error message
            showRatingRequiredMessage();
            return;
        }
        
        // Disable button during submission
        btnNextProduct.setEnabled(false);
        btnNextProduct.setText("Submitting...");
        
        // Submit review
        String email = sessionManager.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = "demo@example.com";
        }
        
        boolean success = databaseHelper.addReview(email, product.getId(), rating, comment);
        
        if (success) {
            // Show success feedback
            showSuccessMessage();
            
            // Move to next product after a short delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                currentProductIndex++;
                displayCurrentProduct();
                
                if (currentProductIndex >= products.size()) {
                    // All reviews submitted
                    showCompletionMessage();
                    if (getActivity() != null) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (getActivity() != null) {
                                if (getActivity() instanceof OrdersActivity) {
                                    ((OrdersActivity) getActivity()).hideWriteReviewFragment();
                                }
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        }, 2000);
                    }
                }
            }, 800);
        } else {
            // Re-enable button on failure
            btnNextProduct.setEnabled(true);
            updateButtonState();
            if (currentProductIndex == products.size() - 1) {
                btnNextProduct.setText("Submit Review");
            } else {
                btnNextProduct.setText("Next Product");
            }
            Toast.makeText(getContext(), "Failed to submit review. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showRatingRequiredMessage() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "⭐ Please select a rating to continue", Toast.LENGTH_SHORT).show();
            // Animate rating bar to draw attention
            ratingBar.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .withEndAction(() -> ratingBar.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .start())
                    .start();
        }
    }
    
    private void showSuccessMessage() {
        if (getContext() != null) {
            String message = currentProductIndex == products.size() - 1 
                ? "✓ Review submitted successfully!" 
                : "✓ Review saved! Moving to next product...";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showCompletionMessage() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "🎉 Thank you for all your reviews!", Toast.LENGTH_LONG).show();
        }
    }
}

