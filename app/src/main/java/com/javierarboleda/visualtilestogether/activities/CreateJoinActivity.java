package com.javierarboleda.visualtilestogether.activities;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dd.CircularProgressButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.ActivityCreateJoinBinding;
import com.javierarboleda.visualtilestogether.models.Channel;

import java.lang.ref.WeakReference;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.javierarboleda.visualtilestogether.VisualTilesTogetherApp.PREF_REQUESTED_CHANNEL;
import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.setChannelQrCode;

/**
 * Created on 11/13/16.
 */

public class CreateJoinActivity extends BaseVisualTilesActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = CreateJoinActivity.class.getSimpleName();
    private static final int RC_BARCODE_CAPTURE = 9001;
    public static String CX_KEY = "cx_key";
    public static String CY_KEY = "cY_key";

    private ActivityCreateJoinBinding binding;
    private RelativeLayout rootLayout;
    private TextInputLayout mJoinTiLayout;
    private TextInputEditText mJoinTiEditText;
    private TextInputLayout mCreateNameTiLayout;
    private TextInputEditText mCreateNameTiEditText;
    private TextInputLayout mJCreateCodeTiLayout;
    private TextInputEditText mCreateCodeTiEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (shouldNotLoad()) return;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_join);
        super.setTopViewGroup(binding.rvRootLayout);
        setUpLayout();
        setupCircularTransition(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String pendingChannel = sharedPreferences.getString(PREF_REQUESTED_CHANNEL, "");
        if (!pendingChannel.isEmpty()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage(
                    String.format(getString(R.string.join_channel_message), pendingChannel))
                    .setTitle(R.string.app_name)
                    .setPositiveButton(R.string.join_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mJoinTiEditText.setText(pendingChannel);
                            binding.cpbJoinButton.callOnClick();
                            sharedPreferences.edit().remove(PREF_REQUESTED_CHANNEL).apply();
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Close.
                                    sharedPreferences.edit().remove(PREF_REQUESTED_CHANNEL).apply();
                                    dialogInterface.cancel();
                                }
                            });
            dialogBuilder.create().show();
        }
    }

    private void setUpLayout() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Glide.with(this).load(R.drawable.vtbg3)
                .bitmapTransform(new BlurTransformation(this, 20))
                .into((ImageView) findViewById(R.id.ivBackgroundImage));

        setupTextFields();
    }

    private void setupTextFields() {
        mJoinTiEditText = binding.tieJoin;
        mJoinTiLayout = binding.tilJoin;
        mJoinTiLayout.setHint(getString(R.string.enter_event_code));
        mJoinTiEditText.setOnEditorActionListener(ActionListener.newInstance(this));

        mCreateNameTiEditText = binding.tieCreateEventName;
        mCreateNameTiLayout = binding.tilCreateEventName;
        mCreateNameTiLayout.setHint(getString(R.string.event_name));
        mCreateNameTiEditText.setOnEditorActionListener(ActionListener.newInstance(this));

        mCreateCodeTiEditText = binding.tieCreateEventCode;
        mJCreateCodeTiLayout = binding.tilCreateEventCode;
        mJCreateCodeTiLayout.setHint(getString(R.string.event_code));
        mCreateCodeTiEditText.setOnEditorActionListener(ActionListener.newInstance(this));

        binding.btnQrCode.requestFocus();
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
                            //noinspection deprecation
                            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }
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
                        app.initChannel(channelId);
                    } else {
                        String uniqueName = parseUniqueNameFromUrl(channelId);
                        Log.d(LOG_TAG, "URL link param channel code: " + uniqueName);
                        if (uniqueName != null) {
                            tryToJoinEvent(uniqueName);
                        } else {
                            Snackbar snackbar = Snackbar
                                    .make(binding.getRoot(), "There's no event for this QR code", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
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

    private String parseUniqueNameFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Uri uri = Uri.parse(url);
        if (!uri.getAuthority().equals(getString(R.string.qr_code_authority))) {
            return null;
        }
        String linkParam = uri.getQueryParameter(getString(R.string.link_param));
        if (linkParam == null) {
            return null;
        }
        return linkParam.substring(linkParam.lastIndexOf('/') + 1);
    }

    private boolean validChannelId(String channelId) {
        if (TextUtils.isEmpty(channelId)) return false;
        return channelId.matches("[-A-Za-z0-9_]*");
    }

    public void joinEventOnClick(View view) {
        if (mJoinTiEditText.getText().toString().isEmpty()) {
            showError("Event code cannot be empty.");
            return;
        }
        hideError();
        animateJoinEventButton(1, 0, true);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                joinEvent();
            }
        }, 500);


    }

    private void animateJoinEventButton(float fromAlpha, float toAlpha, boolean invisible) {
        View[] views = new View[2];
        views[0] = binding.tvJoinText;
        views[1] = binding.viewButtonOutline;
        animateButton(binding.cpbJoinButton, views, fromAlpha, toAlpha, invisible);
    }

    private void joinEvent() {
        tryToJoinEvent(binding.tieJoin.getText().toString());
    }

    private void tryToJoinEvent(String channel) {
        if (channel == null || channel.isEmpty()) {
            app.initChannel();
        } else {
            if (channel.length() > 8) {
                // probably a channelId
                app.initChannel(channel);
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
                            //noinspection LoopStatementThatDoesntLoop
                            for (DataSnapshot channelSnapshot : dataSnapshot.getChildren()) {
                                app.initChannel(channelSnapshot.getKey());
                                break;
                            }
                        } else {
                            // TODO: handle incorrect event code properly
                            app.initChannel();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }
    }

    private void animateButton(CircularProgressButton button, final View[] views, float fromAlpha,
                               float toAlpha, final boolean invisible) {

        Animation animation = new AlphaAnimation(fromAlpha, toAlpha);
        if (invisible) {
            animation.setDuration(300);
            button.setIndeterminateProgressMode(true);
            button.setProgress(50);
            button.setClickable(false);
        } else {
            animation.setDuration(900);
            button.setProgress(0);
            button.setClickable(true);
        }

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (invisible) {
                    for (View view : views) {
                        view.setVisibility(View.INVISIBLE);
                    }
                } else {
                    for (View view : views) {
                        view.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        for (View view : views) {
            view.startAnimation(animation);
        }
    }

    public void createNewEventOnClick(View view) {
        // reset error states
        mCreateNameTiLayout.setErrorEnabled(false);
        mJCreateCodeTiLayout.setErrorEnabled(false);
        boolean error = false;

        final String channelName = mCreateNameTiEditText.getText().toString();
        final String uniqueName = mCreateCodeTiEditText.getText().toString();
        if (TextUtils.isEmpty(channelName)) {
            mCreateNameTiLayout.setError(getString(R.string.error_event_name_empty));
            error = true;
        }
        if (TextUtils.isEmpty(uniqueName)) {
            mJCreateCodeTiLayout.setError(getString(R.string.error_event_code_empty));
        } else {
            if (!error) {
                animateCreateEventButton(1, 0, true);

                final Handler handler = new Handler();
                final boolean finalError = error;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishIfUnique(uniqueName, channelName, app.getUid(),
                                finalError);
                    }
                }, 500);
            }
        }
    }

    private void animateCreateEventButton(float fromAlpha, float toAlpha, boolean invisible) {
        View[] views = new View[2];
        views[0] = binding.tvCreateEventText;
        views[1] = binding.viewButtonOutlineCreateEvent;
        animateButton(binding.cpbCreateEventButton, views, fromAlpha, toAlpha, invisible);
    }

    private void finishIfUnique(final String uniqueName,
                                final String name,
                                final String uId,
                                final boolean errorFlagged) {
        DatabaseReference channelRef = FirebaseDatabase.getInstance().getReference()
                .child(Channel.TABLE_NAME);
        channelRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean unique = true;
                boolean error = errorFlagged;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Channel channel = data.getValue(Channel.class);
                    if (uniqueName.equals(channel.getUniqueName())) {
                        unique = false;
                    }
                }
                if (!unique) {
                    mJCreateCodeTiLayout.setError(uniqueName
                            + getString(R.string.error_event_code_in_use));
                    error = true;
                    animateCreateEventButton(0, 1, false);
                }
                if (!error) {
                    Channel channel = new Channel(name, uniqueName, uId);
                    channel.setLayoutId("demo");
                    onFragmentInteraction(channel);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void  onFragmentInteraction(Channel channel) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(
                Channel.TABLE_NAME);
        String key = dbRef.push().getKey();
        dbRef.child(key).setValue(channel);
        setChannelQrCode(getApplicationContext(), key, channel.getUniqueName());
        app.initChannel(key);
        Log.d(LOG_TAG, "key is " + key);
    }

    @Override
    public void onChannelUpdated() {
        super.onChannelUpdated();
        if (app.getChannel() != null) {
            // Channel is ready.
            startActivity(new Intent(this, TileListActivity.class));
            finish();
        } else {
            showError("Event code does not exist.");
            animateJoinEventButton(0, 1, false);
        }
    }

    private void circularRevealActivity(int cx, int cy) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = null;
            circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);
            circularReveal.setDuration(700);

            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        }
    }

    private boolean shouldShowError() {
        int textLength = mJoinTiEditText.getText().length();
        return textLength > 0 && textLength < 10;
    }

    private void showError(String error) {
        mJoinTiLayout.setErrorEnabled(true);
        mJoinTiLayout.setError(error);
    }

    private void hideError() {
        mJoinTiLayout.setErrorEnabled(false);
    }

    public void signOut(View view) {
        app.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private static final class ActionListener implements TextView.OnEditorActionListener {
        private final WeakReference<CreateJoinActivity> mainActivityWeakReference;

        public static ActionListener newInstance(CreateJoinActivity mainActivity) {
            WeakReference<CreateJoinActivity> mainActivityWeakReference = new WeakReference<>(mainActivity);
            return new ActionListener(mainActivityWeakReference);
        }

        private ActionListener(WeakReference<CreateJoinActivity> mainActivityWeakReference) {
            this.mainActivityWeakReference = mainActivityWeakReference;
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            CreateJoinActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity != null) {
                if (actionId == EditorInfo.IME_ACTION_GO && mainActivity.shouldShowError()) {
                    mainActivity.showError("Danger!");
                } else {
                    mainActivity.hideError();
                }
            }
            return true;
        }
    }

}
