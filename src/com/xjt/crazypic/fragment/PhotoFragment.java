
package com.xjt.crazypic.fragment;

import com.android.gallery3d.common.Utils;
import com.umeng.analytics.MobclickAgent;
import com.xjt.crazypic.NpContext;
import com.xjt.crazypic.activities.CpPictureActivity;
import com.xjt.crazypic.common.EyePosition;
import com.xjt.crazypic.common.GlobalConstants;
import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.common.SynchronizedHandler;
import com.xjt.crazypic.edit.NpEditActivity;
import com.xjt.crazypic.metadata.DataManager;
import com.xjt.crazypic.metadata.MediaItem;
import com.xjt.crazypic.metadata.MediaObject;
import com.xjt.crazypic.metadata.MediaPath;
import com.xjt.crazypic.metadata.MediaSet;
import com.xjt.crazypic.metadata.loader.DataLoadingListener;
import com.xjt.crazypic.metadata.loader.ThumbnailDataLoader;
import com.xjt.crazypic.preference.GlobalPreference;
import com.xjt.crazypic.selectors.SelectionListener;
import com.xjt.crazypic.selectors.SelectionManager;
import com.xjt.crazypic.share.ShareManager;
import com.xjt.crazypic.share.ShareManager.ShareListener;
import com.xjt.crazypic.stat.StatConstants;
import com.xjt.crazypic.utils.LetoolUtils;
import com.xjt.crazypic.utils.RelativePosition;
import com.xjt.crazypic.utils.StorageUtils;
import com.xjt.crazypic.view.BatchDeleteMediaListener;
import com.xjt.crazypic.view.DetailsHelper;
import com.xjt.crazypic.view.GLController;
import com.xjt.crazypic.view.GLView;
import com.xjt.crazypic.view.NpBottomBar;
import com.xjt.crazypic.view.NpDialog;
import com.xjt.crazypic.view.NpTopBar;
import com.xjt.crazypic.view.ThumbnailView;
import com.xjt.crazypic.view.BatchDeleteMediaListener.DeleteMediaProgressListener;
import com.xjt.crazypic.view.NpTopBar.OnActionModeListener;
import com.xjt.crazypic.views.layout.ThumbnailLayout;
import com.xjt.crazypic.views.layout.ThumbnailLayoutBase;
import com.xjt.crazypic.views.opengl.FadeTexture;
import com.xjt.crazypic.views.opengl.GLESCanvas;
import com.xjt.crazypic.views.render.ThumbnailRenderer;
import com.xjt.crazypic.views.utils.ViewConfigs;
import com.xjt.crazypic.R;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:35 AM Apr 19, 2014
 * @Comments:null
 */
public class PhotoFragment extends Fragment implements EyePosition.EyePositionListener, SelectionListener, OnActionModeListener {

    private static final String TAG = PhotoFragment.class.getSimpleName();

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int MSG_LAYOUT_CONFIRMED = 0;
    private static final int MSG_PICK_PHOTO = 1;
    //
    private static final int CURRENT_MODE_BROWSE = 0;
    private static final int CURRENT_MODE_DELETE = 1;
    private static final int CURRENT_MODE_SHARE = 2;

    private NpContext mLetoolContext;

    // photo data
    private MediaPath mDataSetPath;
    private MediaSet mDataSet;
    private ThumbnailDataLoader mAlbumDataSetLoader;
    private int mLoadingBits = 0;

    // views
    private GLController mGLController;
    private ViewConfigs.AlbumPage mConfig;
    private ThumbnailView mThumbnailView;
    private ThumbnailRenderer mRender;
    private RelativePosition mOpenCenter = new RelativePosition();
    private boolean mIsActive = false;

    private String mAlbumTitle;
    private boolean mIsSDCardMountedCorreclty = false;
    private int mCurrentOperationMode = CURRENT_MODE_BROWSE;

    private SynchronizedHandler mHandler;
    protected SelectionManager mSelector;
    private EyePosition mEyePosition; // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mUserDistance; // in pixel
    private float mX;
    private float mY;
    private float mZ;

