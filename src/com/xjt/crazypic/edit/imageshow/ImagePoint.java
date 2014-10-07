package com.xjt.crazypic.edit.imageshow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import com.xjt.crazypic.edit.editors.EditorRedEye;
import com.xjt.crazypic.edit.filters.FilterPoint;
import com.xjt.crazypic.edit.filters.FilterRedEyeRepresentation;
import com.xjt.crazypic.edit.filters.ImageFilterRedEye;

public abstract class ImagePoint extends ImageShow {

    private static final String LOGTAG = "ImageRedEyes";
    protected EditorRedEye mEditorRedEye;
    protected FilterRedEyeRepresentation mRedEyeRep;
    protected static float mTouchPadding = 80;

    public static void setTouchPadding(float padding) {
        mTouchPadding = padding;
    }

    public ImagePoint(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImagePoint(Context context) {
        super(context);
    }

    @Override
    public void resetParameter() {
        ImageFilterRedEye filter = (ImageFilterRedEye) getCurrentFilter();
        if (filter != null) {
            filter.clear();
        }
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);

        Matrix originalToScreen = getImageToScreenMatrix(false);
        Matrix originalRotateToScreen = getImageToScreenMatrix(true);

        if (mRedEyeRep != null) {
            for (FilterPoint candidate : mRedEyeRep.getCandidates()) {
                drawPoint(candidate, canvas, originalToScreen, originalRotateToScreen, paint);
            }
        }
    }

    protected abstract void drawPoint(
            FilterPoint candidate, Canvas canvas, Matrix originalToScreen,
            Matrix originalRotateToScreen, Paint paint);

    public void setEditor(EditorRedEye editorRedEye) {
        mEditorRedEye = editorRedEye;
    }

    public void setRepresentation(FilterRedEyeRepresentation redEyeRep) {
        mRedEyeRep = redEyeRep;
    }
}
