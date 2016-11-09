package com.javierarboleda.visualtilestogether.models;

import java.util.Date;


public class TileContent {
    private String shapeUrl;
    private String shapeFbStorage;
    private int posVotes;
    private int negVotes;
    private Date submitTime;
    private boolean approved;

    public TileContent() {

    }

    public TileContent(boolean approved, int negVotes, int posVotes, String shapeFbStorage, String shapeUrl, Date submitTime) {
        this.approved = approved;
        this.negVotes = negVotes;
        this.posVotes = posVotes;
        this.shapeFbStorage = shapeFbStorage;
        this.shapeUrl = shapeUrl;
        this.submitTime = submitTime;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public int getNegVotes() {
        return negVotes;
    }

    public void setNegVotes(int negVotes) {
        this.negVotes = negVotes;
    }

    public int getPosVotes() {
        return posVotes;
    }

    public void setPosVotes(int posVotes) {
        this.posVotes = posVotes;
    }

    public String getShapeUrl() {
        return shapeUrl;
    }

    public void setShapeUrl(String shapeUrl) {
        this.shapeUrl = shapeUrl;
    }

    public String getShapeFbStorage() {
        return shapeFbStorage;
    }

    public void setShapeFbStorage(String shapeFbStorage) {
        this.shapeFbStorage = shapeFbStorage;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
}
