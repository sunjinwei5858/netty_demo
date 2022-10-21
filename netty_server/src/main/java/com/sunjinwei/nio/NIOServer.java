package com.sunjinwei.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @program: com.sunjinwei.netty
 * @author: sun jinwei
 * @create: 2022-10-21 07:54
 * @description:
 **/
public class NIOServer {

    public static void main(String[] args) throws IOException {
        // serverSelector轮询器负责轮询是否有新连接
        Selector serverSelector = Selector.open();
        // clientSelector负责轮询连接是否有数据可读
        Selector clientSelector = Selector.open();

        // 线程1：每个线程都绑定一个轮询器Selector
        new Thread(() -> {
            try {
                ServerSocketChannel listenerChannel = ServerSocketChannel.open();
                listenerChannel.socket().bind(new InetSocketAddress(8000));
                listenerChannel.configureBlocking(false);
                listenerChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
                while (true) {
                    // 1监测是否有新连接 这里的1指阻塞的时间1ms
                    // 监测到新连接之后 不再创建一个新线程 而是直接将新连接绑定到clientSelector上 这样就不用IO模型中的一万个while死循环
                    if (serverSelector.select(1) > 0) {
                        Set<SelectionKey> set = serverSelector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = set.iterator();
                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();
                            if (key.isAcceptable()) {
                                try {
                                    // 每来一个新连接 不需要创建一个线程 而是注册到clientSelector
                                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                                    clientChannel.configureBlocking(false);
                                    clientChannel.register(clientSelector, SelectionKey.OP_READ);
                                } finally {
                                    keyIterator.remove();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }


        }).start();

        // 线程2：
        new Thread(() -> {
            try {
                while (true) {
                    while (true) {
                        // 2批量轮询哪些连接有数据可读 这里的1指阻塞的时间为1ms
                        // clientSelector被一个while死循环包裹着 如果在某一时刻有多个连接有数据可读，那么通过clientSelector.select(1)方法可以轮询出来 进而批量处理
                        if (clientSelector.select(1) > 0) {
                            Set<SelectionKey> set = clientSelector.selectedKeys();
                            Iterator<SelectionKey> keyIterator = set.iterator();
                            while (keyIterator.hasNext()) {
                                SelectionKey key = keyIterator.next();
                                if (key.isReadable()) {
                                    try {
                                        SocketChannel clientChanel = (SocketChannel) key.channel();
                                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                        // 3面向buffer
                                        clientChanel.read(byteBuffer);
                                        byteBuffer.flip();
                                        System.out.println(Charset.defaultCharset().newDecoder().decode(byteBuffer));
                                    } finally {
                                        keyIterator.remove();
                                        key.interestOps(SelectionKey.OP_READ);

                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {

            }

        }).start();
    }
}