package com.filepicker.cursors.loadercallbacks;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.filepicker.FilePickerConst;
import com.filepicker.cursors.PhotoDirectoryLoader;
import com.filepicker.models.PhotoDirectory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.TITLE;

public class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    public final static int INDEX_ALL_PHOTOS = 0;
    private WeakReference<Context> context;
    private FileResultCallback<PhotoDirectory> resultCallback;

    public PhotoDirLoaderCallbacks(Context context, FileResultCallback<PhotoDirectory> resultCallback) {
      this.context = new WeakReference<>(context);
      this.resultCallback = resultCallback;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      return new PhotoDirectoryLoader(context.get(), args.getBoolean(FilePickerConst.EXTRA_SHOW_GIF, false));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

      if (data == null)  return;
      List<PhotoDirectory> directories = new ArrayList<>();

      while (data.moveToNext()) {

        int imageId  = data.getInt(data.getColumnIndexOrThrow(_ID));
        String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
        String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
        String path = data.getString(data.getColumnIndexOrThrow(DATA));
          String fileName  = data.getString(data.getColumnIndexOrThrow(TITLE));

        PhotoDirectory photoDirectory = new PhotoDirectory();
        photoDirectory.setId(bucketId);
        photoDirectory.setName(name);

        if (!directories.contains(photoDirectory)) {
          photoDirectory.setCoverPath(path);
          photoDirectory.addPhoto(imageId,fileName, path);
          photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
          directories.add(photoDirectory);
        } else {
          directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, fileName, path);
        }

      }

      if (resultCallback != null) {
        resultCallback.onResultCallback(directories);
      }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
  }