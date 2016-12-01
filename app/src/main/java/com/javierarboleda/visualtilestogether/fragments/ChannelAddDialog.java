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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Channel;

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
        final EditText etChannelUniqueName = (EditText) view.findViewById(R.id.etChannelUniqueName);
        Button btOK = (Button) view.findViewById(R.id.btOk);
        Button btCancel = (Button) view.findViewById(R.id.btCancel);

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uniqueName = etChannelUniqueName.getText().toString();
                finishIfUnique(uniqueName, etChannelName.getText().toString(), app.getUid());
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

    private void finishIfUnique(final String uniqueName, final String name, final String uId) {
        DatabaseReference channelRef = FirebaseDatabase.getInstance().getReference()
                .child(Channel.TABLE_NAME);
        channelRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean unique = true;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Channel channel = data.getValue(Channel.class);
                    if (uniqueName.equals(channel.getUniqueName())) {
                        unique = false;
                        break;
                    }
                }
                if (unique) {
                    mListener.onFragmentInteraction(new Channel(name, uniqueName, uId));
                    dismiss();
                } else {
                    Toast.makeText(getContext(),
                            uniqueName + "isn't a unique name, please try again",
                            Toast.LENGTH_LONG);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
