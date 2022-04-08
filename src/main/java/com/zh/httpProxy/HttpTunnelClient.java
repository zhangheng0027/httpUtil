package com.zh.httpProxy;

import com.zh.util.ArrayUtils;
import com.zh.util.MathUtils;
import com.zh.util.ThreadUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpTunnelClient {

    private static final String version = "v1.0";

    private static final Logger log = LogManager.getLogger(HttpTunnelClient.class);
    private static final int PackageLength = 10240;  // 一次报文最长长度
    private static final int lengthByte;  // 报文的长度所需的占的位数
    private static final AtomicInteger globalFlag = new AtomicInteger(1); // 全局flag
    private final Socket serverSocket;
    private final ServerSocket monitorSocket;
    private static final LinkedBlockingQueue<Byte[]> msgQueue = new LinkedBlockingQueue(1024);; // 消息队列
    private static final ConcurrentHashMap<Integer, OutputStream> map = new ConcurrentHashMap(64);

    static {
        lengthByte = MathUtils.getLengthByte(PackageLength);
    }

    /**
     *
     * @param port 监听当前主机的端口
     * @param serverHost 服务端地址
     * @param serverPort 服务端端口
     * @throws IOException
     */
    public HttpTunnelClient(int port, String serverHost, int serverPort) throws IOException {
        serverSocket = new Socket(serverHost, serverPort);
        monitorSocket = new ServerSocket(port);
    }

    public void run() throws IOException, InterruptedException, SchedulerException {
        StringBuffer sb = new StringBuffer(64);
        sb.append(version).append(" ").append(PackageLength);
        Byte[] bytes = new Byte[sb.length()];
        ArrayUtils.byte2Byte(sb.toString().getBytes(), 0, bytes, 0, sb.length());
        msgQueue.put(bytes);
        SimpleTrigger trigger = TriggerBuilder.newTrigger().
                withIdentity("myTrigger").
                startNow().
                withSchedule(SimpleScheduleBuilder.simpleSchedule().
                        withIntervalInSeconds(10).
                        repeatForever()).
                build();
        //创建schedule实例
        StdSchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        JobDetail jobDetail = JobBuilder.newJob(job.class).withIdentity("xintiao").build();
        //执行job
        scheduler.scheduleJob(jobDetail,trigger);

        ThreadUtils.execute(() -> { // 将请求发给服务端
            try(OutputStream outputStream = serverSocket.getOutputStream()) {
                while (true) {
                    Byte[] buf = msgQueue.take();
                    byte[] buff = new byte[buf.length + lengthByte];
                    ArrayUtils.Byte2byte(buf, 0, buff, lengthByte, buf.length);

                    // 将一个包的长度拼接在报文开头
                    byte[] ls = MathUtils.int2bytes(buf.length);
                    System.arraycopy(ls, 0, buff, lengthByte - ls.length, ls.length);

                    byte flag = buff[lengthByte];
                    if (HttpTunnelConstant.type_2 == flag)
                        log.info("长度 {} 新建链接 地址 {}", buff.length, new String(buff, 3, buff.length - 3));
                    else if (HttpTunnelConstant.byte_127 != flag)
                        log.info("发送数据 flag {} 长度 {} 最后一位字符 {}", buff[0], buff.length, buff[buff.length - 1]);
                    outputStream.write(buff);
                    outputStream.flush();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        ThreadUtils.execute(() -> { // 将服务器请求进行分发
            try (InputStream inputStream = serverSocket.getInputStream()) {
                int len;
                byte[] buf = new byte[PackageLength];
                while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
                    if (len < 3)
                        continue;
                    byte flag = buf[0];
                    int i = (((int)buf[1]) << 8) | buf[2];
                    if (HttpTunnelConstant.type_0 == flag) {
                        if (!map.containsKey(i))
                            continue;
                        OutputStream o = map.get(i);
                        o.write(buf, 3, len - 3);
                        o.flush();
                    }
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
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    static class A {
        final private InputStream inputStream;
        final private OutputStream outputStream;
        final private Socket socket;
        final int flag;
        final byte h;
        final byte l;
        A(Socket s) throws IOException {
            flag = globalFlag.incrementAndGet();
            socket = s;
            inputStream = s.getInputStream();
            outputStream = s.getOutputStream();
            map.put(flag, outputStream);
            h = (byte) (flag >> 8);
            l = (byte) (flag & 0xff);
        }

        public void handle() throws IOException, InterruptedException {
            byte[] buff = new byte[PackageLength - 3 - lengthByte];
            int len = inputStream.read(buff);
            if (len <= 0) {
                return;
            }
            String context =  new String(buff, 0, len);
            String[] cs = context.split("\n");
            // 从所读数据中取域名和端口号
            String host = parseServerHost(cs[0]);
            Byte[] buf = new Byte[host.length() + 3];
            buf[0] = HttpTunnelConstant.type_2; // 新建连接
            buf[1] = h;
            buf[2] = l;
            ArrayUtils.byte2Byte(host.getBytes(), 0, buf, 3, host.length());
            msgQueue.put(buf);

            if (cs[0].startsWith("CONNECT")) {
                String ack = "HTTP/1.0 200 Connection established\r\n";
                ack = ack + "Proxy-agent: proxy\r\n\r\n";
                outputStream.write(ack.getBytes());
                outputStream.flush();
            } else {
                Byte[] bytes = new Byte[len + 3];
                bytes[0] = HttpTunnelConstant.type_1;
                bytes[1] = h;
                bytes[2] = l;
                ArrayUtils.byte2Byte(buff, 0, bytes, 3, len);
                msgQueue.put(bytes);
            }
            while ((len = inputStream.read(buff, 0, buff.length)) != -1) {
                Byte[] b = new Byte[len + 3];
                b[0] = HttpTunnelConstant.type_1;
                b[1] = h;
                b[2] = l;
                ArrayUtils.byte2Byte(buff, 0, b, 3, len);
                msgQueue.put(b);
            }
        }

        public void finalize() {
            map.remove(flag);
            HttpProxyUtil.close(inputStream, outputStream, socket);
        }
    }

    public static class job implements Job {
        static AtomicInteger ai = new AtomicInteger(0);
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            if (msgQueue.size() > 0)
                return;

            Byte[] bytes = new Byte[3];
            bytes[0] = HttpTunnelConstant.byte_127;
            bytes[1] = 0;
            bytes[2] = 0;
            try {
                msgQueue.put(bytes);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * 解析请求地址与端口，以空格分隔
     * @param con
     * @return
     */
    public static String parseServerHost(String con) {
        String regExp = "http://([^/]+)/";
        if (con.startsWith("CONNECT"))
            regExp = "CONNECT ([^ ]+) HTTP/";
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(con + "/");
        StringBuffer sb = new StringBuffer(255);
        if (matcher.find()) {
            String host = matcher.group(1);
            if (host.contains(":")) {
               sb.append(host.substring(0, host.indexOf(":"))).append(" ");
               sb.append(host.substring(host.indexOf(":") + 1));
            } else {
                sb.append(host).append(" 80");
            }
        } else {
            regExp = "https://([^/]+)/";
            pattern = Pattern.compile(regExp);
            matcher = pattern.matcher(con + "/");
            if (matcher.find()) {
                String host = matcher.group(1);
                sb.append(host).append(" 443");
            }
        }
        return sb.toString();
    }

    public void finalize() {
        HttpProxyUtil.close(serverSocket);
    }

}
