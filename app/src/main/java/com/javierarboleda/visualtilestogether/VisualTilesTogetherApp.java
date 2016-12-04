package com.javierarboleda.visualtilestogether;

import android.app.Application;
import android.databinding.ObservableArrayMap;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class VisualTilesTogetherApp extends Application {
    private static final String LOG_TAG = VisualTilesTogetherApp.class.getSimpleName();
    private static final String ANONYMOUS = "anonymous";
    private FirebaseAuth firebaseAuth;
    private String uId;
    private User user = null;
    private String channelId = null;
    private Channel channel = null;
    private ObservableArrayMap<String, Tile> tileObservableArrayMap =
            new ObservableArrayMap<>();

    public interface VisualTilesListenerInterface {
        void onError(DatabaseError error);

        void onTilesUpdated();

        void onUserUpdated();

        void onChannelUpdated();
    }

    private static List<WeakReference<VisualTilesListenerInterface>> listeners = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase Auth.
        // Default username is anonymous.
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // Not signed in, launch the Sign In activity
            // NOTE(chris): This may cause a runtime exception, because startActivity is happening
            // outside of an activity context? I'm not sure why this is working.
            // startActivity(new Intent(this, SignInActivity.class));
            // Disabled because all other activities enforce valid user or channel.
        } else {
            uId = firebaseUser.getUid();
            initUser(firebaseUser);
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/RobotoCondensed-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public String getUid() {
        return uId;
    }

    public User getUser() {
        return user;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getChannelId() {
        return channelId;
    }

    public ObservableArrayMap<String, Tile> getTileObservableArrayMap() {
        return tileObservableArrayMap;
    }

    public void signOut() {
        cleanUpSession();
        getFirebaseAuth().signOut();
    }
    private void cleanUpChannel() {
        this.channel = null;
        this.channelId = null;
        if (dbChannelRef != null)
            dbChannelRef.removeEventListener(channelValueEventListener);
        dbChannelRef = null;
        if (dbTileRef != null)
            dbTileRef.removeEventListener(tileEventListener);
        dbTileRef = null;
        tileObservableArrayMap.clear();
    }
    private void cleanUpSession() {
        // Clean up channel derived fields.
        cleanUpChannel();
        dbUserRef.removeEventListener(userValueEventListener);
        user = null;
        uId = null;
        dbUserRef = null;
    }

    private ValueEventListener userValueEventListener =
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getKey() != uId) {
                        cleanUpSession();
                    }
                    if (dataSnapshot.exists()) {
                        uId = dataSnapshot.getKey();
                        user = dataSnapshot.getValue(User.class);
                    } else {
                        // Save the current user object if it's not in DB already.
                        dbUserRef.setValue(user);
                    }
                    for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                        if (listener.get() != null)
                            listener.get().onUserUpdated();
                    }
                    // TODO(team): Add this back in when user can leave a channel
                    // initChannel(user.getChannelId());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // This was no error if the user is being logged out. Notify in onUserUpdated.
                    if (uId == null || user == null) {
                        for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                            if (listener.get() != null)
                                listener.get().onUserUpdated();
                        }
                    } else {
                        for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                            if (listener.get() != null)
                                listener.get().onError(databaseError);
                        }
                    }
                }
            };
    private static DatabaseReference dbUserRef;

    public void initUser(final FirebaseUser fbu) {
        // Create basic user from FBU. Has fewer fields (channel is null).
        uId = fbu.getUid();
        user = User.fromFirebaseUser(fbu);
        if (dbUserRef != null) {
            dbUserRef.removeEventListener(userValueEventListener);
        }
        // Try to load 'full'.
        dbUserRef = FirebaseDatabase.getInstance().getReference().child(User.TABLE_NAME)
                .child(fbu.getUid());
        dbUserRef.addValueEventListener(userValueEventListener);
    }

    private ValueEventListener channelValueEventListener =
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    channel = dataSnapshot.getValue(Channel.class);

                    for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                        if (listener.get() != null)
                            listener.get().onChannelUpdated();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // This was no error if the channel is left. Notify in onChannelUpdated.
                    if (channelId == null || channel == null) {
                        for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                            if (listener.get() != null)
                                listener.get().onChannelUpdated();
                        }
                    } else {
                        for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                            if (listener.get() != null)
                                listener.get().onError(databaseError);
                        }
                    }
                }
            };

    public void initChannel() {
        initChannel(user.getChannelId());
    }

    private DatabaseReference dbChannelRef;
    public void initChannel(String newChannelId) {
        if (newChannelId == null) {
            // Fallback channel for when it doesn't exist.
            // TODO(jav): Remove this once create/join login screen is working.
            newChannelId = "-KWVuJtz9tfBvdQUn4F_";
        }

        if (dbChannelRef != null)
            dbChannelRef.removeEventListener(channelValueEventListener);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbChannelRef = dbRef.child(Channel.TABLE_NAME).child(newChannelId);
        dbChannelRef.addValueEventListener(channelValueEventListener);
        // Already loaded, notify the activity and then skip.
        if (channelId != null && channelId.equals(newChannelId)) {
            channelId = newChannelId;
            for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                if (listener.get() != null)
                    listener.get().onChannelUpdated();
            }
        }

        channelId = newChannelId;
        // Enforce that user and channel are in sync.
        if (user.getChannelId() != channelId) {
            Log.i(LOG_TAG, "User channel is not the same channel as initChannel!");
            user.setChannelId(channelId);

            // Update channel field in DB.
            // Switched from undateChildren() to setValue()
            // so that only the ChannelId field would be rewritten instead of the entire entry.
            // Not sure why this was called after adding a new tile,
            // but it was making the tileIds entry disappear. (geo.)
//            HashMap<String, Object> userUpdates = new HashMap<>();
//            userUpdates.put(User.CHANNEL_ID, channelId);
//            dbRef.child(User.TABLE_NAME).child(uId).updateChildren(userUpdates);
            dbRef.child(User.TABLE_NAME).child(uId).child(User.CHANNEL_ID).setValue(channelId);
        }
        initTilesForChannel();
    }

    private ChildEventListener tileEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            Tile tile = dataSnapshot.getValue(Tile.class);
            tile.setTileId(key);
            Log.i(LOG_TAG, "onChildAdded key " + key);
            if (!tileObservableArrayMap.containsKey(key) ||
                    !tileObservableArrayMap.get(key).equalsValue(tile)) {
                tileObservableArrayMap.put(key, tile);
                Glide.with(VisualTilesTogetherApp.this)
                        .load(tile.getShapeUrl())
                        .downloadOnly(400, 400);
            }
            for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                if (listener.get() != null)
                    listener.get().onTilesUpdated();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            Tile tile = dataSnapshot.getValue(Tile.class);
            tile.setTileId(key);
            Log.i(LOG_TAG, "onChildChanged key " + key);
            Tile mapTile = tileObservableArrayMap.get(key);
            if (mapTile == null || !mapTile.equalsValue(tile)) {
                tileObservableArrayMap.put(key, tile);
            }
            for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                if (listener.get() != null)
                    listener.get().onTilesUpdated();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            Log.i(LOG_TAG, "onChildRemoved key " + key);
            Tile tile = dataSnapshot.getValue(Tile.class);
            tile.setTileId(key);
            if (tileObservableArrayMap.containsKey(key)) {
                tileObservableArrayMap.remove(key);
            }
            for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                if (listener.get() != null)
                    listener.get().onTilesUpdated();
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            // Not interested in this, its covered by onChildChanged.
            Log.i(LOG_TAG, "onChildMoved key " + dataSnapshot.getKey());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            for (WeakReference<VisualTilesListenerInterface> listener : listeners) {
                if (listener.get() != null)
                    listener.get().onError(databaseError);
            }
        }
    };
    private Query dbTileRef;
    private void initTilesForChannel() {
        dbTileRef = FirebaseDatabase.getInstance().getReference().child(Tile.TABLE_NAME)
                .orderByChild(Tile.CHANNEL_ID).equalTo(channelId);
        // Adds unnecessary complexity (onChildMoved):
        // .orderByChild(Tile.POS_VOTES_ID);
        dbTileRef.removeEventListener(tileEventListener);
        dbTileRef.addChildEventListener(tileEventListener);
    }

    public void leaveChannel() {
        cleanUpChannel();
        if (this.user != null && dbUserRef != null) {
            user.setChannelId(null);
            dbUserRef.child(User.CHANNEL_ID).setValue(null);
        }
    }

    public boolean isChannelModerator() {
        if (user == null || channel == null) {
            return false;
        }
        return channel.hasModerator(uId);
    }

    public void addListener(VisualTilesListenerInterface listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public void removeListener(VisualTilesListenerInterface listener) {
        for (Iterator<WeakReference<VisualTilesListenerInterface>> iterator = listeners.iterator();
             iterator.hasNext(); ) {
            WeakReference<VisualTilesListenerInterface> weakRef = iterator.next();
            if (weakRef.get() == listener || weakRef.get() == null) {
                iterator.remove();
            }
        }
    }
}
