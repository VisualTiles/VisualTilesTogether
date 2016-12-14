package com.javierarboleda.visualtilestogether.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.javierarboleda.visualtilestogether.models.TileEffect;

import java.util.Random;

/**
 * Created by chris on 11/18/16.
 */

public class TileEffectTransformer2 {
    private static final String TAG = TileEffectTransformer2.class.getSimpleName();
    private Context context;
    private long stageDuration;
    public TileEffectTransformer2(Context context, long duration) {
        this.context = context;
        this.stageDuration = duration;
    }
    private ObjectAnimator applyDuration(ObjectAnimator animator, long delay, long duration) {
        animator.setStartDelay(delay);
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setAutoCancel(true);
        return animator;
    }
    public AnimatorSet processTileEffect(AnimatorSet as, final View view, TileEffect effect) {
        if (effect == null) return null;
        final long delay = (long) (effect.getEffectOffsetPct() * this.stageDuration);
        final long duration = (long) (effect.getEffectDurationPct() * this.stageDuration);
        final long halfDuration = duration / 2;
        final long thirdDuration = duration / 3;

        if (effect.getEffectType() == null) return null;
        TileEffect.EffectType effectType = TileEffect.EffectType.NONE;
        try {
            effectType = TileEffect.EffectType.valueOf(effect.getEffectType());
        } catch(IllegalArgumentException ex) {
            Log.e(TAG, "Invalid tile effect " + effect.getEffectType() + " seen in tile!");
        }
        ObjectAnimator oa1, oa2, oa3, oa4;
        switch (effectType) {
            case FADE_HALF:
            case FADE_OUT:
                oa1 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.5f, 1f);
                applyDuration(oa1, delay, duration);
                oa1.setRepeatMode(ValueAnimator.RESTART);
                oa1.setRepeatCount(ValueAnimator.INFINITE);
                as.play(oa1);
                break;
            case FADE_IN:
                oa1 = ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1f, 0.5f);
                applyDuration(oa1, delay, duration);
                oa1.setRepeatMode(ValueAnimator.RESTART);
                oa1.setRepeatCount(ValueAnimator.INFINITE);
                as.play(oa1);
                break;
            case FLIP_HORIZONTAL:
            case FLIP_HORIZONTAL_RIGHT:
                ObjectAnimator rotateYRight = ObjectAnimator.ofFloat(view, "rotationY",
                        0f, 360f);
                applyDuration(rotateYRight, delay, duration*2);
                rotateYRight.setRepeatMode(ValueAnimator.RESTART);
                rotateYRight.setRepeatCount(ValueAnimator.INFINITE);
                as.play(rotateYRight);
                break;
            case FLIP_HORIZONTAL_LEFT:
                ObjectAnimator rotateYLeft = ObjectAnimator.ofFloat(view, "rotationY",
                        0f, -360f);
                applyDuration(rotateYLeft, delay, duration*2);
                rotateYLeft.setRepeatMode(ValueAnimator.RESTART);
                rotateYLeft.setRepeatCount(ValueAnimator.INFINITE);
                as.play(rotateYLeft);
                break;
            case PIVOT_LEFT_SMALL:
                oa1 = ObjectAnimator.ofFloat(view, "rotationY", 0f, -60f, 0f, -60f, 0f);
                applyDuration(oa1, delay, duration*4);
                oa1.setRepeatMode(ValueAnimator.REVERSE);
                oa1.setRepeatCount(ValueAnimator.INFINITE);

                oa2 = ObjectAnimator.ofFloat(view, "rotationX", 0f, -30f, 0f, 30f, 0f);
                applyDuration(oa2, delay, duration*2);
                oa1.setRepeatMode(ValueAnimator.REVERSE);
                oa1.setRepeatCount(ValueAnimator.INFINITE);
                as.playTogether(oa1, oa2);
                break;
            case PIVOT_RIGHT_SMALL:
                oa1 = ObjectAnimator.ofFloat(view, "rotationY", 0f, 30f, 0f, -30f, 0f);
                applyDuration(oa1, delay, duration*2);
                oa1.setRepeatMode(ValueAnimator.REVERSE);
                oa1.setRepeatCount(ValueAnimator.INFINITE);

