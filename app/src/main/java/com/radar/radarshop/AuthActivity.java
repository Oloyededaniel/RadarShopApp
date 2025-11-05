package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ScrollView mainScrollView;
    private String prefillEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth); // has tabLayout + viewPager

        // Get prefill email from intent if available
        Intent intent = getIntent();
        if (intent != null) {
            prefillEmail = intent.getStringExtra("prefill_email");
        }

        // 1) If already logged in, skip auth (unless switching accounts)
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn() && prefillEmail == null) {
            // User is already logged in, go to home
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }
        
        // For testing: Uncomment the line below to clear session and force login
        // session.logout();

        // 2) Setup tabs + pager
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        mainScrollView = findViewById(R.id.mainScrollView);

        AuthPagerAdapter adapter = new AuthPagerAdapter(this, prefillEmail);
        viewPager.setAdapter(adapter);
        
        // If prefill email is provided, switch to Sign In tab
        if (prefillEmail != null) {
            viewPager.setCurrentItem(0, false);
        }

        // 3) Attach Tab titles
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Sign In");
            else tab.setText("Sign Up");
        }).attach();

        // 4) Add creative auto-scrolling for better UX
        setupCreativeAutoScrolling();
    }

    private void setupCreativeAutoScrolling() {
        // Auto-scroll to show the form when switching to Sign Up tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                
                // If switching to Sign Up tab (position 1), scroll down to show the form
                if (position == 1) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (mainScrollView != null) {
                            mainScrollView.smoothScrollTo(0, 250);
                        }
                    }, 300);
                }
            }
        });

        // Add smooth scrolling when tab is selected
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Smooth scroll to ensure form is visible
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (mainScrollView != null) {
                        mainScrollView.smoothScrollTo(0, 200);
                    }
                }, 100);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}
