package com.radar.radarshop;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.ProductViewHolder> {
    
    private Context context;
    private List<Product> products;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private OnProductInteractionListener listener;
    private int lastPosition = -1;

    public interface OnProductInteractionListener {
        void onProductClick(Product product);
        void onAddToCart(Product product, int quantity);
        void onRemoveFromCart(Product product);
        void onAddToWishlist(Product product);
        void onRemoveFromWishlist(Product product);
        void onCartCountChanged(int newCount);
    }

    public ProductCardAdapter(Context context, List<Product> products, DatabaseHelper databaseHelper) {
        this.context = context;
        this.products = products != null ? products : new ArrayList<>();
        this.databaseHelper = databaseHelper;
        this.sessionManager = new SessionManager(context);
    }

    public void setOnProductInteractionListener(OnProductInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shopping_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
        
        // Add animation
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addProducts(List<Product> newProducts) {
        if (newProducts != null && !newProducts.isEmpty()) {
            int startPosition = products.size();
            products.addAll(newProducts);
            notifyItemRangeInserted(startPosition, newProducts.size());
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvBrand, tvPrice, tvOriginalPrice, tvRatingCount, tvStockStatus, tvQuantity, tvSavings;
        private RatingBar ratingBar;
        private ImageButton btnWishlist, btnDecrease, btnIncrease;
        private Button btnAddToCart;
        private LinearLayout layoutQuantityControls;
        private boolean isInWishlist = false;
        private boolean isInCart = false;
        private int cartQuantity = 0;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize views
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);
            tvStockStatus = itemView.findViewById(R.id.tvStockStatus);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSavings = itemView.findViewById(R.id.tvSavings);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            layoutQuantityControls = itemView.findViewById(R.id.layoutQuantityControls);

            // Set click listeners
            setupClickListeners();
        }

        private void setupClickListeners() {
            // Product card click
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });

            // Wishlist button
            btnWishlist.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product product = products.get(position);
                    toggleWishlist(product);
                }
            });

            // Add to cart button
            btnAddToCart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product product = products.get(position);
                    addToCart(product);
                }
            });

            // Quantity controls
            btnIncrease.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product product = products.get(position);
                    increaseQuantity(product);
                }
            });

            btnDecrease.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product product = products.get(position);
                    decreaseQuantity(product);
                }
            });
        }

        public void bind(Product product) {
            // Basic product info
            tvProductName.setText(product.getName());
            tvPrice.setText(product.getFormattedPrice());
            
            // Shopping app pricing with original price and savings
            double originalPrice = product.getPrice() * 1.2; // Simulate 20% discount
            if (originalPrice > product.getPrice()) {
                tvOriginalPrice.setText(String.format("$%.2f", originalPrice));
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvSavings.setText("Save " + String.format("%.0f", ((originalPrice - product.getPrice()) / originalPrice) * 100) + "%");
                tvSavings.setVisibility(View.VISIBLE);
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvSavings.setVisibility(View.GONE);
            }
            
            // Brand (show if available)
            if (product.getBrand() != null && !product.getBrand().isEmpty()) {
                tvBrand.setText(product.getBrand());
                tvBrand.setVisibility(View.VISIBLE);
            } else {
                tvBrand.setVisibility(View.GONE);
            }

            // Rating
            ratingBar.setRating((float) product.getAverageRating());
            tvRatingCount.setText("(" + product.getTotalReviews() + ")");

            // Stock status
            if (product.isInStock()) {
                tvStockStatus.setText("In Stock");
                tvStockStatus.setTextColor(context.getResources().getColor(R.color.success_green));
                tvStockStatus.setVisibility(View.VISIBLE);
            } else {
                tvStockStatus.setText("Out of Stock");
                tvStockStatus.setTextColor(context.getResources().getColor(R.color.error_red));
                tvStockStatus.setVisibility(View.VISIBLE);
                btnAddToCart.setEnabled(false);
                btnAddToCart.setText("Out of Stock");
            }

            // Load product image
            loadProductImage(product);

            // Check wishlist status
            checkWishlistStatus(product);

            // Check cart status
            checkCartStatus(product);

            // Update UI based on cart status
            updateCartUI();
        }

        private void loadProductImage(Product product) {
            // Get main product image
            List<ProductImage> images = databaseHelper.getProductImages(product.getId());
            String imageUrl = null;
            
            for (ProductImage image : images) {
                if ("main".equals(image.getImageType()) || "thumbnail".equals(image.getImageType())) {
                    imageUrl = image.getImageUrl();
                    break;
                }
            }
            
            // If no main image, get first available image
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

        private void checkWishlistStatus(Product product) {
            String userEmail = sessionManager.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                isInWishlist = databaseHelper.isInWishlist(userEmail, product.getId());
                updateWishlistIcon();
            }
        }

        private void checkCartStatus(Product product) {
            try {
                String userEmail = sessionManager.getEmail();
                if (userEmail != null && !userEmail.isEmpty() && databaseHelper != null) {
                    cartQuantity = databaseHelper.getCartQuantity(userEmail, product.getId());
                    isInCart = cartQuantity > 0;
                } else {
                    cartQuantity = 0;
                    isInCart = false;
                }
            } catch (Exception e) {
                android.util.Log.e("ProductCardAdapter", "Error checking cart status", e);
                cartQuantity = 0;
                isInCart = false;
            }
        }

        private void updateWishlistIcon() {
            if (isInWishlist) {
                btnWishlist.setImageResource(R.drawable.ic_favorite_filled);
                btnWishlist.setColorFilter(context.getResources().getColor(android.R.color.holo_red_light));
            } else {
                btnWishlist.setImageResource(R.drawable.ic_favorite_border);
                btnWishlist.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
            }
        }

        private void updateCartUI() {
            if (isInCart && cartQuantity > 0) {
                btnAddToCart.setVisibility(View.GONE);
                layoutQuantityControls.setVisibility(View.VISIBLE);
                tvQuantity.setText(String.valueOf(cartQuantity));
            } else {
                btnAddToCart.setVisibility(View.VISIBLE);
                layoutQuantityControls.setVisibility(View.GONE);
            }
        }

        private void toggleWishlist(Product product) {
            String userEmail = sessionManager.getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                Toast.makeText(context, "Please log in to use wishlist", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isInWishlist) {
                databaseHelper.removeFromWishlist(userEmail, product.getId());
                isInWishlist = false;
                Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onRemoveFromWishlist(product);
                }
            } else {
                databaseHelper.addToWishlist(userEmail, product.getId());
                isInWishlist = true;
                Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onAddToWishlist(product);
                }
            }
            updateWishlistIcon();
        }

        private void addToCart(Product product) {
            String userEmail = sessionManager.getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                Toast.makeText(context, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!product.isInStock()) {
                Toast.makeText(context, "Product is out of stock", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseHelper.addToCart(userEmail, product.getId(), 1);
            isInCart = true;
            cartQuantity = 1;
            updateCartUI();
            
            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
            
            if (listener != null) {
                listener.onAddToCart(product, 1);
            }
        }

        private void increaseQuantity(Product product) {
            String userEmail = sessionManager.getEmail();
            if (userEmail == null || userEmail.isEmpty()) return;

            if (cartQuantity < product.getStockQuantity()) {
                cartQuantity++;
                databaseHelper.updateCartQuantity(userEmail, product.getId(), cartQuantity);
                tvQuantity.setText(String.valueOf(cartQuantity));
                
                if (listener != null) {
                    listener.onAddToCart(product, 1);
                }
            } else {
                Toast.makeText(context, "Maximum stock reached", Toast.LENGTH_SHORT).show();
            }
        }

        private void decreaseQuantity(Product product) {
            String userEmail = sessionManager.getEmail();
            if (userEmail == null || userEmail.isEmpty()) return;

            if (cartQuantity > 1) {
                cartQuantity--;
                databaseHelper.updateCartQuantity(userEmail, product.getId(), cartQuantity);
                tvQuantity.setText(String.valueOf(cartQuantity));
                
                if (listener != null) {
                    listener.onRemoveFromCart(product);
                }
            } else {
                // Remove from cart completely
                databaseHelper.removeFromCart(userEmail, product.getId());
                isInCart = false;
                cartQuantity = 0;
                updateCartUI();
                
                if (listener != null) {
                    listener.onRemoveFromCart(product);
                }
            }
        }
    }
}
