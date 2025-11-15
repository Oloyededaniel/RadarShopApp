package com.radar.radarshop;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class OrderDetailsDialog extends Dialog {
    
    private DatabaseHelper.Order order;
    private List<DatabaseHelper.OrderItem> orderItems;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private OnOrderActionListener listener;
    
    private TextView tvOrderNumber;
    private TextView tvOrderDate;
    private TextView tvOrderStatus;
    private TextView tvOrderTotal;
    private TextView tvOrderItemsCount;
    private TextView tvTrackingNumber;
    private TextView tvShippingMethod;
    private TextView tvShippingAddress;
    private ImageView ivStatusIcon;
    private RecyclerView recyclerViewItems;
    private Button btnAction;
    private Button btnClose;
    
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    public interface OnOrderActionListener {
        void onBuyAgain(DatabaseHelper.Order order);
        void onWriteReview(DatabaseHelper.Order order);
        void onTrackOrder(DatabaseHelper.Order order);
        void onViewDetails(DatabaseHelper.Order order);
    }
    
    public OrderDetailsDialog(Context context, DatabaseHelper.Order order, 
                             List<DatabaseHelper.OrderItem> orderItems,
                             DatabaseHelper databaseHelper, SessionManager sessionManager,
                             OnOrderActionListener listener) {
        super(context);
        this.order = order;
        this.orderItems = orderItems;
        this.databaseHelper = databaseHelper;
        this.sessionManager = sessionManager;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_order_details);
        
        // Set dialog window layout parameters for proper sizing
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9), // 90% of screen width
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
        
        initializeViews();
        setupOrderDetails();
        setupActionButton();
        setupCloseButton();
    }
    
    private void initializeViews() {
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvOrderItemsCount = findViewById(R.id.tvOrderItemsCount);
        tvTrackingNumber = findViewById(R.id.tvTrackingNumber);
        tvShippingMethod = findViewById(R.id.tvShippingMethod);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        btnAction = findViewById(R.id.btnAction);
        btnClose = findViewById(R.id.btnClose);
    }
    
    private void setupOrderDetails() {
        // Set order information
        tvOrderNumber.setText(order.orderNumber);
        tvOrderDate.setText(formatOrderDate(order.orderDate));
        tvOrderStatus.setText(capitalizeFirst(order.status));
        tvOrderTotal.setText(currencyFormat.format(order.totalAmount));
        tvOrderItemsCount.setText(order.itemsCount + " items");
        tvTrackingNumber.setText(order.trackingNumber);
        tvShippingMethod.setText(capitalizeFirst(order.shippingMethod));
        
        // Set shipping address
        String address = order.shippingAddress + ", " + order.shippingCity + 
                        ", " + order.shippingState + " " + order.shippingZip;
        tvShippingAddress.setText(address);
        
        // Set status icon
        setStatusIcon(order.status);
        
        // Setup order items RecyclerView
        OrderItemAdapter adapter = new OrderItemAdapter(orderItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewItems.setAdapter(adapter);
    }
    
    private void setupActionButton() {
        String status = order.status.toLowerCase();
        
        switch (status) {
            case "delivered":
                // Check if order has been reviewed
                String userEmail = sessionManager.getEmail();
                if (userEmail == null || userEmail.isEmpty()) {
                    userEmail = "demo@example.com";
                }
                boolean isReviewed = databaseHelper.hasOrderBeenReviewed(userEmail, order.id);
                
                if (isReviewed) {
                    btnAction.setText("Review Submitted");
                    btnAction.setEnabled(false);
                    btnAction.setAlpha(0.6f);
                    btnAction.setOnClickListener(null);
                } else {
                    btnAction.setText("Write Review");
                    btnAction.setEnabled(true);
                    btnAction.setAlpha(1.0f);
                    btnAction.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onWriteReview(order);
                        }
                        dismiss();
                    });
                }
                break;
            case "shipped":
                btnAction.setText("Track Order");
                btnAction.setOnClickListener(v -> {
                    showTrackingInfo();
                });
                break;
            case "processing":
            default:
                btnAction.setText("View Details");
                btnAction.setOnClickListener(v -> {
                    showOrderInfo();
                });
                break;
        }
    }
    
    private void setupCloseButton() {
        btnClose.setOnClickListener(v -> dismiss());
    }
    
    private void addItemsToCart() {
        String userEmail = sessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "demo@example.com";
        }
        
        for (DatabaseHelper.OrderItem item : orderItems) {
            databaseHelper.addToCart(userEmail, item.productId, item.quantity);
        }
    }
    
    private void setStatusIcon(String status) {
        switch (status.toLowerCase()) {
            case "delivered":
                ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                break;
            case "shipped":
                ivStatusIcon.setImageResource(R.drawable.ic_local_shipping);
                break;
            case "processing":
            default:
                ivStatusIcon.setImageResource(R.drawable.ic_inventory);
                break;
        }
    }
    
    private void showCartMessage() {
        Toast.makeText(getContext(), "Products have been added to cart!", Toast.LENGTH_LONG).show();
        dismiss();
    }
    
    private void showTrackingInfo() {
        Toast.makeText(getContext(), "Tracking: " + order.trackingNumber, Toast.LENGTH_LONG).show();
        dismiss();
    }
    
    private void showOrderInfo() {
        Toast.makeText(getContext(), "Order Details: " + order.orderNumber, Toast.LENGTH_SHORT).show();
        dismiss();
    }
    
    private String formatOrderDate(String dateString) {
        try {
            // Try to parse the date string and format it
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy h:mm:ss a", java.util.Locale.US);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US);
            java.util.Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            // If parsing fails, return the original string or a default
            return dateString != null ? dateString : "Unknown Date";
        }
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    // Inner class for order items adapter
    private static class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
        private List<DatabaseHelper.OrderItem> items;
        private static final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
        
        public OrderItemAdapter(List<DatabaseHelper.OrderItem> items) {
            this.items = items;
        }
        
        @Override
        public OrderItemViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_product, parent, false);
            return new OrderItemViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(OrderItemViewHolder holder, int position) {
            DatabaseHelper.OrderItem item = items.get(position);
            holder.bind(item);
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        static class OrderItemViewHolder extends RecyclerView.ViewHolder {
            private TextView tvProductName;
            private TextView tvProductQuantity;
            private TextView tvProductPrice;
            private TextView tvProductTotal;
            
            public OrderItemViewHolder(View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
                tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
                tvProductTotal = itemView.findViewById(R.id.tvProductTotal);
            }
            
            public void bind(DatabaseHelper.OrderItem item) {
                tvProductName.setText(item.productName);
                tvProductQuantity.setText("Qty: " + item.quantity);
                tvProductPrice.setText(currencyFormat.format(item.price));
                tvProductTotal.setText(currencyFormat.format(item.totalPrice));
            }
        }
    }
}
