package com.javierarboleda.visualtilestogether.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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
import com.javierarboleda.visualtilestogether.models.User;
import com.javierarboleda.visualtilestogether.util.FirebaseUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.deleteTile;
import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.toggleTileApproval;

/**
 * Created by geo on 11/22/16.
 */
public class TileListRecyclerViewAdapter extends FirebaseRecyclerAdapter<Object, TileListRecyclerViewAdapter.TileViewHolder> {
    private static final String TAG = TileListRecyclerViewAdapter.class.getSimpleName();
    private final int mMsgsResId;
    private Context mContext;
    TileListFragment.TileListFragmentListener mListener;
    private VisualTilesTogetherApp app;
    private int mLastPosition = -1;
    private RecyclerView mRecyclerView;
    private static final int CARD_ANIMATION_DURATION_MS = 400;

    public TileListRecyclerViewAdapter(Context context,
                                       int itemLayout,
                                       Query query,
                                       int msgsResId,
                                       VisualTilesTogetherApp visualTilesTogetherApp) {
        super(Object.class,
                itemLayout,
                TileViewHolder.class,
                query);
        mContext = context;
        mListener = (TileListFragment.TileListFragmentListener) context;
        mMsgsResId = msgsResId;
        app = visualTilesTogetherApp;
    }

    public int getMsgsResId() {
        return mMsgsResId;
    }

