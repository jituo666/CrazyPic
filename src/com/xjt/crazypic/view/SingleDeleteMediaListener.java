
package com.xjt.crazypic.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

import com.xjt.crazypic.metadata.DataManager;
import com.xjt.crazypic.metadata.MediaPath;
import com.xjt.crazypic.R;

/**
 * @Author Jituo.Xuan
 * @Date 4:44:26 PM May 18, 2014
 * @Comments:null
 */
public class SingleDeleteMediaListener implements OnClickListener{

    SingleDeleteMediaProgressListener progressListener;
    DataManager manager;
    Context context;
    MediaPath mPath;

    public interface SingleDeleteMediaProgressListener {

        public void onConfirmDialogDismissed(boolean confirmed);
    }

    public SingleDeleteMediaListener(Activity c, MediaPath p, DataManager m, SingleDeleteMediaProgressListener l) {
        context = c;
        manager = m;
        mPath = p;
        progressListener = l;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ok_btn) {
            if (mPath != null) {
                new DeleteMeidaTask().execute();
            }
        } else if (v.getId() == R.id.cancel_btn) {
            if (progressListener != null) {
                progressListener.onConfirmDialogDismissed(false);
            }
        }
    }

    private class DeleteMeidaTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setTitle(context.getString(R.string.common_delete));
            dialog.setMessage(context.getString(R.string.common_delete_waiting));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            manager.delete(mPath);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (progressListener != null) {
                progressListener.onConfirmDialogDismissed(true);
            }
        }

    }

}
