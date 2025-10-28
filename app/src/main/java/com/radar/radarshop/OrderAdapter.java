package com.radar.radarshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    
    private List<DatabaseHelper.Order> orders;
    private OnOrderActionListener listener;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    public interface OnOrderActionListener {
        void onViewDetails(DatabaseHelper.Order order);
        void onBuyAgain(DatabaseHelper.Order order);
        void onTrackOrder(DatabaseHelper.Order order);
        void onOrderCardClicked(DatabaseHelper.Order order);
    }
    
    public OrderAdapter(List<DatabaseHelper.Order> orders, OnOrderActionListener listener) {
        this.orders = orders;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DatabaseHelper.Order order = orders.get(position);
        holder.bind(order);
    }
    
    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }
    
    public void updateOrders(List<DatabaseHelper.Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }
    
    class OrderViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivStatusIcon;
        private TextView tvOrderNumber;
        private TextView tvOrderDate;
        private TextView tvStatusBadge;
        private TextView tvItemsCount;
        private TextView tvTotalAmount;
        private TextView tvTrackingNumber;
        private Button btnViewDetails;
        private Button btnAction;
        
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvTrackingNumber = itemView.findViewById(R.id.tvTrackingNumber);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
        
        public void bind(DatabaseHelper.Order order) {
            // Set order number
            tvOrderNumber.setText(order.orderNumber);
            
            // Set order date
            tvOrderDate.setText(formatOrderDate(order.orderDate));
            
            // Set items count
            tvItemsCount.setText(order.itemsCount + " items");
            
            // Set total amount
            tvTotalAmount.setText(currencyFormat.format(order.totalAmount));
            
            // Set tracking number
            tvTrackingNumber.setText(order.trackingNumber);
            
            // Set status-specific UI
            setStatusUI(order.status);
            
            // Set up card click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderCardClicked(order);
                }
            });
            
            // Set up click listeners for buttons
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(order);
                }
            });
            
            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    if ("delivered".equals(order.status)) {
                        listener.onBuyAgain(order);
                    } else if ("shipped".equals(order.status)) {
                        listener.onTrackOrder(order);
                    } else {
                        listener.onViewDetails(order);
                    }
                }
            });
        }
        
        private void setStatusUI(String status) {
            switch (status.toLowerCase()) {
                case "delivered":
                    ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                    tvStatusBadge.setText("Delivered");
                    tvStatusBadge.setBackgroundResource(R.drawable.status_delivered_background);
                    btnAction.setText("Buy Again");
                    break;
                case "shipped":
                    ivStatusIcon.setImageResource(R.drawable.ic_local_shipping);
                    tvStatusBadge.setText("Shipped");
                    tvStatusBadge.setBackgroundResource(R.drawable.status_shipped_background);
                    btnAction.setText("Track Order");
                    break;
                case "processing":
                default:
                    ivStatusIcon.setImageResource(R.drawable.ic_inventory);
                    tvStatusBadge.setText("Processing");
                    tvStatusBadge.setBackgroundResource(R.drawable.status_processing_background);
                    btnAction.setText("View Details");
                    break;
            }
        }
        
        private String formatOrderDate(String dateString) {
            try {
                // Try to parse the date string and format it
                SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                // If parsing fails, return the original string or a default
                return dateString != null ? dateString : "Unknown Date";
            }
        }
    }
}
