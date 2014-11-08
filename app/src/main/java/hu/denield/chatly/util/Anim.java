package hu.denield.chatly.util;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Anim {

    public static void showViewWithAnimation(Context context, View view, int animationResourceId) {
        animate(context, view, animationResourceId);
        view.setVisibility(View.VISIBLE);
    }

    public static void hideViewWithAnimation(Context context, View view, int animationResourceId) {
        animate(context, view, animationResourceId);
        view.setVisibility(View.GONE);
    }

    private static void animate(Context context, View view, int animationResourceId) {
        Animation animation = AnimationUtils.loadAnimation(context, animationResourceId);
        view.startAnimation(animation);
    }

}