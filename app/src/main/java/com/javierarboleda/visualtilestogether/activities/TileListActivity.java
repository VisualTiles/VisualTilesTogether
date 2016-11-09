package com.javierarboleda.visualtilestogether.activities;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.models.TileContent;

public class TileListActivity extends AppCompatActivity {

  private static final String TILES_TABLE = "tiles";
  private ProgressBar mProgressBar;
  private RecyclerView mRvTileList;
  private DatabaseReference mFirebaseDatabaseReference;
  private FirebaseRecyclerAdapter<TileContent, TileViewholder> mFirebaseAdapter;
  private FirebaseStorage mFirebaseStorage;
  private Context mContext;
  private LinearLayoutManager mLinearLayoutManager;
  private StorageReference mShapesRef;

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

    mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    mRvTileList = (RecyclerView) findViewById(R.id.rvTileList);

    // get the shapes folder of Firebase Storage for this app
    mFirebaseStorage = FirebaseStorage.getInstance();
    mShapesRef = mFirebaseStorage
        .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
        .child("shapes");

    // this should grab https://visual-tiles-together.firebaseio.com/
    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

    // bind the tiles table to the RecyclerView
    mFirebaseAdapter = new FirebaseRecyclerAdapter<TileContent, TileViewholder>
        (TileContent.class,
            R.layout.tile_list_item,
            TileViewholder.class,
            mFirebaseDatabaseReference.child(TILES_TABLE)) {

      @Override
      protected void populateViewHolder(final TileViewholder viewHolder, final TileContent tile,
                                        int position) {
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
}
