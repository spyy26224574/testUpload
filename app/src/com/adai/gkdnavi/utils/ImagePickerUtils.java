package com.adai.gkdnavi.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.adai.gkdnavi.R;

/**
 * Created by admin on 2016/9/19.
 */
public class ImagePickerUtils {

    public static final int REQUEST_CODE_PICKIMAGE=0X110;
    public static final int REQUEST_CODE_CROPIMAGE=0X111;
    public void pickImage(Activity activity){
        pickImage(activity,REQUEST_CODE_PICKIMAGE);
    }
    public void pickImage(final Activity activity, final int requestCode){
        if(activity==null)return;
        String[] items=new String[]{activity.getResources().getString(R.string.take_photo),activity.getResources().getString(R.string.photo_album)};
        new AlertDialog.Builder(activity).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:

                        break;
                    case 1:
                        startPickImage(activity,requestCode);
                        break;
                }
            }
        }).create().show();
    }

    private void takePhoto(Activity activity,int requestCode){

    }

    private void startPickImage(Activity activity,int requestCode){
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        getAlbum.setType("image/*");
        activity.startActivityForResult(getAlbum, requestCode);
    }
}