                oa2 = ObjectAnimator.ofFloat(view, "rotationX", 0f, -60f, 0f, 60f, 0f);
                applyDuration(oa2, delay, duration*4);
                oa1.setRepeatMode(ValueAnimator.REVERSE);
                oa1.setRepeatCount(ValueAnimator.INFINITE);
                as.playTogether(oa1, oa2);
                break;
            case NOD_NO:
                oa1 = ObjectAnimator.ofFloat(view, "rotationY", 0f, 60f, 0f, -60f, 0f);
                applyDuration(oa1, delay, duration);
                oa1.setRepeatMode(ValueAnimator.RESTART);
                as.setInterpolator(new FastOutLinearInInterpolator());
                as.play(oa1);
                break;
            case NOD_YES:
                oa1 = ObjectAnimator.ofFloat(view, "rotationX", 0f, 60f, 0f, -60f, 0f);
                applyDuration(oa1, delay, duration);
                oa1.setRepeatMode(ValueAnimator.RESTART);
                as.setInterpolator(new FastOutLinearInInterpolator());
                as.play(oa1);
                break;
            case ROTATE_LEFT:
                ObjectAnimator rotateLeft = ObjectAnimator.ofFloat(view, "rotation", 0f, -360f);
                applyDuration(rotateLeft, delay, duration*2);
                rotateLeft.setRepeatMode(ValueAnimator.RESTART);
                rotateLeft.setRepeatCount(ValueAnimator.INFINITE);
                as.play(rotateLeft);
                break;
            case ROTATE_RIGHT:
                ObjectAnimator rotateRight = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
                applyDuration(rotateRight, delay, duration*2);
                rotateRight.setRepeatMode(ValueAnimator.RESTART);
                rotateRight.setRepeatCount(ValueAnimator.INFINITE);
                as.setInterpolator(new AccelerateDecelerateInterpolator());
                as.play(rotateRight);
                break;
            case FLY_AWAY:
                ObjectAnimator rotateAway = ObjectAnimator.ofFloat(view, "rotation", 0f, 90f);
                float r = view.getRootView().getRight() - view.getLeft();
                ObjectAnimator translateAwayX = ObjectAnimator.ofFloat(view, "translationX",
                        0,
                        (new Random().nextFloat()*r*2)-r);
                ObjectAnimator translateAwayY = ObjectAnimator.ofFloat(view, "translationY",
                        0,
                        view.getRootView().getBottom() - view.getTop());
                applyDuration(rotateAway, delay, duration);
                applyDuration(translateAwayX, delay, duration);
                applyDuration(translateAwayY, delay, duration);
                rotateAway.setRepeatMode(ValueAnimator.REVERSE);
                translateAwayX.setRepeatMode(ValueAnimator.REVERSE);
                translateAwayY.setRepeatMode(ValueAnimator.REVERSE);
                rotateAway.setRepeatCount(ValueAnimator.INFINITE);
                translateAwayX.setRepeatCount(ValueAnimator.INFINITE);
                translateAwayY.setRepeatCount(ValueAnimator.INFINITE);
                as.setInterpolator(new AccelerateDecelerateInterpolator());
                as.playTogether(rotateAway, translateAwayX, translateAwayY);
                break;
            case THUMP:
                oa1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f, 1f);
                oa2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f, 1f);
                applyDuration(oa1, delay, duration);
                applyDuration(oa2, delay, duration);
                oa1.setRepeatMode(ValueAnimator.RESTART);
                oa2.setRepeatMode(ValueAnimator.RESTART);
                as.playTogether(oa1, oa2);
                break;
            /*
            case FLY_AWAY:
                RotateAnimation rotate1 = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f , Animation.RELATIVE_TO_SELF,0.5f );
                rotate1.setStartOffset(delay);
                rotate1.setDuration(duration);
                as.addAnimation(rotate1);
                TranslateAnimation trans1 =  new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.1f, Animation.RELATIVE_TO_PARENT, 0.3f,
                        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.9f);
                trans1.setStartOffset(delay);
                trans1.setDuration(duration);
                as.addAnimation(trans1);
                break;
            case NONE:
                as.setFillBefore(false);
                as.setFillAfter(false);
                break;
            case FREEZE:
                // Fill is enabled... so the animation just pauses at where it left off when this
                // animation was triggered.
                as.setFillBefore(true);
                as.setFillAfter(true);
                break;*/
        }
        return as;
    }

    public long getStageDuration() {
        return stageDuration;
    }

    public void setStageDuration(long stageDuration) {
        this.stageDuration = stageDuration;
    }
}
