package com.radar.radarshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/** Simple ListView adapter for products (used in MainActivity). */
public class ProductAdapter extends BaseAdapter {

    private final Context context;
    private List<Product> products;
    private final LayoutInflater inflater;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.inflater = LayoutInflater.from(context);
    }

    /** Replace data and refresh list. */
    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return products == null ? 0 : products.size(); }

    @Override public Object getItem(int position) { return products.get(position); }

    @Override public long getItemId(int position) { return products.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_product, parent, false);
            h = new ViewHolder(convertView);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Product p = products.get(position);
        h.nameTv.setText(p.getName());
        h.priceTv.setText(String.format("$%.2f", p.getPrice()));
        return convertView;
    }

    static class ViewHolder {
        final TextView nameTv;
        final TextView priceTv;
        ViewHolder(View root) {
            nameTv  = root.findViewById(R.id.tvProductName);
            priceTv = root.findViewById(R.id.tvProductPrice);
        }
    }
}
