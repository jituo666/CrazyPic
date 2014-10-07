package com.xjt.crazypic.animations;

import com.xjt.crazypic.views.opengl.GLESCanvas;


public abstract class CanvasAnim extends Animation {

    public abstract int getCanvasSaveFlags();
    public abstract void apply(GLESCanvas canvas);
}
