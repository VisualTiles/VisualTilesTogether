package com.javierarboleda.visualtilestogether.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.javierarboleda.visualtilestogether.models.Channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.TRANSPARENT;

/**
 * Created by geo on 12/6/16.
 */

public class SetChannelQrCode extends IntentService {
    private static final String KEY = "key";
    private static final String UNIQUENAME = "uniqueName";
    private static final String LOG_TAG = SetChannelQrCode.class.getSimpleName();

    public SetChannelQrCode() {
        super("SetChannelQrCode");
    }

    public static void launch(Context context, String key, String uniqueName) {
        Intent intent = new Intent(context, SetChannelQrCode.class);
        intent.putExtra(KEY, key);
        intent.putExtra(UNIQUENAME, uniqueName);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String key = intent.getStringExtra(KEY);
            String uniqueName = intent.getStringExtra(UNIQUENAME);
            try {
                // generate the QR code
                BitMatrix bitMatrix = new MultiFormatWriter()
                        .encode(key, BarcodeFormat.QR_CODE, 400, 400);
                Bitmap bitmap = textOnBitmap(createBitmap(bitMatrix), uniqueName);

                // convert it to an input stream so it can be uploaded
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                ByteArrayInputStream inputstream = new ByteArrayInputStream(baos.toByteArray());

                // upload it to Firebase Storage
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference qrCodesRef = firebaseStorage
                        .getReferenceFromUrl("gs://visual-tiles-together.appspot.com")
                        .child("qrCodes");

                // Countdown latch keeps the service going so the callbacks can do their thing
                final CountDownLatch latch = new CountDownLatch(1);
//                Log.d(LOG_TAG, "uploading QR code");
                UploadTask uploadTask = qrCodesRef.child(key).putStream(inputstream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Log.d(LOG_TAG, "failed " + e);
                        latch.countDown();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // set the channel's QR code url to point to the QR code image
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                        Log.d(LOG_TAG, "success, adding " + downloadUrl);
                        if (downloadUrl == null) {
                            latch.countDown();
                        } else {
                            DatabaseReference channelRef = FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child(Channel.TABLE_NAME);
                            channelRef.child(key)
                                    .child(Channel.QRCODE_URL)
                                    .setValue(downloadUrl.toString(), new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            latch.countDown();
                                        }
                                    });
                        }
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    private static Bitmap textOnBitmap(Bitmap bitmap, String s) {
        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        // text size in pixels
        paint.setTextSize(48);
        // text shadow
        // draw text to the Canvas bottom
        Rect bounds = new Rect();
        paint.getTextBounds(s, 0, s.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = bitmap.getHeight();

        canvas.drawText(s, x, y, paint);
        return bitmap;
    }

    private static Bitmap createBitmap(BitMatrix bitMatrix) {
        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : TRANSPARENT;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
