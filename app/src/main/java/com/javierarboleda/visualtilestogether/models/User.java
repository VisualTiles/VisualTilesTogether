package com.javierarboleda.visualtilestogether.models;


import com.google.firebase.auth.FirebaseUser;

public class User {
    String name;
    String email;
    String photoUrl;
    boolean isAnonymous;

    public User() {
    }

    public User(String email, boolean isAnonymous, String name, String photoUrl) {
        this.email = email;
        this.isAnonymous = isAnonymous;
        this.name = name;
        this.photoUrl = photoUrl;
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
        User user = new User(fbu.getEmail(),
                fbu.isAnonymous(),
                fbu.getDisplayName(),
                fbu.getPhotoUrl().toString());

        return user;
    }
}
