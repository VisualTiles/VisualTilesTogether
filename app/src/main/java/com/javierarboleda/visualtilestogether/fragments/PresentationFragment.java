package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.databinding.ObservableMap;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
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
import com.google.firebase.database.DatabaseError;
import com.javierarboleda.visualtilestogether.BuildConfig;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.PresentLayout;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.TileEffect;
import com.javierarboleda.visualtilestogether.util.TileEffectTransformer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PresentationFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PresentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresentationFragment extends Fragment
        implements PresentLayout.PresentLayoutListener,
        VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String TAG = PresentationFragment.class.getSimpleName();
    private static final String ARG_EDITOR_MODE = "editor_mode";

    private VisualTilesTogetherApp app;
    private boolean isEditorMode;
    private PercentFrameLayout mainLayout;
    private PercentFrameLayout viewContainer;
    private HashMap<Integer, ImageView> imageResourceImageViews;
    private HashMap<Integer, ImageView> tileFrontImageViews;
    private HashMap<Integer, Animation> tileAnimations;
    private Animation defaultAnimation;
    private Handler animationHandler = new Handler();
    private TileEffect defaultEffect;
    private TileEffectTransformer tileEffectTransformer;

    private PresentLayout layout;

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
        if (getArguments() != null) {
            isEditorMode = getArguments().getBoolean(ARG_EDITOR_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_presentation, container, false);
        imageResourceImageViews = new HashMap<>();
        tileFrontImageViews = new HashMap<>();
        tileAnimations = new HashMap<>();
        if (app.getUser() == null ||
                app.getChannel() ==  null) {
            return view;
        }
        if (layout == null) {
            layout = PresentLayout.createDemoLayout();
            /*
            if (database != null && !channelId.isEmpty()) {
                database.setValue(layout);
            }*/
        }
        tileEffectTransformer = new TileEffectTransformer(getContext(),
                app.getChannel().getMasterEffectDuration());
        layout.setListener(this);
        setupTileListListener();
        viewContainer = (PercentFrameLayout) view.findViewById(R.id.viewContainer);
        mainLayout = (PercentFrameLayout) view.findViewById(R.id.mainLayout);
        drawLayout(savedInstanceState);
        loadTilesForChannel();
        return view;
    }

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
                            Tile tile = layout.getTiles()[i];
                            if (tile != null && tile.getTileId().equals(key)) {
                                // Found the tile that updated. Update it in layout.
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layout.setTile(position, newTile);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        app.removeListener(this);
    }

    public interface PresentationFragmentListener {
        void onTileTapped(int position, Tile tile);
    }

    private void moveRelativeView(View view, Rect bounds) {
        if (BuildConfig.DEBUG && !(view.getLayoutParams()
                instanceof PercentFrameLayout.LayoutParams)) {
            throw new RuntimeException("View is not within a PercentRelativeLayout");
        }
        PercentFrameLayout.LayoutParams params = (PercentFrameLayout.LayoutParams)
                view.getLayoutParams();
        Point max = layout.getLayoutSize();
        params.getPercentLayoutInfo().topMarginPercent = bounds.top / (float) max.y;
        params.getPercentLayoutInfo().leftMarginPercent = bounds.left / (float) max.x;
        params.getPercentLayoutInfo().heightPercent = (bounds.bottom - bounds.top) / (float) max.y;
        params.getPercentLayoutInfo().widthPercent = (bounds.right - bounds.left) / (float) max.x;
        view.invalidate();
    }

    private void loadTile(int position, ImageView view) {
        if (view == null) {
            Log.e(TAG, "Null imageview reference in loadTile for position " + position);
            return;
        }
        if (layout == null) {
            Log.e(TAG, "Null layout reference in loadTile for position " + position);
            return;
        }
        Tile tile = layout.getTiles()[position];
        if (tile == null) {
            Log.i(TAG, "Null tile reference in loadTile for position " + position);
            // TODO: Maybe expect this null to unload a tile.
            view.setTag(null);
            view.setImageResource(android.R.color.transparent);
            return;
        }
        if (tile.getShapeUrl() == null) {
            Log.e(TAG, "Null getShapeUrl reference in loadTile for position " + position);
            return;
        }
        Glide.with(this).load(tile.getShapeUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade(layout.getTransitionMs())
                .into(view);
    }

    @Override
    public void updateTile(int position, Tile tile) {
        loadTile(position, tileFrontImageViews.get(position));
        scheduleTileAnimation(position, true);
    }

    public PresentLayout stepLayout(PresentLayout left, PresentLayout right, float stepPercent) {
        // TODO(chris): This enables animation.
        return null;
    }

    private void drawLayout(Bundle savedInstanceState) {
        if (layout == null) return;
        for (int i = 0; i < layout.getTileCount(); i++) {
            ImageView view = null;
            if (tileFrontImageViews.get(i) == null) {
                view = buildTileView(i, savedInstanceState);
                viewContainer.addView(view);
                tileFrontImageViews.put(i, view);
            }
            moveRelativeView(view, layout.getTilePositions()[i]);
            loadTile(i, view);
        }
        // Delete stale tileFrontImageViews.
        for (int i = layout.getTileCount(); i < tileFrontImageViews.size(); i++) {
            viewContainer.removeView(tileFrontImageViews.get(i));
        }
        for (int i = 0; i < layout.getImageCount(); i++) {
            ImageView view = null;
            if (imageResourceImageViews.get(i) == null) {
                view = buildTileView(i, savedInstanceState);
                viewContainer.addView(view);
                imageResourceImageViews.put(i, view);
            }
            moveRelativeView(view, layout.getImagePositions()[i]);
            Glide.with(this).load(layout.getImageUrls()[i])
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(view);
        }
        // Delete stale imageResourceImageViews.
        for (int i = layout.getImageCount(); i < imageResourceImageViews.size(); i++) {
            viewContainer.removeView(imageResourceImageViews.get(i));
        }
        Glide.with(this).load(layout.getBackgroundUrl()).asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(
                        new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap>
                                    glideAnimation) {
                                mainLayout.setBackground(new BitmapDrawable(resource));
                            }
                        }
                );
    }

    private void loadTilesForChannel() {
        if (layout == null) return;
        ArrayList<String> posToTileIds = app.getChannel()
                .getPositionToTileIds();
        if (posToTileIds == null)
            return;
        for (int i = 0; i < layout.getTileCount(); i++) {
            if (posToTileIds.size() > i) {
                String tileId = posToTileIds.get(i);
                // Unset fields are markers.
                // TODO(team): Load a placeholder image in this case?
                if (tileId.isEmpty()) {
                    layout.setTile(i, null);
                }
                Tile loadTile = app.getTileObservableArrayMap()
                        .get(tileId);
                layout.setTile(i, loadTile);
            }
            Log.i(TAG, "Loading tile index " + i);
        }
        defaultEffect = app.getChannel().getDefaultEffect();
        invalidateAllTileAnimations();
        resyncAndStartAnimation();
    }

    private ImageView buildTileView(final int position, Bundle savedInstanceState) {
        ImageView iv = (ImageView) getLayoutInflater(savedInstanceState).inflate(R.layout
                .iv_tile, null);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onTileTapped(position, layout.getTiles()[position]);
            }
        });
        return iv;
    }

    private Runnable animationRunner = new Runnable() {
        @Override
        public void run() {
            if (layout == null) return;
            animationHandler.postDelayed(animationRunner,
                    app.getChannel().getMasterEffectDuration());
            for (int i = 0; i < layout.getTileCount(); i++) {
                scheduleTileAnimation(i, false);
            }
        }
    };

    private void invalidateAllTileAnimations() {
        for (int i = 0; i < layout.getTileCount(); i++) {
            scheduleTileAnimation(i, true);
        }
    }
    private void scheduleTileAnimation(int i, boolean invalidate) {
        ImageView tileIv = tileFrontImageViews.get(i);
        if (tileIv == null)
            return;
        // tileIv.clearAnimation();
        Tile loadedTile = layout.getTiles()[i];
        if (loadedTile == null)
            return;
        Animation effect = null;
        if (invalidate) {
            defaultAnimation = tileEffectTransformer.processTileEffect(defaultEffect);
            if (tileAnimations.get(i) != null) {
                tileAnimations.get(i).cancel();
            }
            if (loadedTile.hasEffect()) {
                effect = tileEffectTransformer.processTileEffect(loadedTile.getTileEffect());
                tileAnimations.put(i, effect);
            }
        } else {
            if (tileAnimations.get(i) != null) {
                effect = tileAnimations.get(i);
            } else {
                effect = defaultAnimation;
            }
        }
        if (effect != null) {
            tileIv.startAnimation(effect);
        }
    }

    private void resyncAndStartAnimation() {
        animationHandler.removeCallbacks(animationRunner);
        long syncTime = app.getChannel().getChannelSyncTime();
        long duration = app.getChannel().getMasterEffectDuration();
        if (duration == 0L) duration = 1000L;
        // Run some fraction of the duration different between now and remote sync time.
        long delay = (System.currentTimeMillis() - syncTime) % duration;
        animationHandler.postDelayed(animationRunner, delay);
    }

    @Override
    public void onChannelUpdated() {
        // Listens for effects and loads tileFrontImageViews.
        tileEffectTransformer.setStageDuration(app.getChannel().getMasterEffectDuration());
        loadTilesForChannel();
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
