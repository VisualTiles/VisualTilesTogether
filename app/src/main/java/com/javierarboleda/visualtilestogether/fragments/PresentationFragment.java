package com.javierarboleda.visualtilestogether.fragments;

import android.animation.AnimatorSet;
import android.content.Context;
import android.databinding.ObservableMap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.percent.PercentFrameLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Layout;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.TileEffect;
import com.javierarboleda.visualtilestogether.util.PresentationUtil;
import com.javierarboleda.visualtilestogether.util.TileEffectTransformer2;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PresentationFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PresentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresentationFragment extends Fragment
        implements VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String TAG = PresentationFragment.class.getSimpleName();
    private static final String ARG_EDITOR_MODE = "editor_mode";

    private VisualTilesTogetherApp app;
    private boolean isEditorMode;
    private PercentFrameLayout mainLayout;
    private PercentFrameLayout viewContainer;
    private SparseArray<ImageView> imageResourceImageViews;
    private SparseArray<ImageView> tileFrontImageViews;
    private SparseArray<AnimatorSet> tileAnimations;
    private SparseArray<Tile> tileCache;
    private Handler animationHandler = new Handler();
    private int masterEffectDuration = 5000;
    private TileEffect defaultEffect;
    private TileEffectTransformer2 tileEffectTransformer;
    private LayoutInflater inflater;
    private boolean isPaused = true;
    private Integer backgroundColor = null;

    private String layoutId = null;
    private Layout layout;

    private PresentationFragmentListener mListener;

    public PresentationFragment() {
        // Required empty public constructor
    }


    public static PresentationFragment newInstance(boolean isEditorMode) {
        PresentationFragment fragment = new PresentationFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_EDITOR_MODE, isEditorMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = getLayoutInflater(savedInstanceState);
        if (getArguments() != null) {
            isEditorMode = getArguments().getBoolean(ARG_EDITOR_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_presentation, container, false);
        if (app.getUser() == null ||
                app.getChannel() == null) {
            return view;
        }

        imageResourceImageViews = new SparseArray<>();
        tileFrontImageViews = new SparseArray<>();
        tileAnimations = new SparseArray<>();
        tileCache = new SparseArray<>();

        viewContainer = (PercentFrameLayout) view.findViewById(R.id.viewContainer);
        mainLayout = (PercentFrameLayout) view.findViewById(R.id.mainLayout);
        loadChannelData();
        return view;
    }

    private final ValueEventListener layoutChangedEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!dataSnapshot.exists()) {
                layout = Layout.createDemoLayout();
            } else {
                layout = dataSnapshot.getValue(Layout.class);
            }
            drawLayout();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Do nothing at the moment?
        }
    };

    private final ObservableMap.OnMapChangedCallback<ObservableMap<String, Tile>, String, Tile>
            tilesChangedCallback = new ObservableMap.OnMapChangedCallback<ObservableMap<String,
            Tile>, String, Tile>() {
        @Override
        public void onMapChanged(ObservableMap<String, Tile> sender, String key) {
            if (layout == null)
                return;
            final Tile newTile = sender.get(key);
            for (int i = 0; i < layout.getTileCount(); i++) {
                final int position = i;
                Tile tile = tileCache.get(i);
                // NOTE: There can be multiple tiles for each image. Don't break early.
                if (tile != null && tile.getTileId().equals(key)) {
                    // Found the tile that updated. Update it in layout.
                    // I'm pretty sure this event handler doesn't run on the UI thread,
                    // so enqueue it in UI later.
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTile(position, newTile);
                        }
                    });
                }
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PresentationFragmentListener) {
            mListener = (PresentationFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PresentationFragmentListener");
        }
        app = (VisualTilesTogetherApp) getActivity().getApplication();
        app.addListener(this);
        app.getTileObservableArrayMap().addOnMapChangedCallback(tilesChangedCallback);
        if (layoutId != null) {
            FirebaseDatabase.getInstance().getReference(Layout.TABLE_NAME).child(layoutId)
                    .addValueEventListener(layoutChangedEventListener);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        app.removeListener(this);
        isPaused = true;
        app.getTileObservableArrayMap().removeOnMapChangedCallback(tilesChangedCallback);
        if (layoutId != null) {
            FirebaseDatabase.getInstance().getReference(Layout.TABLE_NAME).child(layoutId)
                    .removeEventListener(layoutChangedEventListener);
        }
    }

    public interface PresentationFragmentListener {
        void onTileTapped(int position, Tile tile);
    }

    private void loadTileImage(int position, ImageView view) {
        if (view == null) {
            Log.e(TAG, "Null imageview reference in loadTileImage for position " + position);
            return;
        }
        if (layout == null) {
            Log.e(TAG, "Null layout reference in loadTileImage for position " + position);
            return;
        }
        Tile tile = tileCache.get(position);
        if (tile == null) {
            Log.i(TAG, "Null tile reference in loadTileImage for position " + position);
            // TODO: Maybe expect this null to unload a tile.
            view.setTag(null);
            view.setImageDrawable(new ColorDrawable(fallbackImageColor()));
            return;
        }
        if (tile.getShapeUrl() == null) {
            Log.e(TAG, "Null getShapeUrl reference in loadTileImage for position " + position);
            return;
        }
        Glide.with(this).load(tile.getShapeUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                /*
                .bitmapTransform(new ColorFilterTransformation(
                        getContext(), resolveTileColor(tile)))*/
                // .crossFade((int) app.getChannel().getMasterEffectDuration())
                .into(view);
        view.setColorFilter(new PorterDuffColorFilter(resolveTileColor(tile) | 0xFF000000,
                PorterDuff.Mode.MULTIPLY));
    }

    private int fallbackImageColor() {
        return isEditorMode ? Color.parseColor("#AA660000") : Color.TRANSPARENT;
    }

    private int resolveTileColor(Tile tile) {
        if (tile.getTileColor() != null)
            return tile.getTileColor();
        Integer channelColor = app.getChannel().getDefaultTileColor();
        if (channelColor != null)
            return channelColor;
        // Should always be defined.
        return layout.getDefaultTileColor();
    }

    public void updateTile(final int position, Tile tile) {
        tileCache.put(position, tile);

        if (layout == null) return;
        if (position >= layout.getTileCount()) return;

        loadTileImage(position, tileFrontImageViews.get(position));
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scheduleTileAnimation(position);
            }
        }, getStartDelay());
    }

    private void drawLayout() {
        if (layout == null) return;
        for (int i = 0; i < layout.getTileCount(); i++) {
            ImageView view = tileFrontImageViews.get(i);
            if (view == null) {
                view = buildTileView(i, viewContainer);
                viewContainer.addView(view);
                tileFrontImageViews.put(i, view);
            }
            PresentationUtil.moveRelativeView(layout, view, layout.getTilePositions().get(i));
            loadTileImage(i, view);
            scheduleTileAnimation(i);
        }
        for (int i = 0; i < layout.getImageCount(); i++) {
            ImageView view = imageResourceImageViews.get(i);
            if (view == null) {
                view = buildTileView(i, viewContainer);
                viewContainer.addView(view);
                imageResourceImageViews.put(i, view);
            }
            PresentationUtil.moveRelativeView(layout, view, layout.getImagePositions().get(i));
            String imgUrl = layout.getImageUrls().get(i);
            if (imgUrl != null) {
                Glide.with(this).load(imgUrl)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        // .crossFade((int) app.getChannel().getMasterEffectDuration())
                        .into(view);
            }
        }

        List<Integer> tileRemovals = new ArrayList<>();
        for (int i = 0; i < tileFrontImageViews.size(); i++) {
            Integer key = tileFrontImageViews.keyAt(i);
            ImageView view = tileFrontImageViews.get(key);
            if (key >= 0 && key < layout.getTileCount()) {
                break;
            }
            if (tileAnimations.get(key) != null) {
                tileAnimations.get(key).removeAllListeners();
                tileAnimations.get(key).end();
                tileAnimations.get(key).cancel();
                tileAnimations.remove(key);
            }
            viewContainer.removeView(view);
            tileRemovals.add(key);
        }
        for (Integer i : tileRemovals) {
            tileFrontImageViews.remove(i);
        }
        List<Integer> imageRemovals = new ArrayList<>();
        for (int i = 0; i < imageResourceImageViews.size(); i++) {
            Integer key = imageResourceImageViews.keyAt(i);
            ImageView view = imageResourceImageViews.get(key);
            if (key >= 0 && key < layout.getImageCount())
                break;
            viewContainer.removeView(view);
            imageRemovals.add(key);
        }
        for (Integer i : imageRemovals) {
            imageResourceImageViews.remove(i);
        }
        if (backgroundColor == null) {
            String backgroundUrl = layout.getBackgroundUrl();
            if (backgroundUrl != null) {
                Glide.with(this).load(backgroundUrl)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .placeholder(new ColorDrawable(layout.getBackgroundColor()))
                        .into(new ViewTarget<View, GlideDrawable>(mainLayout) {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                this.view.setBackground(resource);
                            }
                        });
            }
        } else {
            mainLayout.setBackgroundColor(backgroundColor);
        }
        viewContainer.invalidate();
    }

    private void loadChannelData() {
        Channel channel = app.getChannel();
        masterEffectDuration = (int) channel.getMasterEffectDuration();
        tileEffectTransformer = new TileEffectTransformer2(getContext(), masterEffectDuration);
        boolean needsDraw = false;
        if (channel.getChannelBackgroundColor() != null &&
                !channel.getChannelBackgroundColor().equals(backgroundColor))
            needsDraw = true;
        backgroundColor = channel.getChannelBackgroundColor();

        String newLayoutId = app.getChannel().getLayoutId();
        if (newLayoutId != null && !newLayoutId.isEmpty()) {
            if (this.layoutId != null) {
                if (!this.layoutId.equals(newLayoutId)) {
                    FirebaseDatabase.getInstance().getReference(Layout.TABLE_NAME).child(this.layoutId)
                            .removeEventListener(layoutChangedEventListener);
                    FirebaseDatabase.getInstance().getReference(Layout.TABLE_NAME).child(newLayoutId)
                            .addValueEventListener(layoutChangedEventListener);
                }
            } else {
                FirebaseDatabase.getInstance().getReference(Layout.TABLE_NAME).child(newLayoutId)
                        .addValueEventListener(layoutChangedEventListener);
            }
            this.layoutId = newLayoutId;
        } else {
            // Channel doesn't have a layout ID. Use demo.
            layout = Layout.createDemoLayout();
            needsDraw = true;
        }

        // Lightweight image updater. Not necessary because drawLayout does this too.
        ArrayList<String> posToTileIds = channel.getPositionToTileIds();
        if (posToTileIds == null)
            return;
        for (int i = 0; i < posToTileIds.size(); i++) {
            String tileId = posToTileIds.get(i);
            // Unset fields are markers.
            // TODO(team): Load a placeholder image in this case?
            if (tileId == null || tileId.isEmpty()) {
                updateTile(i, null);
            }
            Tile tile = app.getTileObservableArrayMap().get(tileId);
            updateTile(i, tile);
        }

        if (needsDraw) drawLayout();
        defaultEffect = channel.getDefaultEffect();
        resyncAndStartAnimation();
    }

    private ImageView buildTileView(final int position, ViewGroup root) {
        // Don't pass 'root' or else 'root' will be returned from .inflate. (Dumb.).
        ImageView iv = (ImageView) inflater.inflate(R.layout.iv_tile, null);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onTileTapped(position, tileCache.get(position));
            }
        });
        return iv;
    }

    private long getStartDelay() {
        return 0;
        /*
        long syncTime = app.getChannel().getChannelSyncTime();
        long time = AnimationUtils.currentAnimationTimeMillis();
        long delay = (time - syncTime) % masterEffectDuration;
        return delay;
        */
    }

    private Runnable animationRunner = new Runnable() {
        @Override
        public void run() {
            // Transformer Animators are infinitely repeating now.
            // Keep heartbeat going regardless of if the layout is ready.
            // animationHandler.postDelayed(animationRunner, masterEffectDuration);

            if (layout == null) return;
            for (int i = 0; i < layout.getTileCount(); i++) {
                scheduleTileAnimation(i);
            }
        }
    };

    private void scheduleTileAnimation(int i) {
        ImageView tileIv = tileFrontImageViews.get(i);
        if (tileIv == null)
            return;
        Tile loadedTile = tileCache.get(i);
        if (loadedTile == null)
            return;
        // When this function is called, rebuild animation set.
        AnimatorSet animatorSet = tileAnimations.get(i);
        if (animatorSet != null) {
            animatorSet.removeAllListeners();
            animatorSet.end();
            animatorSet.cancel();
        }
        tileIv.requestLayout();
        tileIv.invalidate();

        animatorSet = new AnimatorSet();
        // The caller of this function now handles it.
        animatorSet.setInterpolator(new LinearInterpolator());
        boolean hasEffect = loadedTile.getTileEffect() != null;
        if (hasEffect) {
            tileEffectTransformer.processTileEffect(animatorSet, tileIv,
                    loadedTile.getTileEffect());
        } else {
            tileEffectTransformer.processTileEffect(animatorSet, tileIv,
                    defaultEffect);
        }
        animatorSet.start();
        tileAnimations.put(i, animatorSet);
    }

    private void resyncAndStartAnimation() {
        animationHandler.postDelayed(animationRunner, getStartDelay());
        // Note: This function always waits an entire revolution... maybe have it run in the past?
        // tile animations will need to be seeded with delay - duration as their start time.
    }

    @Override
    public void onChannelUpdated() {
        loadChannelData();
    }

    @Override
    public void onError(DatabaseError error) {
        // Activity handles errors.
    }

    @Override
    public void onTilesUpdated() {
        // This is too inefficient since it doesn't return what tile changed. Listens to tile
        // changes are handled by setupTileListListener.
    }

    @Override
    public void onUserUpdated() {
        // Activity is responsible.
    }
}
