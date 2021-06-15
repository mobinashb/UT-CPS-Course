package com.mobina.cocoruncontroller.core.Wifi;

import android.os.AsyncTask;

import com.mobina.cocoruncontroller.MainActivity;
import com.mobina.cocoruncontroller.core.Wifi.Connection.ReadThread;
import com.mobina.cocoruncontroller.core.Wifi.Connection.WriteThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ChatClient extends AsyncTask {
    private String hostname;
    private int port;
    public WriteThread writeThread;
    public MainActivity.OnUpdateListener listener;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void setUpdateListener(MainActivity.OnUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Object[] objects) {
        try {
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(hostname, port)), 500);

            System.out.println("Connected to the chat server");

            new ReadThread(socket, this.listener).start();
            this.writeThread = new WriteThread(socket);
            this.writeThread.start();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
        return null;

    }

    public void sendNewMsg(String msg) {
        this.writeThread.setNewData(msg);
    }
}