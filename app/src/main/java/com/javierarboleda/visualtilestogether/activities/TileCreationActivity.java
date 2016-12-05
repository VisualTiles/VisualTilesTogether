package com.javierarboleda.visualtilestogether.activities;

import android.animation.ObjectAnimator;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.databinding.ActivityTileCreationBinding;
import com.javierarboleda.visualtilestogether.fragments.ShapeAddDialog;
import com.javierarboleda.visualtilestogether.views.CanvasView;

import java.util.Random;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.createTile;

public class TileCreationActivity extends AppCompatActivity
        implements ShapeAddDialog.OnFragmentInteractionListener {

    ActivityTileCreationBinding binding;
    CanvasView mCanvas;
    private static final String TILES_TABLE = "tiles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tile_creation);

        setUpToolbar();

        mCanvas = binding.cvVisualTile;

        Display d = getWindowManager().getDefaultDisplay();
        Point canvasDimen = new Point();
        d.getSize(canvasDimen);
        mCanvas.getLayoutParams().height = canvasDimen.x;
        mCanvas.setBaseColor(Color.TRANSPARENT);

        mCanvas.setMode(CanvasView.Mode.DRAW);
        mCanvas.setDrawer(CanvasView.Drawer.PEN);
        mCanvas.setPaintStrokeWidth(16F);
        mCanvas.setPaintStrokeColor(Color.WHITE);
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        String[] tileCraetionTitles = getResources().getStringArray(R.array.tile_creation_titles);
        String title = tileCraetionTitles[new Random().nextInt(tileCraetionTitles.length)];
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Menu toolbarMenu = binding.toolbar.getMenu();
        getMenuInflater().inflate(R.menu.action_menu_tile_creation_toolbar, toolbarMenu);

        Menu topMenu = binding.amvTop.getMenu();
        getMenuInflater().inflate(R.menu.action_menu_title_editor_top, topMenu);

        for (int i = 0; i < topMenu.size(); i++) {
            topMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
        }

        Menu bottomMenu = binding.amvBottom.getMenu();
        getMenuInflater().inflate(R.menu.action_menu_tile_editor_bottom, bottomMenu);

        for (int i = 0; i < bottomMenu.size(); i++) {
            bottomMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
        }

        Menu bottomMenu2 = binding.amvBottom2.getMenu();
        getMenuInflater().inflate(R.menu.action_menu_tile_editor_bottom_more_options, bottomMenu2);

        binding.amvBottom2.setRotationX(90);

        for (int i = 0; i < bottomMenu2.size(); i++) {
            bottomMenu2.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View bottomMenu = binding.amvBottom;
        View bottomMenu2 = binding.amvBottom2;

        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityCompat.finishAfterTransition(this);
                return true;
            case R.id.action_add_tile_to_upcoming:
                Bitmap bitmap = mCanvas.getScaleBitmap(300, 300);
                onFragmentInteraction(bitmap);
                ActivityCompat.finishAfterTransition(this);
                return true;
            case R.id.action_undo:
                mCanvas.undo();
                return true;
            case R.id.action_redo:
                mCanvas.redo();
                return true;
            case R.id.action_draw_mode:
                mCanvas.setMode(CanvasView.Mode.DRAW);
                mCanvas.setDrawer(CanvasView.Drawer.PEN);
                return true;
            case R.id.action_line_mode:
                mCanvas.setMode(CanvasView.Mode.DRAW);
                mCanvas.setDrawer(CanvasView.Drawer.LINE);
                return true;
            case R.id.action_box_mode:
                mCanvas.setMode(CanvasView.Mode.DRAW);
                mCanvas.setDrawer(CanvasView.Drawer.RECTANGLE);
                return true;
            case R.id.action_circle_mode:
                mCanvas.setMode(CanvasView.Mode.DRAW);
                mCanvas.setDrawer(CanvasView.Drawer.CIRCLE);
                return true;
            case R.id.action_eraser_mode:
                mCanvas.setMode(CanvasView.Mode.ERASER);
                mCanvas.setDrawer(CanvasView.Drawer.PEN);
                return true;
            case R.id.action_stroke_width_small:
                mCanvas.setPaintStrokeWidth(8F);
                return true;
            case R.id.action_stroke_width_medium:
                mCanvas.setPaintStrokeWidth(16F);
                return true;
            case R.id.action_stroke_width_large:
                mCanvas.setPaintStrokeWidth(24F);
                return true;
            case R.id.action_stroke_width_xlarge:
                mCanvas.setPaintStrokeWidth(32F);
                return true;
            case R.id.action_stroke_width_xxlarge:
                mCanvas.setPaintStrokeWidth(40F);
                return true;
            case R.id.action_choose_stroke_width:
                startMenuRotateAnimation(bottomMenu, bottomMenu2);
                return true;
            case R.id.action_back_to_drawing_tools:
                startMenuRotateAnimation(bottomMenu2, bottomMenu);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startMenuRotateAnimation(View bottomMenu, View bottomMenu2) {
        ObjectAnimator rotateOutAnimation;
        ObjectAnimator rotateInAnimation;
        rotateOutAnimation =
                ObjectAnimator.ofFloat(bottomMenu, "rotationX", 0, 90)
                .setDuration(200);
        rotateInAnimation =
                ObjectAnimator.ofFloat(bottomMenu2, "rotationX", -90, 0)
                        .setDuration(200);

        rotateOutAnimation.start();
        rotateInAnimation.setStartDelay(180);
        rotateInAnimation.start();
    }

    @Override
    public void onFragmentInteraction(Bitmap bitmap) {
        createTile(bitmap, ((VisualTilesTogetherApp) getApplication()).getChannelId(),
                ((VisualTilesTogetherApp) getApplication()).getUid());
    }
}
