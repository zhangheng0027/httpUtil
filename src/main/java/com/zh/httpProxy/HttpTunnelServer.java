package com.zh.httpProxy;

import com.zh.util.ThreadUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class HttpTunnelServer {
	private final ServerSocket server;
	public HttpTunnelServer(int port) throws IOException {
		server = new ServerSocket(port);
	}

	public void run() throws IOException {
		while (true) {
			Socket socket = server.accept();
			ThreadUtils.execute(() -> {
				try {
					new A(socket).handle();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}


	private static class A {

		final Socket socket;
		final InputStream clientInputStream;
		final OutputStream clientOutputStream;
		final String clientVersion; // 客户端版本， 预留字段
		final int packageLength; // 约定每个包最长长度
		final LinkedBlockingQueue<Byte[]> msgQueue; // 消息队列
		A(Socket socket) throws IOException {
			msgQueue = new LinkedBlockingQueue(1024);
			this.socket = socket;
			clientInputStream = socket.getInputStream();;
			clientOutputStream = socket.getOutputStream();
			byte[] bytes = new byte[10240];
			int len = clientInputStream.read(bytes, 0, bytes.length);
			String[] contexts = new String(bytes, 0, len).split(" ");
			clientVersion = contexts[0];
			packageLength = Integer.valueOf(contexts[1]);
		}

		public void handle() {

			// 将消息返回给客户端
			ThreadUtils.execute(() -> {
				while (true) {
					try {
						Byte[] buf = msgQueue.take();
						byte[] buff = new byte[buf.length];
						System.arraycopy(buf, 0, buff, 0, buf.length);
						clientOutputStream.write(buff);
					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}
				}
			});

			try { // 处理来自客户端的请求
				int len;
				byte buf[] = new byte[packageLength];
				Map<Integer, OutputStream> map = new HashMap<>(32);
				while ((len = clientInputStream.read(buf, 0, buf.length)) != -1) {
					if (len < 3)
						continue;
					int i = (((int)buf[1]) << 8) | buf[2];
					byte flag = buf[0];
					if (1 == flag) { // 发送数据
						OutputStream o =  map.get(i);
						o.write(buf,3, len - 3);
						o.flush();
					} else if (2 == flag) { // 新建连接
						String[] context = new String(buf, 3, len - 3).split(" ");
						Socket s = new Socket(context[0], Integer.valueOf(context[1]));
						map.put(i, s.getOutputStream());
						ThreadUtils.execute(() -> {
							handleReceive(i, socket);
						});
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
			int ai = 0xf;
			try(InputStream in = socket.getInputStream()) {
				int len;
				byte buf[] = new byte[packageLength - 3];
				Map<Integer, OutputStream> map = new HashMap<>(32);
				while ((len = in.read(buf, 0, buf.length)) != -1) {
					Byte[] buff = new Byte[len + 3];
					System.arraycopy(buf, 0, buff, 3, len);
					buff[0] = HttpTunnelConstant.type_0; // 返回信息
					buff[1] = h; // 高位
					buff[2] = l; // 地位
					msgQueue.put(buff);
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
