package com.javierarboleda.visualtilestogether.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.util.FirebaseUtil;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.TRANSPARENT;

/**
 * Created by geo on 12/20/16.
 */

public class ShowChannelFragment extends Fragment {
    private static final String UNIQUE_NAME = "unique_name";
    private Subscription mSubscription;
    private String mUniqueName;
    private VisualTilesTogetherApp mApp;
    private Context mContext;

    public ShowChannelFragment() {
    }

    public static ShowChannelFragment newInstance(String uniqueName) {
        Bundle args = new Bundle();
        args.putString(UNIQUE_NAME, uniqueName);
        ShowChannelFragment fragment = new ShowChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mUniqueName = args.getString(UNIQUE_NAME);
        mApp = (VisualTilesTogetherApp) getActivity().getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_channel, container, false);

        final ImageView ivQrCode = (ImageView) view.findViewById(R.id.ivQrCode);

        mSubscription = Observable.just(
                makeQrBitmap(
                        FirebaseUtil.buildChannelDeepLink(mContext, mUniqueName),
                        mUniqueName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        ivQrCode.setImageBitmap(bitmap);
                    }
                });

        if (mApp.isChannelModerator()) {
            Button deleteChannel = (Button) view.findViewById(R.id.btnDeleteChannel);
            deleteChannel.setVisibility(View.VISIBLE);
            deleteChannel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
                    dialogBuilder.setMessage(
                            "Are you sure you want to delete this channel? WARNING! This action " +
                                    "CANNOT BE REVERSED!")
                            .setTitle("Delete Channel '" + mUniqueName + "'")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mApp.deleteChannel();
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                    dialogBuilder.create().show();
                }
            });
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSubscription.unsubscribe();
    }

    private static Bitmap makeQrBitmap(String key, String name) {
        BitMatrix bitMatrix;
//        Log.d(LOG_TAG, "makeQrBitmap(" + key + ", " + name + ")");
        try {
            bitMatrix = new MultiFormatWriter()
                    .encode(key, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
        return textOnBitmap(createBitmap(bitMatrix), name);
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
