package com.zh.httpProxy;

public class HttpTunnelConstant {


    /** server 端返回给 client 端 **/
    public static final byte type_0 = 0;

    /** client 端发送给 server 端 **/
    public static final byte type_1 = 1;

    /** client 收到新建连接请求，通知 server 新建连接 **/
    public static final byte type_2 = 2;

    /** 发送心跳包 **/
    public static final byte byte_127 = 127;

}
