package com.javierarboleda.visualtilestogether.models;


import com.android.annotations.Nullable;
import com.google.firebase.auth.FirebaseUser;

public class User {
    public static final String TABLE_NAME = "users";
    public static final String CHANNEL_ID = "channelId";
    @Nullable
    private String channelId;
    private String name;
    private String email;
    private String photoUrl;
    private boolean isAnonymous;

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
        User user = new User(null,
                fbu.getEmail(),
                fbu.isAnonymous(),
                fbu.getDisplayName(),
                fbu.getPhotoUrl().toString());
        return user;
    }
}
