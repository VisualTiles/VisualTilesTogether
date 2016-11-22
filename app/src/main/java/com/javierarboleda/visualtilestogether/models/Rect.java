package com.javierarboleda.visualtilestogether.models;

/**
 * A firebase compatible version of rect for visual tile objects.
 */

public class Rect {
    public int bottom;
    public int left;
    public int right;
    public int top;

    public Rect() {
    }

    public Rect(int left, int top, int width) {
        this.left = left;
        this.top = top;
        this.right = left+width;
        this.bottom = top+width;
    }

    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }
}