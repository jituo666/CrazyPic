
package com.xjt.crazypic.edit.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xjt.crazypic.edit.colorpicker.ColorHueView;
import com.xjt.crazypic.edit.colorpicker.ColorListener;
import com.xjt.crazypic.edit.editors.Editor;
import com.xjt.crazypic.R;

public class SliderHue implements Control {

    public static String LOGTAG = "SliderHue";
    private ColorHueView mColorOpacityView;
    private ParameterHue mParameter;
    Editor mEditor;

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        mEditor = editor;
        Context context = container.getContext();
        mParameter = (ParameterHue) parameter;
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout lp = (LinearLayout) inflater.inflate(
                R.layout.np_edit_hue, container, true);

        mColorOpacityView = (ColorHueView) lp.findViewById(R.id.hueView);
        updateUI();
        mColorOpacityView.addColorListener(new ColorListener() {

            @Override
            public void setColor(float[] hsvo) {
                mParameter.setValue((int) (360 * hsvo[3]));
                mEditor.commitLocalRepresentation();
            }

            @Override
            public void addColorListener(ColorListener l) {
            }
        });
    }

    @Override
    public View getTopView() {
        return mColorOpacityView;
    }

    @Override
    public void setPrameter(Parameter parameter) {
        mParameter = (ParameterHue) parameter;
        if (mColorOpacityView != null) {
            updateUI();
        }
    }

    @Override
    public void updateUI() {
        mColorOpacityView.setColor(mParameter.getColor());
    }
}
