package com.javierarboleda.visualtilestogether.activities;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.ActivityTileCreationBinding;
import com.javierarboleda.visualtilestogether.fragments.ChannelAddDialog;
import com.javierarboleda.visualtilestogether.fragments.ShapeAddDialog;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.views.CanvasView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

public class TileCreationActivity extends AppCompatActivity
        implements ChannelAddDialog.OnFragmentInteractionListener,
        ShapeAddDialog.OnFragmentInteractionListener {

    ActivityTileCreationBinding binding;
    CanvasView mCanvas;
    private static final String TILES_TABLE = "tiles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tile_creation);


        mCanvas = binding.cvVisualTile;

        Display d = getWindowManager().getDefaultDisplay();
        Point canvasDimen = new Point();
        d.getSize(canvasDimen);
        mCanvas.getLayoutParams().height = canvasDimen.x;

        mCanvas.setMode(CanvasView.Mode.DRAW);
        mCanvas.setDrawer(CanvasView.Drawer.PEN);

        mCanvas.setPaintStrokeWidth(10F);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Menu topMenu = binding.amvTop.getMenu();
        getMenuInflater().inflate(R.menu.action_menu_title_editor_top, topMenu);

        for (int i = 0; i < topMenu.size(); i++) {
            topMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
        }

        Menu bottomMenu = binding.amvBottom.getMenu();
        getMenuInflater().inflate(R.menu.action_menu_tile_editor_bottom, bottomMenu);

        for (int i = 0; i < bottomMenu.size(); i++) {
            bottomMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add_tile_to_upcoming:
                Bitmap bitmap = mCanvas.getScaleBitmap(300, 300);
                onFragmentInteraction(bitmap);
                finish();
                return true;
            case R.id.action_undo:
                mCanvas.undo();
                return true;
            case R.id.action_redo:
                mCanvas.redo();
                return true;
            case R.id.action_clear:
                mCanvas.clear();
                return true;
            case R.id.action_draw_mode:
                mCanvas.setMode(CanvasView.Mode.DRAW);
                mCanvas.setDrawer(CanvasView.Drawer.PEN);
                return true;
            case R.id.action_line_mode:
                mCanvas.setMode(CanvasView.Mode.DRAW);
                mCanvas.setDrawer(CanvasView.Drawer.LINE);
                return true;
            case R.id.action_box_mode:
                mCanvas.setMode(CanvasView.Mode.DRAW);
                mCanvas.setDrawer(CanvasView.Drawer.RECTANGLE);
                return true;
            case R.id.action_circle_mode:
                mCanvas.setMode(CanvasView.Mode.DRAW);
                mCanvas.setDrawer(CanvasView.Drawer.CIRCLE);
                return true;
            case R.id.action_text_mode:
                mCanvas.setMode(CanvasView.Mode.TEXT);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Channel channel) {

    }

    @Override
    public void onFragmentInteraction(Bitmap bitmap) {
        // get the shapes folder of Firebase Storage for this app
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(TILES_TABLE);
        final String key = dbRef.push().getKey();

        StorageReference shapesRef = mFirebaseStorage
                .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
                .child("shapes");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ByteArrayInputStream inputstream = new ByteArrayInputStream(baos .toByteArray());
        UploadTask uploadTask = shapesRef.child(key).putStream(inputstream);
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
}
