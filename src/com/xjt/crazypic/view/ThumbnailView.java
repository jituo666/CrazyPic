package com.xjt.crazypic.view;

import com.xjt.crazypic.NpContext;
import com.xjt.crazypic.animations.AnimationTime;
import com.xjt.crazypic.animations.ThumbnailAnim;
import com.xjt.crazypic.animations.ThumbnailRisingAnim;
import com.xjt.crazypic.animations.ThumbnailScatteringAnim;
import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.common.SynchronizedHandler;
import com.xjt.crazypic.preference.GlobalPreference;
import com.xjt.crazypic.utils.RelativePosition;
import com.xjt.crazypic.utils.Utils;
import com.xjt.crazypic.views.layout.ThumbnailLayoutBase;
import com.xjt.crazypic.views.opengl.GLESCanvas;
import com.xjt.crazypic.views.utils.UIListener;
import com.xjt.crazypic.views.utils.ViewScrollerHelper;
import com.xjt.crazypic.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @Author Jituo.Xuan
 * @Date 2:50:38 PM Mar 25, 2014
 * @Comments:null
 */

public class ThumbnailView extends GLView {

    private static final String TAG = ThumbnailView.class.getSimpleName();

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;

    public static final int RENDER_MORE_PASS = 1;
    public static final int RENDER_MORE_FRAME = 2;

    private int mStartIndex = ThumbnailLayoutBase.INDEX_NONE;

    private int mOverscrollEffect = OVERSCROLL_NONE;
    private final Paper mPaper = new Paper();
    private ThumbnailAnim mAnimation = null;
    private final Rect mTempRect = new Rect(); // to prevent allocating memory
    private boolean mDownInScrolling;
    private ViewScrollerHelper mScroller;
    private GestureDetector mGestureDetector;
    private UIListener mUIListener;
    private SynchronizedHandler mHandler;
    private Listener mListener;
    private ThumbnailLayoutBase mLayout;
    private Renderer mRenderer;

    private ScrollBarView mScrollBar;

    private NpContext mLetoolContext;

    //////////////////////////////////////////////////////////////Animations////////////////////////////////////////////////////////////////////

    public void startScatteringAnimation(RelativePosition position, boolean scatterX, boolean scatterY, boolean scatterZ) {
        if (GlobalPreference.isAnimationOpen(mLetoolContext.getActivityContext())) {
            mAnimation = new ThumbnailScatteringAnim(position, scatterX, scatterY, scatterZ);
            mAnimation.start();
            if (mLayout.getThumbnailHeight() != 0)
                invalidate();
        }
    }

    public void startRisingAnimation() {
        if (GlobalPreference.isAnimationOpen(mLetoolContext.getActivityContext())) {
            mAnimation = new ThumbnailRisingAnim();
            mAnimation.start();
            if (mLayout.getThumbnailCount() != 0)
                invalidate();
        }
    }

    public boolean advanceAnimation(long animTime) {
        if (mAnimation != null) {
            return mAnimation.calculate(animTime);
        }
        return false;
    }

    //////////////////////////////////////////////////////////////Event Handler//////////////////////////////////////////////////////////////
    public interface Listener {

        public void onDown(int index);

        public void onUp(boolean followedByLongPress);

        public void onSingleTapUp(int index);

        public void onLongTap(int index);

        public void onScrollPositionChanged(int position, int total);
    }

    public static class SimpleListener implements Listener {

        @Override
        public void onDown(int index) {
        }

        @Override
        public void onUp(boolean followedByLongPress) {
        }

        @Override
        public void onSingleTapUp(int index) {
        }

        @Override
        public void onLongTap(int index) {
        }

        @Override
        public void onScrollPositionChanged(int position, int total) {
        }

    }

    private class MyGestureListener implements GestureDetector.OnGestureListener {

        private boolean isDown;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            cancelDown(false);
            int scrollLimit = mLayout.getScrollLimit();
            if (scrollLimit == 0)
                return false;
            float velocity = velocityY;
            mScroller.fling((int) -velocity, 0, scrollLimit);
            if (mUIListener != null)
                mUIListener.onUserInteractionBegin();
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            cancelDown(false);
            float distance = distanceY;
            int overDistance = mScroller.startScroll(Math.round(distance), 0, mLayout.getScrollLimit());
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
                mPaper.overScroll(overDistance);
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            LLog.i(TAG, "onSingleTapUp");
            cancelDown(false);
            if (mDownInScrolling)
                return true;
            int index = mLayout.getThumbnailIndexByPosition(e.getX(), e.getY());
            if (index != ThumbnailLayoutBase.INDEX_NONE && mListener != null)
                mListener.onSingleTapUp(index);
            return true;
        }

