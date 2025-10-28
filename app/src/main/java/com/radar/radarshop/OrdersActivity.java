package com.radar.radarshop;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrdersActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        
        // Set up basic UI
        TextView title = findViewById(R.id.tvOrdersTitle);
        if (title != null) {
            title.setText("Your Orders");
        }
    }
}
