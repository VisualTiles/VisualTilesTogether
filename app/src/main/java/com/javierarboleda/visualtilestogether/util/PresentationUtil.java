package com.javierarboleda.visualtilestogether.util;

import android.support.percent.PercentFrameLayout;
import android.util.Log;
import android.view.View;

import com.javierarboleda.visualtilestogether.BuildConfig;
import com.javierarboleda.visualtilestogether.models.Layout;
import com.javierarboleda.visualtilestogether.models.Rect;

/**
 * Created by chris on 12/6/16.
 */

public class PresentationUtil {
    private static final String TAG = PresentationUtil.class.getSimpleName();
    public static void moveRelativeView(Layout layout, View view, Rect bounds) {
        if (view == null) {
            Log.e(TAG, "moveRelativeView called on null view. >:(");
            return;
        }
        if (BuildConfig.DEBUG && !(view.getLayoutParams()
                instanceof PercentFrameLayout.LayoutParams)) {
            throw new RuntimeException("View is not within a PercentRelativeLayout");
        }
        PercentFrameLayout.LayoutParams params = (PercentFrameLayout.LayoutParams)
                view.getLayoutParams();
        int height = layout.getLayoutHeight();
        int width = layout.getLayoutWidth();
        params.getPercentLayoutInfo().topMarginPercent = bounds.top / (float) height;
        params.getPercentLayoutInfo().leftMarginPercent = bounds.left / (float) width;
        params.getPercentLayoutInfo().heightPercent = (bounds.bottom - bounds.top) / (float) height;
        params.getPercentLayoutInfo().widthPercent = (bounds.right - bounds.left) / (float) width;
        view.invalidate();
    }
}
