package com.javierarboleda.visualtilestogether.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.fragments.ChannelAddDialog;
import com.javierarboleda.visualtilestogether.models.Channel;

public class MainActivity extends AppCompatActivity
 implements ChannelAddDialog.OnFragmentInteractionListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String CHANNELS_TABLE = "channels";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btShowGallery = (Button) findViewById(R.id.btShowGallery);
        btShowGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TileListActivity.class));
            }
        });

        Button btAddChannel = (Button) findViewById(R.id.btAddChannel);
        btAddChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChannelAddDialog.newInstance().show(getSupportFragmentManager(), "new channel");
            }
        });
    }

    @Override
    public void onFragmentInteraction(Channel channel) {
        Log.d(LOG_TAG, "new channel (" + channel.getName() + ", "
                + channel.getStartTime().toString() + ", "
                + channel.getEndTime().toString() + ")");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(CHANNELS_TABLE);
        String key = dbRef.push().getKey();
        dbRef.child(key).setValue(channel);
        Log.d(LOG_TAG, "key is " + key);
    }
}
