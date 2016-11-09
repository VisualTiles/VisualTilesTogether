package com.javierarboleda.visualtilestogether.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.models.TileContent;
import com.javierarboleda.visualtilestogether.models.User;

public class TileListActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TILES_TABLE = "tiles";
    private static final String ANONYMOUS = "anonymous";
    private static final String LOG_TAG = TileListActivity.class.getSimpleName();
    private static final String USERS_TABLE = "users";

    private ProgressBar mProgressBar;
    private RecyclerView mRvTileList;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<TileContent, TileViewholder> mFirebaseAdapter;
    private FirebaseStorage mFirebaseStorage;
    private Context mContext;
    private LinearLayoutManager mLinearLayoutManager;
    private StorageReference mShapesRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private String mUid;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDbUsers;
    private User mUser;

    public static class TileViewholder extends RecyclerView.ViewHolder {
        public ImageView ivShape;
        public TextView tvYes;
        public TextView tvNo;

        public TileViewholder(View itemView) {
            super(itemView);
            ivShape = (ImageView) itemView.findViewById(R.id.ivShape);
            tvYes = (TextView) itemView.findViewById(R.id.tvYes);
            tvNo = (TextView) itemView.findViewById(R.id.tvNo);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_tile_list);

        // Initialize Firebase Auth
        // Default username is anonymous.
        mUsername = ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUid = mFirebaseUser.getUid();
            mUsername = mFirebaseUser.getDisplayName();
            mUser = User.fromFirebaseUser(mFirebaseUser);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRvTileList = (RecyclerView) findViewById(R.id.rvTileList);

        // get the shapes folder of Firebase Storage for this app
        mFirebaseStorage = FirebaseStorage.getInstance();
        mShapesRef = mFirebaseStorage
                .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
                .child("shapes");

        // this should grab https://visual-tiles-together.firebaseio.com/
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDbUsers = mFirebaseDatabaseReference.child(USERS_TABLE);
        mDbUsers.child(mUid).setValue(mUser);

        // bind the tiles table to the RecyclerView
        mFirebaseAdapter = new FirebaseRecyclerAdapter<TileContent, TileViewholder>
                (TileContent.class,
                        R.layout.tile_list_item,
                        TileViewholder.class,
                        mFirebaseDatabaseReference.child(TILES_TABLE)) {

            @Override
            protected void populateViewHolder(final TileViewholder viewHolder, final TileContent tile, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (tile.getShapeUrl() != null) {
                    Glide.with(mContext)
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
                viewHolder.tvYes.setText(String.valueOf(tile.getPosVotes()));
                viewHolder.tvNo.setText(String.valueOf(tile.getNegVotes()));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
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
