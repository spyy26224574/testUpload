package com.adai.gkdnavi.utils.imageloader;


import android.widget.ImageView;


public class ImageLoaderParameter {
    private String url; //url
    private int placeHolder; //占位图
    private ImageView imgView; //ImageView的实例
    private ImageLoaderParameter(Builder builder) {
        this.url = builder.url;
        this.placeHolder = builder.placeHolder;
        this.imgView = builder.imgView;
    }
    public String getUrl() {
        return url;
    }

    public int getPlaceHolder() {
        return placeHolder;
    }

    public ImageView getImgView() {
        return imgView;
    }

    public static class Builder {
        private String url;
        private int placeHolder;
        private ImageView imgView;

        public Builder() {
            this.url = "";
            this.placeHolder = -1;
            this.imgView = null;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder placeHolder(int placeHolder) {
            this.placeHolder = placeHolder;
            return this;
        }

        public Builder imgView(ImageView imgView) {
            this.imgView = imgView;
            return this;
        }

        public ImageLoaderParameter build() {
            return new ImageLoaderParameter(this);
        }

    }
}
