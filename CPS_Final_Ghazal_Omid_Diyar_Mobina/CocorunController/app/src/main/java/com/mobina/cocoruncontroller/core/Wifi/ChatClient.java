package com.mobina.cocoruncontroller.core.Wifi;

import android.os.AsyncTask;

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
    OnUpdateListener listener;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public interface OnUpdateListener {
        public void onUpdate(String data);
    }

    public void setUpdateListener(OnUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Object[] objects) {
        try {
//            Socket socket = new Socket(hostname, port);
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(hostname, port)), 500);

            System.out.println("Connected to the chat server");

            new ReadThread(socket).start();
            this.writeThread = new WriteThread(socket);
            this.writeThread.start();
//            new WriteThread(socket).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
        return null;

    }
    public void sendNewMsg(String msg){
        this.writeThread.setNewData(msg);
    }


//    public static void main(String[] args) {
////        if (args.length < 2) return;
//
//        String hostname = "localhost";
//        int port = 8989;
//
//        ChatClient client = new ChatClient(hostname, port);
//        client.execute();
//    }
}


//import android.os.AsyncTask;
//import android.util.Log;
//
//import com.mobina.cocoruncontroller.MainActivity;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//
///**
// * Created by ash on 16/2/18.
// */
//
//public class ChatClient extends AsyncTask{
//    private static String data;
//    private static final String TAG = "===ClientSocket";
//    private Socket socket;
//    private PrintWriter writer;
//    OnUpdateListener listener;
//    private boolean interrupted = false;
//
//    public ChatClient() {
//
//    }
//
//    public interface OnUpdateListener {
//        public void onUpdate(String data);
//    }
//
//    public void setUpdateListener(OnUpdateListener listener) {
//        this.listener = listener;
//    }
//
//    @Override
//    protected Object doInBackground(Object[] objects) {
//        try {
//            Log.d(ChatClient.TAG," started DoInBackground");
//            this.socket = new Socket();
//            Log.d(ChatClient.TAG," create a socket");
//            this.socket.bind(null);
//            Log.d(ChatClient.TAG," bind to server");
//            this.socket.connect((new InetSocketAddress(MainActivity.IP, 8888)), 500);
//            Log.d(ChatClient.TAG," connected to server");
//
//            while (!interrupted) {
////                this.writer = new PrintWriter(this.socket.getOutputStream());
////                Log.d(ChatClient.TAG," Writer created");
//                BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
//                Log.d(ChatClient.TAG," Reader created");
//                StringBuilder sb = new StringBuilder();
//                String line;
//                Log.d(ChatClient.TAG," Create StringBuilder");
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                    sb.append(line);
//                }
//                Log.d(ChatClient.TAG," End StringBuilder");
//                reader.close();
//                Log.d(ChatClient.TAG, "Completed ReceiveDataTask");
//                String receivedData = sb.toString();
//                System.out.println(receivedData);
//
//                if (listener != null) {
//                    listener.onUpdate(receivedData);
//                }
//            }
//            socket.close();
//            return null;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d(ChatClient.TAG,"IOException occurred");
//        }
//        return null;
//    }
//
//
//    @Override
//    protected void onPostExecute(Object o) {
//        super.onPostExecute(o);
//        Log.d(ChatClient.TAG,"SendDataTask Completed");
//    }
//
//
//    public void sendMsg(String msg){
//        int len;
//        byte buf[]  = new byte[1024];
//        try{
//            OutputStream outputStream = this.socket.getOutputStream();
//            InputStream inputStream = null;
//            inputStream = new ByteArrayInputStream(msg.getBytes());
//
//            while ((len = inputStream.read(buf)) != -1) {
//                outputStream.write(buf, 0, len);
//            }
//            outputStream.close();
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
////        this.writer.println(msg);
////        this.writer.flush();
//    }
//
//}