
package com.xjt.crazypic.views.render;

import android.content.Context;

import com.xjt.crazypic.NpContext;
import com.xjt.crazypic.imagedata.adapters.ThumbnailSetDataWindow;
import com.xjt.crazypic.metadata.MediaPath;
import com.xjt.crazypic.metadata.loader.ThumbnailSetDataLoader;
import com.xjt.crazypic.view.ThumbnailView;
import com.xjt.crazypic.views.opengl.ColorTexture;
import com.xjt.crazypic.views.opengl.ResourceTexture;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:16 PM Jul 24, 2014
 * @Comments:null
 */
public abstract class ThumbnailSetRenderer extends AbstractThumbnailRender {

    private static final String TAG = ThumbnailSetRenderer.class.getSimpleName();

    private static final int CACHE_SIZE = 48;

    protected NpContext mLetoolContext;

    protected  ColorTexture mDefaulTexture;

    protected  ResourceTexture mVideoPlayIcon;
    protected  ResourceTexture mVideoOverlay;

    protected ThumbnailView mThumbnailView;
    protected ThumbnailSetDataWindow mDataWindow;

    protected int mPressedIndex = -1;
    protected boolean mAnimatePressedUp;
    protected MediaPath mHighlightItemPath = null;

    protected LabelSpec mLabelSpec;

    public static class LabelSpec {

        public int labelHeight;
        public int backgroundColor;
        public int titleFontSize;
        public int countFontSize;
        public int titleColor;
        public int countColor;
        public int borderSize;
    }

    private class MyCacheListener implements ThumbnailSetDataWindow.Listener {

        @Override
        public void onSizeChanged(int size) {
            mThumbnailView.setThumbnailCount(size);
        }

        @Override
        public void onContentChanged() {
            mThumbnailView.invalidate();
        }
    }

    protected ThumbnailSetRenderer (Context context) {
        super(context);
    }
    public void setModel(ThumbnailSetDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mDataWindow = null;
            mThumbnailView.setThumbnailCount(0);
        }
        if (model != null) {
            mDataWindow = new ThumbnailSetDataWindow(mLetoolContext, model, mLabelSpec, CACHE_SIZE);
            mDataWindow.setListener(new MyCacheListener());
            mThumbnailView.setThumbnailCount(mDataWindow.size());
        }
    }

    public void setPressedIndex(int index) {
        if (mPressedIndex == index)
            return;
        mPressedIndex = index;
        mThumbnailView.invalidate();
    }

    public void setPressedUp() {
        if (mPressedIndex == -1)
            return;
        mAnimatePressedUp = true;
        mThumbnailView.invalidate();
    }

    public void setHighlightItemPath(MediaPath path) {
        if (mHighlightItemPath == path)
            return;
        mHighlightItemPath = path;
        mThumbnailView.invalidate();
    }

    @Override
    public void prepareDrawing() {
    }

    public void pause() {
        mDataWindow.pause();
    }

    public void resume() {
        mDataWindow.resume();
    }

    @Override
    public void onVisibleThumbnailRangeChanged(int visibleStart, int visibleEnd) {
        if (mDataWindow != null) {
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    @Override
    public void onThumbnailSizeChanged(int width, int height) {
        if (mDataWindow != null) {
            mDataWindow.onThumbnailSizeChanged(width, height);
        }
    }

}
