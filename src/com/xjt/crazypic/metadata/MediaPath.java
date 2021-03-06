
package com.xjt.crazypic.metadata;

import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.utils.Utils;

import java.lang.ref.WeakReference;

public class MediaPath {

    /**
     * Path pattern" prefix:$Root/source/type/setId/itemId 
     * identity: id
     */
    private static final String TAG = "Path";
    private final String mPrefix;
    private final int mIdentity;
    private String optionFilePath;
    private WeakReference<MediaObject> mObject;

    public MediaPath(String prefix, int identity) {
        mPrefix = prefix;
        mIdentity = identity;
        optionFilePath = "";
    }

    public void setFilePath(String f) {
        optionFilePath = f;
    }

    public void setObject(MediaObject object) {
        synchronized (MediaPath.class) {
            Utils.assertTrue(mObject == null || mObject.get() == null);
            mObject = new WeakReference<MediaObject>(object);
        }
    }

    public MediaObject getObject() {
        synchronized (MediaPath.class) {
            return (mObject == null) ? null : mObject.get();
        }
    }

    public int getMediaType() {
        LLog.i(TAG, " mPrefix:" + mPrefix);
        String name[] = mPrefix.split("/");
        if (name.length < 2) {
            throw new IllegalArgumentException(toString());
        }

        LLog.i(TAG, " name[1]:" + name[2]);
        return getTypeFromString(name[2]);
    }

    public static int getTypeFromString(String s) {
        if (MediaObject.MEDIA_TYPE_ALL_STRING.equals(s))
            return MediaObject.MEDIA_TYPE_ALL;
        if (MediaObject.MEDIA_TYPE_IMAGE_STRING.equals(s))
            return MediaObject.MEDIA_TYPE_IMAGE;
        if (MediaObject.MEDIA_TYPE_VIDEO_STRING.equals(s))
            return MediaObject.MEDIA_TYPE_VIDEO;
        throw new IllegalArgumentException(s);
    }

    @Override
    public String toString() {
        synchronized (MediaPath.class) {
            StringBuilder sb = new StringBuilder();
            sb.append(" Path Prefix:").append(mPrefix);
            sb.append(" Path Identity:").append(mIdentity);
            return sb.toString();
        }
    }

    public String getPrefix() {
        return mPrefix;
    }

    public int getIdentity() {
        return mIdentity;
    }
    
    public String getFilePath() {
        return optionFilePath;
    }
}
