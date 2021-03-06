
package com.xjt.crazypic.view;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.umeng.analytics.MobclickAgent;

import com.xjt.crazypic.animations.AnimationTime;
import com.xjt.crazypic.animations.CanvasAnim;
import com.xjt.crazypic.common.ApiHelper;
import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.common.OrientationSource;
import com.xjt.crazypic.views.opengl.BasicTexture;
import com.xjt.crazypic.views.opengl.GLES11Canvas;
import com.xjt.crazypic.views.opengl.GLES20Canvas;
import com.xjt.crazypic.views.opengl.GLESCanvas;
import com.xjt.crazypic.views.opengl.UploadedBitmapTexture;
import com.xjt.crazypic.views.utils.MotionEventHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * @Author Jituo.Xuan
 * @Date 6:35:55 PM Mar 20, 2014
 * @Comments:null
 */
public class GLRootView extends GLSurfaceView implements GLSurfaceView.Renderer, GLController {

    private static final String TAG = GLRootView.class.getSimpleName();

    private static final int FLAG_INITIALIZED = 1;
    private static final int FLAG_NEED_LAYOUT = 2;

    private GL11 mGL;
    private GLESCanvas mCanvas;
    private GLView mContentView;
    private OrientationSource mOrientationSource;
    // mCompensation is the difference between the UI orientation on GLCanvas and the framework orientation. See OrientationManager for details.
    private int mCompensation;
    // mCompensationMatrix maps the coordinates of touch events. It is kept sync with mCompensation.
    private Matrix mCompensationMatrix = new Matrix();
    private int mDisplayRotation;

    private int mFlags = FLAG_NEED_LAYOUT;
    private volatile boolean mRenderRequested = false;
    private boolean mInDownState = false;
    private final ReentrantLock mRenderLock = new ReentrantLock();
    private final Condition mFreezeCondition = mRenderLock.newCondition();
    private boolean mFreeze;
    private final ArrayList<CanvasAnim> mAnimations = new ArrayList<CanvasAnim>();

    private final ArrayDeque<OnGLIdleListener> mIdleListeners = new ArrayDeque<OnGLIdleListener>();

    private final IdleRunner mIdleRunner = new IdleRunner();

    public GLRootView(Context context) {
        this(context, null);
    }

