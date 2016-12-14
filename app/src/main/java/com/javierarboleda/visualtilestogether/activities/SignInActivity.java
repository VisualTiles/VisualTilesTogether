/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.javierarboleda.visualtilestogether.activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;
import android.widget.VideoView;

import com.dd.CircularProgressButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TutorialImageAdapter;

import java.util.Timer;
import java.util.TimerTask;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignInActivity extends BaseVisualTilesActivity implements
        View.OnClickListener {
    private static final String LOG_TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private CircularProgressButton mSignInButton;

    private View preSignInButtons;

    private ViewPager mPager;

    Timer mTimer;
    int mPage = 0;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        super.setTopViewGroup((ViewGroup) findViewById(R.id.activity_entrance));

        bypassIfSignedIn();

        // Assign fields
        mSignInButton = (CircularProgressButton) findViewById(R.id.sign_in_button);
        preSignInButtons = findViewById(R.id.pre_sign_in_buttons);

        // Set click listeners
        mSignInButton.setOnClickListener(this);

        // Initialize FirebaseAuth.
        mFirebaseAuth = FirebaseAuth.getInstance();

        initTutorialView();

        resetPreSignInState();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void initTutorialView() {
        mPager = (ViewPager) findViewById(R.id.tutorial_view_pager);
        mPager.setAdapter(new TutorialImageAdapter(this));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tutorial_tab_layout);
        tabLayout.setupWithViewPager(mPager, true);

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new RemindTask(), 0, 4000);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mPage = position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Animation animation = new AlphaAnimation(0f, 1f);
        animation.setDuration(1400);
        findViewById(R.id.ivTutorial).startAnimation(animation);
        findViewById(R.id.tutorial_view_pager).startAnimation(animation);
        findViewById(R.id.tutorial_tab_layout).startAnimation(animation);
        findViewById(R.id.view_footer).startAnimation(animation);

        final VideoView videoView = (VideoView) findViewById(R.id.vvTutorialVideo);

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName()
                + "/" + R.raw.video_tutorial_1));

        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.seekTo(0);
                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.seekTo(0);
                videoView.start();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:

                animateSignInButton(1, 0, true);

                signIn();
                break;
        }
    }

    private void animateSignInButton(float fromAlpha, float toAlpha, final boolean invisible) {

        final View googleIconImageView = findViewById(R.id.ivGoogleIcon);
        final View signInTextView = findViewById(R.id.tvSignIn);
        final View buttonOutlineView =  findViewById(R.id.viewButtonOutline);

        Animation animation = new AlphaAnimation(fromAlpha, toAlpha);
        if (invisible) {
            animation.setDuration(300);
            mSignInButton.setIndeterminateProgressMode(true);
            mSignInButton.setProgress(50);
            mSignInButton.setClickable(false);
        } else {
            animation.setDuration(900);
            mSignInButton.setProgress(0);
            mSignInButton.setClickable(true);
        }


        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (invisible) {
                    googleIconImageView.setVisibility(View.INVISIBLE);
                    signInTextView.setVisibility(View.INVISIBLE);
                    buttonOutlineView.setVisibility(View.INVISIBLE);
                } else {
                    googleIconImageView.setVisibility(View.VISIBLE);
                    signInTextView.setVisibility(View.VISIBLE);
                    buttonOutlineView.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        googleIconImageView.startAnimation(animation);
        signInTextView.startAnimation(animation);
        buttonOutlineView .startAnimation(animation);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(LOG_TAG, "Google Sign In failed.");

                animateSignInButton(0, 1, false);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(LOG_TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignInActivity.this, "Google Auth failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(LOG_TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            app.initUser(task.getResult().getUser());
                        }
                    }
                });
    }

    @Override
    public void onChannelUpdated() {
        super.onChannelUpdated();
        if (app.getChannel() == null) {
            Toast.makeText(this, "Channel didn't load correctly.", Toast.LENGTH_LONG).show();
            resetPreSignInState();
            return;
        }
        launchMainActivity();
    }

    @Override
    public void onError(DatabaseError error) {
        super.onError(error);
        resetPreSignInState();
    }

    private void resetPreSignInState() {
        preSignInButtons.setVisibility(View.VISIBLE);
    }

    protected void bypassIfSignedIn() {
        if (app.getUser() != null) {
            if (app.getChannel() != null) {
                launchMainActivity();
            } else {
                launchChannelCreateActivity(findViewById(R.id.rlSignIn));
            }
        }
    }

    @Override
    public void onUserUpdated() {
        if (app.getUser() != null) {
            // Show channel creation or join buttons.
            launchChannelCreateActivity(findViewById(R.id.rlSignIn));
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // For ViewPager mTimer to change mPage
    class RemindTask extends TimerTask {

        @Override
        public void run() {

            // As the TimerTask run on a seprate thread from UI thread we have
            // to call runOnUiThread to do work on UI thread.
            runOnUiThread(new Runnable() {
                public void run() {

                    if (mPage > mPager.getAdapter().getCount() - 1) {
                        mPage = 0;
                        mPager.setCurrentItem(mPage++);
                    } else {
                        mPager.setCurrentItem(mPage++);
                    }
                }
            });

        }
    }
}
