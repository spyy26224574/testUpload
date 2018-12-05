package com.adai.camera.novatek.data;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.utils.FileSizeUtils;
import com.adai.gkdnavi.utils.LogUtils;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MenuItem;
import com.example.ipcamera.domain.MenuOption;
import com.example.ipcamera.domain.MovieRecord;
import com.example.ipcamera.domain.MovieSizeItem;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangxy on 2017/8/7 10:00.
 * 单例模式
 */

public class NovatekRepository implements NovatekDataSource {
    private DataSourceSimpleCallBack mDataSourceSimpleCallBack;
    private List<MenuItem> mMenuItems = null;
    private List<MovieSizeItem> mMovieSizeItems = null;
    private List<Integer> mSupportCmd = null;
    private SparseArray<String> mCurStatusMap = null;
    private static NovatekRepository INSTANCE = null;

    private NovatekRepository() {
    }

    public static NovatekRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (NovatekRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NovatekRepository();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void initDataSource(@NonNull DataSourceSimpleCallBack dataSourceSimpleCallBack) {
        mDataSourceSimpleCallBack = dataSourceSimpleCallBack;
        CameraUtils.sendCmd(Contacts.BASE_URL + NovatekWifiCommands.CAMERA_QUERY_COMMANDS, new CameraUtils.CmdListener() {
            @Override
            public void onResponse(String response) {
                LogUtils.e(response);
                try {
                    InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    DomParseUtils domParseUtils = new DomParseUtils();
                    mSupportCmd = domParseUtils.querySupportCmd(is);
                } catch (Exception ignore) {
                }
                CameraUtils.getSDCardStatus(new CameraUtils.SDCardStatusListener() {
                    @Override
                    public void success(int status) {
                        CameraUtils.sendCmd(NovatekWifiCommands.CAMERA_GET_SD_SPACE, null, new CameraUtils.CmdCallback() {
                            @Override
                            public void success(int commandId, String par, MovieRecord movieRecord) {
                                try {
                                    CameraUtils.SDCARD_MEMORY = FileSizeUtils.FormetFileSize(Long.valueOf(movieRecord.getValue()));
                                } catch (Exception ignore) {

                                }
                                getMenuItems();
                            }

                            @Override
                            public void failed(int commandId, String par, String error) {
                                getMenuItems();
                            }
                        });
                    }

                    @Override
                    public void error(String error) {
                        getMenuItems();
                    }
                });
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                CameraUtils.getSDCardStatus(new CameraUtils.SDCardStatusListener() {
                    @Override
                    public void success(int status) {
                        getMenuItems();
                    }

                    @Override
                    public void error(String error) {
                        getMenuItems();
                    }
                });
            }
        });
    }

    public boolean cmdIsSupported(int cmd) {
        return mSupportCmd == null || mSupportCmd.size() == 0 || mSupportCmd.contains(cmd);
    }

    /**
     * 获取对应id的子菜单
     */
    public SparseArray<String> getMenuItem(int commandId) {
        SparseArray<String> stringSparseArray = null;
//        if (commandId == NovatekWifiCommands.MOVIE_SET_RECORD_SIZE) {
//            //录制质量以3030获取的为准
//            if (mMovieSizeItems != null) {
//                //内容处理
//                stringSparseArray = new SparseArray<>();
//                for (MovieSizeItem movieSizeItem : mMovieSizeItems) {
//                    if (movieSizeItem.getType() == 4) {
//                        stringSparseArray.put(movieSizeItem.getIndex(), movieSizeItem.getName());
//                    }
//                }
//                return stringSparseArray;
//            }
//        }
        if (mMenuItems != null) {
            for (MenuItem menuItem : mMenuItems) {
                if (menuItem.getCmd() == commandId) {
                    stringSparseArray = new SparseArray<>();
                    for (MenuOption menuOption : menuItem.getOption()) {
                        stringSparseArray.put(menuOption.getIndex(), menuOption.getId());
                    }
                    break;
                }
            }
        }
        return stringSparseArray;
    }

    public String getCurState(int commandId) {
        SparseArray<String> menuItem = getMenuItem(commandId);
        String curState = "";
        if (menuItem != null) {
            try {
                curState = menuItem.get(Integer.valueOf(getCurStateId(commandId)));
            } catch (Exception ignore) {

            }
        }
        return curState;
    }

    public String getCurStateId(int command) {
        if (mCurStatusMap != null) {
            return mCurStatusMap.get(command);
        }
        return null;
    }

    public void setCurStateId(int command, String curStateId) {
        if (mCurStatusMap == null) {
            return;
        }
        mCurStatusMap.put(command, curStateId);
    }

    private void getMenuItems() {
        CameraUtils.sendCmd(Contacts.URL_QUERY_MENUITEM, new CameraUtils.CmdListener() {
            @Override
            public void onResponse(String response) {
                LogUtils.e(response);
                try {
                    InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    DomParseUtils domParseUtils = new DomParseUtils();
                    mMenuItems = domParseUtils.queryMenuItemXml(is);
//                    getMovieSize();
                    mDataSourceSimpleCallBack.success();//初始化摄像头配置成功
                } catch (Exception e) {
                    LogUtils.e(e.getMessage());
                    e.printStackTrace();
                    if (mDataSourceSimpleCallBack != null) {
                        mDataSourceSimpleCallBack.error(e.getMessage());
                    }
                }
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (mDataSourceSimpleCallBack != null) {
                    mDataSourceSimpleCallBack.error(volleyError.getMessage());
                }
            }
        });
    }

    private void getMovieSize() {
        CameraUtils.sendCmd(Contacts.URL_QUERY_MOVIE_SIZE, new CameraUtils.CmdListener() {
            @Override
            public void onResponse(String response) {
                LogUtils.e(response);
                try {
                    DomParseUtils dom = new DomParseUtils();
                    InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    mMovieSizeItems = dom.queryMovieSizeXml(is);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mDataSourceSimpleCallBack.success();//初始化摄像头配置成功
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                mDataSourceSimpleCallBack.error(volleyError.getMessage());
            }
        });
    }

    @Override
    public void getStatus(@NonNull final DataSourceSimpleCallBack dataSourceSimpleCallBack) {
        CameraUtils.sendCmd(Contacts.URL_QUERY_CURRENT_STATUS, new CameraUtils.CmdListener() {
            @Override
            public void onResponse(String response) {
                LogUtils.e(response);
                DomParseUtils domParseUtils = new DomParseUtils();
                int i = domParseUtils.parastr(response);
                HashMap<String, String> map = domParseUtils.hMap;
                if (mCurStatusMap == null) {
                    mCurStatusMap = new SparseArray<>();
                } else {
                    mCurStatusMap.clear();
                }
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    try {
                        int key = Integer.valueOf(entry.getKey());
                        String value = entry.getValue();
                        mCurStatusMap.put(key, value);
                    } catch (Exception ignore) {

                    }
                }
                dataSourceSimpleCallBack.success();
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                dataSourceSimpleCallBack.error(volleyError.getMessage());
            }
        });
    }

    @Override
    public void getFileList(@NonNull final GetFileListCallback getFileListCallback) {
        CameraUtils.getFileList(new CameraUtils.GetFileListListener() {
            @Override
            public void success(List<FileDomain> fileDomains) {
                getFileListCallback.success(fileDomains);
            }

            @Override
            public void error(String error) {
                getFileListCallback.failed(error);
            }
        });
    }

}
