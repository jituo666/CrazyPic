
package com.xjt.crazypic.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;
import com.xjt.crazypic.NpApp;
import com.xjt.crazypic.NpContext;
import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.common.OrientationManager;
import com.xjt.crazypic.common.ThreadPool;
import com.xjt.crazypic.fragment.GalleryGridFragment;
import com.xjt.crazypic.fragment.GalleryListFragment;
import com.xjt.crazypic.imagedata.utils.LetoolBitmapPool;
import com.xjt.crazypic.metadata.DataManager;
import com.xjt.crazypic.metadata.MediaItem;
import com.xjt.crazypic.preference.GlobalPreference;
import com.xjt.crazypic.view.GLController;
import com.xjt.crazypic.view.GLRootView;
import com.xjt.crazypic.view.NpBottomBar;
import com.xjt.crazypic.view.NpEmptyView;
import com.xjt.crazypic.view.NpTopBar;
import com.xjt.crazypic.R;

/**
 * @Author Jituo.Xuan
 * @Date 8:16:18 PM Jul 24, 2014
 * @Comments:null
 */
public class CpPictureActivity extends FragmentActivity implements NpContext {

    private static final String TAG = CpPictureActivity.class.getSimpleName();

    public static final long SPLASH_INTERVAL = 24 * 60 * 60 * 1000l; // 闪屏时间间隔
    public static final String KEY_ALBUM_TITLE = "album_title";
    public static final String KEY_MEDIA_PATH = "media_path";
    public static final String KEY_ALBUM_ID = "album_id";
    public static final String KEY_PICKING = "picking_pic";

    private NpTopBar mTopBar;
    private NpBottomBar mBottomBar;
    private ViewGroup mMainView;
    private GLRootView mGLESView;
    private ImageView mSplashScreen;
    private OrientationManager mOrientationManager;
    public boolean mAlbumIsDirty = false;
    public boolean mImagePicking = false; // 是否是选择模式

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.local_media_main_view);
        mImagePicking = this.getIntent().getBooleanExtra(KEY_PICKING, false);
        //
        mTopBar = new NpTopBar(this, (ViewGroup) findViewById(R.id.letool_top_bar_container));
        mBottomBar = new NpBottomBar(this, (ViewGroup) findViewById(R.id.letool_bottom_bar_container));
        mMainView = (ViewGroup) findViewById(R.id.local_image_browse_main_view);
        mGLESView = (GLRootView) mMainView.findViewById(R.id.gl_root_view);
        mSplashScreen = (ImageView) findViewById(R.id.splash_screen);
        mOrientationManager = new OrientationManager(this);
        startFirstFragment();
        //
        if ((System.currentTimeMillis() - GlobalPreference.getLastSpashTime(this)) > SPLASH_INTERVAL) {
            GlobalPreference.setLastSpashTime(this, System.currentTimeMillis());
            mSplashScreen.setVisibility(View.VISIBLE);
            mSplashScreen.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mSplashScreen.setVisibility(View.GONE);
                }
            }, 3000);
        }
    }

    private void startFirstFragment() {
        Fragment fragment = mImagePicking ? new GalleryListFragment() : new GalleryGridFragment();
        Bundle data = new Bundle();
        data.putString(CpPictureActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
        fragment.setArguments(data);
        pushContentFragment(fragment, null, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (mGLESView.getVisibility() == View.VISIBLE)
            mGLESView.onResume();
        getDataManager().resume();
        mOrientationManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        if (mGLESView.getVisibility() == View.VISIBLE)
            mGLESView.onPause();
        getDataManager().pause();
        mOrientationManager.pause();
        LetoolBitmapPool.getInstance().clear();
        MediaItem.getBytesBufferPool().clear();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showEmptyView(int iconResIcon, int messageResId) {
        NpEmptyView emptyView = (NpEmptyView) LayoutInflater.from(this).inflate(R.layout.local_media_empty_view, null);
        emptyView.updataView(iconResIcon, messageResId);
        //
        ViewGroup normalView = (ViewGroup) mMainView.findViewById(R.id.normal_root_view);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        normalView.removeAllViews();
        normalView.addView(emptyView, lp);
        normalView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmptyView() {
        ViewGroup normalView = (ViewGroup) mMainView.findViewById(R.id.normal_root_view);
        normalView.removeAllViews();
        normalView.setVisibility(View.GONE);
    }

    public void pushContentFragment(Fragment newFragment, Fragment oldFragment, boolean backup) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (oldFragment != null) {
            ft.remove(oldFragment);
            if (backup)
                ft.addToBackStack(null);
        }
        LLog.i(TAG, " add :" + newFragment.getClass().getSimpleName());
        ft.add(newFragment, newFragment.getClass().getSimpleName());
        ft.commit();
    }

    public void popContentFragment() {
        LLog.i(TAG, " popBackStack :" + getSupportFragmentManager().getBackStackEntryCount());
        getSupportFragmentManager().popBackStack();
    }

    //

    @Override
    public void onBackPressed() {
        if (getLetoolTopBar().getActionBarMode() == NpTopBar.ACTION_BAR_MODE_SELECTION) {
            getLetoolTopBar().exitSelection();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            popContentFragment();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LLog.i(TAG, "onKeyDown menu1:" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_MENU) {
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public NpTopBar getLetoolTopBar() {
        return mTopBar;
    }

    @Override
    public NpBottomBar getLetoolBottomBar() {
        return mBottomBar;
    }

    @Override
    public DataManager getDataManager() {
        return ((NpApp) getApplication()).getDataManager();
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((NpApp) getApplication()).getThreadPool();
    }

    @Override
    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    public GLController getGLController() {
        return mGLESView;
    }

    @Override
    public boolean isImagePicking() {
        return mImagePicking;
    }

    @Override
    public View getGuidTipView() {
        return findViewById(R.id.function_guide_tip);
    }

    @Override
    public boolean isAlbumDirty() {
        return mAlbumIsDirty;
    }

    @Override
    public void setAlbumDirty(boolean dirty) {
        mAlbumIsDirty = dirty;
    }

}
