package com.adai.gkdnavi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Gallery;
import android.widget.RelativeLayout;

import com.adai.gkdnavi.adapter.ShareGalleryAdapter;

import java.util.ArrayList;
import java.util.List;

public class ShareLayoutActivity extends Activity {

    private List<Appinfo> appinfoList;
    private android.widget.Gallery sharegallery;
    private android.widget.RelativeLayout content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_share_layout);
        init();
    }

    private void init(){
        this.content = (RelativeLayout) findViewById(R.id.content);
        this.sharegallery = (Gallery) findViewById(R.id.share_gallery);
        appinfoList=getShareApps(this);
        ShareGalleryAdapter adapter=new ShareGalleryAdapter(this,appinfoList);
        sharegallery.setAdapter(adapter);
    }

    private List<Appinfo> getShareApps(Context context) {
        List<Appinfo> appinfos=new ArrayList<Appinfo>();
        PackageManager pManager = context.getPackageManager();
        Appinfo info=new Appinfo();
        info.icon=getApplicationInfo().loadIcon(pManager);
        info.laucherClassname=EditVideoActivity.class.getName();
        info.pkgName=getApplication().getPackageName();
        info.title=getString(R.string.share_title);
        appinfos.add(info);
        List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("video/*");
        //      intent.setType("*/*");

        mApps = pManager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        if(mApps!=null&&mApps.size()>0){
            for(ResolveInfo resolveInfo:mApps){
                Appinfo appinfo=new Appinfo();
                appinfo.pkgName=resolveInfo.activityInfo.packageName;
//              showLog_I(TAG, "pkg>" + resolveInfo.activityInfo.packageName + ";name>" + resolveInfo.activityInfo.name);
                appinfo.laucherClassname=resolveInfo.activityInfo.name;
                appinfo.title=resolveInfo.loadLabel(pManager).toString();
                appinfo.icon=resolveInfo.loadIcon(pManager);
                appinfos.add(appinfo);
            }
        }
        return appinfos;
    }

    public class Appinfo{
        public Drawable icon;
        public String pkgName;
        public String title;
        public String laucherClassname;
    }
}
