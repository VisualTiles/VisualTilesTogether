package com.javierarboleda.visualtilestogether.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.databinding.ActivityCreateJoinBinding;

/**
 * Created on 11/13/16.
 */

public class CreateJoinActivity extends AppCompatActivity {

    ActivityCreateJoinBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_join);
    }

    public void joinEventOnClick(View view) {

        VisualTilesTogetherApp.initChannel();

    }

    public void createNewEventOnClick(View view) {
    }
}
