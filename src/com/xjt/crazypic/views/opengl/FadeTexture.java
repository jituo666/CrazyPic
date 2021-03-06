
package com.xjt.crazypic.views.opengl;

import com.xjt.crazypic.animations.AnimationTime;
import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.utils.Utils;

// FadeTexture is a texture which fades the given texture along the time.
public abstract class FadeTexture implements Texture {

    @SuppressWarnings("unused")
    private static final String TAG = "FadeTexture";

    // The duration of the fading animation in milliseconds
    public static final int DURATION = 180;

    private final long mStartTime;
    private final int mWidth;
    private final int mHeight;
    private final boolean mIsOpaque;
    private boolean mIsAnimating;

    public FadeTexture(int width, int height, boolean opaque) {
        mWidth = width;
        mHeight = height;
        mIsOpaque = opaque;
        mStartTime = now();
        mIsAnimating = true;
    }

    @Override
    public void draw(GLESCanvas canvas, int x, int y) {
        draw(canvas, x, y, mWidth, mHeight);
    }

    @Override
    public boolean isOpaque() {
        return mIsOpaque;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    public boolean isAnimating() {
        if (mIsAnimating) {
            if (now() - mStartTime >= DURATION) {
                mIsAnimating = false;
            }
        }
        return mIsAnimating;
    }

    protected float getRatio() {
        float r = (float) (now() - mStartTime) / DURATION;
        return Utils.clamp(1.0f - r, 0.0f, 1.0f);
    }

    private long now() {
        return AnimationTime.get();
    }
}
