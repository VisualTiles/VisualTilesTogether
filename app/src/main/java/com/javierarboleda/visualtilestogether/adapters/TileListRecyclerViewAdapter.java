package com.javierarboleda.visualtilestogether.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.models.Tile;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.deleteTile;
import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.toggleTileApproval;

/**
 * Created by geo on 11/22/16.
 */
public class TileListRecyclerViewAdapter extends FirebaseRecyclerAdapter<Object, TileListRecyclerViewAdapter.TileViewholder> {

    private final Context mContext;
    TileListFragment.TileListFragmentListener mListener;
    private VisualTilesTogetherApp mVisualTilesTogetherApp;
    private int mLastPosition;
    private RecyclerView mRecyclerView;


    public TileListRecyclerViewAdapter(Context context,
                                       int itemLayout,
                                       Query query,
                                       VisualTilesTogetherApp visualTilesTogetherApp) {
        super(Object.class,
                itemLayout,
                TileViewholder.class,
                query);
        mContext = context;
        mListener = (TileListFragment.TileListFragmentListener) context;
        mVisualTilesTogetherApp = visualTilesTogetherApp;
    }

    @Override
    protected void populateViewHolder(
            final TileViewholder viewHolder, final Object object, int position) {

        viewHolder.itemView.startAnimation(getAnimation(position));
        mLastPosition = position;
        // if the key starts with a '-' then it must be a tileId...
        if (getRef(position).getKey().charAt(0) == '-') {
            doTheWork(viewHolder, getRef(position).getKey());
        } else { // ...otherwise the tileId is the value, which we now have to go get
            getRef(position).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    doTheWork(viewHolder, dataSnapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private Animation getAnimation(int position) {
        int orientation = ((LinearLayoutManager)mRecyclerView.getLayoutManager()).getOrientation();
        if (orientation == LinearLayoutManager.VERTICAL) {
            return AnimationUtils.loadAnimation(mContext,
                    (position > mLastPosition) ? R.anim.in_from_bottom
                            : R.anim.in_from_top);
        } else {
            return AnimationUtils.loadAnimation(mContext,
                    (position > mLastPosition) ? R.anim.in_from_right
                            : R.anim.in_from_left);
        }
    }

    protected void doTheWork(final TileViewholder viewHolder, final String tileId) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        viewHolder.tileRef = dbRef.child(Tile.TABLE_NAME)
                .child(tileId);
//                Log.d(LOG_TAG, "populateViewHolder key, tileref: " + getRef(position).getKey() + ", " + viewHolder.tileRef);

        if (viewHolder.tileEventListener != null) {
            viewHolder.tileRef.removeEventListener(viewHolder.tileEventListener);
        }
        viewHolder.tileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.tile = dataSnapshot.getValue(Tile.class);
//                        Log.d(LOG_TAG, "onDataChange tile: " + tile);
                if (viewHolder.tile == null) {
                    return;
                }
                if (viewHolder.tile.getShapeUrl() != null && mContext != null) {
                    Glide.with(mContext.getApplicationContext())
                            .load(viewHolder.tile.getShapeUrl())
                            .into(viewHolder.ivShape);
                }

                viewHolder.ibUpVote.setEnabled(true);
                viewHolder.ibDownVote.setEnabled(true);

                final String userId = mVisualTilesTogetherApp.getUid();

                if (dataSnapshot.child(Tile.USER_VOTES).child(userId).exists()) {
                    boolean userVote = dataSnapshot.child(Tile.USER_VOTES)
                            .child(userId)
                            .getValue(boolean.class);
                    if (userVote) {
                        // the user has already voted "yes"
                        viewHolder.ibUpVote.setEnabled(false);
                    } else {
                        // the user has already voted "no"
                        viewHolder.ibDownVote.setEnabled(false);
                    }
                }

                viewHolder.ibUpVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (viewHolder.ibDownVote.isEnabled()) {
                            // the user has voted "yes"
                            onVoteClicked(viewHolder.tileRef, 1, 0);
                            viewHolder.tileRef.child(Tile.USER_VOTES)
                                    .child(userId)
                                    .setValue(true);
                        } else {
                            // the user has retracted a "no" vote
                            onVoteClicked(viewHolder.tileRef, 0, -1);
                            viewHolder.tileRef.child(Tile.USER_VOTES)
                                    .child(userId)
                                    .removeValue();
                        }
                    }
                });

