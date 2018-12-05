package com.adai.gkdnavi.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/10 10:54
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class GsonUtils {
    public static <T> T json2Bean(String json, Class<T> clazz) {
        T t = null;
        Gson gson = new Gson();
        try {
            t = gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return t;
    }
//
//    public static String bean2Json(Class<T> clazz) {
//        Gson gson = new Gson();
//        return gson.toJson();
//    }
}
