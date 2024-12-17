package com.notorein.threedmodeling;


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

    public static void animateClick(View view, int repeat) {
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
                scaleUpY.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        super.onAnimationEnd(animation);
                        if (repeat > 1) {
                            animateClick(view, repeat - 1);
                        }
                    }
                });
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
    public static void animateClickDisableOthers(View view) {
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

    public static void animateClickDisableOthers(View view, int repeat) {
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
                scaleUpY.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        super.onAnimationEnd(animation);
                        if (repeat > 1) {
                            animateClickDisableOthers(view, repeat - 1);
                        }
                    }
                });
            }
        });
    }


    public static void animateClickDisableOthers(View view, long duration) {
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


    public static void animateClickDisableOthers(View clickedButton, View... otherButtons) {
        boolean isClickedButtonSelected = clickedButton.isSelected();

        if (isClickedButtonSelected) {
            // If the clicked button is already selected, re-enable all buttons
            for (View button : otherButtons) {
                button.setEnabled(true);
                button.setAlpha(1.0f);
                button.setSelected(false);
            }
            clickedButton.setSelected(false);
        } else {
            // Select the clicked button
            clickedButton.setSelected(true);
            clickedButton.setEnabled(true);
            clickedButton.setAlpha(1.0f);

            // Deselect and disable all other buttons and set their alpha to 0.67
            for (View button : otherButtons) {
                if (button != clickedButton) {
                    button.setSelected(false);
                    button.setEnabled(false);
                    button.setAlpha(0.27f);
                }
            }
        }

    }
    public static void enableAllButtons(View... buttons) {
        for (View button : buttons) {
            button.setEnabled(true);
            button.setAlpha(1.0f);
            button.setSelected(false);
        }
    }

    public static void disableAllButtons(View... buttons) {
        for (View button : buttons) {
            button.setEnabled(false);
            button.setAlpha(0.67f);
            button.setSelected(false);
        }
    }


}
