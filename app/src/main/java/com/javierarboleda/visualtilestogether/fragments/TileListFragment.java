package com.javierarboleda.visualtilestogether.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.adapters.TileListRecyclerViewAdapter;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.util.sidemenu.interfaces.ScreenShotable;

import java.util.Random;

public abstract class TileListFragment extends Fragment
    implements ScreenShotable {
    private static final String LOG_TAG = TileListFragment.class.getSimpleName();

    private Bitmap bitmap;
    private View containerView;
    private ProgressBar mProgressBar;
    private TextView tvEmptyRvList;
    private RecyclerView mRvTileList;
    private FirebaseRecyclerAdapter<Object, TileListRecyclerViewAdapter.TileViewHolder> mFirebaseAdapter;
    private String[] mNoTilesMsgs;
    private Context mContext;
    private RecyclerView.LayoutManager mLayoutManager;
    VisualTilesTogetherApp visualTilesTogetherApp;

    private TileListFragmentListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        visualTilesTogetherApp =  (VisualTilesTogetherApp) getActivity().getApplication();
    }

    public int getBackgroundColorResId() {
        return R.color.colorPrimaryDark;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        final View view = inflater.inflate(R.layout.fragment_tile_list, container, false);
        containerView = view;
        view.setBackgroundColor(ContextCompat.getColor(mContext, getBackgroundColorResId())) ;

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmptyRvList = (TextView) view.findViewById(R.id.tvEmptyRvList);
        mRvTileList = (RecyclerView) view.findViewById(R.id.rvTileList);
        // for now don't light up the ProgressBar
        // TODO: show "no tiles" instead of ProgressBar when tile list is empty`
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // bind the tiles table to the RecyclerView
        mFirebaseAdapter = getAdapter(dbRef);

        // the resource Id for an array of "empty list" strings
        // is passed in through the adapter because
        // the adapter is the one element
        // that every derived class has to provide
        int msgsResId = ((TileListRecyclerViewAdapter)mFirebaseAdapter).getMsgsResId();
        if (msgsResId > 0) {
            mNoTilesMsgs = getResources().getStringArray(msgsResId);
        }
        emptyMessage(mFirebaseAdapter.getItemCount());

        // watch for realtime changes
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mFirebaseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int tileCount = mFirebaseAdapter.getItemCount();
                emptyMessage(mFirebaseAdapter.getItemCount());

                // Manipulate scroll when linear layout (not true for tile select).
                if (mLayoutManager instanceof LinearLayoutManager) {
                    int lastVisiblePosition = ((LinearLayoutManager) mLayoutManager)
                            .findLastCompletelyVisibleItemPosition();
                    if (lastVisiblePosition == -1 ||
                            (positionStart >= (tileCount - 1) &&
                                    lastVisiblePosition == (positionStart - 1))) {
                        mRvTileList.scrollToPosition(positionStart);
                    }
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                emptyMessage(mFirebaseAdapter.getItemCount());
            }
        });

        // hook up the RecyclerView
        mLayoutManager = getLayoutManager();
        mRvTileList.setLayoutManager(mLayoutManager);
        mRvTileList.setAdapter(mFirebaseAdapter);
        return view;
    }

    private void emptyMessage(int tileCount) {
        if (tvEmptyRvList != null) {
            if (tileCount == 0 && mNoTilesMsgs != null) {
                Random r = new Random();
                tvEmptyRvList.setText(mNoTilesMsgs[r.nextInt(mNoTilesMsgs.length)]);
            }
            tvEmptyRvList.setVisibility(tileCount == 0? View.VISIBLE : View.INVISIBLE);
        }
    }

    abstract TileListRecyclerViewAdapter getAdapter(DatabaseReference dbRef);

    RecyclerView.LayoutManager getLayoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        return layoutManager;
    }

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

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (containerView == null) {
                    TileListFragment.this.bitmap = null;
                    return;
                }
                final Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                // Draw must run on UI thread.
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        containerView.draw(canvas);
                        TileListFragment.this.bitmap = bitmap;
                    }
                });
            }
        };
        thread.start();
    }

    public interface TileListFragmentListener {
        void updateSelectedTile(Tile tile);
    }
}
