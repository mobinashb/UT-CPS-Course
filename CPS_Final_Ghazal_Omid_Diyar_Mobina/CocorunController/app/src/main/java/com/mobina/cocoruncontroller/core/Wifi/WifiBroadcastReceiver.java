package com.mobina.cocoruncontroller.core.Wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.mobina.cocoruncontroller.MainActivity;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "===WifiBReceiver";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                 MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mActivity, "WIFI P2P ENABLED : " + Constants.P2P_WIFI_ENABLED, Toast.LENGTH_SHORT).show();
                mActivity.setStatusView(Constants.P2P_WIFI_ENABLED);
            } else {
                Toast.makeText(mActivity, "WIFI P2P NOT ENABLED : " + Constants.P2P_WIFI_DISABLED, Toast.LENGTH_SHORT).show();
                mActivity.setStatusView(Constants.P2P_WIFI_DISABLED);
            }
//            mActivity.setStatusView(Constants.DISCOVERY_INITATITED);

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Toast.makeText(mActivity, "WIFI_P2P_PEERS_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
            if (mManager != null) {
                MyPeerListener myPeerListener = new MyPeerListener(mActivity);
                mManager.requestPeers(mChannel, myPeerListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                Toast.makeText(mActivity, "WIFI_P2P_CONNECTION_CHANGED_ACTION : " + Constants.NETWORK_CONNECT, Toast.LENGTH_SHORT).show();
                mActivity.setStatusView(Constants.NETWORK_CONNECT);
            } else {
                Toast.makeText(mActivity, "WIFI_P2P_CONNECTION_CHANGED_ACTION : " + Constants.NETWORK_DISCONNECT, Toast.LENGTH_SHORT).show();
                mActivity.setStatusView(Constants.NETWORK_DISCONNECT);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Toast.makeText(mActivity, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION : " + Constants.NETWORK_DISCONNECT, Toast.LENGTH_SHORT).show();
        }
        else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 100000);
            if( state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED ) {
                mActivity.setStatusView(Constants.DISCOVERY_INITATITED);
            } else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                mActivity.setStatusView(Constants.DISCOVERY_STOPPED);
            }
        }
    }
}
