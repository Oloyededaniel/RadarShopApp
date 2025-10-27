package com.radar.radarshop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Random;

public class ScatteredGridLayoutManager extends RecyclerView.LayoutManager {
    
    private int spanCount = 2; // Fixed to 2 categories per row
    private int itemWidth;
    private int itemHeight;
    private Random random = new Random();
    
    public ScatteredGridLayoutManager(Context context) {
        super();
    }
    
    public ScatteredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super();
    }
    
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }
    
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        
        detachAndScrapAttachedViews(recycler);
        
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        
        // Calculate item width to fit exactly 2 categories per row with proper spacing
        int marginBetweenItems = 16; // 16dp margin between items
        int totalMargin = marginBetweenItems; // margin between the 2 items
        itemWidth = (availableWidth - totalMargin) / 2; // Each item takes half the available width minus margins
        itemHeight = 180; // Fixed height for consistent appearance
        
        // Fixed to 2 categories per row as requested
        spanCount = 2;
        
        int currentRow = 0;
        int currentCol = 0;
        
        for (int i = 0; i < getItemCount(); i++) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            
            // Set the calculated dimensions
            ViewGroup.LayoutParams params = child.getLayoutParams();
            params.width = itemWidth;
            params.height = itemHeight;
            child.setLayoutParams(params);
            
            measureChildWithMargins(child, 0, 0);
            
            // Add some randomness to create scattered effect (reduced for better fit)
            int randomOffsetX = random.nextInt(8) - 4; // -4 to +4 pixels
            int randomOffsetY = random.nextInt(6) - 3; // -3 to +3 pixels
            
            // Calculate position
            int left = getPaddingLeft() + (currentCol * (itemWidth + marginBetweenItems)) + randomOffsetX;
            int top = getPaddingTop() + (currentRow * (itemHeight + 20)) + randomOffsetY;
            int right = left + itemWidth;
            int bottom = top + itemHeight;
            
            // Ensure we don't go out of bounds
            left = Math.max(getPaddingLeft(), left);
            top = Math.max(getPaddingTop(), top);
            right = Math.min(getWidth() - getPaddingRight(), right);
            bottom = Math.min(getHeight() - getPaddingBottom(), bottom);
            
            layoutDecorated(child, left, top, right, bottom);
            
            currentCol++;
            if (currentCol >= spanCount) {
                currentCol = 0;
                currentRow++;
            }
        }
    }
    
    @Override
    public boolean canScrollVertically() {
        // Enable scrolling when content height exceeds container height
        return true;
    }
    
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        // Calculate if we can scroll based on content height
        int totalContentHeight = calculateTotalContentHeight();
        int containerHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        
        if (totalContentHeight <= containerHeight) {
            return 0; // No scrolling needed if content fits
        }
        
        offsetChildrenVertical(-dy);
        return dy;
    }
    
    private int calculateTotalContentHeight() {
        if (getItemCount() == 0) return 0;
        
        int spanCount = 2;
        int itemHeight = 180;
        int rowSpacing = 20;
        int rows = (int) Math.ceil((double) getItemCount() / spanCount);
        
        return rows * (itemHeight + rowSpacing) + getPaddingTop() + getPaddingBottom();
    }
    
    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        if (getItemCount() == 0) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }
        
        int availableWidth = View.MeasureSpec.getSize(widthSpec) - getPaddingLeft() - getPaddingRight();
        int marginBetweenItems = 16;
        int totalMargin = marginBetweenItems;
        int calculatedItemWidth = (availableWidth - totalMargin) / 2;
        int calculatedItemHeight = 180; // Fixed height for consistency
        
        int spanCount = 2; // Fixed to 2 categories per row
        
        // Calculate total height based on number of categories
        int rows = (int) Math.ceil((double) getItemCount() / spanCount);
        int rowSpacing = 20; // Space between rows
        int totalHeight = rows * (calculatedItemHeight + rowSpacing) + getPaddingTop() + getPaddingBottom();
        
        // Set the measured dimension to allow scrolling when content exceeds screen height
        int requestedHeight = View.MeasureSpec.getSize(heightSpec);
        int finalHeight = Math.max(totalHeight, requestedHeight);
        
        setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), finalHeight);
    }
}
