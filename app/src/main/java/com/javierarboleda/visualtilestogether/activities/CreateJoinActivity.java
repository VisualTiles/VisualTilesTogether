package com.javierarboleda.visualtilestogether.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.databinding.ActivityCreateJoinBinding;
import com.javierarboleda.visualtilestogether.fragments.ChannelAddDialog;
import com.javierarboleda.visualtilestogether.models.Channel;

/**
 * Created on 11/13/16.
 */

public class CreateJoinActivity extends AppCompatActivity implements
        ChannelAddDialog.OnFragmentInteractionListener,
        VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String LOG_TAG = CreateJoinActivity.class.getSimpleName();

    ActivityCreateJoinBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_join);
        VisualTilesTogetherApp.addListener(this);
    }

    @Override
    protected void onDestroy() {
        VisualTilesTogetherApp.removeListener(this);
        super.onDestroy();
    }

    public void joinEventOnClick(View view) {
        String channel = binding.etEventCode.getText().toString();
        if (channel == null || channel.isEmpty()) {
            VisualTilesTogetherApp.initChannel();
        } else {
            VisualTilesTogetherApp.initChannel(channel);
        }

//        if (VisualTilesTogetherApp.getChannel() == null) {
//            Toast.makeText(this, "Channel is null but IDK LOL!", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "Channel is ready too =)", Toast.LENGTH_LONG).show();
//        }
//        startActivity(new Intent(this, MainActivity.class));
//        finish();

    }

    public void createNewEventOnClick(View view) {

        ChannelAddDialog.newInstance().show(getSupportFragmentManager(), "new channel");
    }

    @Override
    public void onFragmentInteraction(Channel channel) {
        Log.d(LOG_TAG, "new channel (" + channel.getName() + ", "
                + channel.getStartTime().toString() + ", "
                + channel.getEndTime().toString() + ")");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(
                Channel.TABLE_NAME);
        String key = dbRef.push().getKey();
        dbRef.child(key).setValue(channel);
        VisualTilesTogetherApp.addListener(this);
        VisualTilesTogetherApp.getUser().setChannelId(key);
        VisualTilesTogetherApp.initChannel(key);
        Log.d(LOG_TAG, "key is " + key);
    }


    @Override
    public void onError(DatabaseError error) {
        Toast.makeText(this, "Oh no! User db failed (or cancelled)!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUserReady() {

    }

    @Override
    public void onChannelReady() {
        if (VisualTilesTogetherApp.getChannel() == null) {
            Toast.makeText(this, "Channel is null but IDK LOL!", Toast.LENGTH_LONG).show();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
