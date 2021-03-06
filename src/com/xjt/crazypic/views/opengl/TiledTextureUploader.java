package com.xjt.crazypic.views.opengl;

import java.util.ArrayDeque;

import android.os.SystemClock;

import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.view.GLController;
import com.xjt.crazypic.view.GLController.OnGLIdleListener;

public class TiledTextureUploader implements OnGLIdleListener {

    private static final String TAG = TiledTextureUploader.class.getSimpleName();
    private static final int INIT_CAPACITY = 8;

    // We are targeting at 60fps, so we have 16ms for each frame.
    // In this 16ms, we use about 4~8 ms to upload tiles.
    private static final long UPLOAD_TILE_LIMIT = 4; // ms

    private final ArrayDeque<TiledTexture> mTextures = new ArrayDeque<TiledTexture>(INIT_CAPACITY);

    private final GLController mGLController;
    private boolean mIsQueued = false;

    public TiledTextureUploader(GLController glController) {
        mGLController = glController;
    }

    public synchronized void clear() {
        mTextures.clear();
    }

    public synchronized void addTexture(TiledTexture t) {
        if (t.isReady())
            return;
        mTextures.addLast(t);

        if (mIsQueued)
            return;
        mIsQueued = true;
        mGLController.addOnGLIdleListener(this);
    }

    @Override
    public boolean onGLIdle(GLESCanvas canvas, boolean renderRequested) {
        ArrayDeque<TiledTexture> deque = mTextures;
        synchronized (this) {
            long now = SystemClock.uptimeMillis();
            long dueTime = now + UPLOAD_TILE_LIMIT;
            while (now < dueTime && !deque.isEmpty()) {
                long time = System.currentTimeMillis();
                TiledTexture t = deque.peekFirst();
                if (t.uploadNextTile(canvas)) {
                    deque.removeFirst();
                    mGLController.requestRender();
                    //LLog.i(TAG, "-----------:::" + (System.currentTimeMillis() - time));
                }
                now = SystemClock.uptimeMillis();
            }
            mIsQueued = !mTextures.isEmpty();

            // return true to keep this listener in the queue
            return mIsQueued;
        }
    }
}
