package com.innotech.innotechpush.socket;

import java.net.Socket;

/**
 * 调用ISocket方法后反馈的回调
 */
public interface SocketCallback {

    /**
     * 连接成功
     *
     * @param socket：套接字对象
     */
    void onConnectSuccess(Socket socket);

    /**
     * 连接断开
     *
     * @param code：1、socket异常，2、读（-1），3、写失败
     * @param exception：异常内容
     */
    void onDisconnect(Integer code, String exception);

    /**
     * 读数据
     *
     * @param requestId：消息标识
     * @param command：命令
     * @param json：读到的信息
     */
    void onReadData(Integer requestId, Integer command, String json);

    /**
     * 发送信息后的结果
     *
     * @param result：是否发送成功
     * @param command：命令
     * @param json：发送的内容
     */
    void onSendData(Boolean result, Integer command, String json);


}
