
package com.xjt.crazypic.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.xjt.crazypic.R;
import com.xjt.crazypic.imagedata.blobcache.BlobCacheManager;
import com.xjt.crazypic.preference.GlobalPreference;
import com.xjt.crazypic.settings.NpPreference;
import com.xjt.crazypic.stat.StatConstants;
import com.xjt.crazypic.utils.StorageUtils;
import com.xjt.crazypic.utils.StringUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class CpSettingsActivity extends Activity implements OnClickListener {

    private NpPreference mAnimSwitch;
    private NpPreference mRememberUISwitch;
    private NpPreference mClearCache;
    private NpPreference mVersionCheck;
    private NpPreference mAppAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_settings_list);

        TextView titleView = (TextView) findViewById(R.id.main_title_text);
        titleView.setOnClickListener(this);
        View navi = findViewById(R.id.navi_button);
        navi.setOnClickListener(this);
        mAnimSwitch = (NpPreference) findViewById(R.id.anim_switch);
        mRememberUISwitch = (NpPreference) findViewById(R.id.remember_ui_switch);
        mClearCache = (NpPreference) findViewById(R.id.clear_cache);
        mVersionCheck = (NpPreference) findViewById(R.id.version_update_check);
        mAppAbout = (NpPreference) findViewById(R.id.app_about);
        String x = getString(R.string.clear_cache_desc, getCacheSize(), StringUtils.formatBytes(StorageUtils.getExternalStorageAvailableSize()));

        mAnimSwitch.setSettingItemText(getString(R.string.anim_switch_title), getString(R.string.anim_switch_desc), true);
        mAnimSwitch.setChecked(GlobalPreference.isAnimationOpen(this));
        mRememberUISwitch.setSettingItemText(getString(R.string.remember_ui_switch_title), getString(R.string.remember_ui_switch_desc), true);
        mRememberUISwitch.setChecked(GlobalPreference.rememberLastUI(this));
        mClearCache.setSettingItemText(getString(R.string.clear_cache_title), x, false);
        mVersionCheck.setSettingItemText(getString(R.string.version_update_check_title), getVersion(), false);
        mAppAbout.setSettingItemText(R.string.app_about, R.string.app_about_desc);

        mAnimSwitch.setOnClickListener(this);
        mRememberUISwitch.setOnClickListener(this);
        mClearCache.setOnClickListener(this);
        mVersionCheck.setOnClickListener(this);
        mAppAbout.setOnClickListener(this);
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
    public void onClick(View v) {
        if (v.getId() == R.id.anim_switch) {
            mAnimSwitch.setChecked(!mAnimSwitch.isChecked());
            GlobalPreference.setAnimationOpen(this, mAnimSwitch.isChecked());
        } else if (v.getId() == R.id.remember_ui_switch) {
            mRememberUISwitch.setChecked(!mRememberUISwitch.isChecked());
            GlobalPreference.setRememberLastUI(this, mRememberUISwitch.isChecked());
            GlobalPreference.setLastUI(this, "");
        } else if (v.getId() == R.id.clear_cache) {
            if (StorageUtils.externalStorageAvailable()) {
                MobclickAgent.onEvent(this, StatConstants.EVENT_KEY_CLEAR_CAHCE);
                new ClearCacheTask().execute();
            }
        } else if (v.getId() == R.id.version_update_check) {
            MobclickAgent.onEvent(this, StatConstants.EVENT_KEY_UPDATE_CHECK);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            final Context context = this;
            UmengUpdateAgent.setDefault();
            UmengUpdateAgent.setUpdateAutoPopup(false);
            UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

                @Override
                public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    switch (updateStatus) {
                        case UpdateStatus.Yes: // has update
                            UmengUpdateAgent.showUpdateDialog(context, updateInfo);
                            break;
                        case UpdateStatus.No: // has no update
                            Toast.makeText(context, R.string.app_no_update, Toast.LENGTH_SHORT).show();
                            break;
                        case UpdateStatus.NoneWifi: // none wifi
                            Toast.makeText(context, R.string.app_update_only_wifi, Toast.LENGTH_SHORT).show();
                            break;
                        case UpdateStatus.Timeout: // time out
                            Toast.makeText(context, R.string.common_connect_timeout, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
            UmengUpdateAgent.setUpdateOnlyWifi(false);
            UmengUpdateAgent.forceUpdate(context);
            progressDialog.setMessage(getString(R.string.common_update_checking));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        } else if (v.getId() == R.id.app_about) {
            Intent itAbout = new Intent(this, AboutActivity.class);
            startActivity(itAbout);
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else if (v.getId() == R.id.navi_button) {
            finish();
        }
    }

    private String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            Date now = new Date(info.lastUpdateTime);
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINESE);
            return getString(R.string.version_update_check_desc, info.versionName, f.format(now));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private class ClearCacheTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(CpSettingsActivity.this);
            dialog.setTitle(getString(R.string.common_clear_cache));
            dialog.setMessage(getString(R.string.common_clear_cache_waitting));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            BlobCacheManager.clearCachedFiles(CpSettingsActivity.this);
            ImageLoader.getInstance().clearDiscCache();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
                String x = getString(R.string.clear_cache_desc, getCacheSize(), StringUtils.formatBytes(StorageUtils.getExternalStorageAvailableSize()));
                mClearCache.setSettingItemText(getString(R.string.clear_cache_title), x, false);
            }
            Toast t = Toast.makeText(CpSettingsActivity.this, R.string.clear_cache_finished, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
    }

    private String getCacheSize() {
        try {
            return StringUtils.formatBytes(getFolderSize(getApplication().getExternalCacheDir()));
        } catch (Exception e) {
            return "0B";
        }
    }

    private static long getFolderSize(java.io.File file) throws Exception {
        long size = 0;
        java.io.File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++)
        {
            if (fileList[i].isDirectory())
            {
                size = size + getFolderSize(fileList[i]);
            } else
            {
                size = size + fileList[i].length();
            }
        }
        return size;
    }
}
