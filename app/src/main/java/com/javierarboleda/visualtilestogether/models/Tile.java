package com.javierarboleda.visualtilestogether.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;

@IgnoreExtraProperties
public class Tile {
    public static final String TABLE_NAME = "tiles";
    public static final String CHANNEL_ID = "channelId";
    public static final String CREATOR_ID = "creatorId";
    public static final String USER_VOTES = "userVotes";
    public static final String TILE_COLOR = "tileColor";
    public static final String TILE_EFFECT = "tileEffect";
    private String shapeUrl;
    private String shapeFbStorage;
    private String creatorId;
    private String channelId;
    private String tileId;  // Must be manually filled, only filled in the Application's tile cache.
    private int posVotes;
    private int negVotes;
    private long submitTimeMs;
    private boolean approved;
    private TileEffect tileEffect;
    /**
     * Optional: If set, overrides Layout.defaultTileColor and Channel.defaultTileColor for this
     * tile.
     */
    private Integer tileColor;

    public Tile() {}

    private static boolean almostEqual(double a, double b) {
        return Math.abs(a-b)<1E-7;
    }

    public boolean equalsValue(Tile t) {
        if (t == null) return false;
        boolean isEqual = true;
        if (tileEffect == null && t.getTileEffect() != null) isEqual = false;
        else if (tileEffect != null && t.getTileEffect() == null) isEqual = false;
        else if (tileEffect != null && t.getTileEffect() != null) {
            if (!tileEffect.getEffectType().equals(t.getTileEffect().getEffectType()))
                isEqual = false;
            if (!almostEqual(tileEffect.getEffectDurationPct(),
                    t.getTileEffect().getEffectDurationPct()))
                isEqual = false;
            if (!almostEqual(tileEffect.getEffectOffsetPct(),
                    t.getTileEffect().getEffectOffsetPct()))
                isEqual = false;
        }
        return (isEqual &&
                (shapeUrl == null || shapeUrl.equals(t.getShapeUrl())) &&
                (shapeFbStorage == null || shapeFbStorage.equals(t.getShapeFbStorage())) &&
                (creatorId == null || creatorId.equals(t.getCreatorId())) &&
                (channelId == null || channelId.equals(t.getChannelId())) &&
                posVotes == t.getPosVotes() &&
                negVotes == t.getNegVotes() &&
                approved == t.approved &&
                submitTimeMs == t.getSubmitTimeMs() &&
                ((t.getTileColor() == null && tileColor == null) ||
                        (tileColor != null && tileColor.equals(t.getTileColor()))));
    }

    public Tile(boolean approved, int negVotes, int posVotes, String shapeFbStorage,
                String shapeUrl, long submitTimeMs) {
        this.approved = approved;
        this.negVotes = negVotes;
        this.posVotes = posVotes;
        this.shapeFbStorage = shapeFbStorage;
        this.shapeUrl = shapeUrl;
        this.submitTimeMs = submitTimeMs;
    }

    public Tile(boolean approved, String channelId, String creatorId, int negVotes, int posVotes,
                String shapeFbStorage, String shapeUrl, long submitTimeMs) {
        this.approved = approved;
        this.channelId = channelId;
        this.creatorId = creatorId;
        this.negVotes = negVotes;
        this.posVotes = posVotes;
        this.shapeFbStorage = shapeFbStorage;
        this.shapeUrl = shapeUrl;
        this.submitTimeMs = submitTimeMs;
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

    public long getSubmitTimeMs() {
        return submitTimeMs;
    }

    public void setSubmitTimeMs(long submitTimeMs) {
        this.submitTimeMs = submitTimeMs;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTimeMs = submitTime.getTime();
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

    public Integer getTileColor() {
        return tileColor;
    }

    public void setTileColor(Integer tileColor) {
        this.tileColor = tileColor;
    }
}
