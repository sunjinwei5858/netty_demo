package com.sunjinwei.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @program: com.sunjinwei.netty
 * @author: sun jinwei
 * @create: 2022-10-21 08:23
 * @description:
 **/
public class NettyServer {

    public static void main(String[] args) {
        // 最小化参数配置：线程模型+IO模型+连接读写逻辑处理

        // 引导类：将引导服务端的启动工作
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // boss指老板 worker指员工 一个老板负责从外面接活 很多员工负责具体干活
        // 表示监听端口 接收新连接的线程组
        NioEventLoopGroup boss = new NioEventLoopGroup();
        // 表示处理每一个连接的数据读写的线程组
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap
                // 给引导类配置两大线程组 这个引导类的线程模型也就定型类
                .group(boss, worker)
                // 指定服务端的IO模型为为NIO，如果想指定BIO 那么就是OioServerSocketChannel.class
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                // 本地绑定一个8000端口
                .bind(1000).addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (future.isSuccess()) {
                            System.out.println("端口绑定成功");
                        } else {
                            System.out.println("端口绑定失败");
                            bind(serverBootstrap, 1001);
                        }
                    }
                });

    }

    private static void bind(ServerBootstrap serverBootstrap, int port) {
        serverBootstrap.bind(port).addListener(
                new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if (future.isSuccess()) {
                            System.out.println("端口[" + port + "]绑定成功");
                        } else {
                            System.out.println("端口[" + port + "]绑定失败");
                            bind(serverBootstrap, port + 1);
                        }
                    }
                }
        );
    }
}