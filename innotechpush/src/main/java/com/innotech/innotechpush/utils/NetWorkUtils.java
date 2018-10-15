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
    //正式环境
    public static final String HOST = "gw.d.ywopt.com";
    //测试环境
//    public static final String HOST = "gw.t.ywopt.com";
    private static final String BASE_URL = "https://" + HOST + "/v1/pushaksk";
    public static final String URL_UPDATEUSERINFO = BASE_URL + "/updateuserinfo";
    public static final String URL_ALIAS = BASE_URL + "/userbindalias";
    public static final String URL_SOCKET_ADDR = BASE_URL + "/socketaddr";
    public static final String URL_CLIENT_MSG_NOTIFY = BASE_URL + "/clientmsgnotify";
    public static final String URL_CLIENT_LOG = BASE_URL + "/clientlog";
    public static final String PATH_UPDATEUSERINFO = "/v1/pushaksk/updateuserinfo";
    public static final String PATH_ALIAS = "/v1/pushaksk/userbindalias";
    public static final String PATH_SOCKET_ADDR = "/v1/pushaksk/socketaddr";
    public static final String PATH_CLIENT_MSG_NOTIFY = "/v1/pushaksk/clientmsgnotify";
    public static final String PATH_CLIENT_LOG = "/v1/pushaksk/clientlog";
    private static int CONNECT_TIMEOUT = 5000;

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
                    // 设置文件类型:
                    connection.setRequestProperty("Content-Type", "application/json");
                    // 设置接收类型否则返回415错误
                    //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
//                    connection.setRequestProperty("accept", "application/json");
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
                    e.printStackTrace();
                    if (mCallBack != null) {
                        mCallBack.onFail("sendPostRequest方法出现异常 Exception:" + e.getMessage());
                    }
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
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
