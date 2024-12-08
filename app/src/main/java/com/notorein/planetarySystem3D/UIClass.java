package com.notorein.planetarySystem3D;


import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class UIClass {

    public static void animateClick(View view) {
        // Example animation: scaling the view

        // ######################################################################## SCALE DOWN
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(
                view,
                "scaleX", 0.7f
        );
        scaleDownX.setDuration(300);
        scaleDownX.setInterpolator(new AccelerateDecelerateInterpolator());
        // --------------------------------------------------------------------
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(
                view,
                "scaleY", 0.7f  // Fix: Use "scaleY" here
        );
        scaleDownY.setDuration(300);
        scaleDownY.setInterpolator(new AccelerateDecelerateInterpolator());

        // ######################################################################### SCALE UP AGAIN

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(
                view,
                "scaleX", 1f
        );
        scaleUpX.setDuration(300);
        scaleUpX.setInterpolator(new AccelerateDecelerateInterpolator());
        // --------------------------------------------------------------------
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(
                view,
                "scaleY", 1f  // Fix: Use "scaleY" here
        );
        scaleUpY.setDuration(300);
        scaleUpY.setInterpolator(new AccelerateDecelerateInterpolator());
        // ######################################################################### SCALE UP AGAIN

        scaleDownY.start();
        scaleDownX.start();
        scaleDownY.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                scaleUpX.start();
                scaleUpY.start();
            }
        });
    }

    public static void animateClick(View view, long duration) {
        // Example animation: scaling the view

        // ######################################################################## SCALE DOWN
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(
                view,
                "scaleX", 0.7f
        );
        scaleDownX.setDuration(duration);
        scaleDownX.setInterpolator(new AccelerateDecelerateInterpolator());
        // --------------------------------------------------------------------
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(
                view,
                "scaleY", 0.7f  // Fix: Use "scaleY" here
        );
        scaleDownY.setDuration(duration);
        scaleDownY.setInterpolator(new AccelerateDecelerateInterpolator());

        // ######################################################################### SCALE UP AGAIN

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(
                view,
                "scaleX", 1f
        );
        scaleUpX.setDuration(duration);
        scaleUpX.setInterpolator(new AccelerateDecelerateInterpolator());
        // --------------------------------------------------------------------
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(
                view,
                "scaleY", 1f  // Fix: Use "scaleY" here
        );
        scaleUpY.setDuration(duration);
        scaleUpY.setInterpolator(new AccelerateDecelerateInterpolator());
        // ######################################################################### SCALE UP AGAIN

        scaleDownY.start();
        scaleDownX.start();
        scaleDownY.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                scaleUpX.start();
                scaleUpY.start();
            }
        });
    }

}
