package com.xjt.crazypic.edit.colorpicker;

public interface ColorListener {
    void setColor(float[] hsvo);
    public void addColorListener(ColorListener l);
}
