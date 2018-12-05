package com.presenter;

import com.ivew.IBaseView;

/**
 * Created by huangxy on 2017/8/4 17:01.
 */

public interface IBasePresenter<T extends IBaseView> {
    void attachView(T view);

    void detachView();
}
