package com.javierarboleda.visualtilestogether.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.fragments.ChannelAddDialog;
import com.javierarboleda.visualtilestogether.fragments.ShapeAddDialog;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity
 implements ChannelAddDialog.OnFragmentInteractionListener,
        ShapeAddDialog.OnFragmentInteractionListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (VisualTilesTogetherApp.getUser() == null ||
                VisualTilesTogetherApp.getChannel() ==  null) {
            finish();
        }

        Button btAddShape = (Button) findViewById(R.id.btAddShape);
        btAddShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShapeAddDialog.newInstance().show(getSupportFragmentManager(), "new shape");
            }
        });

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
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(
                Channel.TABLE_NAME);
        String key = dbRef.push().getKey();
        dbRef.child(key).setValue(channel);
        // TODO(jav): Save user when channel is defined; so they don't get stuck in a loop.
        VisualTilesTogetherApp.initChannel(key);
        Log.d(LOG_TAG, "key is " + key);
    }

    @Override
    public void onFragmentInteraction(Bitmap bitmap) {
        // get the shapes folder of Firebase Storage for this app
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(
                Tile.TABLE_NAME);
        final String key = dbRef.push().getKey();

        StorageReference shapesRef = mFirebaseStorage
                .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
                .child("shapes");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ByteArrayInputStream inputstream = new ByteArrayInputStream(baos .toByteArray());
        uploadTask = shapesRef.child(key).putStream(inputstream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dbRef.child(key).setValue(null);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Tile tile = new Tile(false, 0, 0, null, downloadUrl.toString(), new Date());
                dbRef.child(key).setValue(tile);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_present:
                startActivity(new Intent(this, PresentationActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
