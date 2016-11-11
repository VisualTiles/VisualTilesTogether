package com.javierarboleda.visualtilestogether.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.models.PresentLayout;
import com.javierarboleda.visualtilestogether.models.Tile;

/**
 * Created by chris on 11/10/16.
 */

public class PresentationActivity extends AppCompatActivity implements
        PresentLayout.PresentLayoutListener {
    private PresentLayout layout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        layout = PresentLayout.createDemoLayout();
        layout.setListener(this);
    }

    private void updateLayout(PresentLayout layout, int transitionMs) {
        this.layout.setListener(this);
        this.layout = layout;
        // TODO(chris): This does a transition between current and new, and does drawing.
    }

    private PresentLayout stepLayout(PresentLayout left, PresentLayout right, float stepPercent) {
        // TODO(chris): This enables animation.
        return null;
    }

    private void drawLayout() {
        // TODO(chris): This does drawing stuff.
    }

    @Override
    public void updateTile(int position, Tile tile, int transitionMs) {
        // TODO(chris): This replaces an imgview with a new image with a transition.
    }
}
