package com.radar.radarshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AccountSwitcherAdapter extends RecyclerView.Adapter<AccountSwitcherAdapter.AccountViewHolder> {
    
    private List<SessionManager.SavedAccount> accounts;
    private OnAccountClickListener listener;
    
    public interface OnAccountClickListener {
        void onAccountClick(SessionManager.SavedAccount account);
    }
    
    public AccountSwitcherAdapter(List<SessionManager.SavedAccount> accounts, OnAccountClickListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_switcher, parent, false);
        return new AccountViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        SessionManager.SavedAccount account = accounts.get(position);
        holder.bind(account, listener);
        
        // Add entrance animation
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(20f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(position * 50)
                .start();
    }
    
    @Override
    public int getItemCount() {
        return accounts != null ? accounts.size() : 0;
    }
    
    static class AccountViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvAvatar;
        private TextView tvAccountName;
        private TextView tvAccountEmail;
        
        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            tvAccountEmail = itemView.findViewById(R.id.tvAccountEmail);
        }
        
        public void bind(SessionManager.SavedAccount account, OnAccountClickListener listener) {
            // Set avatar initials
            String fullName = account.getFullName();
            String initials = makeInitials(fullName, account.email);
            if (tvAvatar != null) {
                tvAvatar.setText(initials);
            }
            
            // Set account name
            if (tvAccountName != null) {
                String displayName = fullName.isEmpty() ? account.email : fullName;
                tvAccountName.setText(displayName);
            }
            
            // Set email
            if (tvAccountEmail != null) {
                tvAccountEmail.setText(account.email);
            }
            
            // Set click listener
            if (cardView != null) {
                cardView.setOnClickListener(v -> {
                    // Add scale animation
                    cardView.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                cardView.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .withEndAction(() -> {
                                            if (listener != null) {
                                                listener.onAccountClick(account);
                                            }
                                        })
                                        .start();
                            })
                            .start();
                });
            }
        }
        
        private String makeInitials(String fullName, String email) {
            // Try full name
            if (fullName != null && !fullName.trim().isEmpty()) {
                String[] parts = fullName.trim().split("\\s+");
                if (parts.length >= 2) {
                    return "" + Character.toUpperCase(parts[0].charAt(0))
                            + Character.toUpperCase(parts[1].charAt(0));
                } else if (parts.length == 1 && parts[0].length() > 0) {
                    return "" + Character.toUpperCase(parts[0].charAt(0));
                }
            }
            // Fallback: email username
            if (email != null && !email.isEmpty()) {
                String[] split = email.split("@", 2);
                String user = split.length > 0 ? split[0] : "";
                if (!user.isEmpty()) {
                    char c1 = Character.toUpperCase(user.charAt(0));
                    char c2 = user.length() > 1 ? Character.toUpperCase(user.charAt(1)) : 0;
                    return c2 == 0 ? String.valueOf(c1) : ("" + c1 + c2);
                }
            }
            return "U";
        }
    }
}
