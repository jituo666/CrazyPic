package com.xjt.crazypic.edit.imageshow;

public interface Oval {
    void setCenter(float x, float y);
    void setRadius(float w, float h);
    float getCenterX();
    float getCenterY();
    float getRadiusX();
    float getRadiusY();
    void setRadiusY(float y);
    void setRadiusX(float x);

}
