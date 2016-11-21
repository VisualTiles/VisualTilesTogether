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

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.setChannelQrCode;

/**
 * Created on 11/13/16.
 */

public class CreateJoinActivity extends AppCompatActivity implements
        ChannelAddDialog.OnFragmentInteractionListener,
        VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String LOG_TAG = CreateJoinActivity.class.getSimpleName();

    private ActivityCreateJoinBinding binding;
    private VisualTilesTogetherApp visualTilesTogetherApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_join);
        visualTilesTogetherApp = (VisualTilesTogetherApp) getApplication();
        visualTilesTogetherApp.addListener(this);
    }

    @Override
    protected void onDestroy() {
        visualTilesTogetherApp.removeListener(this);
        super.onDestroy();
    }

    public void joinEventOnClick(View view) {
        String channel = binding.etEventCode.getText().toString();
        if (channel == null || channel.isEmpty()) {
            visualTilesTogetherApp.initChannel();
        } else {
            visualTilesTogetherApp.initChannel(channel);
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
        setChannelQrCode(key);
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
        // Do nothing.
    }

    @Override
    public void onChannelUpdated() {
        if (visualTilesTogetherApp.getChannel() == null) {
            Toast.makeText(this, "Channel is null but IDK LOL!", Toast.LENGTH_LONG).show();
        }
        startActivity(new Intent(this, TileListActivity.class));
        finish();
    }

    @Override
    public void onTilesUpdated() {
        // Not interested at this point. Do nothing.
    }
}
