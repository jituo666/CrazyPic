
package com.xjt.crazypic.edit.controller;

public class BasicParameterInt implements ParameterInteger {

    private final String TAG = "BasicParameterInt";

    protected String mParameterName;
    protected Control mControl;
    protected int mMaximum = 100;
    protected int mMinimum = 0;
    protected int mDefaultValue;
    protected int mValue;
    public final int ID;
    protected FilterView mEditor;

    public BasicParameterInt(int id, int value) {
        ID = id;
        mValue = value;
    }

    public BasicParameterInt(int id, int value, int min, int max) {
        ID = id;
        mValue = value;
        mMinimum = min;
        mMaximum = max;
    }

    @Override
    public String getParameterName() {
        return mParameterName;
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    @Override
    public String getValueString() {
        return mParameterName + mValue;
    }

    @Override
    public void setController(Control control) {
        mControl = control;
    }

    @Override
    public int getMaximum() {
        return mMaximum;
    }

    @Override
    public int getMinimum() {
        return mMinimum;
    }

    @Override
    public int getDefaultValue() {
        return mDefaultValue;
    }

    @Override
    public int getValue() {
        return mValue;
    }

    @Override
    public void setValue(int value) {
        mValue = value;
        if (mEditor != null) {
            mEditor.commitLocalRepresentation();
        }
    }

    @Override
    public String toString() {
        return getValueString();
    }

    @Override
    public void setFilterView(FilterView editor) {
        mEditor = editor;
    }

    @Override
    public void copyFrom(Parameter src) {
        if (!(src instanceof BasicParameterInt)) {
            throw new IllegalArgumentException(src.getClass().getName());
        }
        BasicParameterInt p = (BasicParameterInt) src;
        mMaximum = p.mMaximum;
        mMinimum = p.mMinimum;
        mDefaultValue = p.mDefaultValue;
        mValue = p.mValue;
    }
}
