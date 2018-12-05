package com.adai.camera.sunplus.filemanager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.camera.sunplus.SDKAPI.FileOperation;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.tool.SunplusImageLoadManager;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.icatch.wificam.customer.type.ICatchFile;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by huangxy on 2017/4/12 16:16.
 */

public class SunplusImageFragment extends Fragment {
    private ICatchFile mICatchFile;
    private PagerAdapter mPagerAdapter;
    private AlertDialog mAlertDialog;
    PhotoView image;
    private Activity mActivity;

    public SunplusImageFragment() {

    }

    public SunplusImageFragment(ICatchFile iCatchFile) {
        mICatchFile = iCatchFile;
    }

    public void setAdapter(SunplusPictureBrowseActivity.ImagePagerAdapter imagePagerAdapter) {
        mPagerAdapter = imagePagerAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        image = (PhotoView) view.findViewById(R.id.image);
        image.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                FragmentActivity activity = getActivity();
                if (activity instanceof SunplusPictureBrowseActivity) {
                    ((SunplusPictureBrowseActivity) activity).toggleFrame();
                }
            }
        });
        if (GlobalInfo.previewFileList != null && mPagerAdapter != null) {
            image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mAlertDialog = new AlertDialog.Builder(getContext())
                            .setMessage(R.string.wheter_delete_file)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final boolean ret = FileOperation.getInstance().deleteFile(mICatchFile);
                                            UIUtils.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (ret) {
                                                        //删除成功
                                                        GlobalInfo.previewFileList.remove(mICatchFile);
                                                        if (GlobalInfo.previewFileList.size() > 0) {
                                                            mPagerAdapter.notifyDataSetChanged();
                                                        } else {
                                                            mActivity.setResult(Activity.RESULT_OK);
                                                            mActivity.finish();
                                                        }
                                                    } else {
                                                        //删除失败
                                                        ToastUtil.showShortToast(mActivity, getString(R.string.deleted_failure));
                                                    }
                                                }
                                            });

                                        }
                                    }).start();
                                }
                            }).setNegativeButton(R.string.cancel, null)
                            .create();
                    mAlertDialog.show();
                    return true;
                }
            });
        }
        SunplusImageLoadManager.getInstance().loadImage(mICatchFile, R.drawable.default_image_holder, image, false);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }
}