                viewHolder.ibDownVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (viewHolder.ibUpVote.isEnabled()) {
                            // the user has voted "no"
                            onVoteClicked(viewHolder.tileRef, 0, 1);
                            viewHolder.tileRef.child(Tile.USER_VOTES)
                                    .child(userId)
                                    .setValue(false);
                        } else {
                            // the user has retracted a "yes" vote
                            onVoteClicked(viewHolder.tileRef, -1, 0);
                            viewHolder.tileRef.child(Tile.USER_VOTES)
                                    .child(userId)
                                    .removeValue();
                        }
                    }
                });

                viewHolder.tvVotesTotal.setText(String.valueOf(viewHolder.tile.getPosVotes()
                        - viewHolder.tile.getNegVotes()));

                if (mVisualTilesTogetherApp.isChannelModerator()) {
                    viewHolder.miPublish.setVisible(true);
                    viewHolder.miPublish.setIcon(viewHolder.tile.isApproved() ?
                            R.drawable.ic_unpublish_black_24px : R.drawable.ic_publish_black_24px);
                    viewHolder.miDelete.setVisible(true);
                } else {
                    viewHolder.miPublish.setVisible(false);
                    viewHolder.miDelete.setVisible(userId.equals(viewHolder.tile.getCreatorId()));
                }
                viewHolder.tbTileListItem.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
//                                Log.d(LOG_TAG, "Clicked a toolbar item");
                        switch (item.getItemId()) {
                            case R.id.action_publish:
                                toggleTileApproval(viewHolder.tileRef);
                                return true;
                            case R.id.action_delete:
                                deleteTile(viewHolder.tileRef,
                                        tileId,
                                        viewHolder.tile.getChannelId(),
                                        viewHolder.tile.getCreatorId());
                                return true;
                        }
                        return false;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        viewHolder.tileRef.addValueEventListener(viewHolder.tileEventListener);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onViewDetachedFromWindow(TileViewholder holder) {
        // Prevent problems when fast scrolling due to
        // the view being reused while the animation is happening
        holder.itemView.clearAnimation();
        super.onViewDetachedFromWindow(holder);
    }

    // run a transaction to uptick positive votes or negative votes
    // depending on the value of the vote increment
    private void onVoteClicked(DatabaseReference tileRef, final int posVote, final int negVote) {
        tileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Tile tile = mutableData.getValue(Tile.class);
                if (tile == null) {
                    return Transaction.success(mutableData);
                }

                tile.setPosVotes(tile.getPosVotes() + posVote);
                tile.setNegVotes(tile.getNegVotes() + negVote);

                mutableData.setValue(tile);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
//                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
            }
        });
    }

    public static class TileViewholder extends RecyclerView.ViewHolder {
        ImageView ivShape;
        ImageButton ibUpVote;
        ImageButton ibDownVote;
        TextView tvVotesTotal;
        Toolbar tbTileListItem;
        MenuItem miPublish;
        MenuItem miDelete;
        ValueEventListener tileEventListener;
        RelativeLayout rlMain;
        Tile tile;
        DatabaseReference tileRef;

        public TileViewholder(View itemView) {
            super(itemView);
            ivShape = (ImageView) itemView.findViewById(R.id.ivShape);
            ibUpVote = (ImageButton) itemView.findViewById(R.id.ibUpVote);
            ibDownVote = (ImageButton) itemView.findViewById(R.id.ibDownVote);
            tvVotesTotal = (TextView) itemView.findViewById(R.id.tvVotesTotal);
            tbTileListItem = (Toolbar) itemView.findViewById((R.id.tbTileListItem));
            tbTileListItem.inflateMenu(R.menu.tile_list_menu);
            miPublish = tbTileListItem.getMenu().findItem(R.id.action_publish);
            miDelete = tbTileListItem.getMenu().findItem(R.id.action_delete);
            rlMain = (RelativeLayout) itemView.findViewById(R.id.rlMain);
            tileEventListener = null;
        }
    }
}
