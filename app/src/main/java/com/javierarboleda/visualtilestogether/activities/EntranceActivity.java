package com.javierarboleda.visualtilestogether.activities;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TutorialImageAdapter;
import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

public class EntranceActivity extends AppCompatActivity {
  private static final String TAG = EntranceActivity.class.getSimpleName();
  private GoogleApiClient mGoogleApiClient;
  private static int RC_SIGN_IN = 1;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entrance);
    ViewPager pager = (ViewPager) findViewById(R.id.tutorial_view_pager);
    pager.setAdapter(new TutorialImageAdapter(this));
    TabLayout tabLayout = (TabLayout) findViewById(R.id.tutorial_tab_layout);
    tabLayout.setupWithViewPager(pager, true);
    pager.setPageTransformer(false, new ParallaxPagerTransformer(R.id.ivTutorial));

    buildSignInClient();
    loadSignIn();
    attemptAutoLogin();
  }

  private void attemptAutoLogin() {
    OptionalPendingResult<GoogleSignInResult> pendingResult =
        Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
    if (pendingResult.isDone()) {
      handleSignInResult(pendingResult.get());
    } else {
      pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
        @Override
        public void onResult(@NonNull GoogleSignInResult result) {
          handleSignInResult(result);
        }
      });
    }
  }

  private void loadSignIn() {
    SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
    signInButton.setSize(SignInButton.SIZE_STANDARD);
    findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startSignInIntent();
      }
    });
  }

  private void buildSignInClient() {
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build();
    // Build a GoogleApiClient with access to the Google Sign-In API and the
    // options specified by gso.
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
          }
        })
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();
  }

  private void startSignInIntent() {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      handleSignInResult(result);
    }
  }

  private void handleSignInResult(GoogleSignInResult result) {
    if (result.isSuccess()) {
      // Signed in successfully, show authenticated UI.
      GoogleSignInAccount acct = result.getSignInAccount();
      String idToken = acct.getIdToken();
      // TODO(geo): Use this token for the database calls.. maybe store somewhere.
      Toast.makeText(this, "Hello " + acct.getDisplayName(), Toast.LENGTH_LONG).show();
    }
  }

}
