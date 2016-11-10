package com.javierarboleda.visualtilestogether;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.javierarboleda.visualtilestogether.activities.SignInActivity;
import com.javierarboleda.visualtilestogether.models.User;


public class VisualTilesTogetherApp extends Application {
    private static final String LOG_TAG = VisualTilesTogetherApp.class.getSimpleName();
    private static final String ANONYMOUS = "anonymous";
    private static String username;
    private static FirebaseAuth firebaseAuth;
    private static String uId;
    private static User user;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase Auth
        // Default username is anonymous.
        username = ANONYMOUS;
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            return;
        } else {
            uId = firebaseUser.getUid();
            username = firebaseUser.getDisplayName();
            user = User.fromFirebaseUser(firebaseUser);
        }
    }

    public static FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public static String getUid() {
        return uId;
    }

    public static User getUser() {
        return user;
    }

    public static void resetUserame() {
        username = ANONYMOUS;
    }
}
