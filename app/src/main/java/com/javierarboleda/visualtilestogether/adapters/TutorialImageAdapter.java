package com.javierarboleda.visualtilestogether.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.javierarboleda.visualtilestogether.R;

/**
 * Created by chris on 11/8/16.
 */

public class TutorialImageAdapter extends PagerAdapter {
  int[] tutorialResourceIds = {
      R.color.colorAccent,
      R.color.colorPrimary,
      R.color.colorPrimaryDark
  };

  Context context;
  LayoutInflater layoutInflater;
  public TutorialImageAdapter(Context context) {
    this.context = context;
    this.layoutInflater = (LayoutInflater) context.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return tutorialResourceIds.length;
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == ((LinearLayout) object);
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    View itemView = layoutInflater.inflate(R.layout.tutorial_item, container, false);

    ImageView imageView = (ImageView) itemView.findViewById(R.id.ivTutorial);
    imageView.setBackgroundColor(ContextCompat.getColor(context, tutorialResourceIds[position]));
    container.addView(itemView);

    return itemView;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((LinearLayout) object);
  }
}
