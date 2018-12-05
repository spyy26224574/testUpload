package com.adai.camera.mstar.setting;

import com.adai.camera.mstar.data.MstarDataSource;
import com.adai.camera.mstar.data.MstarRepository;
import com.adai.gkdnavi.R;

/**
 * Created by huangxy on 2017/10/14 12:01.
 */

public class MstarSettingPresenter extends MstarSettingContract.Presenter {

    @Override
    public void init() {
        mView.showLoading(R.string.please_wait);
        MstarRepository.getInstance().initDataSource(new MstarDataSource.DataSourceSimpleCallBack() {
            @Override
            public void success() {
                mView.settingsInited();
            }

            @Override
            public void error(String error) {
//                mView.hideLoading();
//                mView.showToast(R.string.access_camera_state_failed);
                mView.settingsInited();
            }
        });
    }

    @Override
    public void getStatus() {
        MstarRepository.getInstance().getStatus(new MstarDataSource.DataSourceSimpleCallBack() {
            @Override
            public void success() {
                mView.hideLoading();
                mView.getStatusSuccess();
            }

            @Override
            public void error(String error) {
                mView.hideLoading();
                mView.showToast(R.string.access_camera_state_failed);
            }
        });
    }

}
