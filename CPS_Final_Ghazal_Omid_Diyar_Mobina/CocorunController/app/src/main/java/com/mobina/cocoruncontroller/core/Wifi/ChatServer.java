package com.mobina.cocoruncontroller.core.Wifi;

import android.os.AsyncTask;

import com.mobina.cocoruncontroller.MainActivity;
import com.mobina.cocoruncontroller.core.Wifi.Connection.ReadThread;
import com.mobina.cocoruncontroller.core.Wifi.Connection.WriteThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatServer extends AsyncTask {
    private int port;
    public WriteThread writeThread;
    public MainActivity.OnUpdateListener listener;

    public ChatServer(int port) {
        this.port = port;
    }

    public void setUpdateListener(MainActivity.OnUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Object[] objects) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Chat Server is listening on port " + port);
            Socket socket = serverSocket.accept();
            new ReadThread(socket, this.listener).start();
            this.writeThread = new WriteThread(socket);
            this.writeThread.start();

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public void sendNewMsg(String msg){
        this.writeThread.setNewData(msg);
    }
}