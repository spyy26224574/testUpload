package com.adai.gkdnavi.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

import java.util.List;

public class SystemUtil {

	public static boolean isSystemSurport(){
		String DISPLAY=android.os.Build.DISPLAY;
		String MANUFACTURER=android.os.Build.MANUFACTURER;
		if(DISPLAY.startsWith("Flyme")||MANUFACTURER.equals("Meizu")){
			return false;
		}
		return true;
	}
	
	public static void openApp(Context context,String packageName) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi=null;
		try {
			pi = pm.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(pi.packageName);

		List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);

		ResolveInfo ri = apps.iterator().next();
		if (ri != null ) {
//		String packageName = ri.activityInfo.packageName;
		String className = ri.activityInfo.name;

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		ComponentName cn = new ComponentName(packageName, className);

		intent.setComponent(cn);
		context.startActivity(intent);
		}
		}

	public static String getAppversion(Context context){
		if(context==null)return null;
		PackageManager manager=context.getPackageManager();
		try {
			PackageInfo pkginfo = manager.getPackageInfo(context.getPackageName(), 0);
			return pkginfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
