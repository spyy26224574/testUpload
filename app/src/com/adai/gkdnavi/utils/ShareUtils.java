package com.adai.gkdnavi.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkd.contacts.AccessTokenKeeper;
import com.adai.gkd.contacts.Constants_qq;
import com.adai.gkd.contacts.Constants_weibo;
import com.adai.gkd.contacts.Constants_wx;
import com.adai.gkdnavi.EditVideoActivity;
import com.adai.gkdnavi.R;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;
import com.example.ipcamera.application.VLCApplication;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gdut.bsx.share2.FileUtil;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;

/**
 * Created by admin on 2016/9/18.
 */
public class ShareUtils {
    public void showShareDialog(final Activity context, final String url, final String title, final String description, final String coverPicture) {
        if (TextUtils.isEmpty(url)) {
            ToastUtil.showShortToast(context, context.getString(R.string.not_support_share));
        }
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View contentView = View.inflate(context, R.layout.dialog_share, null);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        Log.e("shareUtils", "showShareDialog: url=" + url);
        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        List<Appinfo> thirdApp = getThirdApp(context);
        DialogListAdapter adapter = new DialogListAdapter(context, thirdApp, true);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Appinfo appinfo, int position) {
                switch (appinfo.type) {
                    case Appinfo.WECHART://微信
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                shareUrlToWx(context, url, title == null ? "" : title, description == null ? "" : description, false, coverPicture == null ? "" : coverPicture);
                            }
                        }).start();
                        break;
                    case Appinfo.WECHARTMOMENTS://朋友圈

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                shareUrlToWx(context, url, title == null ? "" : title, description == null ? "" : description, true, coverPicture == null ? "" : coverPicture);
                            }
                        }).start();
                        break;
                    case Appinfo.QQ://qq
                        shareUrlToQQ(context, url, title == null ? "" : title, description == null ? "" : description, coverPicture == null ? "" : coverPicture);
                        break;
                    case Appinfo.SINNAWEIBO://微博
                        shareUrlToWeibo(context, url, title == null ? "" : title, description == null ? "" : description);
                        break;
                    default:
                        break;
                }
                bottomSheetDialog.dismiss();
            }
        });
        recyclerView.setAdapter(adapter);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.show();
    }

    public void shareToApp(int appTag, final Activity context, final String url, final String title, final String description, final String coverPicture) {
        switch (appTag) {
            case Appinfo.WECHART://微信
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        shareUrlToWx(context, url, title == null ? "" : title, description == null ? "" : description, false, coverPicture == null ? "" : coverPicture);
                    }
                }).start();
                break;
            case Appinfo.WECHARTMOMENTS://朋友圈

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        shareUrlToWx(context, url, title == null ? "" : title, description == null ? "" : description, true, coverPicture == null ? "" : coverPicture);
                    }
                }).start();
                break;
            case Appinfo.QQ://qq
                shareUrlToQQ(context, url, title == null ? "" : title, description == null ? "" : description, coverPicture == null ? "" : coverPicture);
                break;
            case Appinfo.SINNAWEIBO://微博
                shareUrlToWeibo(context, url, title == null ? "" : title, description == null ? "" : description);
                break;
            default:
                break;
        }
    }


    private void shareUrlToWeibo(final Activity context, String url, String title, String des) {
        IWeiboShareAPI weiboAPI = WeiboShareSDK.createWeiboAPI(context, Constants_weibo.WEIBO_APPKEY);
        weiboAPI.registerApp();
        WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();
        WebpageObject webpageObject = new WebpageObject();
        webpageObject.identify = Utility.generateGUID();
        webpageObject.title = title;
        webpageObject.description = des;
        webpageObject.actionUrl = url;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        // 设置 Bitmap 类型的图片到视频对象里         设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        webpageObject.setThumbImage(bitmap);
        webpageObject.defaultText = "Webpage";

        weiboMultiMessage.mediaObject = webpageObject;
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = buildTransaction("url");
        request.multiMessage = weiboMultiMessage;
        if (weiboAPI.isWeiboAppInstalled()) {
            //安装了最新的微博客户端
            weiboAPI.sendRequest(context, request);
        } else {
            AuthInfo authInfo = new AuthInfo(context, Constants_weibo.WEIBO_APPKEY, Constants_weibo.REDIRECT_URL, Constants_weibo.SCOPE);
            Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(context);
            String token = "";
            if (accessToken != null) {
                token = accessToken.getToken();
            }
            weiboAPI.sendRequest(context, request, authInfo, token, new WeiboAuthListener() {

                @Override
                public void onWeiboException(WeiboException arg0) {
                }

                @Override
                public void onComplete(Bundle bundle) {
                    Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                    AccessTokenKeeper.writeAccessToken(context, newToken);
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }


    private void shareUrlToQQ(Activity context, String url, String title, String des, String coverPicture) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, des);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
        if (coverPicture != null) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, coverPicture);
        }
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, context.getString(R.string.app_name));
//        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        Tencent instance = Tencent.createInstance(Constants_qq.QQ_APP_ID, context);
        instance.shareToQQ(context, params, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                Log.e("share", "onComplete: ");
            }

            @Override
            public void onError(UiError uiError) {
                Log.e("share", "onError: " + uiError);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    /**
     * @param isTimeline 是否发送到朋友圈，true发送到朋友圈，false发送到聊天
     */
    private void shareUrlToWx(final Context activity, String url, String title, String description, boolean isTimeline, String coverPicture) {
        IWXAPI wxapi = WXAPIFactory.createWXAPI(activity, Constants_wx.WX_APP_ID);
        if (!wxapi.isWXAppInstalled()) {
            UIUtils.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showShortToast(activity, activity.getString(R.string.WeChat_client_not_installed));
                }
            });
            return;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;

        if (coverPicture != null) {
            Bitmap src = GetLocalOrNetBitmap(coverPicture);
            if (src != null) {
                Bitmap thumb = Bitmap.createScaledBitmap(src, 120, 120, true);//压缩Bitmap
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.PNG, 100, stream);
                msg.thumbData = stream.toByteArray();
            }
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction(url);//用于唯一标识一个请求
        req.message = msg;
        req.scene = isTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        wxapi.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private List<Appinfo> getThirdApp(Context context) {
        List<Appinfo> appinfos = new ArrayList<>();
        Resources resources = context.getResources();
        //qq
        Appinfo qq = new Appinfo();
        qq.icon = resources.getDrawable(R.drawable.qq);
        qq.title = "QQ";
        qq.type = Appinfo.QQ;
        appinfos.add(qq);
        //微信
        Appinfo wechat = new Appinfo();
        wechat.icon = resources.getDrawable(R.drawable.wechat);
        wechat.title = context.getString(R.string.wechat);
        wechat.type = Appinfo.WECHART;
        appinfos.add(wechat);
        //朋友圈
        Appinfo wechatmoments = new Appinfo();
        wechatmoments.icon = resources.getDrawable(R.drawable.wechatmoments);
        wechatmoments.title = context.getString(R.string.wechatmoments);
        wechatmoments.type = Appinfo.WECHARTMOMENTS;
        appinfos.add(wechatmoments);
        //新浪微博
        Appinfo sinaweibo = new Appinfo();
        sinaweibo.icon = resources.getDrawable(R.drawable.sinaweibo);
        sinaweibo.title = context.getString(R.string.sinaweibo);
        sinaweibo.type = Appinfo.SINNAWEIBO;
        appinfos.add(sinaweibo);
        return appinfos;
    }

    public void sharePhoto(final Activity activity, final ArrayList<String> paths) {
//        ArrayList<Uri> uris = new ArrayList<>();
//        for (String path : paths) {
//            Uri uri;
//            if (Build.VERSION.SDK_INT >= 24) {
//                uri = FileProvider.getUriForFile(activity, "com.ligo.awfcardv.fileprovider", new File(path));
//            } else {
//                uri = Uri.fromFile(new File(path));
//            }
//            uris.add(uri);
//        }
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("image/*");
//        if (Build.VERSION.SDK_INT >= 24) {
//            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        }
//        shareIntent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
//        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        activity.startActivity(shareIntent);
        Uri shareImageUri = FileUtil.getFileUri(activity, ShareContentType.IMAGE, new File(paths.get(0)));
        new Share2.Builder(activity)
                // 指定分享的文件类型
                .setContentType(ShareContentType.IMAGE)
                // 设置要分享的文件 Uri
                .setShareFileUri(shareImageUri)
                // 设置分享选择器的标题
                .forcedUseSystemChooser(true)
                .build()
                // 发起分享
                .shareBySystem();

//        RecyclerView content = new RecyclerView(activity);
//        content.setLayoutManager(new LinearLayoutManager(activity));
//        List<Appinfo> appinfos = getPhotoShareApps(activity);
//        DialogListAdapter adapter = new DialogListAdapter(activity, appinfos);
//        content.setAdapter(adapter);
//        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
//        bottomSheetDialog.setContentView(content);
//        adapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(Appinfo appinfo, int position) {
//                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//                shareIntent.setComponent(new ComponentName(appinfo.pkgName, appinfo.laucherClassname));
//                shareIntent.setType("image/*");
//                shareIntent.putExtra(Intent.EXTRA_STREAM, uris);
//                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                bottomSheetDialog.dismiss();
//                activity.startActivity(shareIntent);
//            }
//        });
//        bottomSheetDialog.show();
//        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
//        View contentView = View.inflate(activity, R.layout.dialog_share, null);
//        contentView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetDialog.dismiss();
//            }
//        });
//        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new WrapContentGridLayoutManager(activity, 3));
//        List<Appinfo> thirdApp = getThirdApp(activity);
//        Appinfo sozone = new Appinfo();
//        try {
//            PackageManager packageManager = activity.getPackageManager();
//            sozone.title = activity.getString(R.string.app_name);
//            sozone.type = Appinfo.RIDERS;
//            sozone.icon = packageManager.getApplicationIcon(activity.getPackageName());
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        thirdApp.add(0, sozone);
//        DialogListAdapter adapter = new DialogListAdapter(activity, thirdApp, true);
//        adapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(Appinfo appinfo, int position) {
//                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//                shareIntent.setType("image/*");
//                shareIntent.putExtra(Intent.EXTRA_STREAM, uris);
//                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                shareIntent.setClass(activity, ShareActivity.class);
//                shareIntent.putExtra("app", appinfo.type);
//                shareIntent.putExtra("title", appinfo.title);
//                bottomSheetDialog.dismiss();
//                activity.startActivity(shareIntent);
//            }
//        });
//        recyclerView.setAdapter(adapter);
//        bottomSheetDialog.setContentView(contentView);
//        bottomSheetDialog.show();
    }

    public void shareVideo(final Activity activity, final String videopath) {
//        Uri uri = Uri.parse("file:///" + videopath);
//        Uri uri = null;
//        if (Build.VERSION.SDK_INT >= 24) {
//            uri = FileProvider.getUriForFile(activity, "com.ligo.awfcardv.fileprovider", new File(videopath));
//        } else {
//            uri = Uri.parse("file:///" + videopath);
//        }
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("video/*");
//        if (Build.VERSION.SDK_INT >= 24) {
//            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        }
//        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        activity.startActivity(shareIntent);
        Uri uri = FileUtil.getFileUri(activity, ShareContentType.VIDEO, new File(videopath));
        new Share2.Builder(activity)
                // 指定分享的文件类型
                .setContentType(ShareContentType.VIDEO)
                // 设置要分享的文件 Uri
                .setShareFileUri(uri)
                // 设置分享选择器的标题
                .forcedUseSystemChooser(true)
                .build()
                // 发起分享
                .shareBySystem();
//        activity.finish();
//        RecyclerView content = new RecyclerView(activity);
//        content.setLayoutManager(new LinearLayoutManager(activity));
//        List<Appinfo> appinfos = getVideoShareApps(activity);
//        DialogListAdapter adapter = new DialogListAdapter(activity, appinfos);
//        content.setAdapter(adapter);
//        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
//        bottomSheetDialog.setContentView(content);
//        adapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(Appinfo appinfo, int position) {
//                Uri uri = Uri.parse("file:///" + videopath);
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setComponent(new ComponentName(appinfo.pkgName, appinfo.laucherClassname));
//                shareIntent.setType("video/*");
//                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                bottomSheetDialog.dismiss();
//                activity.startActivity(shareIntent);
//                activity.finish();
//            }
//        });
//        bottomSheetDialog.show();
//        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
//        View contentView = View.inflate(activity, R.layout.dialog_share, null);
//        contentView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetDialog.dismiss();
//            }
//        });
//        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new WrapContentGridLayoutManager(activity, 3));
//        List<Appinfo> thirdApp = getThirdApp(activity);
//        Appinfo sozone = new Appinfo();
//        try {
//            PackageManager packageManager = activity.getPackageManager();
//            sozone.title = activity.getString(R.string.app_name);
//            sozone.type = Appinfo.RIDERS;
//            sozone.icon = packageManager.getApplicationIcon(activity.getPackageName());
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        thirdApp.add(0, sozone);
//        DialogListAdapter adapter = new DialogListAdapter(activity, thirdApp, true);
//        adapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(Appinfo appinfo, int position) {
//                Uri uri = Uri.parse("file:///" + videopath);
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setType("video/*");
//                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                shareIntent.setClass(activity, ShareActivity.class);
//                shareIntent.putExtra("app", appinfo.type);
//                shareIntent.putExtra("title", appinfo.title);
//                bottomSheetDialog.dismiss();
//                activity.startActivity(shareIntent);
//                activity.finish();
//            }
//        });
//        recyclerView.setAdapter(adapter);
//        bottomSheetDialog.setContentView(contentView);
//        bottomSheetDialog.show();
    }

    //    public void shareVideo(final Activity activity, final String savepath, final String logo_path, final int videoTime) {
