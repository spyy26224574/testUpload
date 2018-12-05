package com.adai.gkdnavi.square;

import com.example.ipcamera.application.VLCApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/15 14:14
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class CacheManager {
    private static CacheManager mInstance;

    public static CacheManager getInstance() {
        if (mInstance == null) {
            mInstance = new CacheManager();
        }
        return mInstance;
    }

    /**
     * 将对象存到本地
     *
     * @param path
     * @param object
     * @param <T>
     * @return
     */
    public <T> boolean wirteObject2File(String path, T object) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (oos != null) {
                try {
                    oos.flush();
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return true;
    }

    /**
     * 将本地文件转换成对象
     *
     * @param path
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T readFile2Object(String path, Class<T> clazz) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public <T> boolean setCache(String fileName, T object) {
        return wirteObject2File(VLCApplication.CACHE + fileName, object);
    }


    public <T> T getCache(String fileName, Class<T> clazz) {
        return readFile2Object(VLCApplication.CACHE + fileName, clazz);
    }
}
