package com.javierarboleda.visualtilestogether.models;

import android.graphics.Point;
import android.graphics.Rect;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Simple representation of a present screen.
 */

@IgnoreExtraProperties
public class PresentLayout {
    public interface PresentLayoutListener {
        void updateTile(int position, Tile tile, int transitionMs);
    }

    private PresentLayoutListener listener;

    private String[] imageUrls;
    private Tile[] tiles;
    private Rect[] tilePositions;
    private Rect[] imagePositions;
    private String backgroundUrl = "";
    private Point layoutSize = new Point(600, 360);
    private int transitionMs = 0;

    private int tileCount = 0;

    public int getTileCount() {
        return tileCount;
    }

    private int imageCount = 0;

    public int getImageCount() {
        return imageCount;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public void setTile(int position, Tile tile) {
        if (position >= getTileCount()) throw new ArrayIndexOutOfBoundsException();
        tiles[position] = tile;
        if (listener != null) listener.updateTile(position, tile, transitionMs);
    }

    public String[] getImageUrls() {
        return imageUrls;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public Rect[] getTilePositions() {
        return tilePositions;
    }

    public Rect[] getImagePositions() {
        return imagePositions;
    }

    public Point getLayoutSize() {
        return layoutSize;
    }

    public int getTransitionMs() {
        return transitionMs;
    }

    public void setListener(PresentLayoutListener listener) {
        this.listener = listener;
    }

    public static PresentLayout createDemoLayout() {
        PresentLayout layout = new PresentLayout();
        layout.backgroundUrl = "https://firebasestorage.googleapis.com/v0/b/visual-tiles-together" +
                ".appspot.com/o/backgrounds%2Fpresent_bg1.jpg?alt=media&token=4728d0d0-78f6-" +
                "4f61-8e90-dad6672fc895";
        layout.layoutSize = new Point(600, 360);
        layout.tileCount = 10;
        layout.tilePositions = new Rect[]{
                new Rect(10, 50, 130, 170),
                new Rect(140, 10, 240, 110),
                new Rect(260, 5, 340, 85),
                new Rect(360, 10, 460, 110),
                new Rect(470, 50, 590, 170),
                new Rect(10, 190, 130, 310),
                new Rect(140, 250, 240, 350),
                new Rect(260, 275, 340, 355),
                new Rect(360, 250, 460, 350),
                new Rect(470, 190, 590, 310)
        };

        final String imgUrl = "https://firebasestorage.googleapis.com/v0/b/visual-tiles-together" +
                ".appspot.com/o/shapes%2FBluetooth-Round-Shape" +
                ".png?alt=media&token=98a5bba5-8a72-4b7b-b46b-b521f7ce886e";
        layout.tiles = new Tile[] {
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date()),
                new Tile(true, 0, 0, "", imgUrl, new Date())
        };

        layout.imageCount = 1;
        layout.imagePositions = new Rect[]{new Rect(150, 130, 450, 230)};
        layout.imageUrls = new String[] {
                "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"
        };
        layout.transitionMs = 500;
        return layout;
    }
}
