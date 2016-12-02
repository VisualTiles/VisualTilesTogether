package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.adapters.TileListRecyclerViewAdapter;
import com.javierarboleda.visualtilestogether.models.Tile;

public abstract class TileListFragment extends Fragment {
    private static final String LOG_TAG = TileListFragment.class.getSimpleName();

    private ProgressBar mProgressBar;
    private RecyclerView mRvTileList;
    private FirebaseRecyclerAdapter<Object, TileListRecyclerViewAdapter.TileViewholder> mFirebaseAdapter;
    private Context mContext;
    private LinearLayoutManager mLinearLayoutManager;
    VisualTilesTogetherApp visualTilesTogetherApp;

    private TileListFragmentListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Log.d(LOG_TAG, "enter onCreate");
        visualTilesTogetherApp =  (VisualTilesTogetherApp) getActivity().getApplication();
//        Log.d(LOG_TAG, "exit onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        Log.d(LOG_TAG, "enter onCreateView");
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference dbUsers = dbRef.child(User.TABLE_NAME);
//        dbUsers.child(visualTilesTogetherApp.getUid()).setValue(visualTilesTogetherApp.getUser());

        final View view = inflater.inflate(R.layout.fragment_tile_list, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mRvTileList = (RecyclerView) view.findViewById(R.id.rvTileList);
        // for now don't light up the ProgressBar
        // TODO: show "no tiles" instead of ProgressBar when tile list is empty`
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // bind the tiles table to the RecyclerView
        mFirebaseAdapter = getAdapter(dbRef);

        // watch for realtime changes
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int tileCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.
                        findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (tileCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRvTileList.scrollToPosition(positionStart);
                }
            }
        });

        // hook up the RecyclerView
        mLinearLayoutManager = getLayoutManager();
        mRvTileList.setLayoutManager(mLinearLayoutManager);
        mRvTileList.setAdapter(mFirebaseAdapter);
        return view;
    }

    abstract TileListRecyclerViewAdapter getAdapter(DatabaseReference dbRef);

    abstract LinearLayoutManager getLayoutManager();

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof TileListFragmentListener) {
            mListener = (TileListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    public interface TileListFragmentListener {
        void updateSelectedTile(Tile tile);
    }
}
