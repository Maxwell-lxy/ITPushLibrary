package com.innotech.innotechpush.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 网络请求类
 */

public class NetWorkUtils {
    //    public static final String HOST = "139.196.71.160:1323";
//    private static final String BASE_URL = "http://" + HOST + "/v1/pushaksk";
//    public static final String URL_UPDATEUSERINFO = BASE_URL + "/updateuserinfo";
//    public static final String URL_ALIAS = BASE_URL + "/userbindalias";
//    public static final String PATH_UPDATEUSERINFO = "/v1/pushaksk/updateuserinfo";
//    public static final String PATH_ALIAS = "/v1/pushaksk/userbindalias";
    //星星的测试环境
    public static final String HOST = "139.196.3.78:8081";
    private static final String BASE_URL = "http://" + HOST;
    public static final String URL_UPDATEUSERINFO = BASE_URL + "/update-user-info";
    public static final String URL_ALIAS = BASE_URL + "/user-bind-alias";
    public static final String PATH_UPDATEUSERINFO = "/update-user-info";
    public static final String PATH_ALIAS = "/user-bind-alias";
    private static int CONNECT_TIMEOUT = 5000;

    public static void sendGetRequest(final String urlStr) {
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    //下面对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.i("push_net", "sendGetRequest() result:" + response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void sendPostRequest(final Context context, final String urlStr, final String paramsStr, final String sign) {
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    LogUtils.e(context, "sendPostRequest() url:" + urlStr);
                    URL url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    if (sign != null) {
                        connection.setRequestProperty("Authorization", sign);
                    }
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(CONNECT_TIMEOUT);
                    connection.setReadTimeout(CONNECT_TIMEOUT);
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Charset", "UTF-8");
                    // 设置文件类型:
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    // 设置接收类型否则返回415错误
                    //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
                    connection.setRequestProperty("accept", "application/json");
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes(paramsStr);
                    InputStream in = connection.getInputStream();
                    //下面对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    LogUtils.e(context, "sendPostRequest() response:" + response.toString());
                    SaveData.saveData(context, response.toString(), urlStr);
                } catch (Exception e) {
                    LogUtils.e(context, "sendPostRequest方法出现异常 Exception:" + e.getMessage() + " e.toString():" + e.toString());
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            LogUtils.e(context, "BufferedReader关闭出现异常 Exception:" + e.getMessage() + " e.toString():" + e.toString());
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

}
