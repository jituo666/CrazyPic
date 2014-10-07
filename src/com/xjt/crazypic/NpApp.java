
package com.xjt.crazypic;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.xjt.crazypic.common.ThreadPool;
import com.xjt.crazypic.imagedata.blobcache.BlobCacheService;
import com.xjt.crazypic.metadata.DataManager;

/**
 * @Author Jituo.Xuan
 * @Date 9:07:03 PM May 17, 2014
 * @Comments:null
 */
public interface NpApp {

    public DataManager getDataManager();

    public BlobCacheService getBlobCacheService();

    public ThreadPool getThreadPool();

    public Context getAppContext();

    public Looper getMainLooper();

    public ContentResolver getContentResolver();

    public Resources getResources();
}
