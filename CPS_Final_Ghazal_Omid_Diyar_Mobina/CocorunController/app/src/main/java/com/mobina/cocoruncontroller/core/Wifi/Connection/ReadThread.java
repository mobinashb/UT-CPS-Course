package com.mobina.cocoruncontroller.core.Wifi.Connection;

import com.mobina.cocoruncontroller.layout.WifiFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    public WifiFragment.OnUpdateListener listener;

    public ReadThread(Socket socket, WifiFragment.OnUpdateListener listener) {
        this.socket = socket;
        this.listener = listener;

        try {
            InputStream input = this.socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                if (listener != null) {
                    listener.onUpdate(response);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
}
