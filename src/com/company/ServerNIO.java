package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ServerNIO {

    private static Map<String, SocketChannel> clients = new HashMap();

    private static int CLIENT_ID = 0;

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(new InetSocketAddress(9000));
        System.out.println("Listening for connection");
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            try {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey keys = it.next();
                    final SocketChannel client;
                    try {
                        if (keys.isAcceptable()) {
                            ServerSocketChannel channel = (ServerSocketChannel) keys.channel();
                            client = channel.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                            String clientId = "Client [" + CLIENT_ID++ + "]";
                            clients.put(clientId, client);
                        } else if (keys.isReadable()) {
                            client = (SocketChannel) keys.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(512);
                            String Check = new String(buffer.array());
                            if(!Check.equalsIgnoreCase("exit")) {
                                int count = client.read(buffer);
                                if (count > 0) {
                                    buffer.flip();
                                    String msg = new String(buffer.array());
                                    String senderKey = null;
                                    for (Map.Entry<String, SocketChannel> entry : clients.entrySet()) {
                                        if (client == entry.getValue()) {
                                            senderKey = entry.getKey();
                                            break;
                                        }
                                    }
                                    System.out.println(senderKey + ":" + msg);
                                    for (Map.Entry<String, SocketChannel> entry : clients.entrySet()) {
                                        SocketChannel value = entry.getValue();
                                        ByteBuffer Send = ByteBuffer.allocate(1024);

                                        Send.put((senderKey + ":" + msg).getBytes());
                                        Send.flip();

                                        value.write(Send);
                                    }
                                }
                            }else {


                                String senderKey = null;
                                for (Map.Entry<String, SocketChannel> entry : clients.entrySet()) {
                                    if (client == entry.getValue()) {
                                        senderKey = entry.getKey();
                                        break;
                                    }
                                }
                                for (Map.Entry<String, SocketChannel> entry : clients.entrySet()) {
                                    SocketChannel value = entry.getValue();
                                    ByteBuffer Send = ByteBuffer.allocate(1024);

                                    Send.put((senderKey + ": Disconnected").getBytes());
                                    Send.flip();

                                    value.write(Send);
                                    client.close();
                                    clients.remove(senderKey,client);
                                }
                            }
                        }
                    } catch (IOException e1) {

                    }
                }
                selectedKeys.clear();
            } catch (Exception e) {

            }
        }

    }

}
