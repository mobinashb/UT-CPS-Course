package com.mobina.cocorun.core.Wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.mobina.cocorun.activity.WifiActivity;


public class WifiBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "===WifiBReceiver";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiActivity wifiFragment;

    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                 WifiActivity fragment) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.wifiFragment = fragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(wifiFragment, "WIFI P2P ENABLED : " + Constants.P2P_WIFI_ENABLED, Toast.LENGTH_SHORT).show();
                wifiFragment.setStatusView(Constants.P2P_WIFI_ENABLED);
            } else {
                Toast.makeText(wifiFragment, "WIFI P2P NOT ENABLED : " + Constants.P2P_WIFI_DISABLED, Toast.LENGTH_SHORT).show();
                wifiFragment.setStatusView(Constants.P2P_WIFI_DISABLED);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Toast.makeText(wifiFragment, "WIFI_P2P_PEERS_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
            if (mManager != null) {
                MyPeerListener myPeerListener = new MyPeerListener(wifiFragment);
                mManager.requestPeers(mChannel, myPeerListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                Toast.makeText(wifiFragment, "WIFI_P2P_CONNECTION_CHANGED_ACTION : " + Constants.NETWORK_CONNECT, Toast.LENGTH_SHORT).show();
                wifiFragment.setStatusView(Constants.NETWORK_CONNECT);
            } else {
                Toast.makeText(wifiFragment, "WIFI_P2P_CONNECTION_CHANGED_ACTION : " + Constants.NETWORK_DISCONNECT, Toast.LENGTH_SHORT).show();
                wifiFragment.setStatusView(Constants.NETWORK_DISCONNECT);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Toast.makeText(wifiFragment, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION : " + Constants.NETWORK_DISCONNECT, Toast.LENGTH_SHORT).show();
        }
        else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 10000);
            if( state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED ) {
                wifiFragment.setStatusView(Constants.DISCOVERY_INITIATED);
            } else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                wifiFragment.setStatusView(Constants.DISCOVERY_STOPPED);
            }
        }
    }
}
