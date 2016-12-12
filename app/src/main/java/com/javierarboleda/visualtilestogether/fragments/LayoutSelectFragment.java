package com.javierarboleda.visualtilestogether.fragments;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.percent.PercentFrameLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.FragmentLayoutSelectBinding;
import com.javierarboleda.visualtilestogether.models.Layout;
import com.javierarboleda.visualtilestogether.util.PresentationUtil;
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
    private FirebaseRecyclerAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_layout_select, parent, false);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(Layout.TABLE_NAME);
        mAdapter = new FirebaseRecyclerAdapter<Layout, LayoutViewHolder>(
                Layout.class, R.layout.list_item_layout, LayoutViewHolder.class,
                ref) {
            @Override
            protected void populateViewHolder(LayoutViewHolder viewHolder,
                                              Layout layout, final int position) {
                viewHolder.text.setText(
                        Html.fromHtml("<B>" + layout.getLayoutName() + "</B> (" +
                                layout.getTileCount() + ")"));
                Glide.with(mContext).load(layout.getBackgroundUrl()).into(
                        viewHolder.background);

                viewHolder.viewContainer.removeAllViews();
                for (int i = 0; i < layout.getTileCount(); i++) {
                    View view = new View(mContext);
                    view.setBackgroundResource(R.drawable.layout_list_background_shape);
                    view.setVisibility(View.VISIBLE);
                    viewHolder.viewContainer.addView(view);
                    PresentationUtil.moveRelativeView(
                            layout, view, layout.getTilePositions().get(i));
                }

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.updateChannelLayout(mAdapter.getRef(position).getKey());
                    }
                });
            }
        };
        binding.recyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.recyclerView.setLayoutManager(layoutManager);
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
    }

    @Override
    public void onDetach() {
        mContext = null;
        if (mAdapter != null) mAdapter.cleanup();
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

    public static class LayoutViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public TextView text;
        public ImageView background;
        public PercentFrameLayout viewContainer;
        public LayoutViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            text = ((TextView)itemView.findViewById(R.id.text));
            background = (ImageView)itemView.findViewById(R.id.imgBackground);
            viewContainer = (PercentFrameLayout) itemView.findViewById(R.id.viewContainer);
        }
    }

    public interface LayoutSelectFragmentListener {
        void updateChannelLayout(String layoutName);
    }
}
