package com.filepicker.utils;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.filepicker.cursors.DocScannerTask;
import com.filepicker.cursors.loadercallbacks.FileResultCallback;
import com.filepicker.cursors.loadercallbacks.PhotoDirLoaderCallbacks;
import com.filepicker.models.Document;
import com.filepicker.models.PhotoDirectory;


public class MediaStoreHelper {

  public static void getPhotoDirs(FragmentActivity activity, Bundle args, FileResultCallback<PhotoDirectory> resultCallback) {
    activity.getSupportLoaderManager()
        .initLoader(0, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
  }

  public static void getDocs(FragmentActivity activity, FileResultCallback<Document> fileResultCallback)
  {
    new DocScannerTask(activity,fileResultCallback).execute();
  }
}