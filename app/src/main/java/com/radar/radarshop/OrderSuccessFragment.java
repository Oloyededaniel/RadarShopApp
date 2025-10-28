package com.radar.radarshop;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;

public class OrderSuccessFragment extends Fragment {
    
    private TextView tvOrderTotalAmount;
    private TextView tvRedirectingMessage;
    private double orderTotal;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    // Interface for navigation callback
    public interface OnOrderSuccessListener {
        void onNavigateToOrders();
    }
    
    private OnOrderSuccessListener listener;
    
    public static OrderSuccessFragment newInstance(double total) {
        OrderSuccessFragment fragment = new OrderSuccessFragment();
        Bundle args = new Bundle();
        args.putDouble("order_total", total);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderTotal = getArguments().getDouble("order_total", 0.0);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_success, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvOrderTotalAmount = view.findViewById(R.id.tvOrderTotalAmount);
        tvRedirectingMessage = view.findViewById(R.id.tvRedirectingMessage);
        
        // Set order total
        if (tvOrderTotalAmount != null) {
            tvOrderTotalAmount.setText(currencyFormat.format(orderTotal));
        }
        
        // Start countdown and navigation
        startCountdownAndNavigate();
    }
    
    public void setOnOrderSuccessListener(OnOrderSuccessListener listener) {
        this.listener = listener;
    }
    
    private void startCountdownAndNavigate() {
        // Show countdown animation on redirecting message
        Handler handler = new Handler(Looper.getMainLooper());
        
        // Countdown from 5 to 1
        for (int i = 5; i >= 1; i--) {
            final int count = i;
            handler.postDelayed(() -> {
                if (tvRedirectingMessage != null) {
                    tvRedirectingMessage.setText("Redirecting to your orders in " + count + "...");
                }
            }, (5 - count) * 1000);
        }
        
        // Navigate after 5 seconds
        handler.postDelayed(() -> {
            if (listener != null) {
                listener.onNavigateToOrders();
            }
        }, 5000);
    }
}
