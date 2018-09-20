package com.innotech.innotechpush.sdk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.callback.SocketSendCallback;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.db.ClientLog;
import com.innotech.innotechpush.utils.CommonUtils;
import com.innotech.innotechpush.utils.DataAnalysis;
import com.innotech.innotechpush.utils.FileUtils;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.NetWorkUtils;
import com.innotech.innotechpush.utils.SignUtils;
import com.innotech.innotechpush.utils.TokenUtils;
import com.innotech.innotechpush.utils.UserInfoSPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

public class SocketManager {
    private final static String TAG = "socket-library";
    private static SocketManager instance;
    private static Context context;
    private Socket mSocket;
    private Thread thread;
    //长连接发送数据用的线程
    private Thread sendThread;
    private Runnable sendRunnable;

    public static SocketManager getInstance(Context ct) {
        if (instance == null) {
            synchronized (SocketManager.class) {
                if (instance == null) {
                    context = ct;
                    instance = new SocketManager();
                }
            }
        }
        return instance;
    }

    private SocketManager() {

    }

    /**
     * 初始化长连接
     */
    public synchronized void initSocket() {
        if (mSocket == null || mSocket.isClosed()) {
            try {
                getSocketAddr(new RequestCallback() {
                    @Override
                    public void onSuccess(String hostAndPort) {
                        String[] array = hostAndPort.split(":");
                        try {
                            connectWithHostAndPort(array[0].trim(), Integer.parseInt(array[1].trim()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(String msg) {
                        LogUtils.e(context, msg);
                        new ClientLog(context, LogCode.LOG_DATA_API, "获取长连接地址失败").save();
                        try {
                            Thread.sleep(5000);
                            reConnect();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                LogUtils.e(context, "获取socket信息json参数有误");
                new ClientLog(context, LogCode.LOG_EX_JSON, "获取socket信息json参数有误").save();
            }
        }
    }

    /**
     * 获取长连接地址信息
     */
    private void getSocketAddr(RequestCallback callback) throws JSONException {
        Integer appId = CommonUtils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
        String appKey = CommonUtils.getMetaDataString(context, PushConstant.INNOTECH_APP_KEY);
        if (appId == null || TextUtils.isEmpty(appKey)) {
            LogUtils.e(context, "INNOTECH_APP_ID或INNOTECH_APP_KEY配置有误");
            new ClientLog(context, LogCode.LOG_INIT, "INNOTECH_APP_ID或INNOTECH_APP_KEY配置有误").save();
            return;
        }
        String guid = TokenUtils.getGuid(context);
        if (TextUtils.isEmpty(guid)) {
            LogUtils.e(context, "guid不能为空");
            new ClientLog(context, LogCode.LOG_INIT, "guid不能为空").save();
            return;
        }
        JSONObject object = new JSONObject();
        object.put("app_id", appId);
        object.put("app_key", appKey);
        object.put("guid", guid);
        String json = object.toString();
        String sign = SignUtils.sign("POST", NetWorkUtils.PATH_SOCKET_ADDR, json);
        NetWorkUtils.sendPostRequest(context, NetWorkUtils.URL_SOCKET_ADDR, json, sign, callback);
    }

    /**
     * 建立长连接
     */
    private void connectWithHostAndPort(final String host, final int port) throws JSONException {
        if (!TextUtils.isEmpty(host) && port != 0) {
            final Integer appId = CommonUtils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
            final String appKey = CommonUtils.getMetaDataString(context, PushConstant.INNOTECH_APP_KEY);
            if (appId == null || TextUtils.isEmpty(appKey)) {
                LogUtils.e(context, "INNOTECH_APP_ID或INNOTECH_APP_KEY配置有误");
                new ClientLog(context, LogCode.LOG_INIT, "INNOTECH_APP_ID或INNOTECH_APP_KEY配置有误").save();
                return;
            }
            final String guid = TokenUtils.getGuid(context);
            if (TextUtils.isEmpty(guid)) {
                LogUtils.e(context, "guid不能为空");
                new ClientLog(context, LogCode.LOG_INIT, "guid不能为空").save();
                return;
            }
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mSocket = new Socket(host, port);
                        LogUtils.e(context, "与服务器(" + host + ":" + port + ")连接成功");
                        new ClientLog(context, LogCode.LOG_INIT, "与服务器(" + host + ":" + port + ")连接成功").save();
                        if (isConnecting()) {
                            //登录
                            loginCmd(guid, appId, appKey);
                            readData();
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                        Log.e(TAG, "SocketException异常：" + e.getMessage());
                        try {
                            Thread.sleep(5000);
                            reConnect();
                        } catch (InterruptedException e1) {
                            e.printStackTrace();
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        Log.e(TAG, "UnknownHostException异常：" + e.getMessage());
                        try {
                            Thread.sleep(5000);
                            reConnect();
                        } catch (InterruptedException e1) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "IOException异常：" + e.getMessage());
                        try {
                            Thread.sleep(5000);
                            reConnect();
                        } catch (InterruptedException e1) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    }

    /**
     * 读取数据
     */
    private void readData() throws IOException {
        InputStream is = mSocket.getInputStream();
        byte[] lenB = new byte[16];
        while (is.read(lenB) != -1) {
            int len = getLenByData(lenB);
            int command = getCommandByData(lenB);
            switch (command) {
                case 1://登录成功（LoginRespCmd）
                    LogUtils.e(context, "登录成功");
                    new ClientLog(context, LogCode.LOG_INIT, "登录成功").save();
                    try {
                        String jsonA = getJsonByData(is, len);
                        if (!TextUtils.isEmpty(jsonA) && !"null".equals(jsonA)) {
                            ArrayList<String> list = new ArrayList<String>();
                            JSONArray array = new JSONArray(jsonA);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                PushMessage pushMessage = (PushMessage) DataAnalysis.jsonToT(PushMessage.class.getName(), object.toString());
                                if (pushMessage != null) {
                                    pushMessage.setOffLineMsg(true);
                                    PushMessageManager.getInstance(context).setNewMessage(pushMessage);
                                    list.add(pushMessage.getMsg_id());
                                }
                            }
                            if (list.size() > 0) {
                                if (CommonUtils.isXiaomiDevice()
                                        || CommonUtils.isMIUI()
                                        || CommonUtils.isMeizuDevice()) {
                                    ackCmd(list, 101);
                                } else {
                                    ackCmd(list, 1001);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4://服务器推送消息（ForwardCmd）
                    LogUtils.e(context, "收到服务器推送消息");
                    new ClientLog(context, LogCode.LOG_DATA_COMMON, "收到服务器推送消息").save();
                    //处理推送消息
                    String jsonO = getJsonByData(is, len);
                    if (!TextUtils.isEmpty(jsonO) && !"null".equals(jsonO)) {
                        PushMessage pushMessage = (PushMessage) DataAnalysis.jsonToT(PushMessage.class.getName(), jsonO);
                        if (pushMessage != null) {
                            PushMessageManager.getInstance(context).setNewMessage(pushMessage);
                            ArrayList<String> list = new ArrayList<String>();
                            list.add(pushMessage.getMsg_id());
                            if (CommonUtils.isXiaomiDevice()
                                    || CommonUtils.isMIUI()
                                    || CommonUtils.isMeizuDevice()) {
                                ackCmd(list, 101);
                            } else {
                                ackCmd(list, 1);
                            }
                        }
                    }
                    break;
                case 7://ack回值成功（AckRespCmd）
                    getJsonByData(is, len);
                    Log.e(TAG, "ack回值成功");
                    break;
                case 10://心跳回包（HeartBeatRespCmd）
                    Log.e(TAG, "心跳回包成功");
                    break;
                default:
                    break;
            }
        }
        //while循环中阻塞读，当服务器断开连接后，读操作就会返回-1，从而跳出循环执行后续的代码。
        //服务器断开了，需要重连
        Log.e(TAG, "服务器断开了，正在尝试重连...");
        try {
            Thread.sleep(5000);
            reConnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否连接中
     */
    private boolean isConnecting() {
        if (mSocket != null && mSocket.isConnected() && !mSocket.isClosed() && !mSocket.isInputShutdown()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 客户端断掉了的重连
     * isConnecting()只能判断客户端是否处于正常连接状态
     */
    public synchronized void reConnect() {
        sendData("", 9, new SocketSendCallback() {
            @Override
            public void onResult(boolean result) {
                if (!result) {
                    try {
                        if (mSocket != null) {
                            mSocket.close();
                        }
                        SocketManager.getInstance(context).initSocket();
                        LogUtils.e(context, "正在重连...");
                        new ClientLog(context, LogCode.LOG_DATA_COMMON, "正在重连...").save();
                    } catch (IOException e) {
                        LogUtils.e(context, "socket close异常...");
                        new ClientLog(context, LogCode.LOG_EX_IO, "socket close异常...").save();
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 登录
     */
    public void loginCmd(String guid, int appID, String appKey) {
        try {
            JSONObject object = new JSONObject();
            object.put("guid", guid);
            object.put("app_id", appID);
            object.put("app_key", appKey);
            sendData(object.toString(), 0);
            LogUtils.e(context, "发送登录指令：" + object.toString());
            new ClientLog(context, LogCode.LOG_DATA_COMMON, "发送登录指令：" + object.toString()).save();
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(context, "发送登录指令时，出现异常。");
            new ClientLog(context, LogCode.LOG_EX_JSON, "发送登录指令时，出现异常。").save();
        }
    }

    /**
     * 发送心跳信息
     */
    public void sendHeartData() {
        sendData("", 9);
        LogUtils.e(context, "发送心跳指令");
        new ClientLog(context, LogCode.LOG_DATA_COMMON, "发送心跳指令").save();
    }

    /**
     * 收到推送信息后向服务端发送回值
     *
     * @param type：1、到达 2、展示 3、点击
     */
    public void ackCmd(ArrayList<String> msgList, int type) {
        try {
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            for (String msgID : msgList) {
                array.put(msgID);
            }
            object.put("msg_ids", array);
            object.put("type", type);
            sendData(object.toString(), 6);
            LogUtils.e(context, "发送ack指令：" + object.toString());
            //客户端回执
            JSONArray paramArray = new JSONArray();
            paramArray.put(object);
            InnotechPushMethod.clientMsgNotify(context, paramArray, 0);
            new ClientLog(context, LogCode.LOG_DATA_COMMON, "发送ack指令：" + object.toString()).save();
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(context, "发送ack指令时，出现异常。");
            new ClientLog(context, LogCode.LOG_EX_JSON, "发送ack指令时，出现异常。").save();
        }
    }

    /**
     * 向服务端发消息
     */
    public void sendData(String json, int cmd) {
        sendData(json, cmd, null);
    }

    public void sendData(final String json, final int cmd, final SocketSendCallback callback) {
        if (isConnecting()) {
            LogUtils.e(context, "socket状态为：连接中...");
            new ClientLog(context, LogCode.LOG_DATA_COMMON, "socket状态为：连接中...").save();
            sendRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
                        //4字节剩余包长度
                        byte[] len = CommonUtils.big_intToByte(!TextUtils.isEmpty(json) ? json.length() + 12 : 12, 4);
                        byte[] requestsID = getRequestID();
                        byte[] command = CommonUtils.big_intToByte(cmd, 4);
                        byte[] jsonb = json.getBytes();
                        byte[] data = new byte[len.length + requestsID.length + command.length + jsonb.length];
                        System.arraycopy(len, 0, data, 0, len.length);
                        System.arraycopy(requestsID, 0, data, len.length, requestsID.length);
                        System.arraycopy(command, 0, data, len.length + requestsID.length, command.length);
                        System.arraycopy(jsonb, 0, data, len.length + requestsID.length + command.length, jsonb.length);
                        dos.write(data);
                        if (callback != null) {
                            callback.onResult(true);
                        }
                    } catch (IOException e) {
                        LogUtils.e(context, "socket发送信息失败...");
                        if (callback != null) {
                            callback.onResult(false);
                        } else {
                            reConnect();
                        }
                        e.printStackTrace();
                        new ClientLog(context, LogCode.LOG_EX_IO, "socket发送信息失败...").save();
                    }
                }
            };
            sendThread = new Thread(sendRunnable);
            sendThread.start();
        } else {
            LogUtils.e(context, "socket状态为：已断开连接...");
            if (callback != null) {
                callback.onResult(false);
            } else {
                reConnect();
            }
            new ClientLog(context, LogCode.LOG_DATA_COMMON, "socket状态为：已断开连接...").save();
        }
    }

    /**
     * 生成8字节随机数
     *
     * @return
     */
    private byte[] getRequestID() {
        byte[] b = new byte[8];
        Random random = new Random();
        random.nextBytes(b);
        return b;
    }

    /**
     * 获取服务端回包的信息
     * 4字节剩余包长
     */
    private int getLenByData(byte[] data) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = data[i];
        }
        return CommonUtils.big_bytesToInt(bytes);
    }

    /**
     * 获取服务端回包的信息
     * 8字节requestID
     */
    private byte[] getRequestIDByData(char[] data) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) data[i + 4];
        }
        return bytes;
    }

    /**
     * 获取服务端回包的信息
     * 3字节命令的值
     */
    private int getCommandByData(byte[] data) {
        byte[] bytes = new byte[4];
        bytes[0] = 0;
        for (int i = 1; i < 4; i++) {
            bytes[i] = data[i + 12];
        }
        return CommonUtils.big_bytesToInt(bytes);
    }

    /**
     * 获取服务端回包的信息
     * json数据
     */
    private String getJsonByData(InputStream is, int len) {
        String json = null;
        byte[] lenJ = new byte[len - 12];
        try {
            if (is.read(lenJ) != -1) {
                json = new String(lenJ);
                Log.e(TAG, "收到的数据：" + json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

}