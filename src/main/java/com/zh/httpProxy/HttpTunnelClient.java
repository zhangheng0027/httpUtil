package com.zh.httpProxy;

import com.zh.util.ThreadUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class HttpTunnelClient {


    private static int PackageLength = 10240;
    private final Socket serverSocket;
    private final ServerSocket monitorSocket;
    private final LinkedBlockingQueue<Byte[]> msgQueue = new LinkedBlockingQueue(1024);; // 消息队列
    private final ConcurrentHashMap<Integer, OutputStream> map = new ConcurrentHashMap(64);

    public HttpTunnelClient(int port, String serverHost, int serverPort) throws IOException {
        serverSocket = new Socket(serverHost, serverPort);
        monitorSocket = new ServerSocket(port);
    }

    public void run() throws IOException {

        ThreadUtils.execute(() -> { // 将请求发给服务端
            try(OutputStream outputStream = serverSocket.getOutputStream()) {
                while (true) {
                    Byte[] buf = msgQueue.take();
                    byte[] buff = new byte[buf.length];
                    System.arraycopy(buf, 0, buff, 0, buf.length);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        ThreadUtils.execute(() -> {
            try (InputStream inputStream = serverSocket.getInputStream()) {
                int len;
                byte[] buf = new byte[PackageLength];
                while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
                    if (len < 3)
                        continue;
                    byte flag = buf[0];
                    int i = (((int)buf[1]) << 8) | buf[2];
                    
                }
            } catch (IOException  e) {
                e.printStackTrace();
            }
        });

        while (true) {
            Socket socket = monitorSocket.accept();
            ThreadUtils.execute(() -> {
                try {
                    new A(socket). handle();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    static class A {
        final private InputStream inputStream;
        final private OutputStream outputStream;

        A(Socket s) throws IOException {
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();
        }

        public void handle() {

        }
    }

    public void finalize() {
        HttpProxyUtil.close(serverSocket);
    }

}
