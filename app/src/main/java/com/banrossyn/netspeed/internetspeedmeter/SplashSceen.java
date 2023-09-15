package com.banrossyn.netspeed.internetspeedmeter;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class SplashSceen extends AppCompatActivity {

    ImageView myView;
    int timer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_sceen);

        myView = findViewById(R.id.imageview);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(myView, "alpha",  1f, .3f);
        fadeOut.setDuration(1000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(myView, "alpha", .3f, 1f);
        fadeIn.setDuration(1000);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
                timer++;

                if(timer==2){
                    Intent next = new Intent(SplashSceen.this, HomeActivity.class);
                    SplashSceen.this.startActivity(next);
                    SplashSceen.this.finish();

                }
            }
        });
        mAnimationSet.start();

    }

}
