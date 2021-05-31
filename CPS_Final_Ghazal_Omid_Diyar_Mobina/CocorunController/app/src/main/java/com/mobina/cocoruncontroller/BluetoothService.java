package com.mobina.cocoruncontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Activity {

  public static final int REQUEST_ENABLE_BT=1;
  ListView lv_paired_devices;
  Set<BluetoothDevice> pairedDevices;
  ArrayAdapter adapterPairedDevices;
  BluetoothAdapter bluetoothAdapter;
  public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  public static final int MESSAGE_READ = 0;
  public static final int MESSAGE_WRITE = 1;
  public static final int CONNECTING = 2;
  public static final int CONNECTED = 3;
  public static final int NO_SOCKET_FOUND = 4;

  static String readMessage = null;

  String bluetoothMessage = "00";

  public void initialize() {
    initializeLayout();
    initializeBluetooth();
    startAcceptingConnection();
    initializeClicks();
  }

  @SuppressLint("HandlerLeak")
  Handler mHandler=new Handler()
  {
    @Override
    public void handleMessage(Message msgType) {
      super.handleMessage(msgType);

      switch (msgType.what) {
        case MESSAGE_READ:

          byte[] readbuf = (byte[])msgType.obj;
          String stringReceived = new String(readbuf);

          BluetoothService.handleReadMessage(stringReceived);

          break;
        case MESSAGE_WRITE:

          if(msgType.obj!=null){
            ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msgType.obj);
            connectedThread.write(bluetoothMessage.getBytes());

          }
          break;

        case CONNECTED:
          Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
          break;

        case CONNECTING:
          Toast.makeText(getApplicationContext(),"Connecting...",Toast.LENGTH_SHORT).show();
          break;

        case NO_SOCKET_FOUND:
          Toast.makeText(getApplicationContext(),"No socket found",Toast.LENGTH_SHORT).show();
          break;
      }
    }
  };

  public void sendMessage(String text) {
    bluetoothMessage = text;
    Message msg = mHandler.obtainMessage(MESSAGE_WRITE);
    mHandler.sendMessage(msg);
  }

  public void receiveMessage() {
    Message msg = mHandler.obtainMessage(MESSAGE_READ);
    mHandler.sendMessage(msg);
  }

  public static void handleReadMessage(String text) {
    readMessage = text;
  }

  public void startAcceptingConnection()
  {
    //call this on button click as suited by you

    AcceptThread acceptThread = new AcceptThread();
    acceptThread.start();
    Toast.makeText(getApplicationContext(),"accepting",Toast.LENGTH_SHORT).show();
  }
  public void initializeClicks()
  {
    lv_paired_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
        Object[] objects = pairedDevices.toArray();
        BluetoothDevice device = (BluetoothDevice) objects[position];

        ConnectThread connectThread = new ConnectThread(device);
        connectThread.start();

        Toast.makeText(getApplicationContext(),"device choosen "+device.getName(),Toast.LENGTH_SHORT).show();
      }
    });
  }

  public void initializeLayout()
  {
    lv_paired_devices = (ListView)findViewById(R.id.lv_paired_devices);
    adapterPairedDevices = new ArrayAdapter(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item);
    lv_paired_devices.setAdapter(adapterPairedDevices);
  }

  public void initializeBluetooth()
  {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      // Device doesn't support Bluetooth
      Toast.makeText(getApplicationContext(),"Your Device doesn't support bluetooth. you can play as Single player",Toast.LENGTH_SHORT).show();
      finish();
    }

    if (!bluetoothAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    else {
      pairedDevices = bluetoothAdapter.getBondedDevices();

      if (pairedDevices.size() > 0) {

        for (BluetoothDevice device : pairedDevices) {
          String deviceName = device.getName();
          String deviceHardwareAddress = device.getAddress(); // MAC address

          adapterPairedDevices.add(deviceName + "\n" + deviceHardwareAddress);
        }
      }
    }
  }


  public class AcceptThread extends Thread
  {
    private final BluetoothServerSocket serverSocket;

    public AcceptThread() {
      BluetoothServerSocket tmp = null;
      try {
        // MY_UUID is the app's UUID string, also used by the client code
        tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("NAME",MY_UUID);
      } catch (IOException e) { }
      serverSocket = tmp;
    }

    public void run() {
      BluetoothSocket socket = null;
      // Keep listening until exception occurs or a socket is returned
      while (true) {
        try {
          socket = serverSocket.accept();
        } catch (IOException e) {
          break;
        }

        // If a connection was accepted
        if (socket != null)
        {
          // Do work to manage the connection (in a separate thread)
          mHandler.obtainMessage(CONNECTED).sendToTarget();
        }
      }
    }
  }

  private class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothDevice device) {
      // Use a temporary object that is later assigned to mmSocket,
      // because mmSocket is final
      BluetoothSocket tmp = null;
      mmDevice = device;

      // Get a BluetoothSocket to connect with the given BluetoothDevice
      try {
        // MY_UUID is the app's UUID string, also used by the server code
        tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
      } catch (IOException e) { }
      mmSocket = tmp;
    }

    public void run() {
      // Cancel discovery because it will slow down the connection
      bluetoothAdapter.cancelDiscovery();

      try {
        // Connect the device through the socket. This will block
        // until it succeeds or throws an exception
        mHandler.obtainMessage(CONNECTING).sendToTarget();

        mmSocket.connect();
      } catch (IOException connectException) {
        // Unable to connect; close the socket and get out
        try {
          mmSocket.close();
        } catch (IOException closeException) { }
        return;
      }

      // Do work to manage the connection (in a separate thread)
//            bluetoothMessage = "Initial message"
//            mHandler.obtainMessage(MESSAGE_WRITE,mmSocket).sendToTarget();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
      try {
        mmSocket.close();
      } catch (IOException e) { }
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

      // Get the input and output streams, using temp objects because
      // member streams are final
      try {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException e) { }

      mmInStream = tmpIn;
      mmOutStream = tmpOut;
    }

    public void run() {
      byte[] buffer = new byte[1024];  // buffer store for the stream
      int bytes; // bytes returned from read()

      // Keep listening to the InputStream until an exception occurs
      while (true) {
        try {
          // Read from the InputStream
          bytes = mmInStream.read(buffer);
          // Send the obtained bytes to the UI activity
          mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

        } catch (IOException e) {
          break;
        }
      }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
      try {
        mmOutStream.write(bytes);
      } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
      try {
        mmSocket.close();
      } catch (IOException e) { }
    }
  }
}
