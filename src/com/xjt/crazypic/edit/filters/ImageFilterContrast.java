package com.xjt.crazypic.edit.filters;

import com.xjt.crazypic.R;

import android.graphics.Bitmap;

public class ImageFilterContrast extends SimpleImageFilter {
    private static final String SERIALIZATION_NAME = "CONTRAST";

    public ImageFilterContrast() {
        mName = "Contrast";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation =
                (FilterBasicRepresentation) super.getDefaultRepresentation();
        representation.setName("Contrast");
        representation.setSerializationName(SERIALIZATION_NAME);

        representation.setFilterClass(ImageFilterContrast.class);
        representation.setTextId(R.string.contrast);
        representation.setMinimum(-100);
        representation.setMaximum(100);
        representation.setDefaultValue(0);
        representation.setSampleResource(R.drawable.effect_sample_32);
        representation.setSupportsPartialRendering(true);
        return representation;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, float strength);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float value = getParameters().getValue();
        nativeApplyFilter(bitmap, w, h, value);
        return bitmap;
    }
}
