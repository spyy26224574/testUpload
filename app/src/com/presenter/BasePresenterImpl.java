package com.presenter;

import com.ivew.IBaseView;

/**
 * Created by huangxy on 2017/8/4 17:15.
 */

public class BasePresenterImpl<T extends IBaseView> implements IBasePresenter<T> {
    protected T mView;
    protected boolean isDetached;
    @Override
    public void attachView(T view) {
        isDetached = false;
        mView = view;
    }

    @Override
    public void detachView() {
        isDetached = true;
    }
}
