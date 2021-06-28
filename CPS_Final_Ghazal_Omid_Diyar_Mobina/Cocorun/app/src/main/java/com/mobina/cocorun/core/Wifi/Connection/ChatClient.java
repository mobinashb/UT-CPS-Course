package com.mobina.cocorun.core.Wifi.Connection;

import android.os.AsyncTask;

import com.mobina.cocorun.activity.WifiActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ChatClient extends Thread {
    private String hostname;
    private int port;
    public WriteThread writeThread;
    public WifiActivity.OnUpdateListener listener;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void setUpdateListener(WifiActivity.OnUpdateListener listener) {
        this.listener = listener;
    }


    public void run() {
        try {
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(hostname, port)), 5000);
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