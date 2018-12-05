package com.adai.camera.hisi.net;

/**
 * 
 * Http Get方法的返回结果（包含状态码和返回文本内容）
 * 
 */

public class HttpResult {
	public int statusCode;   //HTTP响应状态码，如200，404； 为-1时表示异常
	public String content;   //HTTP实体内容； 当状态码为-1时，此处表示异常信息
	
	public static final int HTTP_OK = 200;  //HTTP状态码200,表服务器成功响应
    public static final int HTTP_EXCEPTION = -1; /**执行中发生异常，无响应*/
}