    private final GLView mRootPane = new GLView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            NpTopBar actionBar = mLetoolContext.getLetoolTopBar();
            int thumbnailViewLeft = left + mConfig.paddingLeft;
            int thumbnailViewRight = right - left - mConfig.paddingRight;
            int thumbnailViewTop = top + mConfig.paddingTop + actionBar.getHeight();
            int thumbnailViewBottom = bottom - top - mConfig.paddingBottom;
            mRender.setHighlightItemPath(null);
            // Set the mThumbnailView as a reference point to the open animation
            mOpenCenter.setReferencePosition(0, thumbnailViewTop);
            mOpenCenter.setAbsolutePosition((right - left) / 2, (bottom - top) / 2);
            mThumbnailView.layout(thumbnailViewLeft, thumbnailViewTop, thumbnailViewRight, thumbnailViewBottom);
            LetoolUtils.setViewPointMatrix(mMatrix, (right - left) / 2, (bottom - top) / 2, -mUserDistance);
        }

        @Override
        protected void render(GLESCanvas canvas) {
            canvas.save(GLESCanvas.SAVE_FLAG_MATRIX);
            LetoolUtils.setViewPointMatrix(mMatrix, getWidth() / 2 + mX, getHeight() / 2 + mY, mZ);
            canvas.multiplyMatrix(mMatrix, 0);
            super.render(canvas);
            canvas.restore();
        }
    };

    private class MetaDataLoadingListener implements DataLoadingListener {

        @Override
        public void onLoadingStarted() {
            setLoadingBit(BIT_LOADING_RELOAD);
        }

        @Override
        public void onLoadingFinished(boolean loadFailed) {
            clearLoadingBit(BIT_LOADING_RELOAD);
        }
    }

    private void setLoadingBit(int loadTaskBit) {
        mLoadingBits |= loadTaskBit;
    }

    private void clearLoadingBit(int loadTaskBit) {
        mLoadingBits &= ~loadTaskBit;

        LLog.i(TAG, " clearLoadingBit mLoadingBits:" + mLoadingBits + " mIsActive:" + mIsActive);
        if (mLoadingBits == 0 && mIsActive) {
            if (mAlbumDataSetLoader.size() == 0) {
                LLog.i(TAG, " clearLoadingBit mAlbumDataSetLoader.size():" + mAlbumDataSetLoader.size());
                mLetoolContext.showEmptyView(R.drawable.ic_no_picture, R.string.common_error_no_photos);
            } else {
                mLetoolContext.hideEmptyView();
                showGuideTip();
            }
        }
    }

    private void onDown(int index) {
        mRender.setPressedIndex(index);
    }

    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            mRender.setPressedIndex(-1); // Avoid showing press-up animations for long-press.
        } else {
            mRender.setPressedUp();
        }
    }

    private void onSingleTapUp(int thumbnailIndex) {
        if (!mIsActive)
            return;
        if (mSelector.inSelectionMode()) {
            MediaItem item = mAlbumDataSetLoader.get(thumbnailIndex);
            if (item == null)
                return; // Item not ready yet, ignore the click
            MediaPath p = item.getPath();
            p.setFilePath(item.getFilePath());
            mSelector.toggle(p);
            mThumbnailView.invalidate();
        } else { // Render transition in pressed state
            mRender.setPressedIndex(thumbnailIndex);
            mRender.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, thumbnailIndex, 0), FadeTexture.DURATION);
        }
    }

    public void onLongTap(int thumbnailIndex) {
        hideGuideTip();
//        MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_PHOTO_LONG_PRESSED);
//        if (mLetoolContext.isImagePicking())
//            return;
//        MediaItem item = mAlbumDataSetLoader.get(thumbnailIndex);
//        if (item == null)
//            return;
//        MediaPath p = item.getPath();
//        p.setFilePath(item.getFilePath());
//        mSelector.toggle(p);
//        mThumbnailView.invalidate();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LLog.i(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LLog.i(TAG, "onCreate");
        mLetoolContext = (NpContext) getActivity();
        mGLController = mLetoolContext.getGLController();

        initializeData();
        initializeViews();
        mHandler = new SynchronizedHandler(mGLController) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LAYOUT_CONFIRMED: {
                        // mLoadingInsie.setVisibility(View.GONE);
                        break;
                    }
                    case MSG_PICK_PHOTO: {
                        pickPhoto(message.arg1);
                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        mEyePosition = new EyePosition(mLetoolContext.getActivityContext(), this);
        mThumbnailView.startScatteringAnimation(mOpenCenter, true, true, true);
    }

    private void initializeData() {
        Bundle data = getArguments();
        mAlbumTitle = data.getString(CpPictureActivity.KEY_ALBUM_TITLE);
        mDataSetPath = new MediaPath(data.getString(CpPictureActivity.KEY_MEDIA_PATH), data.getInt(CpPictureActivity.KEY_ALBUM_ID));
        mDataSet = mLetoolContext.getDataManager().getMediaSet(mDataSetPath);
        if (mDataSet == null) {
            Utils.fail("MediaSet is null. Path = %s", mDataSetPath);
        }
        mAlbumDataSetLoader = new ThumbnailDataLoader(mLetoolContext, mDataSet);
        mAlbumDataSetLoader.setLoadingListener(new MetaDataLoadingListener());
    }

    private void initializeViews() {
        mSelector = new SelectionManager(mLetoolContext, false);
        mSelector.setSelectionListener(this);
        mConfig = ViewConfigs.AlbumPage.get(mLetoolContext.getActivityContext());
        ThumbnailLayoutBase layout;
        if (mLetoolContext.isImagePicking() || GlobalPreference.isGalleryListMode(getActivity())) {
            mConfig.albumSpec.rowsPort = 4;
        } else {
            mConfig.albumSpec.rowsPort = 3;
        }
        layout = new ThumbnailLayout(mConfig.albumSpec);
        mThumbnailView = new ThumbnailView(mLetoolContext, layout);
        mThumbnailView.setOverscrollEffect(ThumbnailView.OVERSCROLL_3D);
        mThumbnailView.setBackgroundColor(LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.cp_main_background_color)));
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {

            @Override
            public void onDown(int index) {
                PhotoFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                PhotoFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int thumbnailIndex) {
                PhotoFragment.this.onSingleTapUp(thumbnailIndex);
            }

            @Override
            public void onLongTap(int thumbnailIndex) {
                PhotoFragment.this.onLongTap(thumbnailIndex);
            }
        });
        mRender = new ThumbnailRenderer(mLetoolContext, mThumbnailView, mSelector);
        layout.setRenderer(mRender);
        mThumbnailView.setThumbnailRenderer(mRender);
        mRender.setModel(mAlbumDataSetLoader);
        mRootPane.addComponent(mThumbnailView);
    }

    private void initBars() {
        NpTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(NpTopBar.ACTION_BAR_MODE_BROWSE, this);
        topBar.setVisible(View.VISIBLE, false);
        ViewGroup nativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.action_buttons);

        if (mLetoolContext.isImagePicking()) {
            topBar.setTitleText(R.string.pick_picture_item);
            nativeButtons.setVisibility(View.INVISIBLE);
        } else {
            topBar.setTitleText(mAlbumTitle);
            nativeButtons.setVisibility(View.VISIBLE);
            ImageView share = (ImageView) nativeButtons.findViewById(R.id.action_share);
            share.setVisibility(View.VISIBLE);
            ImageView delete = (ImageView) nativeButtons.findViewById(R.id.action_delete);
            delete.setVisibility(View.VISIBLE);
        }
        NpBottomBar bottomBar = mLetoolContext.getLetoolBottomBar();
        bottomBar.setVisible(View.GONE, false);
        mCurrentOperationMode = CURRENT_MODE_BROWSE;
    }

    private void initSelectionBar() {
        NpTopBar actionBar = mLetoolContext.getLetoolTopBar();
        actionBar.setOnActionMode(NpTopBar.ACTION_BAR_MODE_SELECTION, this);
        actionBar.setContractSelectionManager(mSelector);
        String format = getResources().getQuantityString(R.plurals.number_of_items, 0);
        if (mCurrentOperationMode == CURRENT_MODE_SHARE) {
            mLetoolContext.getLetoolTopBar().setTitleText(getResources().getString(R.string.common_share) + String.format(format, 0));
        } else if (mCurrentOperationMode == CURRENT_MODE_DELETE) {
            mLetoolContext.getLetoolTopBar().setTitleText(getResources().getString(R.string.common_delete) + String.format(format, 0));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView" + System.currentTimeMillis());
        initBars();
        mIsSDCardMountedCorreclty = StorageUtils.externalStorageAvailable();
        if (!mIsSDCardMountedCorreclty) {
            mLetoolContext.showEmptyView(R.drawable.ic_launcher, R.string.common_error_nosdcard);
        } else {
            mLetoolContext.hideEmptyView();
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        LLog.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        if (!mIsSDCardMountedCorreclty) {
            return;
        }

        mIsActive = true;
        mGLController.setContentPane(mRootPane);
        mGLController.onResume();
        mGLController.lockRenderThread();
        try {
            setLoadingBit(BIT_LOADING_RELOAD);
            mAlbumDataSetLoader.resume();
            mRender.resume();
            mRender.setPressedIndex(-1);
            mEyePosition.resume();
        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        LLog.i(TAG, "onPause");
        if (!mIsActive) {
            return;
        }
        mIsActive = false;
        mGLController.onPause();
        mGLController.lockRenderThread();
        try {
            mRender.setThumbnailFilter(null);
            mAlbumDataSetLoader.pause();
            mRender.pause();
            DetailsHelper.pause();
            mEyePosition.resume();
        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LLog.i(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideGuideTip();
        LLog.i(TAG, "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LLog.i(TAG, "onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDataSet != null) {
            mDataSet.destroyMediaSet();
        }
        LLog.i(TAG, "onDestroy");
    }

    @Override
    public void onEyePositionChanged(float x, float y, float z) {
        mRootPane.lockRendering();
        mX = x;
        mY = y;
        mZ = z;
        mRootPane.unlockRendering();
        mRootPane.invalidate();
    }

    @Override
    public void onClick(View v) {
        hideGuideTip();
        if (v.getId() == R.id.navi_button) {
            if (mSelector.inSelectionMode()) {
                mSelector.leaveSelectionMode();
            } else {
                mLetoolContext.popContentFragment();
            }
        } else {
            if (!mIsSDCardMountedCorreclty)
                return;
            if (v.getId() == R.id.action_delete) {
                mCurrentOperationMode = CURRENT_MODE_DELETE;
                mSelector.enterSelectionMode();
            } else if (v.getId() == R.id.action_accept) {
                MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_SELECT_OK);
                if (mCurrentOperationMode == CURRENT_MODE_DELETE) {
                    int count = mSelector.getSelectedCount();
                    if (count <= 0) {
                        Toast t = Toast.makeText(getActivity(), R.string.common_selection_delete_tip, Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                        return;
                    }
                    BatchDeleteMediaListener cdl = new BatchDeleteMediaListener(
                            getActivity(), mLetoolContext.getDataManager(),
                            new DeleteMediaProgressListener() {

                                @Override
                                public void onConfirmDialogDismissed(boolean confirmed) {
                                    if (confirmed) {
                                        MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_PHOTO_DELETE);
                                        mSelector.leaveSelectionMode();
                                    }
                                }

                                @Override
                                public ArrayList<MediaPath> onGetDeleteItem() {
                                    return mSelector.getSelected(false);
                                }

                            });
                    final NpDialog dlg = new NpDialog(getActivity());
                    dlg.setTitle(R.string.common_recommend);
                    dlg.setOkBtn(R.string.common_ok, cdl, R.drawable.np_common_pressed_left_bg);
                    dlg.setCancelBtn(R.string.common_cancel, cdl, R.drawable.np_common_pressed_right_bg);
                    dlg.setMessage(R.string.common_delete_tip);
                    dlg.show();
                } else if (mCurrentOperationMode == CURRENT_MODE_SHARE) {
                    int count = mSelector.getSelectedCount();
                    if (count <= 0) {
                        Toast t = Toast.makeText(getActivity(), R.string.common_selection_share_tip, Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                        return;
                    }
                    ArrayList<Uri> uris = new ArrayList<Uri>();
                    for (MediaPath p : mSelector.getSelected(false)) {
                        if (p.getFilePath().length() > 0) {
                            uris.add(Uri.parse("file://" + p.getFilePath()));
                        }
                    }
                    ShareManager.showAllShareDialog(getActivity(), GlobalConstants.MIMI_TYPE_IMAGE, uris,
                            new ShareListener() {

                                @Override
                                public void shareTriggered() {
                                    if (mSelector.inSelectionMode()) {
                                        mSelector.leaveSelectionMode();
                                    }
                                }
                            });
                }
            } else if (v.getId() == R.id.action_share) {
                mCurrentOperationMode = CURRENT_MODE_SHARE;
                mSelector.enterSelectionMode();

            }
        }
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
                initSelectionBar();
                mRootPane.invalidate();
                break;
            }
            case SelectionManager.LEAVE_SELECTION_MODE: {
                initBars();
                mRootPane.invalidate();
                break;
            }
            case SelectionManager.SELECT_ALL_MODE: {
                mRootPane.invalidate();
                break;
            }
        }
    }

    @Override
    public void onSelectionChange(MediaPath path, boolean selected) {
        int count = mSelector.getSelectedCount();
        String format = getResources().getQuantityString(R.plurals.number_of_items, count);
        if (mCurrentOperationMode == CURRENT_MODE_SHARE) {
            mLetoolContext.getLetoolTopBar().setTitleText(getResources().getString(R.string.common_share) + String.format(format, count));
        } else if (mCurrentOperationMode == CURRENT_MODE_DELETE) {
            mLetoolContext.getLetoolTopBar().setTitleText(getResources().getString(R.string.common_delete) + String.format(format, count));
        }
    }

    private Rect getThumbnailRect(int index) {
        Rect r = new Rect();
        Rect rx = mThumbnailView.getThumbnailRect(index);
        int x = (int) mOpenCenter.getX();
        int y = (int) mOpenCenter.getY();
        r.set(x, y, x + rx.width(), y + rx.height());
        return r;
    }

    private void pickPhoto(int index) {
        if (mLetoolContext.isImagePicking()) {
            MediaItem current = mAlbumDataSetLoader.get(index);
            if (current == null || (current.getSupportedOperations() & MediaObject.SUPPORT_EDIT) == 0) {
                return;
            }

            Intent intent = new Intent(NpEditActivity.FILTER_EDIT_ACTION);
            intent.setDataAndType(current.getContentUri(), current.getMimeType()).setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
                intent.setAction(Intent.ACTION_EDIT);
            }
            getActivity().startActivity(intent);
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        } else {
            hideGuideTip();
            Bundle data = new Bundle();
            data.putInt(CpPictureActivity.KEY_ALBUM_ID, mDataSet.getPath().getIdentity());
            data.putString(CpPictureActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
            data.putString(CpPictureActivity.KEY_ALBUM_TITLE, mDataSet.getName());
            Fragment fragment = new FullImageFragment();
            data.putInt(FullImageFragment.KEY_INDEX_HINT, index);
            data.putParcelable(FullImageFragment.KEY_OPEN_ANIMATION_RECT, getThumbnailRect(index));
            fragment.setArguments(data);
            mLetoolContext.pushContentFragment(fragment, this, true);
        }
    }

    public void showGuideTip() {
        if (GlobalPreference.isGuideTipShown(getActivity()) && mAlbumDataSetLoader.size() > 0) {
            final View tip = mLetoolContext.getGuidTipView();
            tip.setVisibility(View.VISIBLE);
            Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_in);
            a.setStartOffset(600);
            a.setDuration(600);
            tip.startAnimation(a);
            Button close = (Button) tip.findViewById(R.id.close_tip);
            close.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    tip.setVisibility(View.GONE);
                    tip.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_out));
                    GlobalPreference.setGuideTipShown(getActivity(), false);
                }
            });
        }
    }

    public void hideGuideTip() {
        if (GlobalPreference.isGuideTipShown(getActivity())) {
            final View tip = mLetoolContext.getGuidTipView();
            if (tip.getVisibility() == View.VISIBLE) {
                tip.setVisibility(View.GONE);
                tip.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_out));
            }
        }
    }
}
