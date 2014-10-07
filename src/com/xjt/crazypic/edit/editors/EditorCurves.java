package com.xjt.crazypic.edit.editors;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.xjt.crazypic.edit.filters.FilterCurvesRepresentation;
import com.xjt.crazypic.edit.filters.FilterRepresentation;
import com.xjt.crazypic.edit.imageshow.ImageCurves;
import com.xjt.crazypic.R;

public class EditorCurves extends Editor {
    public static final int ID = R.id.imageCurves;
    ImageCurves mImageCurves;

    public EditorCurves() {
        super(ID);
    }

    @Override
    protected void updateText() {

    }

    @Override
    public boolean showsPopupIndicator() {
        return true;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        mView = mImageShow = mImageCurves = new ImageCurves(context);
        mImageCurves.setEditor(this);
    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        FilterRepresentation rep = getLocalRepresentation();
        if (rep != null && getLocalRepresentation() instanceof FilterCurvesRepresentation) {
            FilterCurvesRepresentation drawRep = (FilterCurvesRepresentation) rep;
            mImageCurves.setFilterDrawRepresentation(drawRep);
        }
    }
    @Override
    public void setUtilityPanelUI(View actionButton, View editControl) {
        super.setUtilityPanelUI(actionButton,editControl);
        setMenuIcon(true);
    }

    @Override
    public boolean showsSeekBar() {
        return false;
    }
}
