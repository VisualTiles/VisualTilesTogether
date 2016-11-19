package com.javierarboleda.visualtilestogether.models;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Simple representation of a present screen.
 */

@IgnoreExtraProperties
public class PresentLayout {
    public interface PresentLayoutListener {
        void updateTile(int position, @Nullable Tile tile);
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

    public void setTile(int position, @Nullable Tile tile) {
        if (position >= getTileCount()) throw new ArrayIndexOutOfBoundsException();
        tiles[position] = tile;
        if (listener != null) listener.updateTile(position, tile);
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
        layout.layoutSize = new Point(640, 360);
        layout.tileCount = 10;
        layout.tilePositions = new Rect[]{
                new Rect(0,30,150,180),
                new Rect(150,0,270,120),
                new Rect(270,0,370,100),
                new Rect(370,0,490,120),
                new Rect(490,30,640,180),
                new Rect(0,180,150,330),
                new Rect(150,240,270,360),
                new Rect(270,260,370,360),
                new Rect(370,240,490,360),
                new Rect(490,180,640,330),
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
        layout.imagePositions = new Rect[]{new Rect(150, 120, 150+340, 120+120)};
        layout.imageUrls = new String[] {
                "https://firebasestorage.googleapis.com/v0/b/visual-tiles-together.appspot.com/o/images%2Fvisualtiles.png?alt=media&token=a98451d5-dd02-4e85-8f0e-2c33c7000803"
        };
        layout.transitionMs = 500;
        return layout;
    }
}