//        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
//        View contentView = View.inflate(activity, R.layout.dialog_share, null);
//        contentView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetDialog.dismiss();
//            }
//        });
//        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new WrapContentGridLayoutManager(activity, 3));
//        List<Appinfo> thirdApp = getThirdApp(activity);
//        Appinfo sozone = new Appinfo();
//        try {
//            PackageManager packageManager = activity.getPackageManager();
//            sozone.title = activity.getString(R.string.app_name);
//            sozone.type = Appinfo.RIDERS;
//            sozone.icon = packageManager.getApplicationIcon(activity.getPackageName());
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        thirdApp.add(0, sozone);
//        DialogListAdapter adapter = new DialogListAdapter(activity, thirdApp, true);
//        adapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(Appinfo appinfo, int position) {
//                Intent intent = new Intent();
//                intent.setClass(activity, ShareActivity.class);
//                intent.putExtra("shareType", 0);
//                intent.putExtra("video_path", savepath);
//                intent.putExtra("video_logo_path", logo_path);
//                intent.putExtra("video_time", videoTime);
//                intent.putExtra("app", appinfo.type);
//                intent.putExtra("title", appinfo.title);
//                bottomSheetDialog.dismiss();
//                activity.startActivity(intent);
//                activity.finish();
//                File files[] = new File(VLCApplication.TEMP_PATH).listFiles();
//                if (files != null) {
//                    for (File f : files) {
//                        if (FileTypeUtil.TYPE_VIDEO == FileTypeUtil.getFileType(f.getPath())) {
//                            f.delete();
//                        }
//                    }
//                }
//            }
//        });
//        recyclerView.setAdapter(adapter);
//        bottomSheetDialog.setContentView(contentView);
//        bottomSheetDialog.show();
//
//    }
    public void shareVideo(final Activity activity, final String savepath, final String logo_path, final int videoTime) {
//        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
//        View contentView = View.inflate(activity, R.layout.dialog_share, null);
//        contentView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetDialog.dismiss();
//            }
//        });
//        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new WrapContentGridLayoutManager(activity, 4));
//        List<Appinfo> thirdApp = getThirdApp(activity);
//        Appinfo sozone = new Appinfo();
//        try {
//            PackageManager packageManager = activity.getPackageManager();
//            sozone.title = activity.getString(R.string.app_name);
//            sozone.type = Appinfo.RIDERS;
//            sozone.icon = packageManager.getApplicationIcon(activity.getPackageName());
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        thirdApp.add(0, sozone);
//        DialogListAdapter adapter = new DialogListAdapter(activity, thirdApp, true);
//        adapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(Appinfo appinfo, int position) {
//                switch (appinfo.type) {
//                    case Appinfo.INSTAGRAM:
//                        shareToInstagram(activity, "video/*", savepath);
//                        break;
//                    case Appinfo.YOUTUBE:
//                        shareByMob(appinfo.type, "", "", "", "", savepath);
////                        shareToYoutue(activity, "video/*", savepath);
//                        break;
//                    default:
//                        Intent intent = new Intent();
//                        intent.setClass(activity, ShareActivity.class);
//                        intent.putExtra("shareType", 0);
//                        intent.putExtra("video_path", savepath);
//                        intent.putExtra("video_logo_path", logo_path);
//                        intent.putExtra("video_time", videoTime);
//                        intent.putExtra("app", appinfo.type);
//                        intent.putExtra("title", appinfo.title);
//                        activity.startActivity(intent);
//                        File files[] = new File(VLCApplication.TEMP_PATH).listFiles();
//                        if (files != null) {
//                            for (File f : files) {
//                                if (FileTypeUtil.TYPE_VIDEO == FileTypeUtil.getFileType(f.getPath())) {
//                                    f.delete();
//                                }
//                            }
//                        }
//                        break;
//                }
//                bottomSheetDialog.dismiss();
//                activity.finish();
//            }
//        });
//        recyclerView.setAdapter(adapter);
//        bottomSheetDialog.setContentView(contentView);
//        bottomSheetDialog.show();
        Uri uri = Uri.parse("file:///" + savepath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(shareIntent);
        File files[] = new File(VLCApplication.TEMP_PATH).listFiles();
        if (files != null) {
            for (File f : files) {
                if (FileTypeUtil.TYPE_VIDEO == FileTypeUtil.getFileType(f.getPath())) {
                    f.delete();
                }
            }
        }
//        activity.finish();
    }


    private List<Appinfo> getVideoShareApps(Activity context) {
        List<Appinfo> appinfos = new ArrayList<Appinfo>();
        PackageManager pManager = context.getPackageManager();
        Appinfo info = new Appinfo();
        info.icon = context.getApplicationInfo().loadIcon(pManager);
        info.laucherClassname = EditVideoActivity.class.getName();
        info.pkgName = context.getApplication().getPackageName();
        info.title = context.getString(R.string.share_title);
        appinfos.add(info);
        List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("video/*");
        //      intent.setType("*/*");

        mApps = pManager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        if (mApps != null && mApps.size() > 0) {
            for (ResolveInfo resolveInfo : mApps) {
                Appinfo appinfo = new Appinfo();
                appinfo.pkgName = resolveInfo.activityInfo.packageName;
//              showLog_I(TAG, "pkg>" + resolveInfo.activityInfo.packageName + ";name>" + resolveInfo.activityInfo.name);
                appinfo.laucherClassname = resolveInfo.activityInfo.name;
                appinfo.title = resolveInfo.loadLabel(pManager).toString();
                appinfo.icon = resolveInfo.loadIcon(pManager);
                if (!appinfo.pkgName.equals(context.getApplication().getPackageName())) {
                    appinfos.add(appinfo);
                } else {
                    appinfos.add(0, appinfo);
                }
            }
        }
        return appinfos;
    }

    private List<Appinfo> getPhotoShareApps(Activity context) {
        List<Appinfo> appinfos = new ArrayList<Appinfo>();
        PackageManager pManager = context.getPackageManager();
        List<ResolveInfo> mApps;
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("image/*");
        //      intent.setType("*/*");

        mApps = pManager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        if (mApps != null && mApps.size() > 0) {
            for (ResolveInfo resolveInfo : mApps) {
                Appinfo appinfo = new Appinfo();
                appinfo.pkgName = resolveInfo.activityInfo.packageName;
//              showLog_I(TAG, "pkg>" + resolveInfo.activityInfo.packageName + ";name>" + resolveInfo.activityInfo.name);
                appinfo.laucherClassname = resolveInfo.activityInfo.name;
                appinfo.title = resolveInfo.loadLabel(pManager).toString();
                appinfo.icon = resolveInfo.loadIcon(pManager);
                if (!appinfo.pkgName.equals(context.getApplication().getPackageName())) {
                    appinfos.add(appinfo);
                } else {
                    appinfos.add(0, appinfo);
                }
            }
        }
        return appinfos;
    }

    public class Appinfo {
        public static final int WECHART = 0;
        public static final int WECHARTMOMENTS = 1;
        public static final int QQ = 2;
        public static final int SINNAWEIBO = 3;
        public static final int RIDERS = 6;
        public int type = 0;
        public Drawable icon;
        String pkgName;
        public String title;
        String laucherClassname;
    }

    public interface OnItemClickListener {
        void onItemClick(Appinfo appinfo, int position);
    }

    class DialogListAdapter extends RecyclerView.Adapter<DialogListAdapter.MyViewHolder> {
        private List<Appinfo> infos;
        private OnItemClickListener mOnItemClickListener;
        private boolean isGrid = false;
        private Activity mActivity;

        public DialogListAdapter(Activity activity, List<Appinfo> appinfos) {
            mActivity = activity;
            infos = appinfos;
        }

        public DialogListAdapter(Activity activity, List<Appinfo> appinfos, boolean isGrid) {
            mActivity = activity;
            infos = appinfos;
            this.isGrid = isGrid;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
        }

        @Override
        public DialogListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layout = R.layout.item_dialog_list;
            if (isGrid) {
                layout = R.layout.item_dialog_grid;
            }
//            View itemView = View.inflate(parent.getContext(), layout, null);
            View itemView = LayoutInflater.from(mActivity).inflate(layout, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DialogListAdapter.MyViewHolder holder, int position) {
            holder.icon.setBackgroundDrawable(infos.get(position).icon);
            holder.title.setText(infos.get(position).title);
            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(infos.get(holder.getLayoutPosition()), holder.getLayoutPosition());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return infos == null ? 0 : infos.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;

            MyViewHolder(View itemView) {
                super(itemView);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                title = (TextView) itemView.findViewById(R.id.title);
            }
        }
    }


    /**
     * 把网络资源图片转化成bitmap
     *
     * @param url 网络资源图片
     * @return Bitmap
     */
    public static Bitmap GetLocalOrNetBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

}
