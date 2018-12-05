package com.adai.gkdnavi.utils.imageloader;

import android.content.Context;

public abstract class BaseImageLoaderProvider {
	public abstract void loadImage(Context context,ImageLoaderParameter img);
	public void loadRoundImage(Context context,ImageLoaderParameter img){}
}
