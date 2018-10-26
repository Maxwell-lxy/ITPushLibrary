package com.innotech.innotechpush.sdk;

import android.content.Context;
import android.text.TextUtils;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.db.DbUtils;
import com.innotech.innotechpush.utils.CommonUtils;
import com.innotech.innotechpush.utils.DataAnalysis;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.NetWorkUtils;
import com.innotech.innotechpush.utils.SignUtils;
import com.innotech.innotechpush.utils.TokenUtils;
import com.innotech.innotechpush.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class SocketManager {
    private static SocketManager instance;
    private static Context context;
    private Socket mSocket;
    //socket输入流
    private InputStream mInputStream;
    //socket输出流
    private DataOutputStream mDataOutputStream;
    //read线程
    private Thread readThread;
    //write线程
    private Thread writeThread;
    //重连时，使用SynchronousQueue起到锁同步的效果
    private ArrayBlockingQueue<Integer> reConnectQueue;

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
        reConnectQueue = new ArrayBlockingQueue<>(1);
    }

    /**
     * 初始化长连接
     */
    public void initSocket() {
        try {
            boolean isSuccess = getSocketAddr(new RequestCallback() {
                @Override
                public void onSuccess(String hostAndPort) {
                    String[] array = hostAndPort.split(":");
                    try {
                        connectWithHostAndPort(array[0].trim(), Integer.parseInt(array[1].trim()));
                    } catch (JSONException e) {
                        takeRCQueue();
                        LogUtils.e(context, "建立socket时json参数有误");
                        DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, "建立socket时json参数有误");
                    }
                }

                @Override
                public void onFail(String msg) {
                    LogUtils.e(context, msg);
                    takeRCQueue();
                    try {
                        Thread.sleep(5000);
                        reConnect();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DbUtils.addClientLog(context, LogCode.LOG_DATA_API, "获取长连接地址失败：" + msg);
                }
            });
            if (!isSuccess) {//没有执行到网络请求就返回时，如果队列有值，需要消费掉
                takeRCQueue();
            }
        } catch (JSONException e) {
            takeRCQueue();
            LogUtils.e(context, "获取socket信息json参数有误");
            DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, "获取socket信息json参数有误");
        }
    }

    /**
     * 获取长连接地址信息
     */
    private boolean getSocketAddr(RequestCallback callback) throws JSONException {
        Integer appId = CommonUtils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
        String appKey = CommonUtils.getMetaDataString(context, PushConstant.INNOTECH_APP_KEY);
        String guid = TokenUtils.getGuid(context);
        if (TextUtils.isEmpty(guid)) {
            LogUtils.e(context, "guid不能为空");
            DbUtils.addClientLog(context, LogCode.LOG_INIT, "getSocketAddr方法，guid为空");
            return false;
        }
        JSONObject object = new JSONObject();
        object.put("app_id", appId);
        object.put("app_key", appKey);
        object.put("guid", guid);
        String json = object.toString();
        String sign = SignUtils.sign("POST", NetWorkUtils.PATH_SOCKET_ADDR, json);
        NetWorkUtils.sendPostRequest(context, NetWorkUtils.URL_SOCKET_ADDR, json, sign, callback);
        return true;
    }

    /**
     * 建立长连接
     */
    private void connectWithHostAndPort(final String host, final int port) throws JSONException {
        final Integer appId = CommonUtils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
        final String appKey = CommonUtils.getMetaDataString(context, PushConstant.INNOTECH_APP_KEY);
        final String guid = TokenUtils.getGuid(context);
        if (TextUtils.isEmpty(guid)) {
            LogUtils.e(context, "guid不能为空");
            DbUtils.addClientLog(context, LogCode.LOG_INIT, "connectWithHostAndPort方法，guid为空");
            takeRCQueue();
            return;
        }
        try {
            mSocket = new Socket(host, port);
            LogUtils.e(context, "与服务器(" + host + ":" + port + ")连接成功");
            mInputStream = mSocket.getInputStream();
            mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
            takeRCQueue();
            //登录
            loginCmd(guid, appId, appKey);
            readData();
        } catch (Exception e) {
            LogUtils.e(context, "SocketException异常：" + e.getMessage());
            takeRCQueue();
            try {
                Thread.sleep(5000);
                reConnect();
            } catch (InterruptedException e1) {
                e.printStackTrace();
            }
            DbUtils.addClientLog(context, LogCode.LOG_EX_SOCKET, "SocketException异常：" + e.getMessage());
        }
    }

    /**
     * 读取数据
     */
    private void readData() {
        if (readThread != null && readThread.isAlive()) {
            return;
        }
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.e(context, "这里是读的线程readThread:" + readThread);
                    byte[] lenB = new byte[16];
                    int tempRead = 0;
                    while ((tempRead = mInputStream.read(lenB)) != -1) {
                        if(tempRead != 16){
                            LogUtils.e(context, "tempRead:" + tempRead);
                        }
                        int len = getLenByData(lenB);
//                        long requestId = getRequestIDByData(lenB);
                        int command = getCommandByData(lenB);
                        LogUtils.e(context, "len:" + len);
                        LogUtils.e(context, "command:" + command);
                        switch (command) {
                            case 1://登录成功（LoginRespCmd）
                                LogUtils.e(context, "登录成功");
                                try {
                                    String jsonA = getJsonByData(mInputStream, len);
                                    if (!TextUtils.isEmpty(jsonA) && !"null".equals(jsonA)) {
                                        ArrayList<String> list = new ArrayList<>();
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
                                                    || CommonUtils.isMeizuDevice()
                                                    || (Utils.isHuaweiDevice() && PushConstant.hasHuawei)) {
                                                ackCmd(list, 101);
                                            } else {
                                                ackCmd(list, 1001);
                                            }
                                        }
                                    }
                                    //长连接回执之前丢失的回执
                                    InnotechPushMethod.uploadSocketAck(context);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 4://服务器推送消息（ForwardCmd）
                                LogUtils.e(context, "收到服务器推送消息");
                                //处理推送消息
                                String jsonO = getJsonByData(mInputStream, len);
                                if (!TextUtils.isEmpty(jsonO) && !"null".equals(jsonO)) {
                                    PushMessage pushMessage = (PushMessage) DataAnalysis.jsonToT(PushMessage.class.getName(), jsonO);
                                    if (pushMessage != null) {
                                        PushMessageManager.getInstance(context).setNewMessage(pushMessage);
                                        ArrayList<String> list = new ArrayList<>();
                                        list.add(pushMessage.getMsg_id());
                                        if (CommonUtils.isXiaomiDevice()
                                                || CommonUtils.isMIUI()
                                                || CommonUtils.isMeizuDevice()
                                                || (Utils.isHuaweiDevice() && PushConstant.hasHuawei)) {
                                            ackCmd(list, 101);
                                        } else {
                                            ackCmd(list, 1);
                                        }
                                        DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "收到服务器推送消息：" + pushMessage.getMsg_id());
                                    }
                                }
                                break;
                            case 7://ack回值成功（AckRespCmd）
//                                getJsonByData(mInputStream, len);
                                LogUtils.e(context, "ack回值成功");
                                break;
                            case 10://心跳回包（HeartBeatRespCmd）
                                DbUtils.addClientLog(context, LogCode.LOG_EX_SOCKET, "心跳回包成功");
                                LogUtils.e(context, "心跳回包成功");
                                break;
                            default:
                                break;
                        }
                    }
                    //while循环中阻塞读，当服务器断开连接后，读操作就会返回-1，从而跳出循环执行后续的代码。
                    //服务器断开了，需要重连
                    LogUtils.e(context, "服务器断开了，正在尝试重连...");
                    try {
                        Thread.sleep(5000);
                        reConnect();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e(context, "IOException异常：" + e.getMessage());
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        Thread.sleep(5000);
                        reConnect();
                    } catch (InterruptedException e1) {
                        e.printStackTrace();
                    }