    public GLRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFlags |= FLAG_INITIALIZED;
        setEGLContextClientVersion(ApiHelper.HAS_GLES20_REQUIRED ? 2 : 1);
        setEGLConfigChooser(5, 6, 5, 0, 0, 0);
        setRenderer(this);
        getHolder().setFormat(PixelFormat.RGB_565);
    }

    @Override
    public void requestLayoutContentPane() {
        mRenderLock.lock();
        try {
            if (mContentView == null || (mFlags & FLAG_NEED_LAYOUT) != 0)
                return;
            // "View" system will invoke onLayout() for initialization(bug ?), we have to ignore it since the GLThread is not ready yet.
            if ((mFlags & FLAG_INITIALIZED) == 0)
                return;
            mFlags |= FLAG_NEED_LAYOUT;
            requestRender();
        } finally {
            mRenderLock.unlock();
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed)
            requestLayoutContentPane();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;

        int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mInDownState = false;
        } else if (!mInDownState && action != MotionEvent.ACTION_DOWN) {
            return false;
        }

        if (mCompensation != 0) {
            event = MotionEventHelper.transformEvent(event, mCompensationMatrix);
        }

        mRenderLock.lock();
        try {
            // If this has been detached from root, we don't need to handle event
            boolean handled = mContentView != null && mContentView.dispatchTouchEvent(event);
            if (action == MotionEvent.ACTION_DOWN && handled) {
                mInDownState = true;
            }
            return handled;
        } finally {
            mRenderLock.unlock();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void requestRender() {
        if (mRenderRequested)
            return;
        mRenderRequested = true;
        if (ApiHelper.HAS_POST_ON_ANIMATION) {
            postOnAnimation(mRequestRenderOnAnimationFrame);
        } else {
            super.requestRender();
        }
    }

    private Runnable mRequestRenderOnAnimationFrame = new Runnable() {

        @Override
        public void run() {
            superRequestRender();
        }
    };

    private void superRequestRender() {
        super.requestRender();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
        GL11 gl = (GL11) gl1;
        if (mGL != null) {
            LLog.i(TAG, "GLObject has changed from " + mGL + " to " + gl);// The GL Object has changed
        }
        mRenderLock.lock();
        try {
            mGL = gl;
            mCanvas = ApiHelper.HAS_GLES20_REQUIRED ? new GLES20Canvas() : new GLES11Canvas(gl);
            BasicTexture.invalidateAllTextures();

        } finally {
            mRenderLock.unlock();
        }
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        MobclickAgent.setOpenGLContext(gl1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCanvas.setSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        AnimationTime.update();
        mRenderLock.lock();
        while (mFreeze) {
            mFreezeCondition.awaitUninterruptibly();
        }
        try {
            onDrawFrameLocked(gl);
        } finally {
            mRenderLock.unlock();
        }
    }

    private void onDrawFrameLocked(GL10 gl) {

        mCanvas.deleteRecycledResources();// release the unbound textures and deleted buffers.

        UploadedBitmapTexture.resetUploadLimit();// reset texture upload limit

        mRenderRequested = false;

        if ((mOrientationSource != null && mDisplayRotation != mOrientationSource.getDisplayRotation()) || (mFlags & FLAG_NEED_LAYOUT) != 0) {
            layoutContentPane();
        }

        mCanvas.save(GLESCanvas.SAVE_FLAG_ALL);

        rotateCanvas(-mCompensation);

        if (mContentView != null) {
            mContentView.render(mCanvas);
        } else {
            mCanvas.clearBuffer();// Make sure we always draw something to prevent displaying garbage
        }
        mCanvas.restore();

        if (!mAnimations.isEmpty()) {
            long now = AnimationTime.get();
            for (int i = 0, n = mAnimations.size(); i < n; i++) {
                mAnimations.get(i).setStartTime(now);
            }
            mAnimations.clear();
        }

        if (UploadedBitmapTexture.uploadLimitReached()) {
            requestRender();
        }
        synchronized (mIdleListeners) {
            if (!mIdleListeners.isEmpty())
                mIdleRunner.enable();
        }

    }

    private void rotateCanvas(int degrees) {
        if (degrees == 0)
            return;
        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;
        mCanvas.translate(cx, cy);
        mCanvas.rotate(degrees, 0, 0, 1);
        if (degrees % 180 != 0) {
            mCanvas.translate(-cy, -cx);
        } else {
            mCanvas.translate(-cx, -cy);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Register the newly launched animation so that we can set the start time more precisely. (Usually, it takes much longer for first
    // rendering, so we set the animation start time as the time we complete rendering)
    @Override
    public void registerLaunchedAnimation(CanvasAnim animation) {
        mAnimations.add(animation);
    }

    @Override
    public void addOnGLIdleListener(OnGLIdleListener listener) {
        synchronized (mIdleListeners) {
            mIdleListeners.addLast(listener);
            mIdleRunner.enable();
        }
    }

    @Override
    public void requestRenderForced() {
        superRequestRender();
    }

    private void layoutContentPane() {

        int w = getWidth();
        int h = getHeight();
        int displayRotation = 0;
        int compensation = 0;

        mFlags &= ~FLAG_NEED_LAYOUT;
        // Get the new orientation values
        if (mOrientationSource != null) {
            displayRotation = mOrientationSource.getDisplayRotation();
            compensation = mOrientationSource.getCompensation();
        } else {
            displayRotation = 0;
            compensation = 0;
        }

        if (mCompensation != compensation) {
            mCompensation = compensation;
            if (mCompensation % 180 != 0) {
                mCompensationMatrix.setRotate(mCompensation);
                // move center to origin before rotation
                mCompensationMatrix.preTranslate(-w / 2, -h / 2);
                // align with the new origin after rotation
                mCompensationMatrix.postTranslate(h / 2, w / 2);
            } else {
                mCompensationMatrix.setRotate(mCompensation, w / 2, h / 2);
            }
        }
        mDisplayRotation = displayRotation;

        // Do the actual layout.
        if (mCompensation % 180 != 0) {
            int tmp = w;
            w = h;
            h = tmp;
        }
        LLog.i(TAG, "layout content pane w:" + w + "h" + h + ":" + System.currentTimeMillis());
        if (mContentView != null && w != 0 && h != 0) {
            mContentView.layout(0, 0, w, h);
        }
    }

    @Override
    public void lockRenderThread() {
        mRenderLock.lock();
    }

    @Override
    public void unlockRenderThread() {
        mRenderLock.unlock();
    }

    @Override
    public void setContentPane(GLView content) {
        if (mContentView == content)
            return;
        if (mContentView != null) {
            if (mInDownState) {
                long now = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                mContentView.dispatchTouchEvent(cancelEvent);
                cancelEvent.recycle();
                mInDownState = false;
            }
            mContentView.detachFromRoot();
            BasicTexture.yieldAllTextures();
        }
        mContentView = content;
        if (content != null) {
            content.attachToRoot(this);
            requestLayoutContentPane();
        }
    }

    @Override
    public void setOrientationSource(OrientationSource source) {
        mOrientationSource = source;
    }

    @Override
    public int getDisplayRotation() {
        return mDisplayRotation;
    }

    @Override
    public int getCompensation() {
        return mCompensation;
    }

    @Override
    public Matrix getCompensationMatrix() {
        return mCompensationMatrix;
    }

    @Override
    public void freeze() {
        mRenderLock.lock();
        mFreeze = true;
        mRenderLock.unlock();
    }

    @Override
    public void unfreeze() {
        mRenderLock.lock();
        mFreeze = false;
        mFreezeCondition.signalAll();
        mRenderLock.unlock();
    }

    @Override
    public void setLightsOutMode(boolean enabled) {

    }

    // We need to unfreeze in the following methods and in onPause().
    // These methods will wait on GLThread. If we have freezed the GLRootView,
    // the GLThread will wait on main thread to call unfreeze and cause dead lock.
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        unfreeze();
        super.surfaceChanged(holder, format, w, h);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        unfreeze();
        super.surfaceCreated(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        unfreeze();
        super.surfaceDestroyed(holder);
    }

    @Override
    protected void onDetachedFromWindow() {
        unfreeze();
        super.onDetachedFromWindow();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class IdleRunner implements Runnable {

        private boolean mActive = false;// true if the idle runner is in the queue

        @Override
        public void run() {
            OnGLIdleListener listener;
            synchronized (mIdleListeners) {
                mActive = false;
                if (mIdleListeners.isEmpty())
                    return;
                listener = mIdleListeners.removeFirst();
            }
            mRenderLock.lock();
            boolean keepInQueue;
            try {
                keepInQueue = listener.onGLIdle(mCanvas, mRenderRequested);

            } finally {
                mRenderLock.unlock();
            }
            synchronized (mIdleListeners) {
                if (keepInQueue)
                    mIdleListeners.addLast(listener);
                if (!mRenderRequested && !mIdleListeners.isEmpty())
                    enable();
            }
        }

        public void enable() {
            if (mActive) // Who gets the flag can add it to the queue
                return;
            mActive = true;
            queueEvent(this);
        }
    }
}
