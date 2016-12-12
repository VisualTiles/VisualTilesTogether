package com.javierarboleda.visualtilestogether.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;

import java.util.HashMap;

/**
 * Created by chris on 12/11/16.
 */

public abstract class BaseVisualTilesActivity extends AppCompatActivity
implements GoogleApiClient.OnConnectionFailedListener,
        VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String TAG = BaseVisualTilesActivity.class.getSimpleName();
    protected GoogleApiClient mGoogleApiClient;
    protected VisualTilesTogetherApp app;
    protected FirebaseDatabase db;
    private ViewGroup topViewGroup;
    private HashMap<ValueEventListener, Query> firebaseListeners = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Listen for user and channel callbacks.
        app = (VisualTilesTogetherApp) getApplication();
        app.addListener(this);
        db = FirebaseDatabase.getInstance();

        if (!handleIfSignedOut()) {
            handleIfLeftChannel();
        }

        startOfflineListener();

        // Configure Google Sign In API.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(
                        this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppInvite.API)
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.removeListener(this);
        for (ValueEventListener listener : firebaseListeners.keySet()) {
            firebaseListeners.get(listener).removeEventListener(listener);
        }
    }

    private ValueEventListener offlineListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            boolean connected = dataSnapshot.getValue(Boolean.class);
            setOnlineState(connected);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private void startOfflineListener() {
        Query ref = db.getReference(".info/connected");
        ref.addValueEventListener(offlineListener);
        firebaseListeners.put(offlineListener, ref);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        if (topViewGroup != null) {
            Snackbar.make(topViewGroup, "Google Play Services error.", Snackbar.LENGTH_LONG)
                    .show();
        } else {
            // An unresolvable error has occurred and Google APIs (including Sign-In) will not
            // be available.
            Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void launchMainActivity() {
        startActivity(new Intent(this, TileListActivity.class));
        finish();
    }

    protected void launchSignInActivity() {
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    protected void launchChannelCreateActivity() {
        Intent intent = new Intent(this, CreateJoinActivity.class);

        // pass center coordinates of sign in button for circular transition start point
        View signInView = findViewById(R.id.rlSignIn);

        if (signInView != null) {
            int[] xy = new int[2];
            signInView.getLocationOnScreen(xy);

            int cx = xy[0] + (signInView.getWidth() / 2);
            int cy = xy[1] + (signInView.getHeight() / 2);

            intent.putExtra(CreateJoinActivity.CX_KEY, cx);
            intent.putExtra(CreateJoinActivity.CY_KEY, cy);
        }

        startActivity(intent);
        finish();
    }

    protected void signOut() {
        app.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        launchSignInActivity();
    }

    @Override
    public void onChannelUpdated() {
        handleIfLeftChannel();
    }

    @Override
    public void onError(DatabaseError error) {
        if (topViewGroup != null) {
            Snackbar.make(topViewGroup, "Internal error occurred.", Snackbar.LENGTH_LONG)
                    .show();
        } else {
            Toast.makeText(this, "Internal error occurred.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTilesUpdated() {

    }

    private boolean handleIfSignedOut() {
        if (app.getUid() == null || app.getUid().isEmpty() || app.getUser() == null) {
            if (!(this instanceof SignInActivity)) {
                // Go to sign in page.
                launchSignInActivity();
                return true;
            }
        }
        return false;
    }

    protected void setTopViewGroup(ViewGroup topViewGroup) {
        this.topViewGroup = topViewGroup;
    }

    private boolean isOnline = false;
    private Snackbar offlineSnackbar;
    private void setOnlineState(boolean isOnline) {
        this.isOnline = isOnline;

        if (offlineSnackbar != null) {
            offlineSnackbar.dismiss();
        }
        if (!isOnline) {
            if (this.topViewGroup != null) {
                offlineSnackbar = Snackbar.make(topViewGroup, "You are now offline.",
                        Snackbar.LENGTH_LONG);
                offlineSnackbar.show();
            } else {
                Toast.makeText(this, "You are now offline.", Toast.LENGTH_LONG).show();
            }
        } else {
        }
    }

    public boolean isOnline() {
        return isOnline;
    }

    private boolean handleIfLeftChannel() {
        if (app.getChannelId() == null || app.getChannelId().isEmpty()
                || app.getChannel() == null) {
            if (!(this instanceof CreateJoinActivity) && (!(this instanceof SignInActivity))) {
                // Go to sign in page.
                launchChannelCreateActivity();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUserUpdated() {
        handleIfSignedOut();
    }
}
