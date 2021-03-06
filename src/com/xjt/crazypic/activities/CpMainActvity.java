
package com.xjt.crazypic.activities;

import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.xjt.crazypic.R;
import com.xjt.crazypic.fb.ConversationActivity;
import com.xjt.crazypic.stat.StatConstants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CpMainActvity extends Activity implements OnClickListener {

    private ImageView mSettings;
    private ImageView mFeedBack;

    private ImageView mBeautify;
    private ImageView mBrowse;
    private ImageView mInterest;
    private ImageView mGuide;
    private FeedbackAgent mFB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cp_main_activity);
        mSettings = (ImageView) findViewById(R.id.assist_functions_settings);
        mFeedBack = (ImageView) findViewById(R.id.assist_functions_feedback);
        mBeautify = (ImageView) findViewById(R.id.main_functions_beautify);
        mBrowse = (ImageView) findViewById(R.id.main_functions_browse);
        mInterest = (ImageView) findViewById(R.id.main_functions_interest);
        mGuide = (ImageView) findViewById(R.id.main_functions_guide);

        mSettings.setOnClickListener(this);
        mFeedBack.setOnClickListener(this);
        mBeautify.setOnClickListener(this);
        mBrowse.setOnClickListener(this);
        mGuide.setOnClickListener(this);
        mInterest.setOnClickListener(this);
        //
        ImageViewTouchListener l = new ImageViewTouchListener();
        mBeautify.setOnTouchListener(l);
        mBrowse.setOnTouchListener(l);
        mInterest.setOnTouchListener(l);
        mGuide.setOnTouchListener(l);
        mFB = new FeedbackAgent(this);
        mFB.sync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == mSettings) {
            Intent it = new Intent();
            it.setClass(this, CpSettingsActivity.class);
            startActivity(it);
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else if (v == mFeedBack) {
            MobclickAgent.onEvent(this, StatConstants.EVENT_KEY_EDIT_FEEDBACK);
            Intent it = new Intent();
            it.setClass(this, ConversationActivity.class);
            startActivity(it);
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else if (v == mBeautify) {
            Intent it = new Intent();
            it.setClass(this, CpPictureActivity.class);
            it.putExtra(CpPictureActivity.KEY_PICKING, true);
            startActivity(it);
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else if (v == mBrowse) {
            Intent it = new Intent();
            it.setClass(this, CpPictureActivity.class);
            it.putExtra(CpPictureActivity.KEY_PICKING, false);
            startActivity(it);
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else if (v == mInterest) {

        } else if (v == mGuide) {

        }
    }

    private class ImageViewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ((ImageView) v).setImageResource(R.drawable.ic_main_fun_pressed);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                ((ImageView) v).setImageDrawable(null);
            }
            return false;
        }

    }
}
