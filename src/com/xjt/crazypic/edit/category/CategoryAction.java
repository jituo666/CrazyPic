
package com.xjt.crazypic.edit.category;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ArrayAdapter;

import com.xjt.crazypic.edit.NpEditActivity;
import com.xjt.crazypic.edit.filters.FilterRepresentation;
import com.xjt.crazypic.edit.imageshow.ImageManager;
import com.xjt.crazypic.edit.pipeline.ImagePreset;
import com.xjt.crazypic.edit.pipeline.RenderingRequest;
import com.xjt.crazypic.edit.pipeline.RenderingRequestCaller;
import com.xjt.crazypic.R;

public class CategoryAction implements RenderingRequestCaller {

    private static final String TAG = CategoryAction.class.getSimpleName();

    private FilterRepresentation mRepresentation;
    private String mName;
    private Rect mImageFrame;
    private Bitmap mImage;
    private ArrayAdapter<?> mAdapter;
    public static final int FULL_VIEW = 0;
    public static final int CROP_VIEW = 1;
    public static final int ADD_ACTION = 2;
    public static final int SPACER = 3;
    private int mType = CROP_VIEW;
    private NpEditActivity mContext;
    private boolean mCanBeRemoved = false;
    private boolean mIsDoubleAction = false;
    private Bitmap mOverlayBitmap;
    private Bitmap mPortraitImage;
    private int mTextSize = 32;

    public CategoryAction(NpEditActivity context, FilterRepresentation representation, int type, boolean canBeRemoved) {
        this(context, representation, type);
        mCanBeRemoved = canBeRemoved;
        mTextSize = context.getResources().getDimensionPixelSize(R.dimen.category_panel_text_size);
    }

    public CategoryAction(NpEditActivity context, FilterRepresentation representation) {
        this(context, representation, CROP_VIEW);
    }

    public CategoryAction(NpEditActivity context, FilterRepresentation representation, int type) {
        this(context, type);
        setRepresentation(representation);
    }

    public CategoryAction(NpEditActivity context, int type) {
        mContext = context;
        setType(type);
        mContext.registerAction(this);
    }

    public boolean isDoubleAction() {
        return mIsDoubleAction;
    }

    public void setIsDoubleAction(boolean value) {
        mIsDoubleAction = value;
    }

    public boolean canBeRemoved() {
        return mCanBeRemoved;
    }

    public int getType() {
        return mType;
    }

    public FilterRepresentation getRepresentation() {
        return mRepresentation;
    }

    public void setRepresentation(FilterRepresentation representation) {
        mRepresentation = representation;
        mName = representation.getName();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setAdapter(ArrayAdapter<?> adapter) {
        mAdapter = adapter;
    }

    private void postNewIconRenderRequest(int w, int h) {
        if (mRepresentation != null) {
            ImagePreset preset = new ImagePreset();
            preset.addFilter(mRepresentation);
            RenderingRequest.postIconRequest(mContext, w, h, preset, this);
        }
    }

    public void setImageFrame(Rect imageFrame, int orientation) {
        if (mImageFrame != null && mImageFrame.equals(imageFrame)) {
            return;
        }
        if (getType() == ADD_ACTION) {
            return;
        }
        Bitmap temp = ImageManager.getImage().getTemporaryThumbnailBitmap();
        if (temp != null) {
            mImage = temp;
        }
        Bitmap bitmap = ImageManager.getImage().getThumbnailBitmap();
        if (bitmap != null) {
            mImageFrame = imageFrame;
            int w = mImageFrame.width();
            int h = mImageFrame.height();
            postNewIconRenderRequest(w, h);
        }
    }

    private void drawCenteredImage(Bitmap source, Bitmap destination, boolean scale) {
        int minSide = Math.min(destination.getWidth(), destination.getHeight());
        Matrix m = new Matrix();
        float scaleFactor = minSide / (float) Math.min(source.getWidth(), source.getHeight());

        float dx = (destination.getWidth() - source.getWidth() * scaleFactor) / 2.0f;
        float dy = (destination.getHeight() - source.getHeight() * scaleFactor) / 2.0f;
        if (mImageFrame.height() > mImageFrame.width()) {
            dy -= mTextSize;
        }
        m.setScale(scaleFactor, scaleFactor);
        m.postTranslate(dx, dy);
        Canvas canvas = new Canvas(destination);
        canvas.drawBitmap(source, m, new Paint(Paint.FILTER_BITMAP_FLAG));
    }

    public Bitmap getImage() {
        return mImage;
    }

    public void setImage(Bitmap image) {
        mImage = image;
    }

    public void setType(int type) {
        mType = type;
    }

    public void setPortraitImage(Bitmap portraitImage) {
        mPortraitImage = portraitImage;
    }

    public Bitmap getPortraitImage() {
        return mPortraitImage;
    }

    public Bitmap getOverlayBitmap() {
        return mOverlayBitmap;
    }

    public void setOverlayBitmap(Bitmap overlayBitmap) {
        mOverlayBitmap = overlayBitmap;
    }

    public void clearBitmap() {
        if (mImage != null && mImage != ImageManager.getImage().getTemporaryThumbnailBitmap()) {
            ImageManager.getImage().getBitmapCache().cache(mImage);
        }
        mImage = null;
    }

    @Override
    public void available(RenderingRequest request) {
        clearBitmap();
        mImage = request.getBitmap();
        if (mImage == null) {
            mImageFrame = null;
            return;
        }
        if (mRepresentation.getOverlayId() != 0 && mOverlayBitmap == null) {
            mOverlayBitmap = BitmapFactory.decodeResource(mContext.getResources(), mRepresentation.getOverlayId());
        }
        if (mOverlayBitmap != null) {
            if (getRepresentation().getFilterType() == FilterRepresentation.TYPE_BORDER) {
                Canvas canvas = new Canvas(mImage);
                canvas.drawBitmap(mOverlayBitmap, new Rect(0, 0, mOverlayBitmap.getWidth(), mOverlayBitmap.getHeight()),
                        new Rect(0, 0, mImage.getWidth(), mImage.getHeight()), new Paint());
            } else {
                Canvas canvas = new Canvas(mImage);
                canvas.drawARGB(128, 0, 0, 0);
                drawCenteredImage(mOverlayBitmap, mImage, false);
            }
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

}
