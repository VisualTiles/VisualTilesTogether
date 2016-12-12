package com.javierarboleda.visualtilestogether.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.javierarboleda.visualtilestogether.R;

public class ShowQrCodeActivity extends BaseVisualTilesActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr_code);
        super.setTopViewGroup((ViewGroup) findViewById(R.id.activity_show_qr_code));
        setUpToolbar();
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(app.getChannel().getName());
        actionBar.setSubtitle(app.getChannel().getUniqueName());

        ImageView ivQrCode = (ImageView) findViewById(R.id.ivQrCode);

        String qrCodeUrl = app.getChannel().getQrCodeUrl();
        if (qrCodeUrl != null) {
            Glide.with(this).load(qrCodeUrl).into(ivQrCode);
        }
    }
}
