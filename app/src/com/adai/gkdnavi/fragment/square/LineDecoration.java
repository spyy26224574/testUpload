package com.adai.gkdnavi.fragment.square;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by admin on 2016/8/8.
 */
public class LineDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public LineDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if(parent.getChildPosition(view) != 0)
            outRect.top = space;
    }
}
