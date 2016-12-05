package com.javierarboleda.visualtilestogether.fragments;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.FragmentLayoutSelectBinding;
import com.javierarboleda.visualtilestogether.models.Layout;
import com.javierarboleda.visualtilestogether.util.sidemenu.interfaces.ScreenShotable;

/**
 * Created on 12/4/16.
 */

public class LayoutSelectFragment extends Fragment
        implements ScreenShotable {
    private FragmentLayoutSelectBinding binding;
    private LayoutSelectFragmentListener listener;
    private Bitmap bitmap = null;
    private Activity mContext;
    private FirebaseListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_layout_select, parent, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getActivity();

        if (context instanceof LayoutSelectFragmentListener) {
            listener = (LayoutSelectFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LayoutSelectFragmentListener");
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(Layout.TABLE_NAME);
        mAdapter = new FirebaseListAdapter<Layout>(getActivity(), Layout.class,
                android.R.layout.two_line_list_item, ref) {
            @Override
            protected void populateView(View view, final Layout layout, final int position) {
                ((TextView)view.findViewById(android.R.id.text1)).setText(layout.getLayoutName());
                ((TextView)view.findViewById(android.R.id.text2)).setText(layout.getTileCount());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.updateChannelLayout(mAdapter.getRef(position).getKey());
                    }
                });
            }
        };
    }

    @Override
    public void onDetach() {
        mContext = null;
        mAdapter.cleanup();
        listener = null;
        super.onDetach();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (binding.mainLayout == null) {
                    LayoutSelectFragment.this.bitmap = null;
                    return;
                }
                final Bitmap bitmap = Bitmap.createBitmap(binding.mainLayout.getWidth(),
                        binding.mainLayout.getHeight(), Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                // Draw must run on UI thread.
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.mainLayout.draw(canvas);
                        LayoutSelectFragment.this.bitmap = bitmap;
                    }
                });
            }
        };
        thread.start();
    }

    public interface LayoutSelectFragmentListener {
        void updateChannelLayout(String layoutName);
    }
}
