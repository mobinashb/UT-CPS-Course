package com.mobina.cocorun.core.Wifi.Connection;

import android.os.AsyncTask;


import com.mobina.cocorun.activity.WifiActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatServer extends AsyncTask {
    private int port;
    public WriteThread writeThread;
    public WifiActivity.OnUpdateListener listener;

    public ChatServer(int port) {
        this.port = port;
    }

    public void setUpdateListener(WifiActivity.OnUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Object[] objects) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            new ReadThread(socket, this.listener).start();
            this.writeThread = new WriteThread(socket);
            this.writeThread.start();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void sendNewMsg(String msg){
        this.writeThread.setNewData(msg);
    }
}