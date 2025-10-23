package com.radar.radarshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** Adapter for ListView in WishlistActivity. */
public class WishlistAdapter extends ArrayAdapter<Product> {

    private final LayoutInflater inflater;
    private final DatabaseHelper dbHelper;
    private final String userEmail;
    private final List<Product> data;

    public WishlistAdapter(Context context,
                           List<Product> items,
                           String userEmail,
                           DatabaseHelper dbHelper) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
        this.dbHelper = dbHelper;
        this.userEmail = userEmail;
        this.data = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_wishlist, parent, false);
            h = new ViewHolder(convertView);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Product p = getItem(position);
        if (p != null) {
            h.tvName.setText(p.getName());
            h.tvPrice.setText(String.format("$%.2f", p.getPrice()));

            h.btnRemove.setOnClickListener(v -> {
                boolean ok = dbHelper.removeFromWishlist(userEmail, p.getId());
                if (ok) {
                    data.remove(p);
                    notifyDataSetChanged();
                    Toast.makeText(getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to remove", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return convertView;
    }

    static class ViewHolder {
        final TextView tvName;
        final TextView tvPrice;
        final Button btnRemove;

        ViewHolder(View root) {
            tvName = root.findViewById(R.id.tvProductName);
            tvPrice = root.findViewById(R.id.tvProductPrice);
            btnRemove = root.findViewById(R.id.btnRemove);
        }
    }
}
