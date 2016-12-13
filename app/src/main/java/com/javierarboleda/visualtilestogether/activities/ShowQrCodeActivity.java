package com.javierarboleda.visualtilestogether.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.util.FirebaseUtil;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ShowQrCodeActivity extends BaseVisualTilesActivity {

    private Subscription mSubscription;
    private String mUniqueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr_code);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscription.unsubscribe();
    }
}