//                }
                    DbUtils.addClientLog(context, LogCode.LOG_EX_SOCKET, "IOException异常：" + e.getMessage());
                }
            }
        });
        readThread.start();
    }

    /**
     * 是否连接中
     */
    private boolean isConnecting() {
        return mSocket != null && mSocket.isConnected() && !mSocket.isClosed() && !mSocket.isInputShutdown();
    }

    /**
     * 客户端断掉了的重连
     * isConnecting()只能判断客户端是否处于正常连接状态
     */
    public void reConnect() {
        //重现时，使用reConnectQueue,防止多线程并发
        try {
            reConnectQueue.put(1);
            LogUtils.e(context, "重连开始...");
            DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "重连开始...");
            boolean result = sendData("", 9);
            if (!result) {//发送失败，需要重连
                try {
                    if (mSocket != null) {
                        mSocket.close();
                    }
                    SocketManager.getInstance(context).initSocket();
                    LogUtils.e(context, "正在重连...");
                    DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "正在重连...");
                } catch (IOException e) {
                    takeRCQueue();
                    LogUtils.e(context, "socket close异常...");
                    DbUtils.addClientLog(context, LogCode.LOG_EX_IO, "socket close异常..." + e.getMessage());
                }
            } else {//发送成功，长连接正常，解锁
                takeRCQueue();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 登录
     */
    private void loginCmd(String guid, int appID, String appKey) {
        try {
            JSONObject object = new JSONObject();
            object.put("guid", guid);
            object.put("app_id", appID);
            object.put("app_key", appKey);
            boolean result = sendData(object.toString(), 0);
            if (!result) {
                LogUtils.e(context, "发送登录指令失败，进行重连。");
                reConnect();
            } else {
                LogUtils.e(context, "发送登录指令成功：" + object.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(context, "发送登录指令时，出现异常。");
            DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, "发送登录指令时，出现异常。" + guid + ";" + appID + ";" + appKey);
        }
    }

    /**
     * 发送心跳信息
     */
    public void sendHeartData() {
        boolean result = sendData("", 9);
        if (!result) {
            LogUtils.e(context, "发送心跳指令失败，进行重连。");
            reConnect();
        } else {
            LogUtils.e(context, "发送心跳指令成功");
        }
        DbUtils.addClientLog(context, LogCode.LOG_EX_SOCKET, "发送心跳指令");
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
            boolean result = sendData(object.toString(), 6);
            if (!result) {
                LogUtils.e(context, "发送ack指令失败，进行重连。");
                reConnect();
            } else {
                LogUtils.e(context, "发送ack指令成功：" + object.toString());
            }
            //客户端回执
            JSONArray paramArray = new JSONArray();
            paramArray.put(object);
            InnotechPushMethod.clientMsgNotify(context, paramArray, 0);
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(context, "发送ack指令时，出现异常。");
            DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, "发送ack指令时，出现异常。" + msgList.toString());
        }
    }

    /**
     * 向服务端发消息
     */
    public boolean sendData(final String json, final int cmd) {
        boolean result;
        if (isConnecting()) {
            LogUtils.e(context, "socket状态为：连接中...");
            try {
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
                mDataOutputStream.write(data);
                result = true;
            } catch (IOException e) {
                result = false;
                LogUtils.e(context, "socket发送信息失败..." + e.getMessage());
                //存放本次发送的回执
                if (cmd == 6) {
                    DbUtils.addSocketAck(context, json, cmd);
                }
                DbUtils.addClientLog(context, LogCode.LOG_EX_IO, "socket发送信息失败..." + e.getMessage());
            }
        } else {
            result = false;
            LogUtils.e(context, "socket状态为：已断开连接...");
            //存放本次发送的回执
            if (cmd == 6) {
                DbUtils.addSocketAck(context, json, cmd);
            }
            DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "socket状态为：已断开连接...");
        }
        return result;
    }

    /**
     * 生成8字节随机数
     *
     * @return byte[]
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
    private long getRequestIDByData(byte[] data) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = data[i + 4];
        }
        return CommonUtils.longFrom8Bytes(bytes, 0, false);
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
        //
        byte[] lenJ = new byte[len - 12];
        LogUtils.e(context, "getJsonByData：" + lenJ.length);
        try {
            boolean isRead = true;
            int readLen = 0;
            while (isRead) {
                int curReadLen = 0;
                if (lenJ.length - readLen < 1024) {
                    curReadLen = is.read(lenJ, readLen, lenJ.length - readLen);
                } else {
                    curReadLen = is.read(lenJ, readLen, 1024);
                }
                readLen += curReadLen;
                LogUtils.e(context, "readLen：" + readLen);
                LogUtils.e(context, "curReadLen：" + curReadLen);
                if (curReadLen == -1 || readLen == len - 12) {
                    isRead = false;
                }
            }
            json = new String(lenJ);
            printtest(json);
        } catch (Exception e) {
            e.printStackTrace();
            DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, "获取服务端回包的信息解析失败，len" + len + "，异常信息：" + e.getMessage());
        }
        return json;
    }

    private void printtest(String xml) {
        if (xml.length() > 4000) {
            for (int i = 0; i < xml.length(); i += 4000) {
                if (i + 4000 < xml.length())
                    LogUtils.e(context, "getJsonByData：" + xml.substring(i, i + 4000));
                else
                    LogUtils.e(context, "getJsonByData：" + xml.substring(i, xml.length()));
            }
        } else
            LogUtils.e(context, "getJsonByData：" + xml);
    }

    /**
     * 消费队列中的值，从而解除阻塞
     */
    private void takeRCQueue() {
        try {
            if (reConnectQueue.contains(1)) {
                reConnectQueue.take();
                LogUtils.e(context, "重连结束...");
                DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "重连结束...");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
