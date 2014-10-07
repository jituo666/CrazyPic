
package com.xjt.crazypic;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.xjt.crazypic.common.OrientationManager;
import com.xjt.crazypic.common.ThreadPool;
import com.xjt.crazypic.metadata.DataManager;
import com.xjt.crazypic.view.GLController;
import com.xjt.crazypic.view.NpBottomBar;
import com.xjt.crazypic.view.NpTopBar;

/**
 * @Author Jituo.Xuan
 * @Date 9:07:17 PM May 17, 2014
 * @Comments:null
 */
public interface NpContext {

    public Context getActivityContext();

    //
    public DataManager getDataManager();

    public boolean isImagePicking();

    public ThreadPool getThreadPool();

    public GLController getGLController();

    //
    public NpTopBar getLetoolTopBar();

    public NpBottomBar getLetoolBottomBar();

    public OrientationManager getOrientationManager();

    //
    public View getGuidTipView();

    public void showEmptyView(int iconResIcon, int messageResId);

    public void hideEmptyView();

    public void pushContentFragment(Fragment newFragment, Fragment oldFragment, boolean backup);

    public void popContentFragment();

    //
    public boolean isAlbumDirty();
    public void setAlbumDirty(boolean dirty);

}
