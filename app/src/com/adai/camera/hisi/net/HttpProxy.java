package com.adai.camera.hisi.net;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.adai.camera.hisi.sdk.Common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;


/**
 * 一个简单HTTP代理类，封装Http连接操作
 */

public class HttpProxy {

    private static final String TAG = "HttpProxy";
    private static HttpClient httpClient = null;

    static {
        httpClient = AndroidHttpClient.newInstance("HiCamera");
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 4000);
    }


    /**
     * 用GET方法获取HTTP响应结果
     *
     * @param url 完整的HTTP请求地址
     * @return 返回HttpResult对象（状态码和实体内容）
     * @throws IOException
     */
    public static HttpResult doHttpGetForContent(String url) {
        HttpResult result = new HttpResult();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = null;
        HttpEntity entity = null;

        try {
            Log.i(TAG, url);

            //执行Http Get请求
            response = httpClient.execute(httpget);
            entity = response.getEntity();

            //取得状态码和内容
            result.statusCode = response.getStatusLine().getStatusCode();
            result.content = EntityUtils.toString(entity);

            Log.i(TAG, result.content);
            entity.consumeContent();

        } catch (Exception e) {
            result.statusCode = HttpResult.HTTP_EXCEPTION;
            result.content = e.toString();
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 给定一个完整的url，执行HTTP请求，检测是否执行成功
     *
     * @param url
     * @return 成功返回SUCCESS(0), 失败返回FAILURE(-1)sss
     */
    public static int doForSuccess(String url) {
        //执行HTTP请求操作
        HttpResult httpResult = doHttpGetForContent(url);

        /**检测执行结果是否为成功*/
        if (httpResult.statusCode == HttpResult.HTTP_OK) {
            return Common.SUCCESS;
        } else {
            return Common.FAILURE;
        }
    }

    /**
     * 前提条件：获取开关类设置项的值时，HTTP Server返回格式为 enable="1" 或 enable="0"
     * 功能：给定一个完整的url，执行HTTP请求，检测结果然后返回Boolean对象
     *
     * @param url
     * @return enable="1"时返回Boolean.TRUE,enable="1"返回Boolean.FALSE,失败时返回null
     */
    public static Boolean doForBoolean(String url) {
        String value = doForStringByKey(url, "enable");

        if (null == value) {
            return null;
        }

        if (value.equals("1")) {
            return Boolean.TRUE;
        } else if (value.equals("0")) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    /**
     * 获取String,String键值对，
     *
     * @param url
     * @return 获取键值对数量大于0 返回SUCCESS；否则返回FAILURE(-1)
     */
    public static int doForMap(String url, Map<String, String> map) {
        //执行HTTP请求操作
        HttpResult httpResult = doHttpGetForContent(url);

        //DV未成功执行或网络连接发生异常
        if (httpResult.statusCode != HttpResult.HTTP_OK) {
            return Common.FAILURE;
        }

        //解析字符串到map
        int count = StringParser.getKeyValueMap(httpResult.content, map);

        if (count <= 0) {
            return Common.FAILURE;
        }

        return Common.SUCCESS;
    }

    /**
     * 前提条件：HTTP Server只返回一个键值对，数字，正常情况下大于等于0
     *
     * @param url
     * @param key 键
     * @return 成功返回大于等于0的整数，失败返回Results.FAILURE(-1)
     */
    public static int doForIntByKey(String url, String key) {
        String strValue = doForStringByKey(url, key);

        if (null == strValue) {
            return Common.FAILURE;
        }

        int value;
        try {
            value = Integer.parseInt(strValue);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Common.FAILURE;
        }

        if (value < 0) {
            return Common.FAILURE;
        }

        return value;
    }

    /**
     * 前提条件：HTTP Server只返回一个键值对
     * 根据url,键,执行HTTP请求，从返回结果获取其String值
     *
     * @param url
     * @param key 键
     * @return 成功返回String，失败返回null
     */
    public static String doForStringByKey(String url, String key) {
        HttpResult httpResult = doHttpGetForContent(url);

        if (httpResult.statusCode != HttpResult.HTTP_OK) {
            return null;
        }

        TreeMap<String, String> tempMap = new TreeMap<String, String>();

        StringParser.getKeyValueMap(httpResult.content, tempMap);

        return tempMap.get(key);
    }
}
