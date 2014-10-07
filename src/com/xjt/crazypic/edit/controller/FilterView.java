package com.xjt.crazypic.edit.controller;

public interface FilterView {
    public void computeIcon(int index, BitmapCaller caller);

    public void commitLocalRepresentation();
}
