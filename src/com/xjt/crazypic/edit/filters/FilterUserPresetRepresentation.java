
package com.xjt.crazypic.edit.filters;

import com.xjt.crazypic.edit.editors.ImageOnlyEditor;
import com.xjt.crazypic.edit.pipeline.ImagePreset;

public class FilterUserPresetRepresentation extends FilterRepresentation {

    private ImagePreset mPreset;
    private int mId;

    public FilterUserPresetRepresentation(String name, ImagePreset preset, int id) {
        super(name);
        setEditorId(ImageOnlyEditor.ID);
        setFilterType(FilterRepresentation.TYPE_FX);
        setSupportsPartialRendering(true);
        mPreset = preset;
        mId = id;
    }

    public ImagePreset getImagePreset() {
        return mPreset;
    }

    public int getId() {
        return mId;
    }

    public FilterRepresentation copy() {
        FilterRepresentation representation = new FilterUserPresetRepresentation(getName(), new ImagePreset(mPreset), mId);
        return representation;
    }

    @Override
    public boolean allowsSingleInstanceOnly() {
        return true;
    }
}
