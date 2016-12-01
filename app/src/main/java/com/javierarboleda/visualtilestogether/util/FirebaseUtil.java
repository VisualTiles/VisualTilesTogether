package com.javierarboleda.visualtilestogether.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static android.graphics.Color.BLACK;

public class FirebaseUtil {
    private static final String LOG_TAG = FirebaseUtil.class.getSimpleName();

    /**
     * Build the Channel tileIds by going through the Tiles
     * and setting up its Channel's tileIds entry,
     * and setting up the tileIds entry in the creator's User table entry.
     *
     * Shouldn't really ever need to use this
     */
    public static void normalizeDb() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference dbTiles = dbRef.child(Tile.TABLE_NAME);
        dbTiles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Tile tile = postSnapshot.getValue(Tile.class);
                    String tileId = postSnapshot.getKey();
                    updateChannelTileId(tileId, tile);
                    updateUsrerTileId(tileId, tile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference dbChannels = dbRef.child(Channel.TABLE_NAME);
        dbChannels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String channelId = postSnapshot.getKey();
                    setChannelQrCode(channelId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    /**
     * Upload a bitmap to Storage and then create a Tile using the same key
     */
    public static void createTile(Bitmap bitmap, final String channelId, final String uId) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(Tile.TABLE_NAME);
        final String key = dbRef.push().getKey();

        StorageReference shapesRef = firebaseStorage
                .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
                .child("shapes");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
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
                Tile tile = new Tile(false, channelId, uId, 0, 0, null, downloadUrl.toString(),
                        System.currentTimeMillis());
                tile.setChannelId(channelId);
                dbRef.child(key).setValue(tile);
                updateChannelTileId(key, tile);
                updateUsrerTileId(key, tile);
            }
        });
    }

    /**
     * Delete a Tile, after deleting any reference to it in its Channel's tileId list.
     * Then delete its corresponding graphic in Storage
     */
    public static void deleteTile(DatabaseReference tileRef,
                                  @NonNull String tileId,
                                  String channelId,
                                  String uId) {
        if (channelId != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference channelRef = dbRef.child(Channel.TABLE_NAME).child(channelId);
            if (channelRef != null) {
                DatabaseReference tileIdRef = channelRef.child(Channel.TILE_IDS).child(tileId);
                if (tileIdRef != null) {
                    tileIdRef.removeValue();
                }
            }
            DatabaseReference userRef = dbRef.child(User.TABLE_NAME).child(uId);
            if (userRef != null) {
                DatabaseReference tileIdRef = userRef.child(User.TILE_IDS).child(tileId);
                if (tileIdRef != null) {
                    tileIdRef.removeValue();
                }
            }
        }
        tileRef.removeValue();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference shapesRef = firebaseStorage
                .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
                .child("shapes");
        shapesRef.child(tileId).delete();
    }

    /**
     * Toggle a Tile's approved state and update its Channel's tileIds list
     */
    public static void toggleTileApproval(final DatabaseReference tileRef) {
        tileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Tile tile = mutableData.getValue(Tile.class);
                if (tile == null) {
                    return Transaction.success(mutableData);
                }

                tile.setApproved(!tile.isApproved());
                updateChannelTileId(tileRef.getKey(), tile);

                mutableData.setValue(tile);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
//                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
            }
        });
    }

    /**
     * Make sure that any Channel this Tile points to
     * has a corresponding entry in its tileId list,
     * set to true for approved and false if not
     */
    public static void updateChannelTileId(String tileId, Tile tile) {
        if (tile.getChannelId() != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference channelRef = dbRef.child(Channel.TABLE_NAME).child(tile.getChannelId());
            if (channelRef != null) {
                channelRef.child(Channel.TILE_IDS).child(tileId).setValue(tile.isApproved());
            }
        }
    }

    /**
     * Make sure that any Channel this Tile points to
     * has a corresponding entry in its tileId list,
     * set to true for approved and false if not
     */
    public static void updateUsrerTileId(String tileId, Tile tile) {
        if (tile.getCreatorId() != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = dbRef.child(User.TABLE_NAME).child(tile.getCreatorId());
            if (userRef != null) {
                userRef.child(User.TILE_IDS).child(tileId).setValue(true);
            }
        }
    }

    /**
     * Generate a QR code for the channel,
     * for now just use the key as the encoded string
     */
    public static void setChannelQrCode(String key) {
        new QrTask().execute(key);
    }

    private static class QrTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            final String key = strings[0];
            try {
                // generate the QR code
                BitMatrix bitMatrix = new MultiFormatWriter()
                        .encode(key, BarcodeFormat.QR_CODE, 400, 400);
                Bitmap bitmap = createBitmap(bitMatrix);

                // convert it to an input stream so it can be uploaded
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                ByteArrayInputStream inputstream = new ByteArrayInputStream(baos.toByteArray());

                // upload it to Firebase Storage
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference qrCodesRef = firebaseStorage
                        .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
                        .child("qrCodes");
                UploadTask uploadTask = qrCodesRef.child(key).putStream(inputstream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // set the channel's QR code url to point to the QR code image
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        DatabaseReference channelRef = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child(Channel.TABLE_NAME);
                        channelRef.child(key).child(Channel.QRCODE_URL).setValue(downloadUrl.toString());
                    }
                });
                return true;
            } catch (WriterException e) {
                e.printStackTrace();
            }
            return false;
        }

        private static Bitmap createBitmap(BitMatrix bitMatrix) {
            int w = bitMatrix.getWidth();
            int h = bitMatrix.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : Color.TRANSPARENT;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        }
    }
}
