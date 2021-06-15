package com.mobina.cocoruncontroller.core.Wifi;

import android.os.AsyncTask;

import com.mobina.cocoruncontroller.core.Wifi.Connection.ReadThread;
import com.mobina.cocoruncontroller.core.Wifi.Connection.WriteThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatServer extends AsyncTask {
    private int port;
    public WriteThread writeThread;
    OnUpdateListener listener;

    public ChatServer(int port) {
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

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Chat Server is listening on port " + port);
            Socket socket = serverSocket.accept();
            new ReadThread(socket).start();
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

//    public static void main(String[] args) {
//        int port = 8989;
//
//        ChatServer server = new ChatServer(port);
//        server.execute();
//    }
}


//import android.os.AsyncTask;
//import android.util.Log;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//
///**
// * Created by ash on 16/2/18.
// */
//
//public class ChatServer extends AsyncTask {
//
//    private static final String TAG = "===ServerSocketThread";
//    ServerSocket serverSocket;
//    String receivedData = "null";
//    private int port = 8888;
//    private boolean interrupted = false;
//    OnUpdateListener listener;
//    PrintWriter clientWriter;
//
//    public ChatServer() {
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
//    protected Void doInBackground(Object[] objects) {
//        try {
//
//            Log.d(ChatServer.TAG," started DoInBackground");
//            serverSocket = new ServerSocket(port);
//            Log.d(ChatServer.TAG," create a ServerSocket");
//
//            while (!interrupted) {
//                Log.d(ChatServer.TAG,"Accepting ....");
//                Socket client = serverSocket.accept();
//                Log.d(ChatServer.TAG," a new client accepted");
//                this.clientWriter = new PrintWriter(client.getOutputStream());
//                Log.d(ChatServer.TAG,"Accepted Connection");
//                InputStream inputstream = client.getInputStream();
//                Log.d(ChatServer.TAG,"Create InputStream");
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputstream));
//                Log.d(ChatServer.TAG,"Create BufferReader");
//                StringBuilder sb = new StringBuilder();
//                String line;
//                Log.d(ChatServer.TAG,"Create stringBuilder");
//                while ((line = bufferedReader.readLine()) != null) {
//                    System.out.println(line);
//                    sb.append(line);
//                }
//                Log.d(ChatServer.TAG,"End stringBuilder");
//                bufferedReader.close();
//                Log.d(ChatServer.TAG,"Completed ReceiveDataTask");
//                receivedData = sb.toString();
//                System.out.println(receivedData);
//
//                if (listener != null) {
//                    listener.onUpdate(receivedData);
//                }
//
//                Log.d(ChatServer.TAG," ================ " + receivedData);
//            }
//            serverSocket.close();
//
//            return null;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d(ChatServer.TAG,"IOException occurred");
//        }
//        return null;
//    }
//
//    public void sendMsg(String msg){
//        Log.d(ChatServer.TAG, "Start sendMsg");
//       this.clientWriter.println(msg);
//        Log.d(ChatServer.TAG, "Finish sendMsg");
////       this.clientWriter.flush();
//    }
//
//
//    public boolean isInterrupted() {
//        return interrupted;
//    }
//
//    public void setInterrupted(boolean interrupted) {
//        this.interrupted = interrupted;
//    }
//}

