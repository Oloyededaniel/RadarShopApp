package com.radar.radarshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class WishlistCardAdapter extends RecyclerView.Adapter<WishlistCardAdapter.WishlistViewHolder> {
    
    private Context context;
    private List<Product> products;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private OnWishlistItemListener listener;

    public interface OnWishlistItemListener {
        void onItemRemoved(int newCount);
        void onProductClick(Product product);
        void onAddToCart(Product product);
    }

    public WishlistCardAdapter(Context context, List<Product> products, DatabaseHelper databaseHelper) {
        this.context = context;
        this.products = products != null ? products : new ArrayList<>();
        this.databaseHelper = databaseHelper;
        this.sessionManager = new SessionManager(context);
    }

    public void setOnWishlistItemListener(OnWishlistItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist_card, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeProduct(Product product) {
        int position = products.indexOf(product);
        if (position != -1) {
            products.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, products.size());
        }
    }

    class WishlistViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvPrice, tvOriginalPrice;
        private ImageButton btnRemove;
        private Button btnAddToCart;
        private TextView tvSaleBadge, tvOutOfStockBadge;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            tvSaleBadge = itemView.findViewById(R.id.tvSaleBadge);
            tvOutOfStockBadge = itemView.findViewById(R.id.tvOutOfStockBadge);

            // Product card click
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });

            // Remove from wishlist
            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product product = products.get(position);
                    removeFromWishlist(product);
                }
            });

            // Add to cart
            btnAddToCart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product product = products.get(position);
                    if (product.isInStock() && listener != null) {
                        listener.onAddToCart(product);
                    }
                }
            });
        }

        public void bind(Product product) {
            // Product name
            tvProductName.setText(product.getName());
            
            // Price - simulate sale pricing (20% discount)
            double originalPrice = product.getPrice() * 1.2;
            if (originalPrice > product.getPrice()) {
                tvPrice.setText(product.getFormattedPrice());
                tvOriginalPrice.setText(String.format("$%.2f", originalPrice));
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvSaleBadge.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setText(product.getFormattedPrice());
                tvOriginalPrice.setVisibility(View.GONE);
                tvSaleBadge.setVisibility(View.GONE);
            }

            // Stock status
            if (!product.isInStock()) {
                tvOutOfStockBadge.setVisibility(View.VISIBLE);
                btnAddToCart.setText("Out of Stock");
                btnAddToCart.setEnabled(false);
                btnAddToCart.setBackgroundResource(R.drawable.wishlist_add_to_cart_button_disabled);
                btnAddToCart.setTextColor(context.getResources().getColor(android.R.color.white));
            } else {
                tvOutOfStockBadge.setVisibility(View.GONE);
                btnAddToCart.setText("Add to Cart");
                btnAddToCart.setEnabled(true);
                btnAddToCart.setBackgroundResource(R.drawable.wishlist_add_to_cart_button);
                btnAddToCart.setTextColor(context.getResources().getColor(android.R.color.white));
            }

            // Load product image
            loadProductImage(product);
        }

        private void loadProductImage(Product product) {
            List<ProductImage> images = databaseHelper.getProductImages(product.getId());
            String imageUrl = null;
            
            for (ProductImage image : images) {
                if ("main".equals(image.getImageType()) || "thumbnail".equals(image.getImageType())) {
                    imageUrl = image.getImageUrl();
                    break;
                }
            }
            
            if (imageUrl == null && !images.isEmpty()) {
                imageUrl = images.get(0).getImageUrl();
            }

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                    .load(imageUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(12)))
                    .placeholder(R.drawable.product_placeholder)
                    .error(R.drawable.product_placeholder)
                    .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.product_placeholder);
            }
        }

        private void removeFromWishlist(Product product) {
            String userEmail = sessionManager.getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = "demo@example.com";
            }

            boolean success = databaseHelper.removeFromWishlist(userEmail, product.getId());
            if (success) {
                removeProduct(product);
                Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onItemRemoved(products.size());
                }
            } else {
                Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

