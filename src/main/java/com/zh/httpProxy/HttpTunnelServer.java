package com.zh.httpProxy;

import com.zh.util.ArrayUtils;
import com.zh.util.MathUtils;
import com.zh.util.ThreadUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class HttpTunnelServer {
	private static final Logger log = LogManager.getLogger(HttpTunnelServer.class);
	private final ServerSocket server;
	private final int port;
	public HttpTunnelServer(int port) throws IOException {
		this.port = port;
		server = new ServerSocket(port);
	}

	public void run() throws IOException {
		log.info("服务启动，开始监听 {} 端口", port);
		while (true) {
			Socket socket = server.accept();
			ThreadUtils.execute(() -> {
				try {
					new Client(socket).handle();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					log.info("客户端退出链接");
				}
			});
		}
	}


	/**
	 * 第一个报文需要 256 位
	 * 倒数第一位：偏离长度
	 * 倒数第二位：表示实际内容的长度（注意转成 int 型时可能为负数）
	 */
	private static class Client {

		final Socket socket;
		final InputStream clientInputStream;
		final OutputStream clientOutputStream;
		final String clientVersion; // 客户端版本， 预留字段
		final int packageLength; // 约定每个包最长长度
		final int lengthByte;
		final LinkedBlockingQueue<Byte[]> msgQueue; // 消息队列
		Client(Socket socket) throws IOException {
			msgQueue = new LinkedBlockingQueue(1024);
			this.socket = socket;
			clientInputStream = socket.getInputStream();
			clientOutputStream = socket.getOutputStream();
			byte[] bytes = new byte[256];
			int len = clientInputStream.read(bytes, 0, bytes.length);
			if (len != 256)
				throw new HttpContextException("发送报文不符合规格");

			String[] contexts = new String(bytes, bytes[len - 1], ((int)bytes[len - 2]) & 0xFF).split(" ");
			clientVersion = contexts[0];
			packageLength = Integer.valueOf(contexts[1]);
			lengthByte = bytes[len - 1];
		}


		public void handle() {
			log.info("一台客户端进行了连接");
			// 将消息返回给客户端
			ThreadUtils.execute(() -> {
				byte[] t = {'a'};
				while (true) {
					try {
						Byte[] buf = msgQueue.take();
						byte[] buff = new byte[buf.length + lengthByte];
						ArrayUtils.Byte2byte(buf, 0, buff, lengthByte, buf.length);
						byte[] bt = MathUtils.int2bytes(buf.length);
						System.arraycopy(bt, 0, buff, lengthByte - bt.length, bt.length);
						clientOutputStream.write(buff);
						t = buff;
					} catch (InterruptedException | IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			});

			try { // 处理来自客户端的请求
				int len;
				byte buf[] = new byte[packageLength];
				ConcurrentHashMap<Integer, OutputStream> map = new ConcurrentHashMap<>(32);
				while ((len = clientInputStream.read(buf, 0, buf.length)) != -1) {
					int offset = 0; // 偏移量
					boolean isBreak = false;
					while (offset < len) {
						int l = -1;
						if (offset + lengthByte > len) {
							isBreak = true;
							byte[] bs = new byte[lengthByte];
							System.arraycopy(buf, offset, bs, 0, len - offset);
							clientInputStream.read(buf, 0, offset + lengthByte - len);
							System.arraycopy(buf, 0, bs, len - offset, offset + lengthByte - len);
							l = MathUtils.bytes2int(bs, 0, bs.length);
							len = clientInputStream.read(buf, 0, l);
							offset = 0;
						} else if (offset + lengthByte == len) {
							isBreak = true;
							l = MathUtils.bytes2int(buf, offset, lengthByte);
							len = clientInputStream.read(buf, 0, l);
							offset = 0;
						} else {
							l = MathUtils.bytes2int(buf, offset, lengthByte);
							if (offset + lengthByte + l > len) {
								isBreak = true;
								System.arraycopy(buf, offset + lengthByte, buf, 0, len - offset - lengthByte);
								clientInputStream.read(buf, len - offset - lengthByte, offset + lengthByte + l - len);
								len = l;
								offset = 0;
							} else {
								offset += lengthByte;
							}
						}


						int i = (((int)buf[1 + offset]) << 8) | buf[2 + offset];
						byte flag = buf[0 + offset];
						if (HttpTunnelConstant.byte_127 != flag)
							log.info("接受 {} 数据 flag {} 长度 {}", i, flag, l);
						if (HttpTunnelConstant.type_1 == flag) { // 发送数据
							OutputStream o =  map.get(i);
							o.write(buf,3 + offset, l - 3);
							o.flush();
						} else if (HttpTunnelConstant.type_2 == flag) { // 新建连接
							String[] context = new String(buf, 3 + offset, l - 3).split(" ");
							log.info("长度{} 新建连接 {}，客户端地址 {}, 端口 {}", l, i, context[0], context[1]);
							Socket s = new Socket(context[0], Integer.valueOf(context[1]));
							map.put(i, s.getOutputStream());
							ThreadUtils.execute(() -> {
								handleReceive(i, s);
							});
						} else if (HttpTunnelConstant.byte_127 == flag) {
							// 收到心跳包, 进行返回
							Byte[] bytes = new Byte[3];
							bytes[0] = HttpTunnelConstant.byte_127;
							bytes[1] = 0;
							bytes[2] = 0;
							msgQueue.put(bytes);
						}
						if (isBreak)
							break;
						offset += l;

					}



				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}

		/**
		 * 接收外网返回的内容
		 * @param i
		 * @param socket
		 */
		public void handleReceive(int i, Socket socket){
			byte h = (byte) (i >> 8);
			byte l = (byte) (i & 0xff);
			try(InputStream in = socket.getInputStream()) {
				int len;
				byte buf[] = new byte[packageLength - 3 - lengthByte];
				Map<Integer, OutputStream> map = new HashMap<>(32);
				while ((len = in.read(buf, 0, buf.length)) != -1) {
					Byte[] buff = new Byte[len + 3];
					ArrayUtils.byte2Byte(buf, 0, buff, 3, len);
					buff[0] = HttpTunnelConstant.type_0; // 返回信息
					buff[1] = h; // 高位
					buff[2] = l; // 地位
					msgQueue.put(buff);
					log.info("接收外网长度 {}", buff.length);
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		protected void finalize(){
			HttpProxyUtil.close(socket, clientInputStream, clientOutputStream);
		}

	}

//	private static class Model {
//		private int location;
//		private Socket socket;
//
//
//		Model()
//	}

}
