package com.javierarboleda.visualtilestogether.models;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Channel {
    public static final String TABLE_NAME = "channels";
    public static final String TILE_IDS = "tileIds";
    public static final String POS_TO_TILE_IDS = "positionToTileIds";
    public static final String CHANNEL_NAME = "name";
    private String name;
    private Date startTime;
    private Date endTime;
    private ArrayList<String> positionToTileIds;

    // Effect fields.
    /**
     * The timestamp, in local device millis, of when the channel effect looper started.
     */
    private Long channelSyncTime;
    /**
     * The number of milliseconds the animation looper will run before beginning again (and
     * cancelling all incomplete tile animations).
     */
    private Long masterEffectDuration;
    private TileEffect defaultEffect;

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

    public long getMasterEffectDuration() {
        if (masterEffectDuration == null) return 0L;
        return masterEffectDuration;
    }

    public void setMasterEffectDuration(long masterEffectDuration) {
        this.masterEffectDuration = masterEffectDuration;
    }

    public TileEffect getDefaultEffect() {
        return defaultEffect;
    }

    public void setDefaultEffect(TileEffect effect) {
        defaultEffect = effect;
    }

    /**
     * Updates the start time of the default effect.
     * Does nothing without calling saveDefaultEffect.
     */
    public void fireDefaultEffect() {
        defaultEffect.setStartTimeMillis(System.currentTimeMillis());
    }

    /**
     * Saves the object's default effect to the database.
     * @param ref A database reference of this channel.
     */
    public void saveDefaultEffect(DatabaseReference ref) {
        HashMap<String, Object> change = new HashMap<>();
        change.put("defaultEffect", defaultEffect);
        ref.updateChildren(change);
    }

    public long getChannelSyncTime() {
        if (channelSyncTime == null) return 0L;
        return channelSyncTime;
    }
}
