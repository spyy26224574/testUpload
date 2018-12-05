package com.adai.gkdnavi.utils;


import com.example.ipcamera.application.VLCApplication;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Process;
import android.util.DisplayMetrics;

public class UIUtils {

	public static Context getContext()
	{
		return VLCApplication.getAppContext();
	}

	public static Handler getMainHandler()
	{
		return VLCApplication.mMainHandler;
	}

	public static int getMainThreadId()
	{
		return VLCApplication.mMainThreadId;
	}

	public static void post(Runnable task)
	{
		if (Process.myTid() == getMainThreadId())
		{
			// 主线程中执行的
			task.run();
		}
		else
		{
			// 在主线程中运行
			getMainHandler().post(task);
		}
	}

	public static String getPackageName()
	{
		return getContext().getPackageName();
	}

	public static Resources getResources()
	{
		return getContext().getResources();
	}

	public static String getString(int resId)
	{
		return getResources().getString(resId);
	}

	public static int getColor(int resId)
	{
		return getResources().getColor(resId);
	}

	/**
	 * dip转px
	 * 
	 * @param i
	 */
	public static int dip2px(int dip)
	{
		// dip ---> px

		// 公式 ： px = dp * (dpi / 160)
		// dp = 160 * px / dpi
		// Density = px / dp
		// px = dp * density

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float density = metrics.density;
		return (int) (dip * density + 0.5f);
	}

	/**
	 * px转dip
	 * 
	 * @param px
	 * @return
	 */
	public static int px2dip(int px)
	{
		// px = dp * density
		// dp = px/ density

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float density = metrics.density;
		return (int) (px / density + 0.5f);
	}

	public static void postDelayed(Runnable task, long delay)
	{
		getMainHandler().postDelayed(task, delay);
	}

	public static void removeCallbacks(Runnable task)
	{
		getMainHandler().removeCallbacks(task);
	}

}
