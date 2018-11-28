package com.innotech.innotechpush.socket;

/**
 * 提供对外调用的方法
 */
public interface ISocket {

    /**
     * 建立连接
     *
     * @param host：ip
     * @param port：端口
     */
    void connect(String host, Integer port);

    /**
     * 发送信息
     *
     * @param command：命令
     * @param json：传递的数据
     */
    void send(Integer command, String json);

    /**
     * 重连
     */
    void reconnect();

    /**
     * 关闭连接
     */
    void close();

    /**
     * 获取长连接状态
     *
     * @return 是否连接
     */
    boolean getState();
}
