package com.xjt.crazypic.edit.editors;

import android.content.Context;
import android.widget.FrameLayout;

import com.xjt.crazypic.edit.imageshow.ImageShow;
import com.xjt.crazypic.R;

/**
 * The editor with no slider for filters without UI
 */
public class ImageOnlyEditor extends Editor {
    public final static int ID = R.id.imageOnlyEditor;
    private final String TAG = "ImageOnlyEditor";

    public ImageOnlyEditor() {
        super(ID);
    }

    protected ImageOnlyEditor(int id) {
        super(id);
    }

    public boolean useUtilityPanel() {
        return false;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        mView = mImageShow = new ImageShow(context);
    }

}
