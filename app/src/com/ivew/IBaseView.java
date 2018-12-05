package com.ivew;

import android.support.annotation.StringRes;

/**
 * Created by huangxy on 2017/8/4 16:57.
 */

public interface IBaseView {
    void showLoading(String string);

    void showLoading(@StringRes int res);

    void hideLoading();

    void showToast(String string);

    void showToast(@StringRes int res);
}