        private void cancelDown(boolean byLongPress) {
            if (!isDown)
                return;
            isDown = false;
            if (mListener != null)
                mListener.onUp(byLongPress);
        }

        @Override
        public boolean onDown(MotionEvent arg0) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            LLog.i(TAG, "onLongPress");
            cancelDown(true);
            if (mDownInScrolling)
                return;
            lockRendering();
            try {
                int index = mLayout.getThumbnailIndexByPosition(e.getX(), e.getY());
                if (index != ThumbnailLayoutBase.INDEX_NONE && mListener != null)
                    mListener.onLongTap(index);
            } finally {
                unlockRendering();
            }
        }

        @Override
        public void onShowPress(MotionEvent e) {
            GLController root = getGLController();
            root.lockRenderThread();
            try {
                if (isDown)
                    return;
                int index = mLayout.getThumbnailIndexByPosition(e.getX(), e.getY());
                if (index != ThumbnailLayoutBase.INDEX_NONE) {
                    isDown = true;
                    if (mListener != null)
                        mListener.onDown(index);
                }
            } finally {
                root.unlockRenderThread();
            }

        }

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setUserInteractionListener(UIListener listener) {
        mUIListener = listener;
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        if (mUIListener != null)
            mUIListener.onUserInteraction();
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownInScrolling = !mScroller.isFinished();
                mScroller.forceFinished();
                break;
            case MotionEvent.ACTION_UP:
                mPaper.onRelease();
                invalidate();
                break;
        }
        return true;
    }

    
    ////////////////////////////////////////////////////////////////Render/////////////////////////////////////////////////////////////////

    public static interface Renderer {

        public void prepareDrawing();

        public void onVisibleThumbnailRangeChanged(int visibleStart, int visibleEnd);

        public void onThumbnailSizeChanged(int width, int height);

        public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height);

    }

    public void setThumbnailRenderer(Renderer render) {
        mRenderer = render;
        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mLayout.getThumbnailWidth(), mLayout.getThumbnailHeight());
        }
    }

    public void setOverscrollEffect(int kind) {
        mOverscrollEffect = kind;
        mScroller.setOverfling(kind == OVERSCROLL_SYSTEM);
    }
    
    @Override
    protected void render(GLESCanvas canvas) {
        super.render(canvas);
        if (mRenderer == null || mLayout.getThumbnailCount() == 0)
            return;
        mRenderer.prepareDrawing();
        long animTime = AnimationTime.get();
        boolean more = mScroller.advanceAnimation(animTime);
        more |= advanceAnimation(animTime);
        int oldX = mScrollX;
        updateScrollPosition(mScroller.getPosition(), false);
        boolean paperActive = false;
        if (mOverscrollEffect == OVERSCROLL_3D) {
            // Check if an edge is reached and notify mPaper if so.
            int newX = mScrollX;
            int limit = mLayout.getScrollLimit();
            if (oldX > 0 && newX == 0 || oldX < limit && newX == limit) {
                float v = mScroller.getCurrVelocity();
                if (newX == limit)
                    v = -v;
                // I don't know why, but getCurrVelocity() can return NaN.
                if (!Float.isNaN(v)) {
                    mPaper.edgeReached(v);
                }
            }
            paperActive = mPaper.advanceAnimation();
        }
        more |= paperActive;
        if (mAnimation != null) {
            more |= mAnimation.calculate(animTime);
        }
        canvas.translate(-mScrollX, -mScrollY);
        //LLog.i(TAG, "render item start:" + mLayout.getVisibleThumbnailStart() + " end:" + mLayout.getVisibleThumbnailEnd());
        for (int i = mLayout.getVisibleThumbnailEnd() - 1; i >= mLayout.getVisibleThumbnailStart(); --i) {
            if ((renderItem(canvas, i, 0, paperActive) & RENDER_MORE_FRAME) != 0) {
                more = true;
            }
        }

        canvas.translate(mScrollX, mScrollY);
        //renderChild(canvas, mScrollBar);
        if (more) {
            invalidate();
        }
    }

    private int renderItem(GLESCanvas canvas, int index, int pass, boolean paperActive) {
        canvas.save(GLESCanvas.SAVE_FLAG_ALPHA | GLESCanvas.SAVE_FLAG_MATRIX);
        Rect rect = mLayout.getThumbnailRect(index, mTempRect);
        if (paperActive) {
            canvas.multiplyMatrix(mPaper.getTransform(rect, mScrollY), 0);
        } else {
            canvas.translate(rect.left, rect.top, 0);
        }
        if (mAnimation != null && mAnimation.isActive()) {
            mAnimation.apply(canvas, index, rect);
        }
        int result = mRenderer.renderThumbnail(canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
        canvas.restore();
        return result;
    }

    ////////////////////////////////////////////////////////////Layout////////////////////////////////////////////////////////////////////

    public ThumbnailView(NpContext context, ThumbnailLayoutBase layout) {
        Context cxt = context.getActivityContext();
        mLetoolContext = context;
        mGestureDetector = new GestureDetector(cxt, new MyGestureListener());
        mScroller = new ViewScrollerHelper(cxt);
        mLayout = layout;
        int w = Math.round(cxt.getResources().getDimension(R.dimen.common_scroll_bar_width));
        int h = Math.round(cxt.getResources().getDimension(R.dimen.common_scroll_bar_height));
        mScrollBar = new ScrollBarView(cxt, w, h);
        mScrollBar.setVisibility(View.INVISIBLE);
        addComponent(mScrollBar);
        mHandler = new SynchronizedHandler(context.getGLController());
    }

    // Make sure we are still at a resonable scroll position after the size
    // is changed (like orientation change). We choose to keep the center visible thumbnail still visible. This is arbitrary but reasonable.
    @SuppressLint("WrongCall")
    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (!changeSize)
            return;
        int w = r - l;
        int h = b - t;
        mScrollBar.layout(0, 0, w, h);
        int visibleCenterIndex = (mLayout.getVisibleThumbnailStart() + mLayout.getVisibleThumbnailEnd()) / 2;
        mLayout.setThumbnailViewSize(r - l, b - t);
        LLog.i(TAG, " onLayout visibleCenterIndex:" + visibleCenterIndex);
        resetVisibleRange(visibleCenterIndex);
        showScrollBarView();
        if (mOverscrollEffect == OVERSCROLL_3D) {
            mPaper.setSize(r - l, b - t);
        }
    }

    private void showScrollBarView() {

        if (mLayout.getThumbnailCount() > 0 && mLayout.getScrollLimit() <= 0) {
            mScrollBar.setVisibility(View.INVISIBLE);
        } else if (mLayout.getVisibleThumbnailEnd() > 0) {
            //mScrollBar.setVisibility(View.VISIBLE);
            mScrollBar.setVisibility(View.INVISIBLE); // 暂时不显示scroll bar
        }
    }

    public void setThumbnailCount(int thumbnailCount) {
        mLayout.setThumbnailCount(thumbnailCount);
        // mStartIndex is applied the first time setSlotCount is called.
        if (mStartIndex != ThumbnailLayoutBase.INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = ThumbnailLayoutBase.INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(mScrollY);
        showScrollBarView();
    }

    public void setCenterIndex(int index) {
        int thumbnailCount = mLayout.getThumbnailCount();
        if (index < 0 || index >= thumbnailCount) {
            return;
        }
        Rect rect = mLayout.getThumbnailRect(index, mTempRect);
        int position = (rect.top + rect.bottom - getHeight()) / 2;
        setScrollPosition(position);
    }

    public void resetVisibleRange(int centerIndex) {
        Rect rect = mLayout.getThumbnailRect(centerIndex, mTempRect);
        int visibleBegin = mScrollY;
        int visibleLength = getHeight();
        int visibleEnd = visibleBegin + visibleLength;
        int thumbnailBegin = rect.top;
        int thumbnailEnd = rect.bottom;

        int position = visibleBegin;
        if (visibleLength < thumbnailEnd - thumbnailBegin) {
            position = visibleBegin;
        } else if (thumbnailBegin < visibleBegin) {
            position = thumbnailBegin;
        } else if (thumbnailEnd > visibleEnd) {
            position = thumbnailEnd - visibleLength;
        }
        setScrollPosition(position);
    }

    public Rect getThumbnailRect(int thumbnailIndex) {
        return mLayout.getThumbnailRect(thumbnailIndex, mTempRect);
    }

    public int getScrollX() {
        return mScrollX;
    }

    public int getScrollY() {
        return mScrollY;
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        mScroller.setPosition(position);
        updateScrollPosition(position, false);
    }

    private void updateScrollPosition(int position, boolean force) {
        if (!force && (position == mScrollY))
            return;
        mScrollY = position;
        mLayout.setScrollPosition(position);
        if (mListener != null) {
            mListener.onScrollPositionChanged(position, mLayout.getScrollLimit());
            mScrollBar.setContentPosition(position, mLayout.getScrollLimit());
        }
    }

    public int getVisibleThumbnailStart() {
        return mLayout.getVisibleThumbnailStart();
    }

    public int getVisibleThumbnailEnd() {
        return mLayout.getVisibleThumbnailEnd();
    }
}
