package com.adai.gkdnavi.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.adai.gkdnavi.R;

public class PermissionUtils {
	
	@SuppressLint("NewApi")
	public static boolean checkPermission(Activity context,String permission,boolean needrequst){
		
		try{
			ActivityCompat.requestPermissions(context, new String[]{permission},  100);
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
//		int code=ContextCompat.checkSelfPermission(context, permission);
//		String pkg=context.getApplication().getPackageName();
//		code=context.getPackageManager().checkPermission(permission, pkg);
//		if(code==PackageManager.PERMISSION_GRANTED){
//			if(needrequst){
//				
//			}
//			return false;
//		}else{
//			return true;
//		}
	}
	/**
	 * 显示弹框
	 * @param context
	 * @param permission
	 */
	public static void showPermissionDialog(Context context,String permission){
		String name=null;
		String name1=null;
		if(android.Manifest.permission.BLUETOOTH_ADMIN.equals(permission)){
			name="蓝牙";
			name1="改变蓝牙状态";
		}else if(android.Manifest.permission.CHANGE_WIFI_STATE.equals(permission)){
			name="wifi";
			name1="改变wifi状态";
		}else{
			name=permission;
		}
		String str=context.getString(R.string.permissiontext, name,name1);
		new AlertDialog.Builder(context).setTitle("提示").setMessage(str).setPositiveButton("确定", null).create().show();
	}
}
