package com.radar.radarshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    
    private Context context;
    private List<CartItem> cartItems;
    private DatabaseHelper databaseHelper;
    private OnCartItemInteractionListener listener;

    public interface OnCartItemInteractionListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
        void onCheckout();
        void onProductClick(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, DatabaseHelper databaseHelper) {
        this.context = context;
        this.cartItems = cartItems;
        this.databaseHelper = databaseHelper;
    }

    public void setOnCartItemInteractionListener(OnCartItemInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
        
        // Animate item appearance with stagger
        Animation slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.cart_slide_in);
        slideInAnimation.setStartOffset(position * 50); // Stagger animation
        holder.itemView.startAnimation(slideInAnimation);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvPrice;
        private TextView tvQuantity;
        private ImageButton btnDecrease;
        private ImageButton btnIncrease;
        private ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(CartItem item) {
            tvProductName.setText(item.getProductName());
            tvPrice.setText(String.format("$%.2f", item.getPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Set product image (placeholder for now)
            ivProductImage.setImageResource(R.drawable.product_placeholder);

            // Product click to view details
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(item);
                }
            });

            // Quantity controls with button press animations
            btnDecrease.setOnClickListener(v -> {
                // Animate button press
                Animation pressAnimation = AnimationUtils.loadAnimation(context, R.anim.cart_button_press);
                Animation releaseAnimation = AnimationUtils.loadAnimation(context, R.anim.cart_button_release);
                v.startAnimation(pressAnimation);
                pressAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        v.startAnimation(releaseAnimation);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                
                int newQuantity = item.getQuantity() - 1;
                if (listener != null) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                // Animate button press
                Animation pressAnimation = AnimationUtils.loadAnimation(context, R.anim.cart_button_press);
                Animation releaseAnimation = AnimationUtils.loadAnimation(context, R.anim.cart_button_release);
                v.startAnimation(pressAnimation);
                pressAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        v.startAnimation(releaseAnimation);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                
                int newQuantity = item.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            // Remove item with animation
            btnRemove.setOnClickListener(v -> {
                // Animate button press
                Animation pressAnimation = AnimationUtils.loadAnimation(context, R.anim.cart_button_press);
                Animation releaseAnimation = AnimationUtils.loadAnimation(context, R.anim.cart_button_release);
                v.startAnimation(pressAnimation);
                pressAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        v.startAnimation(releaseAnimation);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                
                if (listener != null) {
                    listener.onRemoveItem(item);
                }
            });
        }
    }
}
