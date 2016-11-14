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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.adapters.TutorialImageAdapter;
import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String LOG_TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private SignInButton mSignInButton;

    private View preSignInButtons;
    private View postSignInButtons;
    private Button btnCreateChannel;
    private Button btnJoinChannel;

    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Assign fields
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        preSignInButtons = findViewById(R.id.pre_sign_in_buttons);
        postSignInButtons = findViewById(R.id.post_sign_in_buttons);
        btnCreateChannel = (Button) findViewById(R.id.btnCreateChannel);
        btnJoinChannel = (Button) findViewById(R.id.btnJoinChannel);
        btnJoinChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO(jav): FIX ME! This is allowing you in with a null channel!
                onChannelReady();
            }
        });

        // Set click listeners
        mSignInButton.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth.
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Listen for user and channel callbacks.
        VisualTilesTogetherApp.addListener(this);

        initTutorialView();

        showPreSignInButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VisualTilesTogetherApp.removeListener(this);
    }

    private void initTutorialView() {
        ViewPager pager = (ViewPager) findViewById(R.id.tutorial_view_pager);
        pager.setAdapter(new TutorialImageAdapter(this));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tutorial_tab_layout);
        tabLayout.setupWithViewPager(pager, true);
        pager.setPageTransformer(false, new ParallaxPagerTransformer(R.id.ivTutorial));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
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
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(LOG_TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
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
                            VisualTilesTogetherApp.initUser(task.getResult().getUser());
                        }
                    }
                });
    }

    @Override
    public void onChannelReady() {
        if (VisualTilesTogetherApp.getChannel() == null) {
            Toast.makeText(this, "Channel didn't load correctly.", Toast.LENGTH_LONG).show();
        }
        startActivity(new Intent(this, TileListActivity.class));
        finish();
    }

    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Oh no! User db failed (or cancelled)!", Toast.LENGTH_LONG).show();
        showPreSignInButtons();
    }

    private void showPreSignInButtons() {
        preSignInButtons.setVisibility(View.VISIBLE);
        postSignInButtons.setVisibility(View.GONE);
    }

    private void showPostSignInButtons() {

        Intent intent = new Intent(this, CreateJoinActivity.class);
        startActivity(intent);
        finish();

//        preSignInButtons.setVisibility(View.GONE);
//        postSignInButtons.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserReady() {
        // Show channel creation or join buttons.
        showPostSignInButtons();
    }
}
