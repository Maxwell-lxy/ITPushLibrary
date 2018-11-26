package com.innotech.innotechpush.sdk;

import android.content.Context;
import android.text.TextUtils;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.bean.WriteData;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.callback.SocketSendCallback;
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
import java.util.concurrent.LinkedBlockingQueue;

public class SocketManager {
    private static final int CMD_LOGIN = 0;//登录指令码
    private static final int CMD_ACK = 6;//ack指令码
    private static final int CMD_HEART = 9;//心跳指令码
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
    /**
     * write LinkedBlockingQueue
     * 要写给服务端的信息先放入Queue中，再从Queue中取出进行处理
     * 防止多线程同时写产生批量写失败
     */
    private LinkedBlockingQueue<WriteData> writeQueue;
    //重连时，使用SynchronousQueue起到锁同步的效果
    private ArrayBlockingQueue<Integer> reConnectQueue;

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
        reConnectQueue = new ArrayBlockingQueue<>(1);
        writeQueue = new LinkedBlockingQueue<>();
    }

    /**
     * 初始化长连接
     */
    public synchronized void initSocket() {
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
            LogUtils.e(context, "获取socket信息json参数有误" + e.getMessage());
            DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, "获取socket信息json参数有误" + e.getMessage());
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
        if (!TextUtils.isEmpty(host) && port != 0) {
            final Integer appId = CommonUtils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
            final String appKey = CommonUtils.getMetaDataString(context, PushConstant.INNOTECH_APP_KEY);
            final String guid = TokenUtils.getGuid(context);
            if (TextUtils.isEmpty(guid)) {
                LogUtils.e(context, "guid不能为空");
                takeRCQueue();
                return;
            }
            try {
                mSocket = new Socket(host, port);
                LogUtils.e(context, "与服务器(" + host + ":" + port + ")连接成功");
                mInputStream = mSocket.getInputStream();
                mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                takeRCQueue();
                readData();
                writeData();
                //登录
                loginCmd(guid, appId, appKey);
            } catch (Exception e) {
                LogUtils.e(context, "Exception异常：" + e.getMessage());
                takeRCQueue();
                try {
                    Thread.sleep(5000);
                    reConnect();
                } catch (InterruptedException e1) {
                    e.printStackTrace();
                }
                DbUtils.addClientLog(context, LogCode.LOG_EX_SOCKET, "Exception异常：" + e.getMessage());
            }
        }
    }

    /**
     * 读取数据
     */
    private void readData() {
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        byte[] lenB = readByLen(16);
                        if (lenB == null) break;
                        int len = getLenByData(lenB);
                        long requestId = getRequestIDByData(lenB);
                        int command = getCommandByData(lenB);
                        String json = "";
                        if (len - 12 > 0) {
                            byte[] lenJ = readByLen(len - 12);
                            if (lenJ == null) break;
                            json = new String(lenJ);
                            LogUtils.eLong(context, "readData json:" + json);
                        }
                        switch (command) {
                            case 1://登录成功（LoginRespCmd）
                                LogUtils.e(context, "登录成功");
                                try {
                                    if (!TextUtils.isEmpty(json) && !"null".equals(json)) {
                                        ArrayList<String> list = new ArrayList<>();
                                        JSONArray array = new JSONArray(json);
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
                                                    || (Utils.isHuaweiDevice() && PushConstant.hasHuawei && HuaweiSDK.isUpEMUI41())
//                                        || (Utils.isOPPO() && PushConstant.hasOppo && com.coloros.mcssdk.PushManager.isSupportPush(context))
                                                    ) {
                                                ackCmd(list, 101);
                                            } else {
                                                ackCmd(list, 1001);
                                            }
                                            DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "收到服务器推送消息(offlinemsg)：" + list.toString());
                                        }
                                    }
                                    //长连接回执之前丢失的回执
                                    InnotechPushMethod.uploadSocketAck(context);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 4://服务器推送消息（ForwardCmd）
                                //处理推送消息
                                if (!TextUtils.isEmpty(json) && !"null".equals(json)) {
                                    PushMessage pushMessage = (PushMessage) DataAnalysis.jsonToT(PushMessage.class.getName(), json);
                                    if (pushMessage != null) {
                                        PushMessageManager.getInstance(context).setNewMessage(pushMessage);
                                        ArrayList<String> list = new ArrayList<>();
                                        list.add(pushMessage.getMsg_id());
                                        if (CommonUtils.isXiaomiDevice()
                                                || CommonUtils.isMIUI()
                                                || CommonUtils.isMeizuDevice()
                                                || (Utils.isHuaweiDevice() && PushConstant.hasHuawei && HuaweiSDK.isUpEMUI41())
//                                        || (Utils.isOPPO() && PushConstant.hasOppo && com.coloros.mcssdk.PushManager.isSupportPush(context))
                                                ) {
                                            ackCmd(list, 101);
                                        } else {
                                            ackCmd(list, 1);
                                        }
                                        DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "收到服务器推送消息：" + pushMessage.getMsg_id());
                                    }
                                }
                                break;
                            case 7://ack回值成功（AckRespCmd）
                                LogUtils.e(context, "ack回值成功");
                                break;
                            case 10://心跳回包（HeartBeatRespCmd）
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
                    LogUtils.e(context, "IOException异常：" + e.getMessage());
                    try {
                        Thread.sleep(5000);
                        reConnect();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    DbUtils.addClientLog(context, LogCode.LOG_EX_SOCKET, "IOException异常：" + e.getMessage());
                }
            }
        });
        readThread.start();
    }

    /**
     * 写数据
     */
    private void writeData() {
        writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    boolean isInterrupted = Thread.currentThread().isInterrupted();
                    if (isInterrupted) break;
                    try {
                        WriteData writeData = writeQueue.take();
                        if (isConnecting()) {
                            LogUtils.e(context, "socket状态为：连接中...");
                            try {
                                mDataOutputStream.write(writeData.getData());
                                writeData.getResultQueue().put(true);
                            } catch (IOException e) {
                                LogUtils.e(context, "socket发送信息失败，异常信息：" + e.getMessage());
                                writeData.getResultQueue().put(false);
                                DbUtils.addClientLog(context, LogCode.LOG_EX_IO, "socket发送信息失败，异常信息：" + e.getMessage());
                            }
                        } else {
                            LogUtils.e(context, "socket状态为：已断开连接...");
                            writeData.getResultQueue().put(false);
                            DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "socket状态为：已断开连接...");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        writeThread.start();
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
    public synchronized void reConnect() {
        //重现时，使用reConnectQueue,防止多线程并发
        try {
            reConnectQueue.put(1);
            LogUtils.e(context, "重连开始...");
            DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "重连开始...");
            sendData("", 9, new SocketSendCallback() {
                @Override
                public void onResult(boolean result) {
                    if (!result) {//发送失败，需要重连
                        try {
                            reset();
                            SocketManager.getInstance(context).initSocket();
                        } catch (IOException e) {
                            takeRCQueue();
                            LogUtils.e(context, "socket close异常..." + e.getMessage());
                            DbUtils.addClientLog(context, LogCode.LOG_EX_IO, "socket close异常..." + e.getMessage());
                        }
                    } else {//发送成功，长连接正常，解锁
                        takeRCQueue();
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置所有相关的允许重新连接
     *
     * @throws IOException
     */
    private void reset() throws IOException {
        if (writeThread != null) {
            writeThread.interrupt();
            writeThread = null;
        }
        if (readThread != null) {
            readThread.interrupt();
            readThread = null;
        }
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
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
            object.put("version", PushConstant.INNOTECH_PUSH_VERSION);
            WriteData writeData = new WriteData(CMD_LOGIN, object.toString());
            try {
                writeQueue.put(writeData);
                boolean result = writeData.getResultQueue().take();
                if (!result) {//写失败
                    //重连
                    reConnect();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogUtils.e(context, "发送登录指令：" + object.toString());
        } catch (JSONException e) {
            LogUtils.e(context, "发送登录指令时，出现异常。" + e.getMessage());
            DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, "发送登录指令时，出现异常。" + guid + ";" + appID + ";" + appKey);
        }
    }

    /**
     * 发送心跳信息
     */
    public void sendHeartData() {
        WriteData writeData = new WriteData(CMD_HEART, "");
        try {
            writeQueue.put(writeData);
            boolean result = writeData.getResultQueue().take();
            if (!result) {//写失败
                //重连
                reConnect();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.e(context, "发送心跳指令");
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
            //客户端回执
            JSONArray paramArray = new JSONArray();
            paramArray.put(object);
            InnotechPushMethod.clientMsgNotify(context, paramArray);
            //存入queue
            WriteData writeData = new WriteData(CMD_ACK, object.toString());
            try {
                writeQueue.put(writeData);
                boolean result = writeData.getResultQueue().take();
                if (!result) {//写失败
                    //存数据库，等待补发
                    DbUtils.addSocketAck(context, object.toString(), CMD_ACK);
                    //重连
                    reConnect();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogUtils.e(context, "发送ack指令：" + object.toString());
        } catch (JSONException e) {
            LogUtils.e(context, "发送ack指令时，出现异常。" + e.getMessage());
            DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, "发送ack指令时，出现异常。" + msgList.toString());
        }
    }

    public void sendData(final String json, final int cmd, final SocketSendCallback callback) {
        if (isConnecting()) {
            LogUtils.e(context, "socket状态为：连接中...");
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
                        LogUtils.e(context, "socket发送信息失败，cmd：" + cmd + "，异常信息：" + e.getMessage());
                        //存放本次发送的回执
                        if (cmd == 6) {
                            DbUtils.addSocketAck(context, json, cmd);
                        }
                        if (callback != null) {
                            callback.onResult(false);
                        } else {
                            reConnect();
                        }
                        DbUtils.addClientLog(context, LogCode.LOG_EX_IO, "socket发送信息失败，cmd：" + cmd + "，异常信息：" + e.getMessage());
                    }
                }
            };
            sendThread = new Thread(sendRunnable);
            sendThread.start();
        } else {
            LogUtils.e(context, "socket状态为：已断开连接...");
            //存放本次发送的回执
            if (cmd == 6) {
                DbUtils.addSocketAck(context, json, cmd);
            }
            if (callback != null) {
                callback.onResult(false);
            } else {
                reConnect();
            }
            DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "socket状态为：已断开连接...");
        }
    }

    /**
     * 生成8字节随机数
     *
     * @return 8字节随机数
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
        System.arraycopy(data, 0, bytes, 0, 4);
        return CommonUtils.big_bytesToInt(bytes);
    }

    /**
     * 获取服务端回包的信息
     * 8字节requestID
     */
    private long getRequestIDByData(byte[] data) {
        byte[] bytes = new byte[8];
        System.arraycopy(data, 4, bytes, 0, 8);
        return CommonUtils.longFrom8Bytes(bytes, 0, false);
    }

    /**
     * 获取服务端回包的信息
     * 3字节命令的值
     * 第一位已被服务端用于其他用途，默认补充为0，为了凑齐4字节进行计算
     */
    private int getCommandByData(byte[] data) {
        byte[] bytes = new byte[4];
        bytes[0] = 0;
        System.arraycopy(data, 13, bytes, 1, 3);
        return CommonUtils.big_bytesToInt(bytes);
    }

    /**
     * 读取长度为len的字符数组
     *
     * @param len：长度
     * @return 字符数组
     */
    private byte[] readByLen(int len) throws IOException {
        byte[] result = new byte[len];
        boolean isRead = true;
        int readLen = 0;
        while (isRead) {
            int curReadLen;
            if (result.length - readLen < 1024) {
                curReadLen = mInputStream.read(result, readLen, result.length - readLen);
            } else {
                curReadLen = mInputStream.read(result, readLen, 1024);
            }
            readLen += curReadLen;
//            LogUtils.e(context, "readLen：" + readLen);
            LogUtils.e(context, "curReadLen：" + curReadLen);
            if (readLen == len) isRead = false;
            if (curReadLen == -1) {
                result = null;
                break;
            }
        }
        return result;
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
            LogUtils.e(context, "takeRCQueue：" + e.getMessage());
        }
    }

}
