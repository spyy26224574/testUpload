package com.adai.gkdnavi.utils;

public class ThreadPoolManager {

	private static ThreadPoolManager instance;
	private ThreadPoolProxy mLongPool;
	private ThreadPoolProxy mDownloadPool;

	private ThreadPoolManager() {
		mLongPool = new ThreadPoolProxy(1, 1, 0);
		mDownloadPool = new ThreadPoolProxy(1, 1, 0);
	}

	public static ThreadPoolManager getInstance() {
		if (instance == null) {
			synchronized (ThreadPoolManager.class) {
				if (instance == null) {
					instance = new ThreadPoolManager();
				}
			}
		}
		return instance;
	}

	public ThreadPoolProxy getLongPool() {
		return mLongPool;
	}

	public ThreadPoolProxy getDownloadPool() {
		return mDownloadPool;
	}

}
