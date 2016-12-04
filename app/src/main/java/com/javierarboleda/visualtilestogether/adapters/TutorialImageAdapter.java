package com.javierarboleda.visualtilestogether.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.javierarboleda.visualtilestogether.R;

/**
 * Created by chris on 11/8/16.
 */

public class TutorialImageAdapter extends PagerAdapter {

    private int[][] tutorialResourceIds = {
            {R.string.tutorial_vj_platform_header, R.string.tutorial_vj_platform_body},
            {R.string.tutorial_vj_console_header, R.string.tutorial_vj_console_body},
            {R.string.tutorial_be_part_of_the_show_header, R.string.tutorial_be_part_of_the_show_body}
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

        TextView headerTextView = (TextView) itemView.findViewById(R.id.tvHeader);
        headerTextView.setText(context.getString(tutorialResourceIds[position][0]));
        TextView bodyTextView = (TextView) itemView.findViewById(R.id.tvBody);
        bodyTextView.setText(context.getString(tutorialResourceIds[position][1]));
        container.addView(itemView);

//        Animation animation = new AlphaAnimation(0f, 1f);
//        animation.setDuration(2000);
//        container.findViewById(R.id.ivTutorial).startAnimation(animation);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}
