
package com.xjt.crazypic.edit.imageshow;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.xjt.crazypic.edit.editors.EditorDraw;
import com.xjt.crazypic.edit.filters.FilterDrawRepresentation;
import com.xjt.crazypic.R;

public class ImageDraw extends ImageShow {

    private static final String TAG = ImageDraw.class.getSimpleName();

    final static float INITAL_STROKE_RADIUS = 40;
    private float mCurrentSize = INITAL_STROKE_RADIUS;
    private int mType = 0;
    private FilterDrawRepresentation mFRep;
    private EditorDraw mEditorDraw;
    private long mTimeout;
    private Paint mShadowPaint = new Paint();
    private Paint mIconPaint = new Paint();
    private Paint mBorderPaint = new Paint();
    private Handler mHandler;
    private FilterDrawRepresentation.StrokeData mTmpStrokData = new FilterDrawRepresentation.StrokeData();
    private int DISPLAY_TIME = 100;
    private Matrix mRotateToScreen = new Matrix();
    private Matrix mToOrig;
    private int mBorderColor;
    private int mBorderShadowSize;

    Runnable mUpdateRunnable = new Runnable() {

        @Override
        public void run() {
            invalidate();
        }
    };

    public ImageDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        resetParameter();
        setupConstants(context);
        setupTimer();
    }

    public ImageDraw(Context context) {
        super(context);
        resetParameter();
        setupConstants(context);
        setupTimer();
    }

    private void setupConstants(Context context) {
        Resources res = context.getResources();
        mBorderShadowSize = res.getDimensionPixelSize(R.dimen.draw_rect_shadow);
        float edge = res.getDimensionPixelSize(R.dimen.draw_rect_border_edge);
        mBorderColor = res.getColor(R.color.draw_rect_border);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(edge);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setShadowLayer(mBorderShadowSize, mBorderShadowSize, mBorderShadowSize, Color.BLACK);
    }

    public void setEditor(EditorDraw editorDraw) {
        mEditorDraw = editorDraw;
    }

    public void setFilterDrawRepresentation(FilterDrawRepresentation fr) {
        mFRep = fr;
        mTmpStrokData = new FilterDrawRepresentation.StrokeData();
    }

    public Drawable getIcon(Context context) {

        return null;
    }

    @Override
    public void resetParameter() {
        if (mFRep != null) {
            mFRep.clear();
        }
    }

    public void setSize(int size) {
        mCurrentSize = size;
    }

    public void setStyle(int style) {
        mType = style;
    }

    public int getStyle() {
        return mType;
    }

    public int getSize() {
        return (int) mCurrentSize;
    }

    float[] mTmpPoint = new float[2]; // so we do not malloc

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            boolean ret = super.onTouchEvent(event);
            if (mFRep.getCurrentDrawing() != null) {
                mFRep.clearCurrentSection();
                mEditorDraw.commitLocalRepresentation();
            }
            return ret;
        }
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            if (mFRep.getCurrentDrawing() == null) {
                return super.onTouchEvent(event);
            }
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            calcScreenMapping();
            mTmpPoint[0] = event.getX();
            mTmpPoint[1] = event.getY();
            mToOrig.mapPoints(mTmpPoint);
            mFRep.startNewSection(mTmpPoint[0], mTmpPoint[1]);
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; h++) {
                int p = 0;
                {
                    mTmpPoint[0] = event.getHistoricalX(p, h);
                    mTmpPoint[1] = event.getHistoricalY(p, h);
                    mToOrig.mapPoints(mTmpPoint);
                    mFRep.addPoint(mTmpPoint[0], mTmpPoint[1]);
                }
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            mTmpPoint[0] = event.getX();
            mTmpPoint[1] = event.getY();
            mToOrig.mapPoints(mTmpPoint);
            mFRep.endSection(mTmpPoint[0], mTmpPoint[1]);
        }
        mEditorDraw.commitLocalRepresentation();
        invalidate();
        return true;
    }

    private void calcScreenMapping() {
        mToOrig = getScreenToImageMatrix(true);
        mToOrig.invert(mRotateToScreen);
    }

    private void setupTimer() {
        mHandler = new Handler(getActivity().getMainLooper());
    }

    private void scheduleWakeup(int delay) {
        mHandler.removeCallbacks(mUpdateRunnable);
        mHandler.postDelayed(mUpdateRunnable, delay);
    }

    public Bitmap getBrush(int brushid) {
        Bitmap bitmap;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;
        bitmap = BitmapFactory.decodeResource(getActivity().getResources(), brushid, opt);
        bitmap = bitmap.extractAlpha();

        return bitmap;
    }

    public Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        Matrix m = new Matrix();
        m.setScale(dstWidth / (float) src.getWidth(), dstHeight / (float) src.getHeight());
        Bitmap result = Bitmap.createBitmap(dstWidth, dstHeight, src.getConfig());
        Canvas canvas = new Canvas(result);

        Paint paint = new Paint();
        paint.setFilterBitmap(filter);
        canvas.drawBitmap(src, m, paint);

        return result;

    }

    public void displayDrawLook() {
        if (mFRep == null) {
            return;
        }
        float radius = mTmpStrokData.mRadius;
        mFRep.fillStrokeParameters(mTmpStrokData);

        if (radius != mTmpStrokData.mRadius) {
            mTimeout = DISPLAY_TIME + System.currentTimeMillis();
            scheduleWakeup(DISPLAY_TIME);
        }
    }

    public void drawLook(Canvas canvas) {
        if (mFRep == null) {
            return;
        }
        int cw = canvas.getWidth();
        int ch = canvas.getHeight();
        int centerx = cw / 2;
        int centery = ch / 2;

        // mFRep.fillStrokeParameters(mTmpStrokData);
        mIconPaint.setAntiAlias(true);
        mIconPaint.setStyle(Paint.Style.STROKE);
        float rad = mRotateToScreen.mapRadius(mTmpStrokData.mRadius);

        RectF rec = new RectF();
        rec.set(centerx - rad, centery - rad, centerx + rad, centery + rad);
        mIconPaint.setColor(Color.BLACK);
        mIconPaint.setStrokeWidth(5);
        canvas.drawArc(rec, 0, 360, true, mIconPaint);
        mIconPaint.setColor(Color.WHITE);
        mIconPaint.setStrokeWidth(3);
        canvas.drawArc(rec, 0, 360, true, mIconPaint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calcScreenMapping();
        if (System.currentTimeMillis() < mTimeout) {
            drawLook(canvas);
        }
    }

}
