package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private ListView listWishlist;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        listWishlist = findViewById(R.id.listWishlist);
        TextView empty = findViewById(R.id.emptyWishlist);
        listWishlist.setEmptyView(empty);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        loadWishlist();

        // Tap -> open details
        listWishlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product p = (Product) parent.getItemAtPosition(position);
                if (p == null) return;
                Intent intent = new Intent(WishlistActivity.this, ProductDetailActivity.class);
                intent.putExtra("productId", p.getId());
                intent.putExtra("product", p);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist();
    }

    private void loadWishlist() {
        String email = session.getEmail();
        if (email == null || email.isEmpty()) email = "demo@example.com";
        List<Product> wishlist = db.getWishlist(email);

        WishlistAdapter adapter = new WishlistAdapter(
                this,
                wishlist,
                email,
                db
        );
        listWishlist.setAdapter(adapter);
    }
}
