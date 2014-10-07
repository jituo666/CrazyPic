
package com.xjt.crazypic.selectors;

import com.xjt.crazypic.metadata.MediaPath;

/**
 * @Author Jituo.Xuan
 * @Date 8:18:33 PM Jul 24, 2014
 * @Comments:null
 */
public interface SelectionListener {

    public void onSelectionModeChange(int mode);

    public void onSelectionChange(MediaPath path, boolean selected);

}
