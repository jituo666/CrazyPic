package com.xjt.crazypic.edit.filters;

import com.xjt.crazypic.R;

import android.graphics.Bitmap;

public class ImageFilterShadows extends SimpleImageFilter {
    private static final String SERIALIZATION_NAME = "SHADOWS";
    public ImageFilterShadows() {
        mName = "Shadows";

    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation =
                (FilterBasicRepresentation) super.getDefaultRepresentation();
        representation.setName("Shadows");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterShadows.class);
        representation.setTextId(R.string.shadow_recovery);
        representation.setMinimum(-100);
        representation.setMaximum(100);
        representation.setDefaultValue(0);
        representation.setSupportsPartialRendering(true);
        return representation;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, float  factor);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float p = getParameters().getValue();

        nativeApplyFilter(bitmap, w, h, p);
        return bitmap;
    }
}
