package com.mobina.cocoruncontroller.core.Wifi.Connection;

import com.mobina.cocoruncontroller.layout.WifiFragment;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatServer extends Thread {
    private int port;
    public WriteThread writeThread;
    public WifiFragment.OnUpdateListener listener;

    public ChatServer(int port) {
        this.port = port;
    }

    public void setUpdateListener(WifiFragment.OnUpdateListener listener) {
        this.listener = listener;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            System.out.println("I accepted it");
            new ReadThread(socket, this.listener).start();
            this.writeThread = new WriteThread(socket);
            this.writeThread.start();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendNewMsg(String msg){
        this.writeThread.setNewData(msg);
    }
}