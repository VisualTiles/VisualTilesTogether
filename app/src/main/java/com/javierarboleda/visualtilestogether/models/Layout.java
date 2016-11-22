package com.javierarboleda.visualtilestogether.models;

import android.graphics.Color;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Simple representation of a present screen.
 */

@IgnoreExtraProperties
public class Layout {
    public static final String TABLE_NAME = "layouts";

    private ArrayList<String> imageUrls;
    private ArrayList<Rect> tilePositions;
    private ArrayList<Rect> imagePositions;
    private String layoutName;
    private String backgroundUrl;
    private int backgroundColor = Color.BLACK;
    private int layoutWidth;
    private int layoutHeight;
    private int tileCount;
    private int imageCount;
    private int defaultTileColor = Color.WHITE;

    public Layout() {}

    public String getLayoutName() {
        return layoutName;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public int getTileCount() {
        return tileCount;
    }

    public void setTileCount(int tileCount) {
        this.tileCount = tileCount;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public ArrayList<Rect> getImagePositions() {
        return imagePositions;
    }

    public void setImagePositions(ArrayList<Rect> imagePositions) {
        this.imagePositions = imagePositions;
    }

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public int getLayoutHeight() {
        return layoutHeight;
    }

    public void setLayoutHeight(int layoutHeight) {
        this.layoutHeight = layoutHeight;
    }

    public int getLayoutWidth() {
        return layoutWidth;
    }

    public void setLayoutWidth(int layoutWidth) {
        this.layoutWidth = layoutWidth;
    }

    public ArrayList<Rect> getTilePositions() {
        return tilePositions;
    }

    public void setTilePositions(ArrayList<Rect> tilePositions) {
        this.tilePositions = tilePositions;
    }

    public static Layout createDemoLayout() {
        Layout layout = new Layout();
        layout.backgroundUrl = "https://firebasestorage.googleapis.com/v0/b/visual-tiles-together" +
                ".appspot.com/o/backgrounds%2Fpresent_bg1.jpg?alt=media&token=4728d0d0-78f6-" +
                "4f61-8e90-dad6672fc895";
        layout.layoutWidth = 640;
        layout.layoutHeight = 360;
        layout.tileCount = 10;
        layout.tilePositions = new ArrayList<>(Arrays.asList(
                new Rect(0,30,150),
                new Rect(150,0,120),
                new Rect(270,0,100),
                new Rect(370,0,120),
                new Rect(490,30,150),

                new Rect(0,180,150),
                new Rect(150,240,120),
                new Rect(270,260,100),
                new Rect(370,240,120),
                new Rect(490,180,150)
        ));
//         [{"left": 0, "top": 30, "right": 150, "bottom": 180},
//          {"left": 150, "top": 0, "right": 270, "bottom": 120},
//          {"left": 270, "top": 0, "right": 370, "bottom": 100},
//          {"left": 370, "top": 0, "right": 470, "bottom": 120},
//          {"left": 490, "top": 30, "right": 640, "bottom": 180}]

//        [{"left": 0, "top": 180, "right": 150, "bottom": 330},
//          {"left": 150, "top": 240, "right": 270, "bottom": 360},
//          {"left": 270, "top": 260, "right": 370, "bottom": 360},
//          {"left": 370, "top": 240, "right": 490, "bottom": 360},
//          {"left": 490, "top": 180, "right": 640, "bottom": 330}]

        layout.imageCount = 1;
        // [{"left": 150, "top": 120, "right": 490, "bottom": 240}]
        layout.imagePositions = new ArrayList<>(Arrays.asList(
                new Rect(150, 120, 150 + 340, 120 + 120)));
        layout.imageUrls = new ArrayList<>(Arrays.asList(
                "https://firebasestorage.googleapis.com/v0/b/visual-tiles-together.appspot.com/o/images%2Fvisualtiles.png?alt=media&token=a98451d5-dd02-4e85-8f0e-2c33c7000803"
        ));
        return layout;
    }

    public int getDefaultTileColor() {
        return defaultTileColor;
    }

    public void setDefaultTileColor(int defaultTileColor) {
        this.defaultTileColor = defaultTileColor;
    }
}
