
package com.xjt.crazypic.edit.filters;

import android.util.Log;

import com.xjt.crazypic.edit.editors.ImageOnlyEditor;
import com.xjt.crazypic.surpport.JsonReader;
import com.xjt.crazypic.surpport.JsonWriter;
import com.xjt.crazypic.R;

import java.io.IOException;

public class FilterMirrorRepresentation extends FilterRepresentation {

    public static final String SERIALIZATION_NAME = "MIRROR";
    private static final String SERIALIZATION_MIRROR_VALUE = "value";
    private static final String TAG = FilterMirrorRepresentation.class.getSimpleName();

    Mirror mMirror;

    public enum Mirror {
        NONE('N'), VERTICAL('V'), HORIZONTAL('H'), BOTH('B');

        char mValue;

        private Mirror(char value) {
            mValue = value;
        }

        public char value() {
            return mValue;
        }

        public static Mirror fromValue(char value) {
            switch (value) {
                case 'N':
                    return NONE;
                case 'V':
                    return VERTICAL;
                case 'H':
                    return HORIZONTAL;
                case 'B':
                    return BOTH;
                default:
                    return null;
            }
        }
    }

    public FilterMirrorRepresentation(Mirror mirror) {
        super(SERIALIZATION_NAME);
        setSerializationName(SERIALIZATION_NAME);
        setShowParameterValue(false);
        setFilterClass(FilterMirrorRepresentation.class);
        setFilterType(FilterRepresentation.TYPE_GEOMETRY);
        setSupportsPartialRendering(true);
        setTextId(R.string.mirror);
        setEditorId(ImageOnlyEditor.ID);
        setMirror(mirror);
    }

    public FilterMirrorRepresentation(FilterMirrorRepresentation m) {
        this(m.getMirror());
        setName(m.getName());
    }

    public FilterMirrorRepresentation() {
        this(getNil());
    }

    @Override
    public boolean equals(FilterRepresentation rep) {
        if (!(rep instanceof FilterMirrorRepresentation)) {
            return false;
        }
        FilterMirrorRepresentation mirror = (FilterMirrorRepresentation) rep;
        if (mMirror != mirror.mMirror) {
            return false;
        }
        return true;
    }

    public Mirror getMirror() {
        return mMirror;
    }

    public void set(FilterMirrorRepresentation r) {
        mMirror = r.mMirror;
    }

    public void setMirror(Mirror mirror) {
        if (mirror == null) {
            throw new IllegalArgumentException("Argument to setMirror is null");
        }
        mMirror = mirror;
    }

    public boolean isHorizontal() {
        if (mMirror == Mirror.BOTH
                || mMirror == Mirror.HORIZONTAL) {
            return true;
        }
        return false;
    }

    public boolean isVertical() {
        if (mMirror == Mirror.BOTH
                || mMirror == Mirror.VERTICAL) {
            return true;
        }
        return false;
    }

    public void cycle() {
        switch (mMirror) {
            case NONE:
                mMirror = Mirror.HORIZONTAL;
                break;
            case HORIZONTAL:
                mMirror = Mirror.BOTH;
                break;
            case BOTH:
                mMirror = Mirror.VERTICAL;
                break;
            case VERTICAL:
                mMirror = Mirror.NONE;
                break;
        }
    }

    @Override
    public boolean allowsSingleInstanceOnly() {
        return true;
    }

    @Override
    public FilterRepresentation copy() {
        return new FilterMirrorRepresentation(this);
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        if (!(representation instanceof FilterMirrorRepresentation)) {
            throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
        }
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    @Override
    public void useParametersFrom(FilterRepresentation a) {
        if (!(a instanceof FilterMirrorRepresentation)) {
            throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
        }
        setMirror(((FilterMirrorRepresentation) a).getMirror());
    }

    @Override
    public boolean isNil() {
        return mMirror == getNil();
    }

    public static Mirror getNil() {
        return Mirror.NONE;
    }

    @Override
    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(SERIALIZATION_MIRROR_VALUE).value(mMirror.value());
        writer.endObject();
    }

    @Override
    public void deSerializeRepresentation(JsonReader reader) throws IOException {
        boolean unset = true;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (SERIALIZATION_MIRROR_VALUE.equals(name)) {
                Mirror r = Mirror.fromValue((char) reader.nextInt());
                if (r != null) {
                    setMirror(r);
                    unset = false;
                }
            } else {
                reader.skipValue();
            }
        }
        if (unset) {
            Log.w(TAG, "WARNING: bad value when deserializing " + SERIALIZATION_NAME);
        }
        reader.endObject();
    }
}
