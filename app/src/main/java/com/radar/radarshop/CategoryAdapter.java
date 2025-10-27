package com.radar.radarshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private List<Category> categories;
    private DatabaseHelper databaseHelper;
    private OnCategoryClickListener listener;
    
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }
    
    public CategoryAdapter(List<Category> categories, DatabaseHelper databaseHelper, OnCategoryClickListener listener) {
        this.categories = categories;
        this.databaseHelper = databaseHelper;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_category_card, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, databaseHelper, listener);
        
        // Add staggered entrance animation
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(30f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(position * 100)
                .start();
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView categoryImage;
        private TextView categoryTitle;
        private TextView categoryCount;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            categoryImage = itemView.findViewById(R.id.imgCategory);
            categoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
            categoryCount = itemView.findViewById(R.id.tvCategoryCount);
        }
        
        public void bind(Category category, DatabaseHelper databaseHelper, OnCategoryClickListener listener) {
            // Set category title
            if (categoryTitle != null) {
                categoryTitle.setText(category.getName());
                // Add modern slide-up animation
                categoryTitle.setAlpha(0f);
                categoryTitle.setTranslationY(20f);
                categoryTitle.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setStartDelay(100)
                        .start();
            }
            
            // Get products for this category and set count
            List<Product> products = databaseHelper.getProductsByCategory(category.getId());
            if (categoryCount != null) {
                String countText = products.size() + (products.size() == 1 ? " item" : " items");
                categoryCount.setText(countText);
                categoryCount.setAlpha(0f);
                categoryCount.setTranslationY(20f);
                categoryCount.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setStartDelay(200)
                        .start();
            }
            
            // Set category image - use category's own image URL
            if (categoryImage != null) {
                String imageUrl = category.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // For URL images, use modern placeholder as fallback
                    // In a real app, you would load the image using Glide or Picasso
                    categoryImage.setImageResource(R.drawable.product_placeholder);
                } else {
                    // Use modern placeholder if no image found
                    categoryImage.setImageResource(R.drawable.product_placeholder);
                }
            }
            
            // Set click listener with modern ripple effect
            cardView.setOnClickListener(v -> {
                // Modern ripple effect with scale animation
                cardView.animate()
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .setDuration(80)
                        .withEndAction(() -> {
                            cardView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                                    .withEndAction(() -> {
                                        if (listener != null) {
                                            listener.onCategoryClick(category);
                                        }
                                    })
                                    .start();
                        }).start();
            });
        }
    }
}
