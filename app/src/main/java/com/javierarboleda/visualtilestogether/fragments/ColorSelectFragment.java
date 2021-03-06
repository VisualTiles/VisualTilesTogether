package com.javierarboleda.visualtilestogether.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.FragmentColorSelectBinding;
import com.javierarboleda.visualtilestogether.util.sidemenu.interfaces.ScreenShotable;

/**
 * Created on 11/17/16.
 */

public class ColorSelectFragment extends Fragment
        implements ScreenShotable {
    private FragmentColorSelectBinding binding;
    private ColorSelectFragmentListener listener;
    private ColorFillMode mode = ColorFillMode.SINGLE_TILE;
    private Bitmap bitmap = null;
    private Activity mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_color_select, parent, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        binding.ibSingleTileSelect.setSelected(true);
        mode = ColorFillMode.SINGLE_TILE;
        binding.ibSingleTileSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = ColorFillMode.SINGLE_TILE;
                listener.updateFillMode(mode);
                if (!binding.ibSingleTileSelect.isSelected()) {
                    binding.ibSingleTileSelect.setSelected(true);
                    binding.ibAllTileSelect.setSelected(false);
                    binding.ibBackgroundSelect.setSelected(false);
                }
            }
        });
        binding.ibAllTileSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = ColorFillMode.MULTI_TILE;
                listener.updateFillMode(mode);
                if (!binding.ibAllTileSelect.isSelected()) {
                    binding.ibSingleTileSelect.setSelected(false);
                    binding.ibAllTileSelect.setSelected(true);
                    binding.ibBackgroundSelect.setSelected(false);
                }
            }
        });
        binding.ibBackgroundSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = ColorFillMode.BACKGROUND;
                listener.updateFillMode(mode);
                if (!binding.ibBackgroundSelect.isSelected()) {
                    binding.ibSingleTileSelect.setSelected(false);
                    binding.ibAllTileSelect.setSelected(false);
                    binding.ibBackgroundSelect.setSelected(true);
                }
            }
        });
        binding.btnColor1.setOnClickListener(handleColorPicker);
        binding.btnColor2.setOnClickListener(handleButtonClick);
        binding.btnColor3.setOnClickListener(handleButtonClick);
        binding.btnColor4.setOnClickListener(handleButtonClick);
        binding.btnColor5.setOnClickListener(handleButtonClick);
        binding.btnColor6.setOnClickListener(handleButtonClick);
        binding.btnColor7.setOnClickListener(handleButtonClick);
        binding.btnColor8.setOnClickListener(handleButtonClick);
        binding.btnColor9.setOnClickListener(handleButtonClick);
        binding.btnColor10.setOnClickListener(handleButtonClick);
        binding.btnColor11.setOnClickListener(handleButtonClick);
        binding.btnColor12.setOnClickListener(handleButtonClick);
        binding.btnColor13.setOnClickListener(handleButtonClick);
        binding.btnColor14.setOnClickListener(handleButtonClick);
        binding.btnColor15.setOnClickListener(handleButtonClick);
    }

    private View selectedButton;
    View.OnClickListener handleButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!view.isSelected()) {
                if (selectedButton != null) selectedButton.setSelected(false);
                view.setSelected(true);
                selectedButton = view;
            }

            Integer color = null;
            String colorStr = (String) view.getTag();
            if (!colorStr.isEmpty())
                color = Color.parseColor(colorStr);
            listener.updateSelectedColor(mode, color);
        }
    };
    View.OnClickListener handleColorPicker = new View.OnClickListener() {
        @Override
        public void onClick(final View pickerView) {
            if (!pickerView.isSelected()) {
                if (selectedButton != null) selectedButton.setSelected(false);
                pickerView.setSelected(true);
                selectedButton = pickerView;
            }
            int initialColor = Color.parseColor((String) pickerView.getTag());
            ColorPickerDialogBuilder
                    .with(getContext())
                    .setTitle("Choose color")
                    .initialColor(initialColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .lightnessSliderOnly()
                    .density(12)
                    .setOnColorSelectedListener(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int selectedColor) {
                            // Do nothing yet.
                        }
                    })
                    .setPositiveButton("ok", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            AppCompatImageButton btn = (AppCompatImageButton) pickerView;
                            btn.setTag("#" + Integer.toHexString(selectedColor));
                            StateListDrawable colorState = new StateListDrawable();
                            colorState.addState(
                                    new int[]{android.R.attr.state_selected},
                                    new ColorDrawable(selectedColor));
                            ColorDrawable unselected = new ColorDrawable(selectedColor);
                            unselected.setAlpha(100);
                            colorState.addState(new int[]{}, unselected);
                            btn.setBackground(colorState);
                            listener.updateSelectedColor(mode, selectedColor);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing.
                        }
                    })
                    .build()
                    .show();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getActivity();
        if (context instanceof ColorSelectFragmentListener) {
            listener = (ColorSelectFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ColorSelectFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        mContext = null;
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
                    ColorSelectFragment.this.bitmap = null;
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
                        ColorSelectFragment.this.bitmap = bitmap;
                    }
                });
            }
        };
        thread.start();
    }

    public enum ColorFillMode {
        SINGLE_TILE,
        MULTI_TILE,
        BACKGROUND
    }

    public interface ColorSelectFragmentListener {
        void updateSelectedColor(ColorFillMode mode, Integer color);
        void updateFillMode(ColorFillMode mode);
    }
}
