package com.mobina.cocorun.core.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {


    private static final String NAME = "Cocorun";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

  public static final int MESSAGE_STATE_CHANGE = 1;
  public static final int MESSAGE_READ = 2;
  public static final int MESSAGE_WRITE = 3;
  public static final int MESSAGE_DEVICE_NAME = 4;
  public static final int MESSAGE_TOAST = 5;

  public static final String DEVICE_NAME = "device_name";
  public static final String TOAST = "toast";

  private static final int REQUEST_CONNECT_DEVICE = 1;
  private static final int REQUEST_ENABLE_BT = 2;


    public BluetoothService(Context context, Handler handler) {
      mState = STATE_NONE;
      mHandler = handler;
      mAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    private synchronized void setState(int state) {
      mState = state;

      mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
      return mState;
    }

    public synchronized void start() {

      if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

      if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

      if (mAcceptThread == null) {
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
      }
      setState(STATE_LISTEN);
    }

    public synchronized void connect(BluetoothDevice device) {

      if (mState == STATE_CONNECTING) {
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
      }

      if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

      mConnectThread = new ConnectThread(device);
      mConnectThread.start();
      setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

      if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

      if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

      if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

      mConnectedThread = new ConnectedThread(socket);
      mConnectedThread.start();

      Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
      Bundle bundle = new Bundle();
      bundle.putString(DEVICE_NAME, device.getName());
      msg.setData(bundle);
      mHandler.sendMessage(msg);
      setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
      if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
      if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
      if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
      setState(STATE_NONE);
    }

    public void write(byte[] out) {

      ConnectedThread r;

      synchronized (this) {
        if (mState != STATE_CONNECTED) return;
        r = mConnectedThread;
      }

      r.write(out);
    }

    private void connectionFailed() {
      setState(STATE_LISTEN);

      Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
      Bundle bundle = new Bundle();
      bundle.putString(TOAST, "Unable to connect device");
      msg.setData(bundle);
      mHandler.sendMessage(msg);
    }

    private void connectionLost() {
      setState(STATE_LISTEN);

      Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
      Bundle bundle = new Bundle();
      bundle.putString(TOAST, "Device connection was lost");
      msg.setData(bundle);
      mHandler.sendMessage(msg);
    }

    private class AcceptThread extends Thread {

      private final BluetoothServerSocket mmServerSocket;
      public AcceptThread() {
        BluetoothServerSocket tmp = null;

        try {
          tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
        }
        mmServerSocket = tmp;
      }
      public void run() {
        setName("AcceptThread");
        BluetoothSocket socket = null;

        while (mState != STATE_CONNECTED) {
          try {


            socket = mmServerSocket.accept();
          } catch (IOException e) {
            break;
          }

          if (socket != null) {
            synchronized (BluetoothService.this) {
              switch (mState) {
                case STATE_LISTEN:
                case STATE_CONNECTING:

                  connected(socket, socket.getRemoteDevice());
                  break;
                case STATE_NONE:
                case STATE_CONNECTED:

                  try {
                    socket.close();
                  } catch (IOException e) {
                  }
                  break;
              }
            }
          }
        }
      }
      public void cancel() {
        try {
          mmServerSocket.close();
        } catch (IOException e) {
        }
      }
    }

    private class ConnectThread extends Thread {
      private final BluetoothSocket mmSocket;
      private final BluetoothDevice mmDevice;
      public ConnectThread(BluetoothDevice device) {
        mmDevice = device;
        BluetoothSocket tmp = null;


        try {
          tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
        }
        mmSocket = tmp;
      }
      public void run() {
        setName("ConnectThread");

        mAdapter.cancelDiscovery();

        try {


          mmSocket.connect();
        } catch (IOException e) {
          connectionFailed();

          try {
            mmSocket.close();
          } catch (IOException e2) {
          }

          BluetoothService.this.start();
          return;
        }

        synchronized (BluetoothService.this) {
          mConnectThread = null;
        }

        connected(mmSocket, mmDevice);
      }
      public void cancel() {
        try {
          mmSocket.close();
        } catch (IOException e) {
        }
      }
    }

    private class ConnectedThread extends Thread {
      private final BluetoothSocket mmSocket;
      private final InputStream mmInStream;
      private final OutputStream mmOutStream;
      public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
          tmpIn = socket.getInputStream();
          tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
      }
      public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
          try {

            bytes = mmInStream.read(buffer);

            mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                .sendToTarget();
          } catch (IOException e) {
            connectionLost();
            break;
          }
        }
      }

      public void write(byte[] buffer) {
        try {
          mmOutStream.write(buffer);

          mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
              .sendToTarget();
        } catch (IOException e) {
        }
      }
      public void cancel() {
        try {
          mmSocket.close();
        } catch (IOException e) {
        }
      }
    }


}
