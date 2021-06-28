package com.mobina.cocorun.core.Wifi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


import com.mobina.cocorun.activity.WifiActivity;

import java.util.ArrayList;
import java.util.List;

public class MyPeerListener implements WifiP2pManager.PeerListListener {
    public static final String TAG = "===MyPeerListener";
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    public WifiActivity fragment;

    public MyPeerListener(WifiActivity fragment) {
        this.fragment = fragment;
        Log.d(MyPeerListener.TAG,"MyPeerListener object created");

    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        ArrayList<WifiP2pDevice> deviceDetails = new ArrayList<>();

        Log.d(MyPeerListener.TAG, "OnPeerAvailable()");
        if(wifiP2pDeviceList != null ) {
            Log.d(MyPeerListener.TAG, "OnPeerAvailable1");
            if(wifiP2pDeviceList.getDeviceList().size() == 0) {
                Log.d(MyPeerListener.TAG, "wifiP2pDeviceList size is zero");
                return;
            }

            for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                deviceDetails.add(device);
                Log.d(MyPeerListener.TAG, "Found device :" + device.deviceName + " " + device.deviceAddress);
            }
            if(fragment != null) {
                fragment.setDeviceList(deviceDetails);
            }

        }
        else {
            Log.d(MyPeerListener.TAG, "wifiP2pDeviceList is null");

        }
    }
}

