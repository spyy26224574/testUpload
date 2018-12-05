package com.adai.gkdnavi;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebviewActivity extends BaseActivity {
    /**
     * 标题
     */
    public static final String KEY_TITLE="title";
    /**
     * 网址路径
     */
    public static final String KEY_URL="url";
    private ProgressBar progressbar;
    private WebView webview;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        this.webview = (WebView) findViewById(R.id.webview);
        this.progressbar = (ProgressBar) findViewById(R.id.progressbar);
    }

    @Override
    protected void init() {
        super.init();
        initWebview();
        mTitle=getIntent().getStringExtra(KEY_TITLE);
        setTitle(TextUtils.isEmpty(mTitle)?"":mTitle);
        String url=getIntent().getStringExtra(KEY_URL);
        webview.loadUrl(url);
    }

    private void initWebview(){
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDatabaseEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setUseWideViewPort(true);
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if(TextUtils.isEmpty(mTitle)){
                    mTitle=title;
                    setTitle(mTitle);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                    progressbar.setProgress(newProgress);
//                if(newProgress<100) {
//                progressbar.setVisibility(View.VISIBLE);
//                }else{
//                    progressbar.setVisibility(View.GONE);
//                }
            }

        });
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressbar.setVisibility(View.GONE);
            }
        });
    }
}
