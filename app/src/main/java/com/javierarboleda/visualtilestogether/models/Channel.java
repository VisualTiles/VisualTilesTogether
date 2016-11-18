package com.javierarboleda.visualtilestogether.models;

import java.util.ArrayList;
import java.util.Date;

public class Channel {
    public static final String TABLE_NAME = "channels";
    public static final String TILE_IDS = "tileIds";
    private String name;
    private Date startTime;
    private Date endTime;
    private ArrayList<String> positionToTileIds;

    public Channel() {
    }

    public Channel(String name, Date startTime, Date endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Channel(Date endTime, String name, Date startTime, ArrayList<String> positionToTileIds) {
        this.endTime = endTime;
        this.name = name;
        this.startTime = startTime;
        this.positionToTileIds = positionToTileIds;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public ArrayList<String> getPositionToTileIds() {
        return positionToTileIds;
    }

    public void setPositionToTileIds(ArrayList<String> positionToTileIds) {
        this.positionToTileIds = positionToTileIds;
    }

    public void addTileId(int position, String tileId) {
        
    }
}
