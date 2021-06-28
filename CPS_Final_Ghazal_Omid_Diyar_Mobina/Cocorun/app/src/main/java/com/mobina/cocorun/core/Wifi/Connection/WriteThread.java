package com.mobina.cocorun.core.Wifi.Connection;

import java.io.*;
import java.net.*;


public class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private String newData;
    private boolean hasNewData;

    public WriteThread(Socket socket) {
        this.socket = socket;

        hasNewData = false;
        this.newData = "NOTHING";

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        do {
            if(this.hasNewData){
                writer.println(this.newData);
                this.hasNewData = false;
            }
        } while (!this.newData.equals("bye"));

        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }

    public void setNewData(String newData){
        this.newData = newData;
        this.hasNewData = true;
    }
}