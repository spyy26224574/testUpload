package com.adai.gkdnavi.widget;

import android.content.DialogInterface;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/2 16:30
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class ChoiceListener implements DialogInterface.OnClickListener {
    private int mWhich = -1;//为了防止用户根本没有选择就点击了确认

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mWhich = which;
    }

    public int getWhich() {
        return mWhich;
    }
}
