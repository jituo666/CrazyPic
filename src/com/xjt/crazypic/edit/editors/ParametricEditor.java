
package com.xjt.crazypic.edit.editors;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.xjt.crazypic.common.ApiHelper;
import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.edit.controller.ActionSlider;
import com.xjt.crazypic.edit.controller.BasicSlider;
import com.xjt.crazypic.edit.controller.ChoseBorderColor;
import com.xjt.crazypic.edit.controller.ChoseBorderTexture;
import com.xjt.crazypic.edit.controller.ChoseDrawStyle;
import com.xjt.crazypic.edit.controller.Control;
import com.xjt.crazypic.edit.controller.Parameter;
import com.xjt.crazypic.edit.controller.ParameterActionAndInt;
import com.xjt.crazypic.edit.controller.ParameterBrightness;
import com.xjt.crazypic.edit.controller.ParameterColor;
import com.xjt.crazypic.edit.controller.ParameterHue;
import com.xjt.crazypic.edit.controller.ParameterInteger;
import com.xjt.crazypic.edit.controller.ParameterOpacity;
import com.xjt.crazypic.edit.controller.ParameterSaturation;
import com.xjt.crazypic.edit.controller.ParameterStyle;
import com.xjt.crazypic.edit.controller.ParameterStyles;
import com.xjt.crazypic.edit.controller.ParameterTexture;
import com.xjt.crazypic.edit.controller.SliderBrightness;
import com.xjt.crazypic.edit.controller.SliderHue;
import com.xjt.crazypic.edit.controller.SliderOpacity;
import com.xjt.crazypic.edit.controller.SliderSaturation;
import com.xjt.crazypic.edit.filters.FilterBasicRepresentation;
import com.xjt.crazypic.edit.filters.FilterRepresentation;
import com.xjt.crazypic.R;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class ParametricEditor extends Editor {

    private final String TAG = ParametricEditor.class.getSimpleName();

    public static final int MINIMUM_WIDTH = 600;
    public static final int MINIMUM_HEIGHT = 800;
    public static int ID = R.id.editorParametric;

    private int mLayoutID;
    private int mViewID;
    protected Control mControl;
    protected View mActionButton;
    protected View mEditControl;
    protected static HashMap<String, Class<?>> portraitMap = new HashMap<String, Class<?>>();

    static {
        portraitMap.put(ParameterSaturation.sParameterType, SliderSaturation.class);
        portraitMap.put(ParameterHue.sParameterType, SliderHue.class);
        portraitMap.put(ParameterOpacity.sParameterType, SliderOpacity.class);
        portraitMap.put(ParameterBrightness.sParameterType, SliderBrightness.class);
        portraitMap.put(ParameterColor.sParameterType, ChoseBorderColor.class);
        portraitMap.put(ParameterTexture.sParameterType, ChoseBorderTexture.class);
        portraitMap.put(ParameterStyle.sParameterType, ChoseDrawStyle.class);
        portraitMap.put(ParameterInteger.sParameterType, BasicSlider.class);
        if (ApiHelper.AT_LEAST_14) {
            portraitMap.put(ParameterActionAndInt.sParameterType, ActionSlider.class);
        } else {
            portraitMap.put(ParameterActionAndInt.sParameterType, BasicSlider.class);
        }

    }

    static Constructor<?> getConstructor(Class<?> cl) {
        try {
            return cl.getConstructor(Context.class, ViewGroup.class);
        } catch (Exception e) {
            return null;
        }
    }

    public ParametricEditor() {
        super(ID);
    }

    protected ParametricEditor(int id) {
        super(id);
    }

    protected ParametricEditor(int id, int layoutID, int viewID) {
        super(id);
        mLayoutID = layoutID;
        mViewID = viewID;
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {

        String apply = "";
        if (mShowParameter == SHOW_VALUE_INT) {
            if (getLocalRepresentation() instanceof FilterBasicRepresentation) {
                FilterBasicRepresentation interval = (FilterBasicRepresentation) getLocalRepresentation();
                apply += " " + effectName.toUpperCase() + " " + interval.getStateRepresentation();
            } else {
                apply += " " + effectName.toUpperCase() + " " + parameterValue;
            }
        } else {
            apply += " " + effectName.toUpperCase();
        }
        return apply;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        unpack(mViewID, mLayoutID);
    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        if (getLocalRepresentation() != null && getLocalRepresentation() instanceof FilterBasicRepresentation) {
            FilterBasicRepresentation interval = (FilterBasicRepresentation) getLocalRepresentation();
            mControl.setPrameter(interval);
        }
    }

    @Override
    public Control[] getControls() {
        BasicSlider slider = new BasicSlider();
        return new Control[] {
                slider
        };
    }

    protected Parameter getParameterToEdit(FilterRepresentation rep) {
        if (this instanceof Parameter) {
            return (Parameter) this;
        } else if (rep instanceof Parameter) {
            return ((Parameter) rep);
        }
        return null;
    }

    @Override
    public void setUtilityPanelUI(View actionButton, View editControl) {
        mActionButton = actionButton;
        mEditControl = editControl;
        FilterRepresentation rep = getLocalRepresentation();

        Parameter param = getParameterToEdit(rep);

        if (param != null) {
            control(param, editControl);
        } else {
            mSeekBar = new SeekBar(editControl.getContext());
            LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mSeekBar.setLayoutParams(lp);
            ((LinearLayout) editControl).addView(mSeekBar);
            mSeekBar.setOnSeekBarChangeListener(this);
        }
    }

    protected void control(Parameter p, View editControl) {
        String pType = p.getParameterType();
        Class<?> c = portraitMap.get(pType);

        if (c != null) {
            try {
                mControl = (Control) c.newInstance();
                p.setController(mControl);
                mControl.setUp((ViewGroup) editControl, p, this);
            } catch (Exception e) {
                Log.e(TAG, "Error in loading Control ", e);
            }
        } else {
            Log.e(TAG, "Unable to find class for " + pType);
            for (String string : portraitMap.keySet()) {
                Log.e(TAG, "for " + string + " use " + portraitMap.get(string));
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar sbar, int progress, boolean arg2) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
    }
}
