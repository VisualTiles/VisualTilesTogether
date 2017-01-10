package com.javierarboleda.visualtilestogether.views;

import android.content.Context;
import android.content.res.TypedArray;
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
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.UserType, 0, 0);
        try {
            boolean isUser = a.getBoolean(R.styleable.UserType_state_user, false);
            boolean isModerator = a.getBoolean(R.styleable.UserType_state_moderator, false);
            boolean isBlocked = a.getBoolean(R.styleable.UserType_state_blocked, false);
            mUserType = isModerator? User.MODERATOR
                    : isBlocked? User.BLOCKED
                    : User.REGULAR_USER;
        } finally {
            a.recycle();
        }
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
        if (mUserType != userType) {
            mUserType = userType;
            invalidate();
            refreshDrawableState();
        }
    }
}
