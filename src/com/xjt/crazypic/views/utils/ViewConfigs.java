
package com.xjt.crazypic.views.utils;

import android.content.Context;
import android.content.res.Resources;

import com.xjt.crazypic.common.LLog;
import com.xjt.crazypic.views.layout.ThumbnailLayoutSpec;
import com.xjt.crazypic.views.render.ThumbnailSetRenderer;
import com.xjt.crazypic.views.render.ThumbnailVideoRenderer;
import com.xjt.crazypic.R;

/**
 * @Author Jituo.Xuan
 * @Date 3:28:44 PM Aug 7, 2014
 * @Comments:null
 */
public final class ViewConfigs {

    private static final String TAG = ViewConfigs.class.getSimpleName();

    public static class AlbumSetGridPage {

        private static AlbumSetGridPage sInstance;

        public ThumbnailLayoutSpec albumSetGridSpec;
        public ThumbnailSetRenderer.LabelSpec labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized AlbumSetGridPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumSetGridPage(context);
            }
            return sInstance;
        }

        private AlbumSetGridPage(Context context) {
            Resources r = context.getResources();
            albumSetGridSpec = new ThumbnailLayoutSpec();
            albumSetGridSpec.rowsPort = r.getInteger(R.integer.albumset_grid_rows_port); // 每行有多少个缩略图
            albumSetGridSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.albumset_grid_thumbnail_gap); // 缩略图之间的间隔
            albumSetGridSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_grid_label_height); // 标签的高度

            paddingLeft = r.getDimensionPixelSize(R.dimen.albumset_grid_padding_left); // 整个屏幕左边距
            paddingTop = r.getDimensionPixelSize(R.dimen.albumset_grid_padding_top);  // 整个屏幕上边距
            paddingRight = r.getDimensionPixelSize(R.dimen.albumset_grid_padding_right); // 整个屏幕右边距
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_grid_padding_bottom); // 整个屏幕下边距

            labelSpec = new ThumbnailSetRenderer.LabelSpec();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_grid_label_height); // 标签的高度
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.albumset_grid_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.albumset_grid_count_font_size);
            labelSpec.borderSize =  r.getDimensionPixelSize(R.dimen.albumset_grid_border_size);
            labelSpec.backgroundColor = r.getColor(R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
            LLog.i(TAG, " ---set grid rowsPort:" + albumSetGridSpec.rowsPort + " labelHeight:" + labelSpec.labelHeight);
        }
    }

    public static class AlbumSetListPage {

        private static AlbumSetListPage sInstance;

        public ThumbnailLayoutSpec albumSetListSpec;
        public ThumbnailSetRenderer.LabelSpec labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized AlbumSetListPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumSetListPage(context);
            }
            return sInstance;
        }

        private AlbumSetListPage(Context context) {
            Resources r = context.getResources();
            albumSetListSpec = new ThumbnailLayoutSpec();
            albumSetListSpec.rowsPort = r.getInteger(R.integer.albumset_list_rows_port);
            albumSetListSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.albumset_list_thumbnail_gap);
            albumSetListSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_list_label_height);

            paddingLeft = r.getDimensionPixelSize(R.dimen.albumset_list_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.albumset_list_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.albumset_list_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_list_padding_bottom);

            labelSpec = new ThumbnailSetRenderer.LabelSpec();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_list_label_height);// 标签的高度，这个高度决定了缩略图的绘制范围（宽和高）
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.albumset_list_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.albumset_list_count_font_size);
            labelSpec.borderSize =  r.getDimensionPixelSize(R.dimen.albumset_grid_border_size);
            labelSpec.backgroundColor = r.getColor(R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
            LLog.i(TAG, " ---set list rowsPort:" + albumSetListSpec.rowsPort + " labelHeight:" + labelSpec.labelHeight);
        }
    }

    public static class AlbumPage {

        private static AlbumPage sInstance;

        public ThumbnailLayoutSpec albumSpec;
        public int placeholderColor;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized AlbumPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumPage(context);
            }
            return sInstance;
        }

        private AlbumPage(Context context) {
            Resources r = context.getResources();

            albumSpec = new ThumbnailLayoutSpec();
            albumSpec.rowsPort = r.getInteger(R.integer.album_rows_port);
            albumSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.album_thumbnail_gap);
            albumSpec.tagHeight = r.getDimensionPixelSize(R.dimen.album_label_height);
            albumSpec.tagWidth = r.getDimensionPixelSize(R.dimen.album_label_width);
            //
            paddingLeft = r.getDimensionPixelSize(R.dimen.album_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.album_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.album_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.album_padding_bottom);

        }
    }

    public static class VideoSetPage {

        private static VideoSetPage sInstance;

        public ThumbnailLayoutSpec videoSetSpec;
        public ThumbnailVideoRenderer.LabelSpec labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized VideoSetPage get(Context context) {
            if (sInstance == null) {
                sInstance = new VideoSetPage(context);
            }
            return sInstance;
        }

        private VideoSetPage(Context context) {
            Resources r = context.getResources();
            videoSetSpec = new ThumbnailLayoutSpec();
            videoSetSpec.rowsPort = r.getInteger(R.integer.videoset_rows_port);
            videoSetSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.videoset_thumbnail_gap);
            videoSetSpec.labelHeight = r.getDimensionPixelSize(R.dimen.videoset_label_height);

            paddingLeft = r.getDimensionPixelSize(R.dimen.videoset_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.videoset_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.videoset_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.videoset_padding_bottom);

            labelSpec = new ThumbnailVideoRenderer.LabelSpec();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);
            labelSpec.titleOffset = r.getDimensionPixelSize(R.dimen.videoset_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(R.dimen.videoset_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.videoset_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.videoset_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(R.dimen.videoset_left_margin);
            labelSpec.titleRightMargin = r.getDimensionPixelSize(R.dimen.videoset_title_right_margin);
            labelSpec.iconSize = r.getDimensionPixelSize(R.dimen.videoset_icon_size);
            labelSpec.backgroundColor = r.getColor(R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
        }
    }

    public static class VideoPage {

        private static VideoPage sInstance;

        public ThumbnailLayoutSpec videoSpec;
        public ThumbnailVideoRenderer.LabelSpec labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized VideoPage get(Context context) {
            if (sInstance == null) {
                sInstance = new VideoPage(context);
            }
            return sInstance;
        }

        private VideoPage(Context context) {
            Resources r = context.getResources();
            videoSpec = new ThumbnailLayoutSpec();
            videoSpec.rowsPort = r.getInteger(R.integer.video_rows_port);
            videoSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.video_thumbnail_gap);
            videoSpec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);

            paddingLeft = r.getDimensionPixelSize(R.dimen.video_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.video_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.video_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.video_padding_bottom);

            labelSpec = new ThumbnailVideoRenderer.LabelSpec();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);
            labelSpec.titleOffset = r.getDimensionPixelSize(R.dimen.video_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(R.dimen.video_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.video_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.video_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(R.dimen.video_left_margin);
            labelSpec.titleRightMargin = r.getDimensionPixelSize(R.dimen.video_title_right_margin);
            labelSpec.iconSize = r.getDimensionPixelSize(R.dimen.video_icon_size);
            labelSpec.backgroundColor = r.getColor(R.color.video_label_background);
            labelSpec.titleColor = r.getColor(R.color.video_label_title);
            labelSpec.countColor = r.getColor(R.color.video_label_count);
        }
    }
}
