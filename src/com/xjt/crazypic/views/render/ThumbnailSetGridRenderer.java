package com.xjt.crazypic.views.render;

import com.xjt.crazypic.NpContext;
import com.xjt.crazypic.imagedata.adapters.ThumbnailSetDataWindow.AlbumSetEntry;
import com.xjt.crazypic.selectors.SelectionManager;
import com.xjt.crazypic.view.ThumbnailView;
import com.xjt.crazypic.views.opengl.GLESCanvas;
import com.xjt.crazypic.views.opengl.Texture;
import com.xjt.crazypic.views.opengl.UploadedBitmapTexture;
import com.xjt.crazypic.views.utils.ViewConfigs;

public class ThumbnailSetGridRenderer extends ThumbnailSetRenderer {

    public ThumbnailSetGridRenderer(NpContext activity, ThumbnailView thumbnailView, SelectionManager selector) {
        super(activity, thumbnailView);
        mLabelSpec = ViewConfigs.AlbumSetGridPage.get(activity.getActivityContext()).labelSpec;
    }

    protected static Texture checkLabelTexture(Texture texture) {
        return ((texture instanceof UploadedBitmapTexture) && ((UploadedBitmapTexture) texture).isUploading()) ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        AlbumSetEntry entry = mDataWindow.get(index);
        int renderRequestFlags = 0;
        if (entry != null) {
            renderRequestFlags |= renderContent(canvas, entry, width, height - mLabelSpec.labelHeight);
            renderRequestFlags |= renderLabel(canvas, entry, width, height);
            renderRequestFlags |= renderOverlay(canvas, entry, index, width, height - mLabelSpec.labelHeight);
        }
        return renderRequestFlags;
    }

    protected int renderContent(GLESCanvas canvas, AlbumSetEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        Texture content = entry.bitmapTexture;
        if (content == null) {
            content = mDefaulTexture;
            entry.isWaitLoadingDisplayed = true;
        }
        drawContent(canvas, content, width, height, entry.rotation);
        return renderRequestFlags;
    }

    protected int renderLabel(GLESCanvas canvas, AlbumSetEntry entry, int width, int height) {
        Texture content = checkLabelTexture(entry.labelTexture);
        if (content == null) {
            content = mDefaulTexture;
            return 0;
        }
        int h = mLabelSpec.labelHeight;
        content.draw(canvas, 0, height - h, width, h);
        return 0;
    }

    protected int renderOverlay(GLESCanvas canvas, AlbumSetEntry entry, int index, int width, int height) {
        int renderRequestFlags = 0;

        if (mPressedIndex == index) {
            if (mAnimatePressedUp) {
                drawPressedUpFrame(canvas, width, height);
                renderRequestFlags |= ThumbnailView.RENDER_MORE_FRAME;
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                }
            } else {
                drawPressedFrame(canvas, width, height);
            }
        }
        return renderRequestFlags;
    }



}
