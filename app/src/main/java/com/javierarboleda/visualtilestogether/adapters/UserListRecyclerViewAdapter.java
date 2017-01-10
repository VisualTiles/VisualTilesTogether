package com.javierarboleda.visualtilestogether.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.User;
import com.javierarboleda.visualtilestogether.views.UserButton;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by geo on 12/20/16.
 */

public class UserListRecyclerViewAdapter extends FirebaseRecyclerAdapter<Object, UserListRecyclerViewAdapter.UserViewHolder> {

    private static final String LOG_TAG = UserListRecyclerViewAdapter.class.getSimpleName();
    private final Animation mAnimation;
    private final VisualTilesTogetherApp vistalTilesTogetherApp;
    private RecyclerView mRecyclerView;
    private Context mContext;

    /**
     * @param modelClass      Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
*                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     * @param visualTilesTogetherApp
     */
    public UserListRecyclerViewAdapter(Class<Object> modelClass,
                                       int modelLayout,
                                       Class<UserViewHolder> viewHolderClass,
                                       Query ref,
                                       VisualTilesTogetherApp visualTilesTogetherApp) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.vistalTilesTogetherApp = visualTilesTogetherApp;
        mAnimation = new AlphaAnimation(1, (float) .5);
        mAnimation.setDuration(333);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
    }

    @Override
    protected void populateViewHolder(final UserViewHolder viewHolder, Object model, int position) {
        final DatabaseReference uRef = getRef(position);
        final String uId = uRef.getKey();
        viewHolder.isExpanded = false;
        viewHolder.llExpansion.setVisibility(View.GONE);
        for (int i = 0; i < 3; i++) {
            viewHolder.ubSelectors[i].setOnClickListener(selectorClickListener(viewHolder, i));
        }
        uRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewHolder.userType = dataSnapshot.getValue(int.class);
                    viewHolder.ubUserType.setUserType(viewHolder.userType);
                    boolean isSelf = uId.equals(vistalTilesTogetherApp.getUid());
                    viewHolder.ubUserType.setSelected(!isSelf);
                    if (!isSelf) {
                        viewHolder.ubUserType.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                viewHolder.isExpanded = !viewHolder.isExpanded;
                                if (viewHolder.isExpanded) {
                                    viewHolder.llExpansion.setVisibility(View.VISIBLE);
                                    viewHolder.ubSelectors[viewHolder.userType].setSelected(true);
                                } else {
                                    viewHolder.userType = findSelected(viewHolder.ubSelectors);
                                    uRef.setValue(viewHolder.userType);
                                    viewHolder.ubUserType.clearAnimation();
                                    viewHolder.llExpansion.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference userRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(User.TABLE_NAME)
                .child(uId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewHolder.user = dataSnapshot.getValue(User.class);
                    if (viewHolder.user.getPhotoUrl() != null) {
                        Glide.with(mContext).load(viewHolder.user.getPhotoUrl())
                                .bitmapTransform(new CropCircleTransformation(mContext))
                                .animate(R.anim.zoom_in)
                                .into(viewHolder.ivUserPhoto);
                    }
                    viewHolder.tvUserName.setText(viewHolder.user.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int findSelected(UserButton[] ubSelectors) {
        for (int i = 0; i < 3; i++) {
            if (ubSelectors[i].isSelected()) {
                return i;
            }
        }
        return 0;
    }

    private View.OnClickListener selectorClickListener(final UserViewHolder viewHolder, final int index) {
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < 3; i++) {
                    viewHolder.ubSelectors[i].setSelected(i == index);
                }
                if (viewHolder.userType != index) {
                    viewHolder.ubUserType.startAnimation(mAnimation);
                } else {
                    viewHolder.ubUserType.clearAnimation();
                }
                viewHolder.ubUserType.setUserType(index);
            }
        };
        return ocl;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        mContext = recyclerView.getContext();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserPhoto;
        TextView tvUserName;
        UserButton ubUserType;
        UserButton[] ubSelectors = new UserButton[3];
        View llExpansion;
        User user;
        int userType;
        boolean isExpanded;

        public UserViewHolder(View itemView) {
            super(itemView);

            ivUserPhoto = (ImageView) itemView.findViewById(R.id.ivUserPhoto);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            ubUserType = (UserButton) itemView.findViewById(R.id.ubUserType);
            ubSelectors[0] = (UserButton) itemView.findViewById(R.id.ubUser);
            ubSelectors[1] = (UserButton) itemView.findViewById(R.id.ubModerator);
            ubSelectors[2] = (UserButton) itemView.findViewById(R.id.ubBlocked);
            llExpansion = itemView.findViewById(R.id.llExpansion);
        }
    }
}
