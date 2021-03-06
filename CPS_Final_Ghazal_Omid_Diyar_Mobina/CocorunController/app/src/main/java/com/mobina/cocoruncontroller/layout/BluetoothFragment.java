package com.mobina.cocoruncontroller.layout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.mobina.cocoruncontroller.R;
import com.mobina.cocoruncontroller.core.Bluetooth.BluetoothService;
import com.mobina.cocoruncontroller.utils.BluetoothConfig;
import com.mobina.cocoruncontroller.utils.GameConfig;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothFragment extends Fragment {
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    private BluetoothService mService = null;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter adapterPairedDevices;

    Timer timer;

    public BluetoothFragment(){
        super(R.layout.fragment_bluetooth);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        FragmentActivity activity = getActivity();
        if (this.mBluetoothAdapter == null) {
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.mBluetoothAdapter == null) {
            return;
        }

        if (!this.mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothConfig.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
        else {
            if (this.mService == null) initialize();
            adapterPairedDevices = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.support_simple_spinner_dropdown_item);
            initializeDevices();
            showDialog();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               double steerAngle = Math.asin(((MainActivity)getActivity()).getGameRotationTheta().y) * 360;
                String command = "";
                if (steerAngle > -5 && steerAngle < 5)
                  command = "N";
                else if (steerAngle >= 5 && steerAngle < 65)
                {
                  command = String.format("R%d", (int) ((steerAngle - 5)/12)+1);
                }
                else if (steerAngle <= -5 && steerAngle > -60)
                  command = String.format("L%d", (int) (( (-steerAngle) - 5)/12)+1);
                else
                  command = "N";
                sendMessage(command);
            }
        }, 1000, 2 * GameConfig.REFRESH_INTERVAL);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mService != null) {
            if (mService.getState() == BluetoothService.STATE_NONE) {
                mService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) mService.stop();
    }

    // The Handler that gets information back from the BluetoothService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothConfig.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getActivity(), "connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getActivity(), "connecting", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case BluetoothConfig.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case BluetoothConfig.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    MainActivity.getInstance().vibrate(readMessage);
                    break;
                case BluetoothConfig.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothConfig.DEVICE_NAME);
                    Toast.makeText(getActivity().getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothConfig.MESSAGE_TOAST:
                    Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(BluetoothConfig.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void initialize() {
        this.mService = new BluetoothService(getActivity(), mHandler);
        this.mOutStringBuffer = new StringBuffer("");
    }

    public void initializeDevices()
    {
        this.pairedDevices = this.mBluetoothAdapter.getBondedDevices();
        if (this.pairedDevices.size() > 0) {

            for (BluetoothDevice device : this.pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                this.adapterPairedDevices.add(deviceName + "\n" + deviceHardwareAddress);
            }
        }
    }

    private void showDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder((MainActivity) getActivity());
        builderSingle.setTitle("Select a device: ");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(adapterPairedDevices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Object[] objects = pairedDevices.toArray();
                BluetoothDevice device = (BluetoothDevice) objects[which];
                mService.connect(device);
                Toast.makeText(((MainActivity) getActivity()).getApplicationContext(),"device chosen "+device.getName(),Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }

    private void sendMessage(String message) {

        if (mService == null)
            return;
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mService.write(send);
            mOutStringBuffer.setLength(0);
        }
    }
}
