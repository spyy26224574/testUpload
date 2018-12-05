package com.adai.gkdnavi.utils.imageloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class GlideImageLoaderProvider extends BaseImageLoaderProvider {

    @Override
    public void loadImage(Context context, ImageLoaderParameter img) {
        loadNormal(context, img);
    }


    @Override
    public void loadRoundImage(Context context, ImageLoaderParameter img) {
        if (checkContext(context)) return;
        DrawableRequestBuilder<String> stringDrawableRequestBuilder = Glide.with(context).load(img.getUrl()).transform(new CircleTransform(context)).dontAnimate();
        if (img.getPlaceHolder() == -1) {
            stringDrawableRequestBuilder.into(img.getImgView());
        } else {
            stringDrawableRequestBuilder.placeholder(img.getPlaceHolder()).into(img.getImgView());
        }
    }

    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform,
                                   int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null)
                return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size,
                        Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName();
        }
    }

    private void loadNormal(Context context, ImageLoaderParameter img) {
        if (checkContext(context)) return;
        DrawableRequestBuilder<String> stringDrawableRequestBuilder = Glide.with(context).load(img.getUrl()).dontAnimate();
        if (img.getPlaceHolder() == -1) {
            stringDrawableRequestBuilder.into(img.getImgView());
        } else {
            stringDrawableRequestBuilder.placeholder(img.getPlaceHolder()).into(img.getImgView());
        }
    }

    private boolean checkContext(Context context) {
        if (context == null) {
            return true;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing()) {
                return true;
            }
        }
        return false;
    }
}
