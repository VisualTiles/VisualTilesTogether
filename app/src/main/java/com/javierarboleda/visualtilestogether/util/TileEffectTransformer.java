package com.javierarboleda.visualtilestogether.util;

import android.content.Context;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.models.TileEffect;

/**
 * Created by chris on 11/18/16.
 */

public class TileEffectTransformer {
    private static final String TAG = TileEffectTransformer.class.getSimpleName();
    private Context context;
    private long stageDuration;
    public TileEffectTransformer(Context context, long duration) {
        this.context = context;
        this.stageDuration = duration;
    }
    private Animation applyDuration(Animation animation, long delay, long duration) {
        animation.setStartOffset(delay);
        animation.setDuration(duration);
        return animation;
    }
    public Animation processTileEffect(TileEffect effect) {
        if (effect == null) return null;
        AnimationSet as = new AnimationSet(true);
        as.setInterpolator(new LinearInterpolator());
        as.setFillBefore(true);
        as.setFillAfter(false);
        as.setFillEnabled(true);
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
        switch (effectType) {
            case FADE_HALF:
                Animation fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                applyDuration(fadeOut, delay, halfDuration);
                as.addAnimation(fadeOut);
                Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                applyDuration(fadeIn, delay + halfDuration, halfDuration);
                as.addAnimation(fadeIn);
                break;
            case FLIP_HORIZONTAL:
                as.addAnimation(applyDuration(
                        new Rotate3dAnimation1(0, 0, -90, 90, 0, 0),
                        delay, duration));
                break;
            case ROTATE_LEFT:
                as.addAnimation(applyDuration(
                        new Rotate3dAnimation1(0, 0, 0, 0, 0, 360),
                        delay, duration));
                break;
            case ROTATE_RIGHT:
                as.addAnimation(applyDuration(
                        new Rotate3dAnimation1(0, 0, 0, 0, 0, -360),
                        delay, duration));
                break;
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
                as.setFillEnabled(false);
                break;
            case FREEZE:
                // Fill is enabled... so the animation just pauses at where it left off when this
                // animation was triggered.
                as.setFillBefore(true);
                as.setFillAfter(true);
                as.setFillEnabled(true);
                break;
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
