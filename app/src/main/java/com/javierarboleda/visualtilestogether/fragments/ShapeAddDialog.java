package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.javierarboleda.visualtilestogether.R;

import java.io.FileNotFoundException;

import static android.app.Activity.RESULT_OK;

public class ShapeAddDialog extends DialogFragment {
    final static String LOG_TAG = ShapeAddDialog.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private ImageView ivShape;
    private Bitmap bitmap;

    public ShapeAddDialog() {
        // required public empty constructor
    }

    public static ShapeAddDialog newInstance() {
        return new ShapeAddDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_shape_add, container, false);

        Button btLoadImage = (Button) view.findViewById(R.id.btLoadImage);
        ivShape = (ImageView) view.findViewById(R.id.ivShape);
        Button btOK = (Button) view.findViewById(R.id.btShapeOk);
        Button btCancel = (Button) view.findViewById(R.id.btShapeCancel);

        btLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentInteraction(bitmap);
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
            mListener = (ShapeAddDialog.OnFragmentInteractionListener) context;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            try {
                Bitmap rawBitmap;
                rawBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(targetUri));
                bitmap = Bitmap.createScaledBitmap(rawBitmap, 300, 300, false);
                ivShape.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Bitmap bitmap);
    }
}
