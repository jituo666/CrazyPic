package com.xjt.crazypic.edit.controller;

public interface ParameterInteger extends Parameter {
    static String sParameterType = "ParameterInteger";

    int getMaximum();

    int getMinimum();

    int getDefaultValue();

    int getValue();

    void setValue(int value);
}
