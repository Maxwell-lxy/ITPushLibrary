package com.innotech.innotechpush.utils;

import android.content.Context;

import com.innotech.innotechpush.callback.RequestCallback;

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
    private static int CONNECT_TIMEOUT = 5000;
    //正式环境
//    public static final String HOST = "gw.d.ywopt.com";
//    public static final String HOST_LOG = "push.l.ywopt.com";
    //测试环境
    public static final String HOST = "gw.t.ywopt.com";
    public static final String HOST_LOG = "139.224.168.192:8081";
    /**
     * 用户上报信息
     */
    public static final String PATH_UPDATEUSERINFO = "/v1/pushaksk/updateuserinfo";
    public static final String URL_UPDATEUSERINFO = "https://" + HOST + PATH_UPDATEUSERINFO;
    /**
     * 设置别名
     */
    public static final String PATH_ALIAS = "/v1/pushaksk/userbindalias";
    public static final String URL_ALIAS = "https://" + HOST + PATH_ALIAS;
    /**
     * 获取长连接地址
     */
    public static final String PATH_SOCKET_ADDR = "/v1/pushaksk/socketaddr";
    public static final String URL_SOCKET_ADDR = "https://" + HOST + PATH_SOCKET_ADDR;
    /**
     * 短连接回值，用于华为通知点击时触发
     */
    public static final String PATH_CLIENT_MSG_NOTIFY = "/v1/pushaksk/clientmsgnotify";
    public static final String URL_CLIENT_MSG_NOTIFY = "https://" + HOST + PATH_CLIENT_MSG_NOTIFY;
    /**
     * log server日志
     */
    public static final String PATH_LOG = "/log";
    public static final String URL_LOG = "http://" + HOST_LOG + PATH_LOG;
//    public static final String URL_LOG = "https://" + HOST_LOG + PATH_LOG;

    public synchronized static void sendPostRequest(final Context context, final String urlStr, final String paramsStr, final String sign, final RequestCallback mCallBack) {
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    LogUtils.e(context, "sendPostRequest() url:" + urlStr + " sign:" + sign + " paramsStr:" + paramsStr);
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
                    connection.setRequestProperty("Content-Type", "application/json");
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.write(paramsStr.getBytes());
                    InputStream in = connection.getInputStream();
                    //下面对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    LogUtils.e(context, "sendPostRequest() response:" + response.toString());
                    SaveData.saveData(context, response.toString(), urlStr, mCallBack);
                } catch (Exception e) {
                    LogUtils.e(context, "sendPostRequest方法出现异常 Exception:" + e.getMessage() + " e.toString():" + e.toString());
                    if (mCallBack != null) {
                        mCallBack.onFail("sendPostRequest方法出现异常 Exception:" + e.getMessage());
                    }
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            LogUtils.e(context, "BufferedReader关闭出现异常 Exception:" + e.getMessage() + " e.toString():" + e.toString());
                            if (mCallBack != null) {
                                mCallBack.onFail("BufferedReader关闭出现异常 Exception:" + e.getMessage());
                            }
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
