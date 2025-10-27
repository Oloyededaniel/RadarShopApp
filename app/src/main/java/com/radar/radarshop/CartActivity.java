package com.radar.radarshop;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class CartActivity extends AppCompatActivity{

    private Button btnCheckout;
    private RecyclerView rvCart;
    private DatabaseHelper db;
    private SessionManager session;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        btnCheckout = findViewById(R.id.btnCheckout);
        rvCart = findViewById(R.id.rvCart);
        db = new DatabaseHelper(this);
        session = new SessionManager(this);


    }
}
