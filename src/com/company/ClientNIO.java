package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientNIO {

    public static void main(String[] args) {
        try {
            SocketChannel ClientCh = SocketChannel.open();
            ClientCh.configureBlocking(false);

            Selector selector = Selector.open();
            ClientCh.register(selector, SelectionKey.OP_CONNECT);
            ClientCh.connect(new InetSocketAddress("192.168.1.101", 9000));
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for (SelectionKey key : selectedKeys) {
                    if (key.isConnectable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        if (client.isConnectionPending()) {
                            client.finishConnect();
                            ByteBuffer buff = ByteBuffer.allocate(1024);
                            buff.put(("Connected successfully").getBytes());
                            buff.flip();
                            ClientCh.write(buff);
                            ExecutorService ClientsThread = Executors
                                    .newSingleThreadExecutor(Executors.defaultThreadFactory());
                            ClientsThread.submit(() -> {
                                while (true) {
                                    try {
                                        buff.clear();
                                        Scanner sc = new Scanner(System.in);
                                        String msg = sc.nextLine();
                                        if(!msg.equalsIgnoreCase("exit")) {
                                            buff.put(msg.getBytes());
                                            buff.flip();
                                            client.write(buff);
                                        } else {
                                            msg = "disconnected"; //work with exit the last one that connect but if disconnect the first the rest go with it
                                            buff.put(msg.getBytes());
                                            buff.flip();
                                            client.write(buff);
                                            ClientsThread.shutdown();
                                            client.close();
                                        }
//                                        System.out.println("do 1"); Test
                                    } catch (Exception e) {

                                    }
                                }
                            });
                        }
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel clientCh = (SocketChannel) key.channel();
                        ByteBuffer buff = ByteBuffer.allocate(1024);

                        int read = clientCh.read(buff);
                        if (read > 0) {
                            String msg = new String(buff.array(), 0, read);
                            System.out.println(msg);
//                            System.out.println("do 2");Test
                        }

                    }
                }
                selectedKeys.clear();
            }
        } catch (IOException e) {

        }

    }
}