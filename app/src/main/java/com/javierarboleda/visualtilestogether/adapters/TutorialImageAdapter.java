package com.javierarboleda.visualtilestogether.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.javierarboleda.visualtilestogether.R;

/**
 * Created by chris on 11/8/16.
 */

public class TutorialImageAdapter extends PagerAdapter {
    private int[] backgroundResourceIds = {
            R.drawable.vtbg,
            R.drawable.vtbg2
    };
    private int[] tutorialResourceIds = {
            R.layout.tutorial_page_1,
            R.layout.tutorial_page_2
    };

    private Context context;
    private LayoutInflater layoutInflater;

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
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FrameLayout itemView = (FrameLayout)
                layoutInflater.inflate(R.layout.tutorial_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.ivBackground);
        imageView.setImageResource(backgroundResourceIds[position]);
        container.addView(itemView);

        View page = layoutInflater.inflate(tutorialResourceIds[position], itemView, true);
        Animation animation = new AlphaAnimation(0f, 1f);
        animation.setDuration(2000);
        container.findViewById(R.id.ivTutorial).startAnimation(animation);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
