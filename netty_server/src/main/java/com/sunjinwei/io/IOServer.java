package com.sunjinwei.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: com.sunjinwei
 * @author: sun jinwei
 * @create: 2022-10-20 08:19
 * @description:
 **/
public class IOServer {

    public static void main(String[] args) throws IOException {
        // 创建一个serverSocket来监听8000端口 然后创建一个线程 线程里不断调用阻塞方法serverSocket.accept()获取新连接
        ServerSocket serverSocket = new ServerSocket(8000);
        // 接收新连接线程
        new Thread(() -> {
            while (true) {
                try {
                    // 1阻塞方法获取新连接
                    Socket socket = serverSocket.accept();
                    // 2为每一个新连接都创建一个新线程 负责读取数据
                    new Thread(() -> {
                        try {
                            int len;
                            byte[] data = new byte[1024];
                            InputStream inputStream = socket.getInputStream();
                            // 3按字节流方式读取数据
                            while ((len = inputStream.read(data)) != -1) {
                                System.out.println(new String(data, 0, len));
                            }

                        } catch (IOException e) {

                        }
                    }).start();
                } catch (IOException e) {

                }
            }

        }).start();

    }

}