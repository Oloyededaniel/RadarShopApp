package com.radar.radarshop;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AuthPagerAdapter extends FragmentStateAdapter {
    
    private String prefillEmail;

    public AuthPagerAdapter(@NonNull FragmentActivity fa) { 
        super(fa);
        this.prefillEmail = null;
    }
    
    public AuthPagerAdapter(@NonNull FragmentActivity fa, String prefillEmail) {
        super(fa);
        this.prefillEmail = prefillEmail;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            SignInFragment fragment = new SignInFragment();
            if (prefillEmail != null) {
                Bundle args = new Bundle();
                args.putString("prefill_email", prefillEmail);
                fragment.setArguments(args);
            }
            return fragment;
        } else {
            return new SignUpFragment();
        }
    }

    @Override
    public int getItemCount() { return 2; }
}

