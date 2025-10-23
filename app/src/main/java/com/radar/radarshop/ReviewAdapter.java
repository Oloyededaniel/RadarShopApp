package com.radar.radarshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final List<Review> reviewList;

    public ReviewAdapter(List<Review> reviews) {
        this.reviewList = reviews;
    }

    /** Optional: lets ProductDetailActivity refresh without recreating adapter */
    public void updateData(List<Review> newItems) {
        reviewList.clear();
        reviewList.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ViewHolder holder, int position) {
        Review review = reviewList.get(position);
        String email = review.getUserEmail();
        holder.email.setText((email == null || email.isEmpty()) ? "Anonymous" : email);
        holder.comment.setText(review.getComment());
        holder.rating.setRating(review.getRating());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView email, comment;
        RatingBar rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.textReviewerEmail);
            comment = itemView.findViewById(R.id.textReviewComment);
            rating = itemView.findViewById(R.id.reviewRatingBar);
        }
    }
}
