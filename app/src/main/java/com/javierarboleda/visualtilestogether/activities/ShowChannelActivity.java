package com.javierarboleda.visualtilestogether.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.util.FirebaseUtil;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ShowChannelActivity extends BaseVisualTilesActivity {
    private Subscription mSubscription;
    private String mUniqueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_code);
        super.setTopViewGroup((ViewGroup) findViewById(R.id.activity_show_qr_code));

        mUniqueName = app.getChannel().getUniqueName();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(app.getChannel().getName());
            actionBar.setSubtitle(mUniqueName);
        }

        final ImageView ivQrCode = (ImageView) findViewById(R.id.ivQrCode);

        mSubscription = Observable.just(
                FirebaseUtil.makeQrBitmap(
                        FirebaseUtil.buildChannelDeepLink(this, mUniqueName),
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

        if (app.isChannelModerator()) {
            Button deleteChannel = (Button) findViewById(R.id.btnDeleteChannel);
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
                                    app.deleteChannel();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscription.unsubscribe();
    }
}
