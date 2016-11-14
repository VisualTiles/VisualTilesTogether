package com.javierarboleda.visualtilestogether;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.activities.SignInActivity;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class VisualTilesTogetherApp extends Application {
    private static final String LOG_TAG = VisualTilesTogetherApp.class.getSimpleName();
    private static final String ANONYMOUS = "anonymous";
    private static String username;
    private static FirebaseAuth firebaseAuth;
    private static String uId;
    private static User user = null;
    private static Channel channel = null;

    public interface VisualTilesListenerInterface {
        void onError(DatabaseError error);

        void onUserReady();

        void onChannelReady();
    }

    private static List<WeakReference<VisualTilesListenerInterface>> listeners = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase Auth.
        // Default username is anonymous.
        username = ANONYMOUS;
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Not signed in, launch the Sign In activity
            // NOTE(chris): This may cause a runtime exception, because startActivity is happening
            // outside of an activity context? I'm not sure why this is working.
            startActivity(new Intent(this, SignInActivity.class));
            return;
        } else {
            uId = firebaseUser.getUid();
            username = firebaseUser.getDisplayName();
            initUser(firebaseUser);
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

    public static Channel getChannel() {
        return channel;
    }

    public static void resetUserame() {
        username = ANONYMOUS;
    }

    public static void initUser(final FirebaseUser fbu) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference dbUserRef = dbRef.child(User.TABLE_NAME).child(fbu.getUid());
        dbUserRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            user = dataSnapshot.getValue(User.class);
                        } else {
                            // Create user for first time.
                            user = User.fromFirebaseUser(fbu);
                            dbUserRef.setValue(user);
                        }
                        for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                            if (listener.get() != null)
                                listener.get().onUserReady();
                        }
                        // TODO(team): Add this back in when user can leave a channel
                        // initChannel(user.getChannelId());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                            if (listener.get() != null)
                                listener.get().onError(databaseError);
                        }
                    }
                });
    }

    public static void initChannel() {
        initChannel(user.getChannelId());
    }

    public static void initChannel(String channelId) {
        if (channelId == null) {
            // Fallback channel for when it doesn't exist.
            // TODO(jav): Remove this once create/join login screen is working.
            channelId = "-KWFW_yHrzu2JtKMURlq";
        }
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dbChannel = dbRef.child(Channel.TABLE_NAME);
        dbChannel.child(channelId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        channel = dataSnapshot.getValue(Channel.class);

                        for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                            if (listener.get() != null)
                                listener.get().onChannelReady();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                            if (listener.get() != null)
                                listener.get().onError(databaseError);
                        }
                    }
                });

        // Enforce that user and channel are in sync.
        if (user.getChannelId() != channelId) {
            Log.i(LOG_TAG, "User channel is not the same channel as intiChannel!");
            user.setChannelId(channelId);
            DatabaseReference dbUserRef = dbRef.child(User.TABLE_NAME).child(uId);
            dbUserRef.setValue(user);
        }
    }

    public static void addListener(VisualTilesListenerInterface listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public static void removeListener(VisualTilesListenerInterface listener) {
        for (Iterator<WeakReference<VisualTilesListenerInterface>> iterator = listeners.iterator();
             iterator.hasNext(); ) {
            WeakReference<VisualTilesListenerInterface> weakRef = iterator.next();
            if (weakRef.get() == listener) {
                iterator.remove();
            }
        }
    }
}
