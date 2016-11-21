package com.javierarboleda.visualtilestogether.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@IgnoreExtraProperties
public class Channel {
    public static final String TABLE_NAME = "channels";
    public static final String TILE_IDS = "tileIds";
    private String name;
    private Date startTime;
    private Date endTime;
    private ArrayList<String> positionToTileIds;
    private String layoutId;

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
        this.positionToTileIds.set(position, tileId);
    }

    public void setMasterEffectDuration(Long masterEffectDuration) {
        this.masterEffectDuration = masterEffectDuration;
    }

    public String getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(String layoutId) {
        this.layoutId = layoutId;
    }

    public void setChannelSyncTime(Long channelSyncTime) {
        this.channelSyncTime = channelSyncTime;
    }

    public long getMasterEffectDuration() {
        // Default to 5 second duration if it's not set.
        // TODO(team): Make this a constant.
        if (masterEffectDuration == null) return 5000L;
        return masterEffectDuration;
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
