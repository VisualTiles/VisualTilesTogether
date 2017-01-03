package com.javierarboleda.visualtilestogether.models;


import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import com.google.firebase.auth.FirebaseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

public class User {

    public static final int REGULAR_USER = 0;
    public static final int MODERATOR = 1;
    public static final int BLOCKED = 2;

    @IntDef({REGULAR_USER, MODERATOR, BLOCKED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UserType {}

    public static final String TABLE_NAME = "users";
    public static final String CHANNEL_ID = "channelId";
    public static final String TILE_IDS = "tileIds";
    @Nullable
    private String channelId;
    private String name;
    private String email;
    private String photoUrl;
    private boolean isAnonymous;
    private HashMap<String, Object> tileIds;

    public User() {
    }

    public User(String channelId, String email, boolean isAnonymous, String name, String photoUrl) {
        this.channelId = channelId;
        this.email = email;
        this.isAnonymous = isAnonymous;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channel) {
        this.channelId = channel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public static User fromFirebaseUser(FirebaseUser fbu) {
        // ChannelId is null when this function is called (only for new users, hopefully).
        return new User(null,
                fbu.getEmail(),
                fbu.isAnonymous(),
                fbu.getDisplayName(),
                fbu.getPhotoUrl().toString());
    }

    public HashMap<String, Object> getTileIds() {
        return tileIds;
    }

    public void setTileIds(HashMap<String, Object> tileIds) {
        this.tileIds = tileIds;
    }
}
