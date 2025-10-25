package com.radar.radarshop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView brandTextView;
    private TextView taglineTextView;
    private View backgroundView;
    private View pulseView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Install splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeViews();
        startSplashAnimation();
    }

    private void initializeViews() {
        logoImageView = findViewById(R.id.logoImageView);
        brandTextView = findViewById(R.id.brandTextView);
        taglineTextView = findViewById(R.id.taglineTextView);
        backgroundView = findViewById(R.id.backgroundView);
        pulseView = findViewById(R.id.pulseView);
    }

    private void startSplashAnimation() {
        // Initial state - all elements hidden
        logoImageView.setAlpha(0f);
        logoImageView.setScaleX(0.3f);
        logoImageView.setScaleY(0.3f);
        brandTextView.setAlpha(0f);
        brandTextView.setTranslationY(50f);
        taglineTextView.setAlpha(0f);
        taglineTextView.setTranslationY(30f);
        pulseView.setScaleX(0f);
        pulseView.setScaleY(0f);

        // Create animation sequence
        AnimatorSet animatorSet = new AnimatorSet();

        // 1. Logo entrance with bounce effect
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoImageView, "scaleX", 0.3f, 1.1f, 1.0f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoImageView, "scaleY", 0.3f, 1.1f, 1.0f);
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f);
        
        AnimatorSet logoAnimator = new AnimatorSet();
        logoAnimator.playTogether(logoScaleX, logoScaleY, logoAlpha);
        logoAnimator.setDuration(800);
        logoAnimator.setInterpolator(new OvershootInterpolator(1.2f));

        // 2. Pulse effect for logo
        ObjectAnimator pulseScaleX = ObjectAnimator.ofFloat(pulseView, "scaleX", 0f, 1.2f);
        ObjectAnimator pulseScaleY = ObjectAnimator.ofFloat(pulseView, "scaleY", 0f, 1.2f);
        ObjectAnimator pulseAlpha = ObjectAnimator.ofFloat(pulseView, "alpha", 0f, 0.3f, 0f);
        
        AnimatorSet pulseAnimator = new AnimatorSet();
        pulseAnimator.playTogether(pulseScaleX, pulseScaleY, pulseAlpha);
        pulseAnimator.setDuration(600);
        pulseAnimator.setStartDelay(400);

        // 3. Brand text entrance
        ObjectAnimator brandAlpha = ObjectAnimator.ofFloat(brandTextView, "alpha", 0f, 1f);
        ObjectAnimator brandTranslationY = ObjectAnimator.ofFloat(brandTextView, "translationY", 50f, 0f);
        
        AnimatorSet brandAnimator = new AnimatorSet();
        brandAnimator.playTogether(brandAlpha, brandTranslationY);
        brandAnimator.setDuration(600);
        brandAnimator.setInterpolator(new DecelerateInterpolator());
        brandAnimator.setStartDelay(600);

        // 4. Tagline entrance
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(taglineTextView, "alpha", 0f, 1f);
        ObjectAnimator taglineTranslationY = ObjectAnimator.ofFloat(taglineTextView, "translationY", 30f, 0f);
        
        AnimatorSet taglineAnimator = new AnimatorSet();
        taglineAnimator.playTogether(taglineAlpha, taglineTranslationY);
        taglineAnimator.setDuration(500);
        taglineAnimator.setInterpolator(new DecelerateInterpolator());
        taglineAnimator.setStartDelay(900);

        // 5. Background gradient animation
        ValueAnimator backgroundAnimator = ValueAnimator.ofFloat(0f, 1f);
        backgroundAnimator.setDuration(2000);
        backgroundAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        backgroundAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            backgroundView.setAlpha(0.1f + (progress * 0.4f));
        });

        // Play all animations in sequence
        animatorSet.play(logoAnimator)
                .with(pulseAnimator)
                .before(brandAnimator)
                .before(taglineAnimator)
                .with(backgroundAnimator);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Start continuous pulse animation
                startContinuousPulse();
                
                // Navigate to next activity after delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    navigateToNextActivity();
                }, 1500);
            }
        });

        animatorSet.start();
    }

    private void startContinuousPulse() {
        // Create a subtle continuous pulse effect
        ObjectAnimator pulseScaleX = ObjectAnimator.ofFloat(logoImageView, "scaleX", 1.0f, 1.05f, 1.0f);
        ObjectAnimator pulseScaleY = ObjectAnimator.ofFloat(logoImageView, "scaleY", 1.0f, 1.05f, 1.0f);
        
        // Set repeat properties on individual animators
        pulseScaleX.setRepeatCount(ValueAnimator.INFINITE);
        pulseScaleX.setRepeatMode(ValueAnimator.REVERSE);
        pulseScaleY.setRepeatCount(ValueAnimator.INFINITE);
        pulseScaleY.setRepeatMode(ValueAnimator.REVERSE);
        
        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(pulseScaleX, pulseScaleY);
        pulseSet.setDuration(2000);
        pulseSet.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseSet.start();
    }

    private void navigateToNextActivity() {
        // Check if user is logged in
        SessionManager session = new SessionManager(this);
        
        Log.d("SplashActivity", "Session info: " + session.getSessionInfo());
        
        Intent intent;
        if (session.isLoggedIn()) {
            // User is logged in, go to home
            Log.d("SplashActivity", "User is logged in, going to HomeActivity");
            intent = new Intent(this, HomeActivity.class);
        } else {
            // User not logged in, go to auth
            Log.d("SplashActivity", "User not logged in, going to AuthActivity");
            intent = new Intent(this, AuthActivity.class);
        }
        
        startActivity(intent);
        finish();
        
        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
