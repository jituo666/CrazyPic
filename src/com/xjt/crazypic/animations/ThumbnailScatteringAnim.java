
package com.xjt.crazypic.animations;

import android.graphics.Rect;

import com.xjt.crazypic.utils.RelativePosition;
import com.xjt.crazypic.views.opengl.GLESCanvas;

public class ThumbnailScatteringAnim extends ThumbnailAnim {

    private int PHOTO_DISTANCE = 1000;
    private RelativePosition mCenter;
    private boolean mScatterX, mScatterY, mScatterZ;

    public ThumbnailScatteringAnim(RelativePosition center, boolean scatterX, boolean scatterY, boolean scatterZ) {
        mCenter = center;
        mScatterX = scatterX;
        mScatterY = scatterY;
        mScatterZ = scatterZ;
    }

    @Override
    public void apply(GLESCanvas canvas, int thumbnailIndex, Rect target) {
        canvas.translate(
                mScatterX ? (mCenter.getX() - target.centerX()) * (1 - mProgress) : 0,
                mScatterY ? (mCenter.getY() - target.centerY()) * (1 - mProgress) : 0,
                mScatterZ ? (thumbnailIndex * PHOTO_DISTANCE * (1 - mProgress)) : 0);
        canvas.setAlpha(mProgress);
    }
}
