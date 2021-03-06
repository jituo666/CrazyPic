package com.xjt.crazypic.edit.pipeline;

import android.graphics.Bitmap;

import com.xjt.crazypic.edit.filters.FiltersManager;

public class FullresRenderingRequestTask extends ProcessingTask {

    private CachingPipeline mFullresPipeline = null;
    private boolean mPipelineIsOn = false;

    public void setPreviewScaleFactor(float previewScale) {
        mFullresPipeline.setPreviewScaleFactor(previewScale);
    }

    static class Render implements Request {
        RenderingRequest request;
    }

    static class RenderResult implements Result {
        RenderingRequest request;
    }

    public FullresRenderingRequestTask() {
        mFullresPipeline = new CachingPipeline(FiltersManager.getHighresManager(), "Fullres");
    }

    public void setOriginal(Bitmap bitmap) {
        mFullresPipeline.setOriginal(bitmap);
        mPipelineIsOn = true;
    }

    public void stop() {
        mFullresPipeline.stop();
    }

    public void postRenderingRequest(RenderingRequest request) {
        if (!mPipelineIsOn) {
            return;
        }
        postRequest(request);
    }

    @Override
    public Result doInBackground(Request message) {
        RenderingRequest request = (RenderingRequest) message;
        RenderResult result = null;
        mFullresPipeline.render(request);
        result = new RenderResult();
        result.request = request;
        return result;
    }

    @Override
    public void onResult(Result message) {
        if (message == null) {
            return;
        }
        RenderingRequest request = ((RenderResult) message).request;
        request.markAvailable();
    }

    @Override
    public boolean isDelayedTask() { return true; }
}
