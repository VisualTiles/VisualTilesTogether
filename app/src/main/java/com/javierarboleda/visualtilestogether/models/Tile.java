package com.javierarboleda.visualtilestogether.models;

import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.HashMap;

public class Tile {
    public static final String TABLE_NAME = "tiles";
    public static final String CHANNEL_ID = "channelId";
    public static final String USER_VOTES = "userVotes";
    private String shapeUrl;
    private String shapeFbStorage;
    private String creatorId;
    private String channelId;
    private String tileId;  // Must be manually filled, only filled in the Application's tile cache.
    private int posVotes;
    private int negVotes;
    private Date submitTime;
    private boolean approved;
    private TileEffect tileEffect;

    public Tile() {}

    public boolean equalsValue(Tile t) {
        return (t == null ||
                (shapeUrl != null && shapeUrl.equals(t.getShapeUrl())) &&
                (shapeFbStorage != null && shapeFbStorage.equals(t.getShapeFbStorage())) &&
                (creatorId != null && creatorId.equals(t.getCreatorId())) &&
                (channelId != null && channelId.equals(t.getChannelId())) &&
                posVotes == t.getPosVotes() &&
                negVotes == t.getNegVotes() &&
                approved == t.approved &&
                (submitTime != null && submitTime.compareTo(t.getSubmitTime()) == 0));
    }

    public Tile(boolean approved, int negVotes, int posVotes, String shapeFbStorage,
                String shapeUrl, Date submitTime) {
        this.approved = approved;
        this.negVotes = negVotes;
        this.posVotes = posVotes;
        this.shapeFbStorage = shapeFbStorage;
        this.shapeUrl = shapeUrl;
        this.submitTime = submitTime;
    }

    public Tile(boolean approved, String channelId, String creatorId, int negVotes, int posVotes,
                String shapeFbStorage, String shapeUrl, Date submitTime) {
        this.approved = approved;
        this.channelId = channelId;
        this.creatorId = creatorId;
        this.negVotes = negVotes;
        this.posVotes = posVotes;
        this.shapeFbStorage = shapeFbStorage;
        this.shapeUrl = shapeUrl;
        this.submitTime = submitTime;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
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

    public String getTileId() {
        return tileId;
    }

    public void setTileId(String tileId) {
        this.tileId = tileId;
    }

    public boolean hasEffect() {
        return tileEffect != null;
    }

    public TileEffect getTileEffect() {
        return tileEffect;
    }

    public void setTileEffect(TileEffect tileEffect) {
        this.tileEffect = tileEffect;
    }

    /**
     * Updates the start time of the effect.
     * Does nothing without calling saveEffect().
     */
    public void fireEffect() {
        tileEffect.setStartTimeMillis(System.currentTimeMillis());
    }

    /**
     * Saves the tile's effect to the database.
     * @param ref A database reference of this Tile.
     */
    public void saveEffect(DatabaseReference ref) {
        HashMap<String, Object> change = new HashMap<>();
        change.put("tileEffect", tileEffect);
        ref.updateChildren(change);
    }
}
