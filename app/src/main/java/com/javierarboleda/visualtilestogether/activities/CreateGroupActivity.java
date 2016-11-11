package com.javierarboleda.visualtilestogether.activities;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TutorialImageAdapter;

public class CreateGroupActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_group);
  }
}
