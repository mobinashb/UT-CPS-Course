package com.mobina.cocorun.layout;

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

import com.mobina.cocorun.R;
import com.mobina.cocorun.layout.MainActivity;
import com.mobina.cocorun.core.Bluetooth.BluetoothService;
import com.mobina.cocorun.utils.BluetoothConfig;

import java.util.Set;

public class BluetoothFragment extends Fragment {


    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mService = null;

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter adapterPairedDevices;

    private static BluetoothFragment instance;

    public static BluetoothFragment getInstance() {
        return instance;
    }

    public BluetoothFragment(){
        super(R.layout.fragment_bluetooth);
        System.out.println("SALAM BluetoothFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        instance = this;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        FragmentActivity activity = getActivity();
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothConfig.REQUEST_ENABLE_BT);
        } else {
            if (mService == null) initialize();
            adapterPairedDevices = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.support_simple_spinner_dropdown_item);
            initializeDevices();
            showDialog();
        }
    }

    private void initialize() {
        mService = new BluetoothService(getActivity(), mHandler);
        mOutStringBuffer = new StringBuffer("");
    }

    public void initializeDevices()
    {
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();

                adapterPairedDevices.add(deviceName + "\n" + deviceHardwareAddress);
            }
        }
    }

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
                    ((MainActivity)getActivity()).getCommand(readMessage);
                    break;
                case BluetoothConfig.MESSAGE_DEVICE_NAME:
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

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mService != null) {
            if (mService.getState() == BluetoothService.STATE_NONE) {
                mService.start();
            }
            ((MainActivity) getActivity()).setupGame();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) mService.stop();

    }

    private void showDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
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
                ((MainActivity)getActivity()).setRunning(true);
                Toast.makeText(((MainActivity) getActivity()).getApplicationContext(),"device choosen "+device.getName(),Toast.LENGTH_SHORT).show();
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

    public void sendVibration() {
        sendMessage("V");
    }

}