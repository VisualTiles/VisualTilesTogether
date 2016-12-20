package com.javierarboleda.visualtilestogether.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

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
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.interfaces.FirebaseShortLinkInterface;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.ShortLinkRequest;
import com.javierarboleda.visualtilestogether.models.ShortLinkResponse;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.TRANSPARENT;

public class FirebaseUtil {
    private static final String LOG_TAG = FirebaseUtil.class.getSimpleName();

    /**
     * Build the Channel tileIds by going through the Tiles
     * and setting up its Channel's tileIds entry,
     * and setting up the tileIds entry in the creator's User table entry.
     *
     * Shouldn't really ever need to use this
     *
     * @param context The Application context.
     */
    public static void normalizeDb(final Context context) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        // get the list of userIds and then proceed
        DatabaseReference userRef = dbRef.child(User.TABLE_NAME);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<String> userIds = new ArrayList<>();
                final HashSet<String> legitChannels = new HashSet<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    userIds.add(userSnapshot.getKey());
                }

                /**
                 * go through the channels and clean them up
                 * if they're unnamed they got here because of a glitch in some earlier code
                 * otherwise get the channel's event code (unique name)
                 * (generate a unique name if one doesn't exist due to legacy channels)
                 * finally build or overwrite the QR code image file
                 */
                final DatabaseReference channelsRef = dbRef.child(Channel.TABLE_NAME);
                channelsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot channelSnapshot: dataSnapshot.getChildren()) {
                            String uniqueName;
                            String channelId = channelSnapshot.getKey();
                            Channel channel;
                            try {
                                channel = channelSnapshot.getValue(Channel.class);
                            } catch(Exception e) {
                                Log.e(LOG_TAG, "Corrupt channel in channel table! Deleting...");
                                channelsRef.child(channelId).removeValue();
                                return;
                            }
                            if (channel.getName() == null) {
                                channelsRef.child(channelId).removeValue();
                            } else {
                                legitChannels.add(channelId);

                                uniqueName = channel.getUniqueName();
                                if (uniqueName == null) {
                                    uniqueName = generateChannelUniqueName(dataSnapshot);
                                    channelsRef
                                            .child(channelId)
                                            .child(Channel.CHANNEL_UNIQUE_NAME)
                                            .setValue(uniqueName);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                /**
                 * go through all the tiles and clean them up
                 * make sure each one is represented in the channel designated by its channelId
                 * if there's no channelId or if the channelId doesn't represent an existing channel,
                 * change the tile's channelId to the RONINS channel
                 * if the tile doesn't have a userID (legacy tiles) assign the tile to a random user
                 * then update/create an entry in the tileId table of the tile creator's user table entry
                 */
                final DatabaseReference tilesRef = dbRef.child(Tile.TABLE_NAME);
                tilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot tileSnapshot: dataSnapshot.getChildren()) {
                            Tile tile = tileSnapshot.getValue(Tile.class);
                            String tileId = tileSnapshot.getKey();
                            if (tile.getChannelId() == null ||
                                    !legitChannels.contains(tile.getChannelId())) {
                                tile.setChannelId(Channel.RONINS);
                                tilesRef.child(tileId).child(Tile.CHANNEL_ID).setValue(Channel.RONINS);
                            }
                            if (tile.getCreatorId() == null) {
                                String creatorId = getRandomUser();
                                tile.setCreatorId(creatorId);
                                tilesRef.child(tileId).child(Tile.CREATOR_ID).setValue(creatorId);
                            }
                            updateChannelTileId(tileId, tile);
                            updateUserTileId(tileId, tile);
                        }
                    }

                    private String getRandomUser() {
                        Random r = new Random();
                        return userIds.get(r.nextInt(userIds.size()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static String generateChannelUniqueName(DataSnapshot dataSnapshot) {
        String uniqueName;
        do {
            uniqueName = randomString(8);
        } while (!nameIsUnique(uniqueName, dataSnapshot));
        return uniqueName;
    }

    private static String randomString(int maxLength) {
        StringBuilder s = new StringBuilder();
        Random r = new Random();
        int len = r.nextInt(maxLength - 1) + 1;
        for (int i = 0; i < len; i++) {
            s.append((char)(r.nextInt(26) + 'A'));
        }
        return s.toString();
    }

    private static boolean nameIsUnique(String name, DataSnapshot dataSnapshot) {
        for (DataSnapshot channelSnapshot : dataSnapshot.getChildren()) {
            Channel channel = channelSnapshot.getValue(Channel.class);
            if (channel != null && name.equals(channel.getUniqueName())) {
                return false;
            }
        }
        return true;
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
                dbRef.child(key).removeValue();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                assert downloadUrl != null;
                Tile tile = new Tile(false, channelId, uId, 0, 0, null, downloadUrl.toString(),
                        System.currentTimeMillis());
                tile.setChannelId(channelId);
                dbRef.child(key).setValue(tile);
                updateChannelTileId(key, tile);
                updateUserTileId(key, tile);
            }
        });
    }

    /**
     * Delete a Tile, after deleting any reference to it
     * in its Channel's and creator's tileId lists,
     * and in the Channel's positionToTileIds table.
     * Then delete its corresponding graphic in Storage
     */
    public static void deleteTile(DatabaseReference tileRef,
                                  @NonNull String tileId,
                                  VisualTilesTogetherApp visualTilesTogetherApp,
                                  String channelId,
                                  String uId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        if (!TextUtils.isEmpty(channelId)) {
            DatabaseReference channelRef = dbRef.child(Channel.TABLE_NAME).child(channelId);
            if (channelRef != null) {
                DatabaseReference tileIdRef = channelRef.child(Channel.TILE_IDS).child(tileId);
                if (tileIdRef != null) {
                    tileIdRef.removeValue();
                }
                Channel channel = visualTilesTogetherApp.getChannel();
                ArrayList<String> ids = channel.getPositionToTileIds();
                if (ids != null) {
                    int position = channel.getPositionToTileIds().indexOf(tileId);
                    if (position >= 0) {
                        DatabaseReference positionRef = channelRef
                                .child(Channel.POS_TO_TILE_IDS)
                                .child(String.valueOf(position));
                        if (positionRef != null) {
                            positionRef.setValue("");
                        }
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(uId)) {
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
    private static void updateChannelTileId(String tileId, Tile tile) {
        if (tile.getChannelId() != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference channelRef = dbRef.child(Channel.TABLE_NAME).child(tile.getChannelId());
            if (channelRef != null) {
                channelRef.child(Channel.TILE_IDS).child(tileId).setValue(tile.isApproved());
            }
        }
    }

    /**
     * Make sure that any User this Tile points to
     * has a corresponding entry in its tileId list,
     * set to true for approved and false if not
     */
    private static void updateUserTileId(String tileId, Tile tile) {
        if (tile.getCreatorId() != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = dbRef.child(User.TABLE_NAME).child(tile.getCreatorId());
            if (userRef != null) {
                userRef.child(User.TILE_IDS).child(tileId).setValue(tile.getChannelId());
            }
        }
    }

    /**
     * Generates a deep link URL for this app.
     * @param context The application context.
     * @param channelShortName The short unique name for the channel.
     * @return The QR code URL.
     */
    public static String buildChannelDeepLink(Context context, String channelShortName) {
        final String deepLinkUrl = context.getString(R.string.deep_link_url) + channelShortName;
        Uri.Builder qrCodeUrl = Uri.parse(context.getString(R.string.qr_code_url)).buildUpon();
        qrCodeUrl.appendQueryParameter(context.getString(R.string.link_param), deepLinkUrl);
        qrCodeUrl.appendQueryParameter(context.getString(R.string.apn_param),
                context.getApplicationContext().getPackageName());
        return qrCodeUrl.build().toString();
    }
}