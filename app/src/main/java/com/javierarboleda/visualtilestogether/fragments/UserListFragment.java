package com.javierarboleda.visualtilestogether.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.adapters.UserListRecyclerViewAdapter;
import com.javierarboleda.visualtilestogether.models.Channel;

/**
 * Created by geo on 12/30/16.
 */

public class UserListFragment extends Fragment {
    private static final String LOG_TAG = UserListFragment.class.getSimpleName();
    private VisualTilesTogetherApp visualTilesTogetherApp;
    private RecyclerView mRvUserList;
    private UserListRecyclerViewAdapter mUserFbAdapter;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference dbRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbRef = FirebaseDatabase.getInstance().getReference();
        visualTilesTogetherApp =  (VisualTilesTogetherApp) getActivity().getApplication();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        mRvUserList = (RecyclerView) view.findViewById(R.id.rvUserList);
        mUserFbAdapter = new UserListRecyclerViewAdapter(Object.class,
                R.layout.user_list_item,
                UserListRecyclerViewAdapter.UserViewHolder.class,
                getQuery());

        mUserFbAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                Log.d(LOG_TAG, "AdapterDataObserver onChanged()");
                super.onChanged();
                mUserFbAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                Log.d(LOG_TAG, "AdapterDataObserver onItemRangeInserted(" + positionStart + ", " + itemCount + ")");
                super.onItemRangeChanged(positionStart, itemCount);

                int tileCount = mUserFbAdapter.getItemCount();
                int lastVisiblePosition = mLayoutManager
                        .findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (tileCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRvUserList.scrollToPosition(positionStart);
                }
            }
        });

        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(false);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRvUserList.setLayoutManager(mLayoutManager);
        mRvUserList.setAdapter(mUserFbAdapter);

        return view;
    }

    private Query getQuery() {
        return dbRef
                .child(Channel.TABLE_NAME)
                .child(visualTilesTogetherApp.getChannelId())
                .child(Channel.USERS)
                .orderByKey();
    }
}
