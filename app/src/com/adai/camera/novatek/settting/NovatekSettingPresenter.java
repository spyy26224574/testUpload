package com.adai.camera.novatek.settting;

import android.util.SparseArray;

import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.data.NovatekDataSource;
import com.adai.camera.novatek.data.NovatekRepository;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.FileSizeUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.MovieRecord;

/**
 * Created by huangxy on 2017/8/5 15:29.
 */

public class NovatekSettingPresenter extends NovatekSettingContract.Presenter {
    private NovatekRepository mNovatekRepository;
    private CmdCallback mCmdCallback;

    public NovatekSettingPresenter() {
        mNovatekRepository = NovatekRepository.getInstance();
    }

    @Override
    public void setOnCmdCallback(CmdCallback cmdListener) {
        mCmdCallback = cmdListener;
    }

    @Override
    public void init() {
        mView.showLoading(VLCApplication.getAppContext().getString(R.string.access_camera_state));
        //停止视频流
        CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "0", new CameraUtils.CmdCallback() {
            @Override
            public void success(int commandId, String par, MovieRecord movieRecord) {
                mNovatekRepository.initDataSource(new NovatekDataSource.DataSourceSimpleCallBack() {
                    @Override
                    public void success() {
                        mView.settingsInited();
                    }

                    @Override
                    public void error(String error) {
                        mView.hideLoading();
                        ToastUtil.showShortToast(mView.getAttachedContext(), mView.getAttachedContext().getString(R.string.access_camera_state_failed));
                    }
                });
            }

            @Override
            public void failed(int commandId, String par, String error) {
                mView.hideLoading();
                ToastUtil.showShortToast(mView.getAttachedContext(), mView.getAttachedContext().getString(R.string.access_camera_state_failed));
            }
        });
    }

    @Override
    public void getStatus() {
        mView.showLoading(VLCApplication.getAppContext().getString(R.string.access_camera_state));
        mNovatekRepository.getStatus(new NovatekDataSource.DataSourceSimpleCallBack() {
            @Override
            public void success() {
                CameraUtils.sendCmd(NovatekWifiCommands.CAMERA_GET_DISK_FREE_SPACE, null, new CameraUtils.CmdCallback() {
                    @Override
                    public void success(int commandId, String par, MovieRecord movieRecord) {
                        try {
                            CameraUtils.FREE_MEMORY = FileSizeUtils.FormetFileSize(Long.valueOf(movieRecord.getValue()));
                        } catch (Exception ignore) {
                        }
                        mView.hideLoading();
                        mView.getStatusSuccess();
                    }

                    @Override
                    public void failed(int commandId, String par, String error) {
                        mView.hideLoading();
                        mView.getStatusSuccess();
                    }
                });

            }

            @Override
            public void error(String error) {
                mView.hideLoading();
                ToastUtil.showShortToast(mView.getAttachedContext(), mView.getAttachedContext().getString(R.string.access_camera_state_failed));
            }
        });
    }

    @Override
    public boolean cmdIsSupported(int commandId) {
        return mNovatekRepository.cmdIsSupported(commandId);
    }

    @Override
    public String getState(int commandId) {
        return mNovatekRepository.getCurState(commandId);
    }

    @Override
    public String getStateId(int commandId) {
        return mNovatekRepository.getCurStateId(commandId);
    }

    @Override
    public SparseArray<String> getMenuItem(int commandId) {
        return mNovatekRepository.getMenuItem(commandId);
    }

    @Override
    public void sendCmd(final int commandId, final String par) {
//        if (commandId == NovatekWifiCommands.MOVIE_DATE_PRINT) {
//            mView.showLoading(VLCApplication.getAppContext().getString(R.string.setting));
//            CameraUtils.sendImmediateCmd(Contacts.BASE_URL + commandId + "&par=" + par, new CameraUtils.ImmediateCmdListener() {
//                @Override
//                public boolean onResponse(String response) {
//                    InputStream is;
//                    try {
//                        is = new ByteArrayInputStream(response.getBytes("utf-8"));
//                        DomParseUtils domParseUtils = new DomParseUtils();
//                        MovieRecord record = domParseUtils.getParserXml(is);
//                        if (mCmdCallback != null) {
//                            mCmdCallback.success(commandId, par, record);
//                        }
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                        if (mCmdCallback != null) {
//                            mCmdCallback.failed(e.getMessage());
//                        }
//                    }
//                    mView.hideLoading();
//                    return true;
//                }
//
//                @Override
//                public void onStreamRefreshed(boolean isRefreshed) {
//
//                }
//
//                @Override
//                public void onErrorResponse(Exception e) {
//                    if (mCmdCallback != null) {
//                        mCmdCallback.failed(e.getMessage());
//                    }
//                    mView.hideLoading();
//                }
//            });
//            return;
//        }
        if (!CameraUtils.isRecording || !CameraUtils.hasSDCard || CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
            CameraUtils.sendCmd(commandId, par, new CameraUtils.CmdCallback() {
                @Override
                public void success(int commandId, String par, MovieRecord movieRecord) {
                    if (movieRecord == null || !"0".equals(movieRecord.getStatus())) {
                        if (mCmdCallback != null) {
                            mCmdCallback.failed(commandId, par, "status!=0");
                        }
                    } else {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        if (mCmdCallback != null) {
                            mCmdCallback.success(commandId, par);
                        }
                    }
                }

                @Override
                public void failed(int commandId, String par, String error) {
                    if (mCmdCallback != null) {
                        mCmdCallback.failed(commandId, par, error);
                    }
                }
            });

        } else {
            CameraUtils.sendAutoToggleRecordCmd(commandId, par, new CameraUtils.CmdCallback() {
                @Override
                public void success(int cmd, String parameter, MovieRecord movieRecord) {
                    if (movieRecord == null || !"0".equals(movieRecord.getStatus())) {
                        if (mCmdCallback != null) {
                            mCmdCallback.failed(commandId, par, "status!=0");
                        }
                    } else {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        if (mCmdCallback != null) {
                            mCmdCallback.success(commandId, par);
                        }
                    }
                }

                @Override
                public void failed(int cmd, String parameter, String error) {
                    if (mCmdCallback != null) {
                        mCmdCallback.failed(commandId, par, error);
                    }
                }
            });
        }
    }

}
