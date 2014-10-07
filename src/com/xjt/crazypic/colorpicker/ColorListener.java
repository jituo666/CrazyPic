package com.xjt.crazypic.colorpicker;

public interface ColorListener {
    void setColor(float[] hsvo);
    public void addColorListener(ColorListener l);
}
