
package com.xjt.crazypic.views.render;

import com.xjt.crazypic.NpContext;
import com.xjt.crazypic.R;
import com.xjt.crazypic.imagedata.adapters.ThumbnailSetDataWindow.AlbumSetEntry;
import com.xjt.crazypic.selectors.SelectionManager;
import com.xjt.crazypic.view.ThumbnailView;
import com.xjt.crazypic.views.opengl.GLESCanvas;
import com.xjt.crazypic.views.opengl.ResourceTexture;
import com.xjt.crazypic.views.opengl.Texture;
import com.xjt.crazypic.views.opengl.UploadedBitmapTexture;
import com.xjt.crazypic.views.utils.ViewConfigs;

public class ThumbnailSetGridRenderer extends ThumbnailSetRenderer {

    private ResourceTexture mBorderTexture;

    public ThumbnailSetGridRenderer(NpContext activity, ThumbnailView thumbnailView, SelectionManager selector) {
        super(activity, thumbnailView);
        mThumbnailLabelParam = ViewConfigs.AlbumSetGridPage.get(activity.getActivityContext()).labelSpec;
        mThumbnailParam = ViewConfigs.AlbumSetGridPage.get(activity.getActivityContext()).albumSetGridSpec;
        mBorderTexture = new ResourceTexture(activity.getActivityContext(), R.drawable.ic_gallery_border);
    }

    protected static Texture checkLabelTexture(Texture texture) {
        return ((texture instanceof UploadedBitmapTexture) && ((UploadedBitmapTexture) texture).isUploading()) ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        AlbumSetEntry entry = mDataWindow.get(index);
        int renderRequestFlags = 0;
        if (entry != null) {
            // 缩小绘制整体padding大小
            canvas.translate(mThumbnailLabelParam.labelHeight / 4 + mThumbnailLabelParam.labelHeight / 6, mThumbnailLabelParam.labelHeight / 4);
            width = width - mThumbnailLabelParam.labelHeight / 2 - mThumbnailLabelParam.labelHeight / 3; 
            //
            height = height - mThumbnailLabelParam.labelHeight / 2;
            //开始绘制
            mBorderTexture.draw(canvas, 0, 0, width, height - mThumbnailLabelParam.labelHeight); // 相框
            renderRequestFlags |= renderContent(canvas, entry, width, height - mThumbnailLabelParam.labelHeight); // 图片内容
            renderRequestFlags |= renderLabel(canvas, entry, width, height); // 标签
            renderRequestFlags |= renderOverlay(canvas, entry, index, width, height - mThumbnailLabelParam.labelHeight); // 按下效果
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
        drawContent(canvas, content, width - mThumbnailParam.thumbnailPadding, height - mThumbnailParam.thumbnailPadding, entry.rotation,
                mThumbnailParam.thumbnailPadding);
        return renderRequestFlags;
    }

    protected int renderLabel(GLESCanvas canvas, AlbumSetEntry entry, int width, int height) {
        Texture content = checkLabelTexture(entry.labelTexture);
        if (content == null) {
            content = mDefaulTexture;
            return 0;
        }
        int h = mThumbnailLabelParam.labelHeight;
        content.draw(canvas, 0, height - h, width, h);
        return 0;
    }

    protected int renderOverlay(GLESCanvas canvas, AlbumSetEntry entry, int index, int width, int height) {
        int renderRequestFlags = 0;
        if (mPressedIndex == index) {
            canvas.translate(mThumbnailParam.thumbnailPadding/2, mThumbnailParam.thumbnailPadding/2);
            if (mAnimatePressedUp) {
                drawPressedUpFrame(canvas, width -  mThumbnailParam.thumbnailPadding, height -  mThumbnailParam.thumbnailPadding);
                renderRequestFlags |= ThumbnailView.RENDER_MORE_FRAME;
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                }
            } else {
                drawPressedFrame(canvas, width - mThumbnailParam.thumbnailPadding, height -  mThumbnailParam.thumbnailPadding);
            }
            canvas.translate(-mThumbnailParam.thumbnailPadding/2, -mThumbnailParam.thumbnailPadding/2);
        }
        return renderRequestFlags;
    }

}
