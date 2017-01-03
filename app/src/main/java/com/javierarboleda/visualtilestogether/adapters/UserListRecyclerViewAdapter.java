package com.javierarboleda.visualtilestogether.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import com.javierarboleda.visualtilestogether.models.User;
import com.javierarboleda.visualtilestogether.views.UserButton;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by geo on 12/20/16.
 */

public class UserListRecyclerViewAdapter extends FirebaseRecyclerAdapter<Object, UserListRecyclerViewAdapter.UserViewHolder> {

    private static final String LOG_TAG = UserListRecyclerViewAdapter.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private Context mContext;

    /**
     * @param modelClass      Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public UserListRecyclerViewAdapter(Class<Object> modelClass, int modelLayout, Class<UserViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(final UserViewHolder viewHolder, Object model, int position) {
        DatabaseReference uRef = getRef(position);
        String uId = uRef.getKey();
        Log.d(LOG_TAG, "populateViewHolder, position = " + position);
        uRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewHolder.ubUserType.setUserType(dataSnapshot.getValue(int.class));
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
                    User user = dataSnapshot.getValue(User.class);
                    if (user.getPhotoUrl() != null) {
                        Glide.with(mContext).load(user.getPhotoUrl())
                                .bitmapTransform(new CropCircleTransformation(mContext))
                                .animate(R.anim.zoom_in)
                                .into(viewHolder.ivUserPhoto);
                    }
                    viewHolder.tvUserName.setText(user.getName());
                    Log.d(LOG_TAG, "userRef.onDataChange userName = " + user.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

        public UserViewHolder(View itemView) {
            super(itemView);

            ivUserPhoto = (ImageView) itemView.findViewById(R.id.ivUserPhoto);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            ubUserType = (UserButton) itemView.findViewById(R.id.ubUserType);
        }
    }
}
