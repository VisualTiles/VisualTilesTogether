package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Channel;

import java.util.Date;

public class ChannelAddDialog extends DialogFragment {
    final static String LOG_TAG = ChannelAddDialog.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private VisualTilesTogetherApp app;

    public ChannelAddDialog() {
        // required public empty constructor
    }

    public static ChannelAddDialog newInstance() {
        return new ChannelAddDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (VisualTilesTogetherApp) getActivity().getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_channel_add, container, false);

        final EditText etChannelName = (EditText) view.findViewById(R.id.etChannelName);
        final EditText etStartTime = (EditText) view.findViewById(R.id.etStartTime);
        final EditText etEndTime = (EditText) view.findViewById(R.id.etEndTime);
        Button btOK = (Button) view.findViewById(R.id.btOk);
        Button btCancel = (Button) view.findViewById(R.id.btCancel);

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentInteraction(new Channel(etChannelName.getText().toString(),
//                        Date.valueOf(etStartTime.getText().toString()),
//                        Date.valueOf(etEndTime.getText().toString())));
                        new Date(2016, 11, 11),
                        new Date(2016, 11, 12),
                        app.getUid()));
                dismiss();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ChannelAddDialog.OnFragmentInteractionListener) context;
        }
        catch (ClassCastException e) {
            Log.e(LOG_TAG, "Activity needs to implement TweetAddFragment.OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Channel channel);
    }
}
