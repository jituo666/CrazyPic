
package com.xjt.crazypic.edit.controller;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xjt.crazypic.edit.colorpicker.ColorListener;
import com.xjt.crazypic.edit.colorpicker.ColorPickerDialog;
import com.xjt.crazypic.edit.editors.Editor;
import com.xjt.crazypic.R;

import java.util.Arrays;

public class ChoseBorderColor implements Control {

    private final String TAG = ChoseBorderColor.class.getSimpleName();

    protected ParameterColor mParameter;
    protected LinearLayout mLinearLayout;
    protected Editor mEditor;
    private View mTopView;
    protected int mLayoutID = R.layout.np_edit_control_color_chooser;
    private Context mContext;
    private int mTransparent;
    private int mSelected;
    private static final int OPACITY_OFFSET = 3;
    private int[] mButtonsID = {
            R.id.draw_color_button01,
            R.id.draw_color_button02,
            R.id.draw_color_button03,
            R.id.draw_color_button04,
            R.id.draw_color_button05,
    };
    private Button[] mButton = new Button[mButtonsID.length];

    int mSelectedButton = 0;

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        Resources res = container.getContext().getResources();
        mTransparent = Color.TRANSPARENT;
        mSelected = res.getColor(R.color.np_main_text_color_selected);
        mEditor = editor;
        mContext = container.getContext();
        mParameter = (ParameterColor) parameter;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = inflater.inflate(mLayoutID, container, true);
        mLinearLayout = (LinearLayout) mTopView.findViewById(R.id.listColors);
        mTopView.setVisibility(View.VISIBLE);
        int[] palette = mParameter.getColorPalette();
        for (int i = 0; i < mButtonsID.length; i++) {
            final Button button = (Button) mTopView.findViewById(mButtonsID[i]);
            mButton[i] = button;
            float[] hsvo = new float[4];
            Color.colorToHSV(palette[i], hsvo);
            hsvo[OPACITY_OFFSET] = (0xFF & (palette[i] >> 24)) / (float) 255;
            button.setTag(hsvo);
            GradientDrawable sd = ((GradientDrawable) button.getBackground());
            sd.setColor(palette[i]);
            sd.setStroke(4, (mSelectedButton == i) ? mSelected : mTransparent);

            final int buttonNo = i;
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    selectColor(v, buttonNo);
                }
            });
        }
        ImageView button = (ImageView) mTopView.findViewById(R.id.draw_color_popupbutton);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

    }

    public void setColorSet(int[] basColors) {
        int[] palette = mParameter.getColorPalette();
        for (int i = 0; i < palette.length; i++) {
            palette[i] = basColors[i];
            float[] hsvo = new float[4];
            Color.colorToHSV(palette[i], hsvo);
            hsvo[OPACITY_OFFSET] = (0xFF & (palette[i] >> 24)) / (float) 255;
            mButton[i].setTag(hsvo);
            GradientDrawable sd = ((GradientDrawable) mButton[i].getBackground());
            sd.setColor(palette[i]);
        }

    }

    public int[] getColorSet() {
        return mParameter.getColorPalette();
    }

    private void resetBorders() {
        int[] palette = mParameter.getColorPalette();
        for (int i = 0; i < mButtonsID.length; i++) {
            final Button button = mButton[i];
            GradientDrawable sd = ((GradientDrawable) button.getBackground());
            sd.setColor(palette[i]);
            sd.setStroke(4, (mSelectedButton == i) ? mSelected : mTransparent);
        }
    }

    public void selectColor(View button, int buttonNo) {
        mSelectedButton = buttonNo;
        float[] hsvo = (float[]) button.getTag();
        mParameter.setValue(Color.HSVToColor((int) (hsvo[OPACITY_OFFSET] * 255), hsvo));
        resetBorders();
        mEditor.commitLocalRepresentation();
    }

    @Override
    public View getTopView() {
        return mTopView;
    }

    @Override
    public void setPrameter(Parameter parameter) {
        mParameter = (ParameterColor) parameter;
        updateUI();
    }

    @Override
    public void updateUI() {
        if (mParameter == null) {
            return;
        }
    }

    public void changeSelectedColor(float[] hsvo) {
        int[] palette = mParameter.getColorPalette();
        int c = Color.HSVToColor((int) (hsvo[3] * 255), hsvo);
        final Button button = mButton[mSelectedButton];
        GradientDrawable sd = ((GradientDrawable) button.getBackground());
        sd.setColor(c);
        palette[mSelectedButton] = c;
        mParameter.setValue(Color.HSVToColor((int) (hsvo[OPACITY_OFFSET] * 255), hsvo));
        button.setTag(hsvo);
        mEditor.commitLocalRepresentation();
        button.invalidate();
    }

    public void showColorPicker() {
        ColorListener cl = new ColorListener() {

            @Override
            public void setColor(float[] hsvo) {
                changeSelectedColor(hsvo);
            }

            @Override
            public void addColorListener(ColorListener l) {
            }
        };
        ColorPickerDialog cpd = new ColorPickerDialog(mContext, cl);
        float[] c = (float[]) mButton[mSelectedButton].getTag();
        cpd.setColor(Arrays.copyOf(c, 4));
        cpd.setOrigColor(Arrays.copyOf(c, 4));
        cpd.show();
    }
}
