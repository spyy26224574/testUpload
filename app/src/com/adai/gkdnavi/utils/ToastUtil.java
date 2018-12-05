package com.adai.gkdnavi.utils;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.example.ipcamera.application.VLCApplication;

/**
 *
 */
public class ToastUtil {

    private static Toast mToast;

    private static Handler mHandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() {
            if (mToast != null) {
                mToast.cancel();
                mToast = null;// toast隐藏后，将其置为null
            }
        }
    };

    public static void showShortToast(@StringRes int stringRes) {
        showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppResources().getString(stringRes));
    }

    public static void showShortToast(Context context, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(message);
            mToast.show();
        }
//		TextView text = new TextView(context);// 显示的提示文字
//		text.setText(message);
////		text.setBackgroundColor(Color.BLACK);
//		text.setPadding(10, 10, 10, 10);
//
//		if (mToast != null) {//
//			mHandler.postDelayed(r, 0);//隐藏toast
//		} else {
//			mToast = new Toast(context);
//			mToast.setDuration(Toast.LENGTH_SHORT);
//			mToast.setGravity(Gravity.BOTTOM, 0, 150);
//			mToast.setView(text);
//		}
//
//		mHandler.postDelayed(r, 1000);// 延迟1秒隐藏toast
//		mToast.show();
    }
}