package com.xjt.crazypic.edit.filters;

import android.graphics.Bitmap;

import com.xjt.crazypic.edit.editors.ImageOnlyEditor;
import com.xjt.crazypic.R;

public class ImageFilterNegative extends ImageFilter {
    private static final String SERIALIZATION_NAME = "NEGATIVE";
    public ImageFilterNegative() {
        mName = "Negative";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterDirectRepresentation("Negative",0);
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterNegative.class);
        representation.setTextId(R.string.negative);
        representation.setShowParameterValue(false);
        representation.setEditorId(ImageOnlyEditor.ID);
        representation.setSupportsPartialRendering(true);
        representation.setIsBooleanFilter(true);
        return representation;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h);

    @Override
    public void useRepresentation(FilterRepresentation representation) {

    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        nativeApplyFilter(bitmap, w, h);
        return bitmap;
    }
}
