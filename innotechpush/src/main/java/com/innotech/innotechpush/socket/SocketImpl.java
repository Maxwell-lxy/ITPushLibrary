package com.innotech.innotechpush.socket;

import android.util.Log;

import com.innotech.innotechpush.bean.WriteData;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketImpl implements ISocket {
    private static final String TAG_SOCKET = "IT-Socket";
    private static final Integer SOCKET_EXCEPTION = 1;
    private static final Integer READ_FAIL = 2;
    private static final Integer WRITE_FAIL = 3;
    //业务类的订阅
    private SocketCallback mSocketCallback;
    //套接字对象
    private Socket mSocket;
    //socket输入流
    private InputStream mInputStream;
    //socket输出流
    private DataOutputStream mDataOutputStream;
    //write线程
    private Thread writeThread;
    /**
     * write LinkedBlockingQueue
     * 要写给服务端的信息先放入Queue中，再从Queue中取出进行处理
     * 防止多线程同时写产生批量写失败
     */
    private LinkedBlockingQueue<WriteData> writeQueue;

    @Override
    public void connect(String host, Integer port) {
        try {
            mSocket = new Socket(host, port);
            mInputStream = mSocket.getInputStream();
            mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
            writeQueue = new LinkedBlockingQueue<>();
            if (mSocketCallback != null)
                mSocketCallback.onConnectSuccess(mSocket);
        } catch (IOException e) {
            if (mSocketCallback != null)
                mSocketCallback.onDisconnect(SOCKET_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void send(Integer command, String json) {
        WriteData writeData = new WriteData(command, json);
        try {
            writeQueue.put(writeData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reconnect() {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean getState() {
        return false;
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
                        try {
                            mDataOutputStream.write(writeData.getData());
                            writeData.getResultQueue().put(true);
                        } catch (IOException e) {
                            writeData.getResultQueue().put(false);
                            Log.e(TAG_SOCKET, "writeData InterruptedException:" + e.getMessage());
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG_SOCKET, "writeData InterruptedException:" + e.getMessage());
                    }
                }
            }
        });
//        writeThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    boolean isInterrupted = Thread.currentThread().isInterrupted();
//                    if (isInterrupted) break;
//                    try {
//                        WriteData writeData = writeQueue.take();
//                        if (isConnecting()) {
//                            LogUtils.e(context, "socket状态为：连接中...");
//                            try {
//                                mDataOutputStream.write(writeData.getData());
//                                writeData.getResultQueue().put(true);
//                            } catch (IOException e) {
//                                LogUtils.e(context, "socket发送信息失败，异常信息：" + e.getMessage());
//                                writeData.getResultQueue().put(false);
//                                DbUtils.addClientLog(context, LogCode.LOG_EX_IO, "socket发送信息失败，异常信息：" + e.getMessage());
//                            }
//                        } else {
//                            LogUtils.e(context, "socket状态为：已断开连接...");
//                            writeData.getResultQueue().put(false);
//                            DbUtils.addClientLog(context, LogCode.LOG_DATA_COMMON, "socket状态为：已断开连接...");
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
        writeThread.start();
    }

    public void setSocketCallback(SocketCallback mSocketCallback) {
        this.mSocketCallback = mSocketCallback;
    }
}
