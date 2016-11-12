package com.javierarboleda.visualtilestogether.activities;

import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.ActivityTileCreationBinding;
import com.javierarboleda.visualtilestogether.views.CanvasView;

public class TileCreationActivity extends AppCompatActivity {

    ActivityTileCreationBinding binding;
    CanvasView mCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tile_creation);


        mCanvas = binding.cvVisualTile;

        Display d = getWindowManager().getDefaultDisplay();
        Point canvasDimen = new Point();
        d.getSize(canvasDimen);
        mCanvas.getLayoutParams().height = canvasDimen.x;

        binding.cvVisualTile.setMode(CanvasView.Mode.DRAW);
        binding.cvVisualTile.setDrawer(CanvasView.Drawer.LINE);

        mCanvas.setPaintStrokeWidth(10F);
    }

    // Need to create square view to create artwork



}