    @Override
    protected void populateViewHolder(
            final TileViewHolder viewHolder, final Object object, int position) {

        if (viewHolder.itemView.isSelected()) {
            viewHolder.itemView.setSelected(false);
            if (viewHolder.itemView instanceof CardView) {
                CardView card = (CardView) viewHolder.itemView;
                card.setCardBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.cardViewBackgroundColor));
            }
        }
        if (viewHolder.bubbleMenu != null)
            viewHolder.bubbleMenu.setVisibility(View.INVISIBLE);

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
        boolean isVertical = false;
    RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        if (lm instanceof  LinearLayoutManager) {
            isVertical = ((LinearLayoutManager) lm).getOrientation() == LinearLayoutManager
                    .VERTICAL;
        }
        if (lm instanceof StaggeredGridLayoutManager) {
            isVertical = ((StaggeredGridLayoutManager) lm).getOrientation() ==
                    StaggeredGridLayoutManager.VERTICAL;
        }
        if (isVertical) {
            return AnimationUtils.loadAnimation(mContext,
                    (position > mLastPosition) ? R.anim.in_from_bottom
                            : R.anim.in_from_top);
        } else {
            return AnimationUtils.loadAnimation(mContext,
                    (position > mLastPosition) ? R.anim.in_from_right
                            : R.anim.in_from_left);
        }
    }

    protected void doTheWork(final TileViewHolder viewHolder, final String tileId) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        viewHolder.tileRef = dbRef.child(Tile.TABLE_NAME)
                .child(tileId);
        if (viewHolder.tileEventListener != null) {
            viewHolder.tileRef.removeEventListener(viewHolder.tileEventListener);
        }
        if (viewHolder.ibUpVote != null)
            viewHolder.ibUpVote.setEnabled(true);
        if (viewHolder.ibDownVote != null)
            viewHolder.ibDownVote.setEnabled(true);
        viewHolder.itemView.setSelected(false);
        viewHolder.itemView.setOnClickListener(buildTileClickListener(viewHolder));
        viewHolder.tileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    final String userId = app.getUid();
                    viewHolder.tile = dataSnapshot.getValue(Tile.class);
                    if (viewHolder.tile == null) {
                        return;
                    }
                    if (viewHolder.tile.getShapeUrl() != null && mContext != null) {
                        Glide.with(mContext.getApplicationContext())
                                .load(viewHolder.tile.getShapeUrl())
                                .into(viewHolder.ivShape);
                    }
                    if (viewHolder.ibUpVote != null) {
                        viewHolder.ibUpVote.setEnabled(true);
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
                    }
                    if (viewHolder.ibDownVote != null) {
                        viewHolder.ibDownVote.setEnabled(true);
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
                    }

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

                    viewHolder.tvVotesTotal.setText(String.valueOf(viewHolder.tile.getPosVotes()
                            - viewHolder.tile.getNegVotes()));
                    if (app.isChannelModerator()) {
                        if (viewHolder.btnPublish != null) {
                            viewHolder.btnPublish.setVisibility(View.VISIBLE);
                            viewHolder.btnPublish.setImageResource(viewHolder.tile.isApproved() ?
                                    R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp);
                        }
                        if (viewHolder.btnPublish2 != null) {
                            viewHolder.btnPublish2.setVisibility(View.VISIBLE);
                            viewHolder.btnPublish2.setImageResource(viewHolder.tile.isApproved() ?
                                    R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp);
                        }

                        if (viewHolder.btnShare != null) {
                            viewHolder.btnShare.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    shareTile(viewHolder.tile);
                                }
                            });
                        }
                        if (viewHolder.btnDelete != null)
                            viewHolder.btnDelete.setVisibility(View.VISIBLE);
                    } else {
                        if (viewHolder.btnPublish != null)
                            viewHolder.btnPublish.setVisibility(View.GONE);
                        if (viewHolder.btnPublish2 != null)
                            viewHolder.btnPublish2.setVisibility(View.GONE);
                        if (viewHolder.btnDelete != null)
                            viewHolder.btnDelete.setVisibility(
                                    userId.equals(viewHolder.tile.getCreatorId()) ? View.VISIBLE : View.GONE);
                    }
                    if (viewHolder.btnDelete != null) {
                        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (viewHolder.tile == null) {
                                    Toast.makeText(mContext, "Delete failed. Try again.", Toast
                                            .LENGTH_SHORT).show();
                                    return;
                                }
                                deleteTile(viewHolder.tileRef,
                                        tileId,
                                        app,
                                        app.getChannelId(),
                                        viewHolder.tile.getCreatorId());
                            }
                        });
                    }
                    if (viewHolder.btnPublish != null) {
                        viewHolder.btnPublish.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                toggleTileApproval(viewHolder.tileRef);
                            }
                        });
                    }
                    if (viewHolder.btnPublish2 != null) {
                        viewHolder.btnPublish2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                toggleTileApproval(viewHolder.tileRef);
                            }
                        });
                    }
                    // Clean out old photo load.
                    if (viewHolder.photoListener != null)
                        dbRef.removeEventListener(viewHolder.photoListener);
                    if (viewHolder.ivCreatorImage != null)
                        viewHolder.ivCreatorImage.setVisibility(View.INVISIBLE);

                    String creator = viewHolder.tile.getCreatorId();
                    if (viewHolder.ivCreatorImage != null && creator != null
                            && !creator.isEmpty()) {
                        viewHolder.photoListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Release reference.
                                viewHolder.photoListener = null;
                                if (dataSnapshot.exists()) {
                                    User tileCreator = dataSnapshot.getValue(User.class);
                                    if (tileCreator.getPhotoUrl() != null &&
                                            !tileCreator.getPhotoUrl().isEmpty()) {
                                        viewHolder.ivCreatorImage.setVisibility(View.VISIBLE);
                                        Glide.with(mContext.getApplicationContext()).load(tileCreator.getPhotoUrl())
                                                .bitmapTransform(new CropCircleTransformation(mContext))
                                                .animate(R.anim.zoom_in)
                                                .into(viewHolder.ivCreatorImage);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };
                        dbRef.child(User.TABLE_NAME).child(creator).addListenerForSingleValueEvent(
                                viewHolder.photoListener);
                    }
                } catch (RuntimeException ex) {
                    Log.w(TAG, "Runtime exception occurred: "
                            + ex.getMessage());
                }
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
        mContext = recyclerView.getContext();
    }

    private void shareTile(Tile tile) {
        // Extract Bitmap from ImageView drawable
        Glide.with(mContext).load(tile.getShapeUrl()).asBitmap().into(
                new SimpleTarget<Bitmap>(400, 400) {
            @Override
            public void onResourceReady(Bitmap resource,
                                        GlideAnimation<? super Bitmap> glideAnimation) {
                if (resource == null)
                    return;
                Uri bmpUri = getBitmapUri(resource);
                // Construct share intent as described above based on bitmap
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                String deepLink = "";
                if (app != null && app.getChannel() != null) {
                    deepLink = FirebaseUtil.buildChannelDeepLink(mContext,
                            app.getChannel().getUniqueName());
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                    String.format("This tile was seen in Visual Tiles! Join the show: %s",
                            deepLink));
                shareIntent.setType("image/*");
                mContext.startActivity(Intent.createChooser(shareIntent, "Share Tile using"));
            }
        });
    }

    @Override
    public void onViewDetachedFromWindow(TileViewHolder holder) {
        // Prevent problems when fast scrolling due to
        // the view being reused while the animation is happening
        if (holder.itemView != null)
            holder.itemView.clearAnimation();
        if (holder.ivCreatorImage != null)
            holder.ivCreatorImage.clearAnimation();
        // Clean up event handlers.
        if (holder.photoListener != null)
            FirebaseDatabase.getInstance().getReference().removeEventListener(holder.photoListener);
        if (holder.tileEventListener != null)
            FirebaseDatabase.getInstance().getReference().removeEventListener(
                    holder.tileEventListener);
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

    public static class TileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivShape;
        ImageButton ibUpVote;
        ImageButton ibDownVote;
        TextView tvVotesTotal;
        ValueEventListener tileEventListener;
        ViewGroup rlMain;
        Tile tile;
        DatabaseReference tileRef;
        ImageView ivCreatorImage;
        ValueEventListener photoListener;
        View btnDelete;
        ImageButton btnPublish;
        ImageView btnPublish2;
        View bubbleMenu;
        ImageView btnShare;

        public TileViewHolder(View itemView) {
            super(itemView);
            try {
                ivShape = (ImageView) itemView.findViewById(R.id.ivShape);
                ibUpVote = (ImageButton) itemView.findViewById(R.id.ibUpVote);
                ibDownVote = (ImageButton) itemView.findViewById(R.id.ibDownVote);
                tvVotesTotal = (TextView) itemView.findViewById(R.id.tvVotesTotal);
                rlMain = (ViewGroup) itemView.findViewById(R.id.rlMain);
                tileEventListener = null;
                ivCreatorImage = (ImageView) itemView.findViewById(R.id.ivCreatorImage);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnPublish = (ImageButton) itemView.findViewById(R.id.btnPublish);
                btnPublish2 = (ImageView)itemView.findViewById(R.id.btnPublish2);
                btnShare = (ImageView)itemView.findViewById(R.id.btnShare);
                bubbleMenu = itemView.findViewById(R.id.bubbleMenu);
            } catch (RuntimeException ex) {
                // This catch happens when you click on a nav menu item while scrolling.
                Log.e(TAG, "The user probably tried to switch acitivites while scrolling.");
                // Don't handle.
            }
        }
    }

    public View.OnClickListener buildTileClickListener(
            final TileViewHolder viewHolder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.bubbleMenu == null || viewHolder.itemView == null)
                    return;
                CardView card = (CardView) viewHolder.itemView;
                if (viewHolder.itemView.isSelected()) {
                    /* Menu closed. */
                    ObjectAnimator.ofObject(viewHolder.itemView,
                            "cardBackgroundColor", new ArgbEvaluator(),
                            card.getCardBackgroundColor().getDefaultColor(),
                            ContextCompat.getColor(mContext, R.color.cardViewBackgroundColor))
                            .setDuration(CARD_ANIMATION_DURATION_MS).start();
                    viewHolder.bubbleMenu.setVisibility(View.INVISIBLE);
                    viewHolder.bubbleMenu.setAlpha(1f);
                    viewHolder.bubbleMenu.animate().alpha(0.1f)
                            .setDuration(CARD_ANIMATION_DURATION_MS).start();
                    viewHolder.itemView.setSelected(false);
                } else {
                    /* Menu open. */
                    ObjectAnimator.ofObject(viewHolder.itemView,
                            "cardBackgroundColor", new ArgbEvaluator(),
                            card.getCardBackgroundColor().getDefaultColor(),
                            ContextCompat.getColor(mContext,
                                    R.color.cardViewSelectedBackgroundColor))
                                    .setDuration(CARD_ANIMATION_DURATION_MS).start();
                    viewHolder.bubbleMenu.setVisibility(View.VISIBLE);
                    viewHolder.bubbleMenu.setAlpha(0.1f);
                    viewHolder.bubbleMenu.animate().alpha(1f)
                            .setDuration(CARD_ANIMATION_DURATION_MS).start();
                    viewHolder.itemView.setSelected(true);
                }
            }
        };
    }

    public Uri getBitmapUri(Bitmap bmp) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Background fill paint.
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            Bitmap bitmapResult = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapResult);
            canvas.drawRect(0, 0, 400, 400, paint);
            canvas.drawBitmap(bmp, 0, 0, paint);

            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(
                    mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmapResult.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(
                    mContext,
                    "com.javierarboleda.visualtilestogether.fileprovider",
                    file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

}
