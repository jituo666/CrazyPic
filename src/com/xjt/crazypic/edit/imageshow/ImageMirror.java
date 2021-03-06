package com.xjt.crazypic.edit.imageshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.xjt.crazypic.edit.editors.EditorMirror;
import com.xjt.crazypic.edit.filters.FilterMirrorRepresentation;
import com.xjt.crazypic.edit.imageshow.GeometryMathUtils.GeometryHolder;

public class ImageMirror extends ImageShow {
    private static final String TAG = ImageMirror.class.getSimpleName();
    private EditorMirror mEditorMirror;
    private FilterMirrorRepresentation mLocalRep = new FilterMirrorRepresentation();
    private GeometryHolder mDrawHolder = new GeometryHolder();

    public ImageMirror(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageMirror(Context context) {
        super(context);
    }

    public void setFilterMirrorRepresentation(FilterMirrorRepresentation rep) {
        mLocalRep = (rep == null) ? new FilterMirrorRepresentation() : rep;
    }

    public void flip() {
        mLocalRep.cycle();
        invalidate();
    }

    public FilterMirrorRepresentation getFinalRepresentation() {
        return mLocalRep;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Treat event as handled.
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        ImageManager master = ImageManager.getImage();
        Bitmap image = master.getFiltersOnlyImage();
        if (image == null) {
            return;
        }
        GeometryMathUtils.initializeHolder(mDrawHolder, mLocalRep);
        GeometryMathUtils.drawTransformedCropped(mDrawHolder, canvas, image, getWidth(),
                getHeight());
    }

    public void setEditor(EditorMirror editorFlip) {
        mEditorMirror = editorFlip;
    }

}
