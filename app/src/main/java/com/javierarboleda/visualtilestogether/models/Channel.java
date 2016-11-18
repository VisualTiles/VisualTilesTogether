package com.javierarboleda.visualtilestogether.models;

import java.util.Date;
import java.util.Map;

public class Channel {
    public static final String TABLE_NAME = "channels";
    public static final String TILE_IDS = "TileIds";
    private String name;
    private Date startTime;
    private Date endTime;
    private Map<Integer, String> tileIds;

    public Channel() {
    }

    public Channel(String name, Date startTime, Date endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Channel(Date endTime, String name, Date startTime, Map<Integer, String> tileIds) {
        this.endTime = endTime;
        this.name = name;
        this.startTime = startTime;
        this.tileIds = tileIds;
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

    public Map<Integer, String> getTileIds() {
        return tileIds;
    }

    public void setTileIds(Map<Integer, String> tileIds) {
        this.tileIds = tileIds;
    }

    public void addTileId(int position, String tileId) {
        
    }
}
