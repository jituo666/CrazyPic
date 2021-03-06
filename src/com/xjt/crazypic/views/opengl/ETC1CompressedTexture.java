package com.xjt.crazypic.views.opengl;

import android.opengl.ETC1Util.ETC1Texture;

// BitmapTexture is a texture whose content is specified by a fixed Bitmap.
// The texture does not own the Bitmap. The user should make sure the Bitmap
// is valid during the texture's lifetime. When the texture is recycled, it  does not free the Bitmap.
public class ETC1CompressedTexture extends UploadETC1Texture {

    protected ETC1Texture mContentTexture;
    
    public ETC1CompressedTexture(ETC1Texture texture) {
        super(texture);
        mContentTexture = texture;
    }

    @Override
    protected ETC1Texture onGetETC1Texture() {
        return mContentTexture;
    }

    @Override
    protected void onFreeETC1Texture(ETC1Texture etc1Texture) {
        //
    }
    
    public ETC1Texture getTexture() {
        return mContentTexture;
    }
 
}
