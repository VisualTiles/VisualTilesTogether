package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.databinding.ObservableMap;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.percent.PercentFrameLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.BuildConfig;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Layout;
import com.javierarboleda.visualtilestogether.models.Rect;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.TileEffect;
import com.javierarboleda.visualtilestogether.util.TileEffectTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    private HashMap<Integer, ImageView> imageResourceImageViews;
    private HashMap<Integer, ImageView> tileFrontImageViews;
    private HashMap<Integer, Animation> tileAnimations;
    private HashMap<Integer, Tile> tileCache;
    private Animation defaultAnimation;
    private Handler animationHandler = new Handler();
    private int masterEffectDuration = 5000;
    private TileEffect defaultEffect;
    private TileEffectTransformer tileEffectTransformer;
    private LayoutInflater inflater;
    private boolean isPaused = true;

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

        imageResourceImageViews = new HashMap<>();
        tileFrontImageViews = new HashMap<>();
        tileAnimations = new HashMap<>();
        tileCache = new HashMap<>();

        viewContainer = (PercentFrameLayout) view.findViewById(R.id.viewContainer);
        mainLayout = (PercentFrameLayout) view.findViewById(R.id.mainLayout);
        loadChannelData();
        setupTileListListener();
        return view;
    }

    private ValueEventListener layoutChangedEventListener = new ValueEventListener() {
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

    private void setupTileListListener() {
        app.getTileObservableArrayMap().addOnMapChangedCallback(
                new ObservableMap.OnMapChangedCallback<ObservableMap<String, Tile>, String, Tile>() {
                    @Override
                    public void onMapChanged(ObservableMap<String, Tile> sender, String key) {
                        if (layout == null)
                            return;
                        final Tile newTile = sender.get(key);
                        for (int i = 0; i < layout.getTileCount(); i++) {
                            final int position = i;
                            Tile tile = tileCache.get(i);
                            if (tile != null && tile.getTileId().equals(key)) {
                                // Found the tile that updated. Update it in layout.
                                // I'm pretty sure this event handler doesn't run on the UI thread,
                                // so enqueue it in UI later.
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateTile(position, newTile, true);
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PresentationFragmentListener) {
            mListener = (PresentationFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        app = (VisualTilesTogetherApp) getActivity().getApplication();
        app.addListener(this);
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
        if (layoutId != null) {
            FirebaseDatabase.getInstance().getReference(Layout.TABLE_NAME).child(layoutId)
                    .removeEventListener(layoutChangedEventListener);
        }
    }

    public interface PresentationFragmentListener {
        void onTileTapped(int position, Tile tile);
    }

    private void moveRelativeView(View view, Rect bounds) {
        if (view == null) {
            Log.e(TAG, "moveRelativeView called on null view. >:(");
            return;
        }
        if (BuildConfig.DEBUG && !(view.getLayoutParams()
                instanceof PercentFrameLayout.LayoutParams)) {
            throw new RuntimeException("View is not within a PercentRelativeLayout");
        }
        PercentFrameLayout.LayoutParams params = (PercentFrameLayout.LayoutParams)
                view.getLayoutParams();
        int height = layout.getLayoutHeight();
        int width = layout.getLayoutWidth();
        params.getPercentLayoutInfo().topMarginPercent = bounds.top / (float) height;
        params.getPercentLayoutInfo().leftMarginPercent = bounds.left / (float) width;
        params.getPercentLayoutInfo().heightPercent = (bounds.bottom - bounds.top) / (float) height;
        params.getPercentLayoutInfo().widthPercent = (bounds.right - bounds.left) / (float) width;
        view.invalidate();
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
            view.setImageResource(android.R.color.transparent);
            return;
        }
        if (tile.getShapeUrl() == null) {
            Log.e(TAG, "Null getShapeUrl reference in loadTileImage for position " + position);
            return;
        }
        Glide.with(this).load(tile.getShapeUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade((int) app.getChannel().getMasterEffectDuration())
                .into(view);
    }

    public void updateTile(int position, Tile tile, boolean runNow) {
        tileCache.put(position, tile);

        if (layout == null) return;
        if (position >= layout.getTileCount()) return;

        loadTileImage(position, tileFrontImageViews.get(position));
        // Fire animation onw because the effect is the only mutable field.
        // TODO(team): Adjust tile start to reflect its delta from master channel start time.
        scheduleTileAnimation(position, true, runNow);
    }

    private void drawLayout() {
        if (layout == null) return;
        for (int i = 0; i < layout.getTileCount(); i++) {
            ImageView view = tileFrontImageViews.get(i);
            if (view == null) {
                view = buildTileView(i);
                viewContainer.addView(view);
                tileFrontImageViews.put(i, view);
            }
            moveRelativeView(view, layout.getTilePositions().get(i));
            loadTileImage(i, view);
        }
        for (int i = 0; i < layout.getImageCount(); i++) {
            ImageView view = imageResourceImageViews.get(i);
            if (view == null) {
                view = buildTileView(i);
                viewContainer.addView(view);
                imageResourceImageViews.put(i, view);
            }
            moveRelativeView(view, layout.getImagePositions().get(i));
            String imgUrl = layout.getImageUrls().get(i);
            if (imgUrl != null) {
                Glide.with(this).load(imgUrl)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade((int) app.getChannel().getMasterEffectDuration())
                        .into(view);
            }
        }
        Iterator<Map.Entry<Integer, ImageView>> iterator = tileFrontImageViews.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Integer, ImageView> entry = iterator.next();
            int key = entry.getKey();
            if (key >= 0 && key < layout.getTileCount())
                break;
            if (tileAnimations.containsKey(key)) {
                tileAnimations.get(key).cancel();
                tileAnimations.remove(key);
            }
            entry.getValue().clearAnimation();
            viewContainer.removeView(entry.getValue());
            iterator.remove();
        }
        iterator = imageResourceImageViews.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Integer, ImageView> entry = iterator.next();
            if (entry.getKey() >= 0 && entry.getKey() < layout.getImageCount())
                break;
            viewContainer.removeView(entry.getValue());
            iterator.remove();
        }
        String backgroundUrl = layout.getBackgroundUrl();
        if (backgroundUrl != null) {
            Glide.with(this).load(backgroundUrl).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new SimpleTarget<Bitmap>() {
                              @Override
                              public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap>
                                      glideAnimation) {
                                  // TODO(team): This misses cross fade.
                                  mainLayout.setBackground(new BitmapDrawable(resource));
                              }
                          }
                    );
        }
    }

    private void loadChannelData() {
        Channel channel = app.getChannel();
        masterEffectDuration = (int) channel.getMasterEffectDuration();
        tileEffectTransformer = new TileEffectTransformer(getContext(), masterEffectDuration);

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
            drawLayout();
        }

        // Lightweight image updater. Not necessary because drawLayout does this too.
        ArrayList<String> posToTileIds = channel.getPositionToTileIds();
        if (posToTileIds == null)
            return;
        for (int i = 0; i < posToTileIds.size(); i++) {
            String tileId = posToTileIds.get(i);
            // Unset fields are markers.
            // TODO(team): Load a placeholder image in this case?
            if (tileId.isEmpty()) {
                updateTile(i, null, false);
            }
            Tile tile = app.getTileObservableArrayMap().get(tileId);
            updateTile(i, tile, false);
        }

        defaultEffect = channel.getDefaultEffect();
        invalidateAllTileAnimations(false);
        resyncAndStartAnimation();
    }

    private ImageView buildTileView(final int position) {
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

    private Runnable animationRunner = new Runnable() {
        @Override
        public void run() {
            // Keep heartbeat going reagrdless of if the layout is ready.
            animationHandler.postDelayed(animationRunner, masterEffectDuration);

            if (layout == null) return;
            for (int i = 0; i < layout.getTileCount(); i++) {
                scheduleTileAnimation(i, false, true);
            }
        }
    };

    private void invalidateAllTileAnimations(boolean runNow) {
        // This function can be skipped when layout is not ready, because drawLayout handles this.
        if (layout == null)
            return;
        for (int i = 0; i < layout.getTileCount(); i++) {
            scheduleTileAnimation(i, true, runNow);
        }
    }

    private void scheduleTileAnimation(int i, boolean invalidate, boolean runNow) {
        ImageView tileIv = tileFrontImageViews.get(i);
        if (tileIv == null)
            return;
        // tileIv.clearAnimation();
        Tile loadedTile = tileCache.get(i);
        if (loadedTile == null)
            return;
        boolean hasEffect = loadedTile.getTileEffect() != null;
        Animation effect = null;
        if (hasEffect) {
            if (invalidate || tileAnimations.get(i) == null) {
                if (tileAnimations.get(i) != null) {
                    tileAnimations.get(i).cancel();
                }
                effect = tileEffectTransformer.processTileEffect(loadedTile.getTileEffect());
                tileAnimations.put(i, effect);
            } else {
                effect = tileAnimations.get(i);
            }
        } else {
            if (invalidate || defaultEffect == null) {
                defaultAnimation = tileEffectTransformer.processTileEffect(defaultEffect);
            }
            effect = defaultAnimation;
        }
        if (effect != null && runNow) {
            tileIv.startAnimation(effect);
        }
    }

    private void resyncAndStartAnimation() {
        animationHandler.removeCallbacks(animationRunner);
        long syncTime = app.getChannel().getChannelSyncTime();
        // Run some fraction of the duration different between now and remote sync time.
        long delay = (System.currentTimeMillis() - syncTime) % masterEffectDuration;
        animationHandler.postDelayed(animationRunner, delay);
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
