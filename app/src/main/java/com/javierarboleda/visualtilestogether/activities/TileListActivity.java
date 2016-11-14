package com.javierarboleda.visualtilestogether.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.User;

import static com.javierarboleda.visualtilestogether.VisualTilesTogetherApp.getFirebaseAuth;
import static com.javierarboleda.visualtilestogether.VisualTilesTogetherApp.getUid;
import static com.javierarboleda.visualtilestogether.VisualTilesTogetherApp.getUser;
import static com.javierarboleda.visualtilestogether.VisualTilesTogetherApp.resetUserame;

public class TileListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG_TAG = TileListActivity.class.getSimpleName();

    private ProgressBar mProgressBar;
    private RecyclerView mRvTileList;
    private FirebaseRecyclerAdapter<Tile, TileViewholder> mFirebaseAdapter;
    private Context mContext;
    private LinearLayoutManager mLinearLayoutManager;
    private StorageReference mShapesRef;
    private GoogleApiClient mGoogleApiClient;

    public static class TileViewholder extends RecyclerView.ViewHolder {
        ImageView ivShape;
        ImageButton ibUpVote;
        ImageButton ibDownVote;
        TextView tvVotesTotal;

        public TileViewholder(View itemView) {
            super(itemView);
            ivShape = (ImageView) itemView.findViewById(R.id.ivShape);
            ibUpVote = (ImageButton) itemView.findViewById(R.id.ibUpVote);
            ibDownVote = (ImageButton) itemView.findViewById(R.id.ibDownVote);
            tvVotesTotal = (TextView) itemView.findViewById(R.id.tvVotesTotal);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        final Context appContext = getApplicationContext();
        setContentView(R.layout.activity_tile_list);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TileListActivity.this, TileCreationActivity.class));
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRvTileList = (RecyclerView) findViewById(R.id.rvTileList);

        // get the shapes folder of Firebase Storage for this app
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        mShapesRef = mFirebaseStorage
                .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
                .child("shapes");

        // this should grab https://visual-tiles-together.firebaseio.com/
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dbUsers = dbRef.child(User.TABLE_NAME);
        dbUsers.child(getUid()).setValue(getUser());

        // bind the tiles table to the RecyclerView
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Tile, TileViewholder>
                (Tile.class,
                        R.layout.tile_list_item,
                        TileViewholder.class,
                        dbRef.child(Tile.TABLE_NAME)
                                .orderByChild(Tile.CHANNEL_ID)
                                .equalTo(VisualTilesTogetherApp.getUser().getChannelId())) {

            @Override
            protected void populateViewHolder(
                    final TileViewholder viewHolder, final Tile tile, int position) {
                final DatabaseReference tileRef = getRef(position);

                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (tile.getShapeUrl() != null) {
                    Glide.with(appContext)
                            .load(tile.getShapeUrl())
                            .into(viewHolder.ivShape);
                }
                if (tile.getShapeFbStorage() != null) {
                    mShapesRef
                            .child(tile.getShapeFbStorage())
                            .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(mContext)
                                    .load(uri)
                                    .into(viewHolder.ivShape);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, "shapeFpStorage \"" + tile.getShapeFbStorage() + "\" no workee", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });

                }
                viewHolder.ibUpVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onVoteClicked(tileRef, 1);
                    }
                });
                viewHolder.ibDownVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onVoteClicked(tileRef, -1);
                    }
                });
                viewHolder.tvVotesTotal.setText(String.valueOf(tile.getPosVotes() - tile.getNegVotes()));
            }
        };

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
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRvTileList.setLayoutManager(mLinearLayoutManager);
        mRvTileList.setAdapter(mFirebaseAdapter);
    }

    // run a transaction to uptick positive votes or negative votes
    // depending on the value of the vote increment
    private void onVoteClicked(DatabaseReference tileRef, final int voteIncrement) {
        tileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Tile tile = mutableData.getValue(Tile.class);
                if (tile == null) {
                    return Transaction.success(mutableData);
                }

                if (voteIncrement > 0) {
                    tile.setPosVotes(tile.getPosVotes() + voteIncrement);
                } else if (voteIncrement < 0) {
                    // actually increments
                    tile.setNegVotes(tile.getNegVotes() - voteIncrement);
                }

                mutableData.setValue(tile);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_present:
                startActivity(new Intent(this, PresentationActivity.class));
                return true;
            case R.id.menu_item_sign_out:
                getFirebaseAuth().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                resetUserame();
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
