
package com.xjt.crazypic.views.layout;

import com.xjt.crazypic.common.LLog;

import android.graphics.Rect;

public class ThumbnailLayout extends ThumbnailLayoutBase {

    private static final String TAG = ThumbnailLayout.class.getSimpleName();

    public ThumbnailLayout(ThumbnailLayoutParam spec) {
        mSpec = spec;
    }

    private void initThumbnailLayoutParameters(int majorUnitSize) {
        int count = ((mThumbnailCount + mColumnInMinorDirection - 1) / mColumnInMinorDirection);
        mContentLengthInMajorDirection = count * majorUnitSize + (count - 1) * mThumbnailGap;
    }

    @Override
    protected void initThumbnailLayoutParameters() {
        mThumbnailGap = mSpec.thumbnailGap;
        mColumnInMinorDirection = mSpec.rowsPort;
        mThumbnailWidth = Math.max(1, (mWidth - (mColumnInMinorDirection - 1) * mThumbnailGap) / mColumnInMinorDirection);
        mThumbnailHeight = mThumbnailWidth + mSpec.labelHeight;

        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mThumbnailWidth, mThumbnailHeight);
        }

        initThumbnailLayoutParameters(mThumbnailHeight);
        if (mThumbnailCount > 0) {
            updateVisibleThumbnailRange();
            LLog.i(TAG, "1 initLayoutParameters mContentLengthInMajorDirection:" + mContentLengthInMajorDirection + " column:" + mColumnInMinorDirection);
        } else {
            LLog.i(TAG, "0 initLayoutParameters mContentLengthInMajorDirection:" + mContentLengthInMajorDirection + " column:" + mColumnInMinorDirection);
        }
    }

    @Override
    protected void updateVisibleThumbnailRange() {
        int position = mScrollPosition;
        int startRow = position / (mThumbnailHeight + mThumbnailGap);
        int start = Math.max(0, mColumnInMinorDirection * startRow);
        int endRow = (position + mHeight + mThumbnailHeight + mThumbnailGap - 1) / (mThumbnailHeight + mThumbnailGap);
        int end = Math.min(mThumbnailCount, mColumnInMinorDirection * endRow);
        setVisibleThumbnailRange(start, end);
    }

    @Override
    protected void updateVisibleTagRange() {

    }

    @Override
    public Rect getThumbnailRect(int index, Rect rect) {
        int col, row;

        row = index / mColumnInMinorDirection;
        col = index - row * mColumnInMinorDirection;

        int x = col * (mThumbnailWidth + mThumbnailGap);
        int y = row * (mThumbnailHeight + mThumbnailGap);
        rect.set(x, y, x + mThumbnailWidth, y + mThumbnailHeight);
        return rect;
    }

    @Override
    public int getThumbnailIndexByPosition(float x, float y) {
        int absoluteX = Math.round(x);
        int absoluteY = Math.round(y) + mScrollPosition;

        if (absoluteX < 0 || absoluteY < 0) {
            return INDEX_NONE;
        }

        int columnIdx = absoluteX / (mThumbnailWidth + mThumbnailGap);
        int rowIdx = absoluteY / (mThumbnailHeight + mThumbnailGap);

        if (columnIdx >= mColumnInMinorDirection) {
            return INDEX_NONE;
        }

        if (absoluteX % (mThumbnailWidth + mThumbnailGap) >= mThumbnailWidth) {
            return INDEX_NONE;
        }

        if (absoluteY % (mThumbnailHeight + mThumbnailGap) >= mThumbnailHeight) {
            return INDEX_NONE;
        }

        int index = (rowIdx * mColumnInMinorDirection + columnIdx);

        return index >= mThumbnailCount ? INDEX_NONE : index;
    }

}
