package com.javierarboleda.visualtilestogether.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.models.User;

/**
 * Created by geo on 1/2/17.
 */

public class UserButton extends android.support.v7.widget.AppCompatImageButton {
    private static final String LOG_TAG = UserButton.class.getSimpleName();
    private static final int[] STATE_USER = {R.attr.state_user};
    private static final int[] STATE_MODERATOR = {R.attr.state_moderator};
    private static final int[] STATE_BLOCKED = {R.attr.state_blocked};
    private static final int[][] STATES = {STATE_USER, STATE_MODERATOR, STATE_BLOCKED};
    private @User.UserType int mUserType;

    public UserButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);
        mergeDrawableStates(drawableState, STATES[mUserType]);
        return drawableState;
    }

    public @User.UserType int getUserType() {
        return mUserType;
    }

    public void setUserType(int userType) {
        Log.d(LOG_TAG, "setUserType(" + userType + ")" + " from " + mUserType);
        if (mUserType != userType) {
            mUserType = userType;
            invalidate();
            refreshDrawableState();
        }
    }
}
