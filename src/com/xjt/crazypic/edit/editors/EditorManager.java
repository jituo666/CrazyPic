package com.xjt.crazypic.edit.editors;

import com.xjt.crazypic.edit.EditorPlaceHolder;
import com.xjt.crazypic.edit.editors.BasicEditor;
import com.xjt.crazypic.edit.editors.EditorCurves;
import com.xjt.crazypic.edit.editors.EditorZoom;

public class EditorManager {

    public static void addEditors(EditorPlaceHolder editorPlaceHolder) {
        editorPlaceHolder.addEditor(new EditorGrad());
        editorPlaceHolder.addEditor(new EditorChanSat());
        editorPlaceHolder.addEditor(new EditorZoom());
        editorPlaceHolder.addEditor(new EditorCurves());
        editorPlaceHolder.addEditor(new EditorDraw());
        editorPlaceHolder.addEditor(new EditorVignette());
        editorPlaceHolder.addEditor(new EditorColorBorder());
        editorPlaceHolder.addEditor(new EditorTextureBorder());
        editorPlaceHolder.addEditor(new EditorMirror());
        editorPlaceHolder.addEditor(new EditorRotate());
        editorPlaceHolder.addEditor(new EditorStraighten());
        editorPlaceHolder.addEditor(new EditorCrop());
    }

}
