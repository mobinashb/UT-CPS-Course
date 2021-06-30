package com.mobina.cocoruncontroller.core.Wifi.Connection;

import com.mobina.cocoruncontroller.layout.WifiFragment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ChatClient extends Thread {
    private String hostname;
    private int port;
    public WriteThread writeThread;
    public WifiFragment.OnUpdateListener listener;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void setUpdateListener(WifiFragment.OnUpdateListener listener) {
        this.listener = listener;
    }


    public void run() {
        try {
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(hostname, port)), 5000);
            System.out.println("It accepted me");
            new ReadThread(socket, this.listener).start();
            this.writeThread = new WriteThread(socket);
            this.writeThread.start();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
    }

    public void sendNewMsg(String msg) {
        this.writeThread.setNewData(msg);
    }
}