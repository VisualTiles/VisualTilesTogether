package com.javierarboleda.visualtilestogether.models;

import android.support.annotation.IntDef;

import com.google.firebase.database.DatabaseReference;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.javierarboleda.visualtilestogether.models.User.MODERATOR;

public class Channel {
    public static final String TABLE_NAME = "channels";
    public static final String TILE_IDS = "tileIds";
    public static final String POS_TO_TILE_IDS = "positionToTileIds";
    public static final String CHANNEL_BACKGROUND_COLOR = "channelBackgroundColor";
    public static final String CHANNEL_DISPLAY_NAME = "displayName";
    public static final String CHANNEL_UNIQUE_NAME = "uniqueName";
    public static final String LAYOUT_NAME = "layoutId";
    public static final String DEFAULT_TILE_COLOR = "defaultTileColor";
    public static final String EFFECT_DURATION = "masterEffectDuration";
    public static final String RONINS = "ronins";
    public static final String USER_LIST = "userList";
    public static final String USERS = "users";
    private String name;
    private String uniqueName;

    private ArrayList<String> positionToTileIds;
    private String layoutId;
    private HashMap<String, Boolean> tileIds;
    private ArrayList<String> moderators;

    private HashMap<String, String> userList;
    private HashMap<String, Integer> users;

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
    /**
     * Optional: If set, overrides Layout.defaultTileColor for this channel.
     */
    private Integer defaultTileColor;
    /**
     * Optional: If set, overrides Layout.backgroundUrl for channel with single color.
     */
    private Integer channelBackgroundColor;

    public Channel() {
    }

    public Channel(String name, String uniqueName, String userId) {
        this.name = name;
        this.uniqueName = uniqueName;
        this.addModerator(userId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
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

    public HashMap<String, Boolean> getTileIds() {
        return tileIds;
    }

    public void setTileIds(HashMap<String, Boolean> tileIds) {
        this.tileIds = tileIds;
    }

    public ArrayList<String> getModerators() {
        return moderators;
    }

    public void setModerators(ArrayList<String> moderators) {
        this.moderators = moderators;
    }

    public void addModerator(String moderator) {
        if (this.moderators == null) {
            this.moderators = new ArrayList<>();
        }
        this.moderators.add(moderator);
        if (users == null) {
            this.users = new HashMap<>();
        }
        users.put(moderator, MODERATOR);
    }

    public void removeModerator(String moderator) {
        if (this.moderators != null)
            this.moderators.remove(moderator);
        if (users != null) {
            users.remove(moderator);
        }
    }

    public boolean hasModerator(String moderator) {
        if (this.moderators != null) {
            for (String mod : moderators) {
                if (mod.equals(moderator))
                    return true;
            }
        }
        if (users != null) {
            return users.get(moderator) == MODERATOR;
        }
        return false;
    }

    public Integer getDefaultTileColor() {
        return defaultTileColor;
    }

    public void setDefaultTileColor(Integer defaultTileColor) {
        this.defaultTileColor = defaultTileColor;
    }

    public Integer getChannelBackgroundColor() {
        return channelBackgroundColor;
    }

    public void setChannelBackgroundColor(Integer channelBackgroundColor) {
        this.channelBackgroundColor = channelBackgroundColor;
    }

    public HashMap<String, String> getUserList() {
        return userList;
    }

    public void setUserList(HashMap<String, String> userList) {
        this.userList = userList;
    }

    public HashMap<String, Integer> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, Integer> users) {
        this.users = users;
    }
}
