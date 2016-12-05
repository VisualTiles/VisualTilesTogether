package com.javierarboleda.visualtilestogether.activities;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.databinding.ActivityCreateJoinBinding;
import com.javierarboleda.visualtilestogether.fragments.ChannelAddDialog;
import com.javierarboleda.visualtilestogether.models.Channel;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.setChannelQrCode;

/**
 * Created on 11/13/16.
 */

public class CreateJoinActivity extends AppCompatActivity implements
        ChannelAddDialog.OnFragmentInteractionListener,
        VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String LOG_TAG = CreateJoinActivity.class.getSimpleName();
    private static final int RC_BARCODE_CAPTURE = 9001;
    public static String CX_KEY = "cx_key";
    public static String CY_KEY = "cY_key";

    private ActivityCreateJoinBinding binding;
    private VisualTilesTogetherApp visualTilesTogetherApp;
    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_join);
        visualTilesTogetherApp = (VisualTilesTogetherApp) getApplication();
        if (visualTilesTogetherApp.getUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
        visualTilesTogetherApp.addListener(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Glide.with(this).load(R.drawable.vtbg2)
                .bitmapTransform(new BlurTransformation(this, 5))
                .into((ImageView) findViewById(R.id.ivBackgroundImage));

        setupCircularTransition(savedInstanceState);
    }

    private void setupCircularTransition(Bundle savedInstanceState) {
        rootLayout = binding.rvRootLayout;

        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move);

        final int cx = getIntent().getIntExtra(CX_KEY, -1);
        final int cy = getIntent().getIntExtra(CY_KEY, -1);

        if (savedInstanceState == null && cx != 0) {
            rootLayout.setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity(cx, cy);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        visualTilesTogetherApp.removeListener(this);
        super.onDestroy();
    }

    public void joinEventOnQrCode(View view) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, getAutoFocus());
        intent.putExtra(BarcodeCaptureActivity.UseFlash, getUseFlash());
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    private boolean getUseFlash() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean(getString(R.string.pref_useflash), false);
    }

    private boolean getAutoFocus() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean(getString(R.string.pref_autofocus), true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Log.d(LOG_TAG, "QR read: " + barcode.displayValue);
                    String channelId = barcode.displayValue;
                    if (validChannelId(channelId)) {
                        visualTilesTogetherApp.initChannel(channelId);
                    } else {
                        Log.d(LOG_TAG, "should be showing a snackbar here");
                        Snackbar snackbar = Snackbar
                                .make(binding.getRoot(), "There's no event for this QR code", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                } else {
                    Log.d(LOG_TAG, "No QR code captured, intent data is null");
                }
            } else {
                Log.d(LOG_TAG, "Error reading QR code");
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean validChannelId(String channelId) {
        if (TextUtils.isEmpty(channelId)) return false;
        return channelId.matches("[-A-Za-z0-9_]*");
    }

    public void joinEventOnClick(View view) {
        final String channel = binding.etEventCode.getText().toString();
        if (channel == null || channel.isEmpty()) {
            visualTilesTogetherApp.initChannel();
        } else {
            if (channel.length() >= 16  ) {
                // probably a channelId
                visualTilesTogetherApp.initChannel(channel);
            } else {
                // assume an event code
                // look up its channelId and use that
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference dbChannels = dbRef.child(Channel.TABLE_NAME);
                Query eventCodeRef = dbChannels
                        .orderByChild(Channel.CHANNEL_UNIQUE_NAME)
                        .equalTo(channel);
                eventCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot channelSnapshot : dataSnapshot.getChildren()) {
                                visualTilesTogetherApp.initChannel(channelSnapshot.getKey());
                                break;
                            }
                        } else {
                            // TODO: handle incorrect event code properly
                            visualTilesTogetherApp.initChannel();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }
    }

    public void createNewEventOnClick(View view) {
        ChannelAddDialog.newInstance().show(getSupportFragmentManager(), "new channel");
    }

    @Override
    public void onFragmentInteraction(Channel channel) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(
                Channel.TABLE_NAME);
        String key = dbRef.push().getKey();
        dbRef.child(key).setValue(channel);
        setChannelQrCode(key, channel.getUniqueName());
        visualTilesTogetherApp.addListener(this);
        visualTilesTogetherApp.getUser().setChannelId(key);
        visualTilesTogetherApp.initChannel(key);
        Log.d(LOG_TAG, "key is " + key);
    }


    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Oh no! User db failed (or cancelled)!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUserUpdated() {
        // User updates may naturally happen (if channel ID changes for example).
        // Only handle log-out race conditions.
        if (visualTilesTogetherApp.getUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }

    @Override
    public void onChannelUpdated() {
        if (visualTilesTogetherApp.getChannelId() != null &&
            visualTilesTogetherApp.getChannel() == null) {
            Toast.makeText(this, "Joining channel failed.", Toast.LENGTH_LONG).show();
            return;
        }
        // Channel is ready.
        startActivity(new Intent(this, TileListActivity.class));
        finish();
    }

    @Override
    public void onTilesUpdated() {
        // Not interested at this point. Do nothing.
    }

    private void circularRevealActivity(int cx, int cy) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = null;
            circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);
            circularReveal.setDuration(1000);

            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        }
    }

}
