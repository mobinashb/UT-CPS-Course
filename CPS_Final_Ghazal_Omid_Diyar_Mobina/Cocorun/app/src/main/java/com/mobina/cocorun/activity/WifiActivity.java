package com.mobina.cocorun.activity;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.mobina.cocorun.R;
import com.mobina.cocorun.core.Wifi.Connection.ChatClient;
import com.mobina.cocorun.core.Wifi.Connection.ChatServer;
import com.mobina.cocorun.core.Wifi.Constants;
import com.mobina.cocorun.core.Wifi.MyPeerListener;
import com.mobina.cocorun.core.Wifi.ServiceDiscovery;
import com.mobina.cocorun.core.Wifi.WifiBroadcastReceiver;
import com.mobina.cocorun.utils.GameConfig;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class WifiActivity extends Fragment implements View.OnClickListener, WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "===WifiFragment";
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    WifiP2pDevice device;

    Button buttonDiscoveryStart;
    Button buttonDiscoveryStop;
    Button buttonConnect;
    Button buttonServerStart;
    Button buttonClientStart;
    Button buttonServerStop;
    Button buttonConfigure;
    EditText editTextTextInput;

    ServiceDiscovery serviceDiscovery;

    ListView listViewDevices;
    TextView textViewDiscoveryStatus;
    TextView textViewWifiP2PStatus;
    TextView textViewConnectionStatus;
    TextView textViewReceivedData;
    TextView textViewReceivedDataStatus;
    public static String IP = null;
    public static boolean IS_OWNER = false;

    static boolean stateDiscovery = false;
    static boolean stateWifi = false;
    public static boolean stateConnection = false;

    boolean isServer;
    ChatServer chatServer;
    ChatClient chatClient;

    ArrayAdapter mAdapter;
    WifiP2pDevice[] deviceListItems;

    MainActivity activity;

    public MainActivity getParentActivity(){
        return activity;
    }

    public interface OnUpdateListener {
        public void onUpdate(String data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity)getActivity();
        serviceDiscovery = new ServiceDiscovery();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_wifi, container, false);

        buttonDiscoveryStart = view.findViewById(R.id.main_activity_button_discover_start);
        buttonDiscoveryStop = view.findViewById(R.id.main_activity_button_discover_stop);
        buttonConnect = view.findViewById(R.id.main_activity_button_connect);
        buttonServerStart = view.findViewById(R.id.main_activity_button_server_start);
        buttonServerStop = view.findViewById(R.id.main_activity_button_server_stop);
        buttonClientStart = view.findViewById(R.id.main_activity_button_client_start);
        buttonConfigure = view.findViewById(R.id.main_activity_button_configure);
        listViewDevices = view.findViewById(R.id.main_activity_list_view_devices);
        textViewConnectionStatus = view.findViewById(R.id.main_activiy_textView_connection_status);
        textViewDiscoveryStatus = view.findViewById(R.id.main_activiy_textView_dicovery_status);
        textViewWifiP2PStatus = view.findViewById(R.id.main_activiy_textView_wifi_p2p_status);
        textViewReceivedData = view.findViewById(R.id.main_acitivity_data);
        textViewReceivedDataStatus = view.findViewById(R.id.main_acitivity_received_data);

        editTextTextInput = view.findViewById(R.id.main_acitivity_input_text);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpUI();
        mManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(activity, activity.getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, WifiActivity.this);
        setUpIntentFilter();
        activity.registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.unregisterReceiver(mReceiver);
    }

    private void setUpIntentFilter() {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void setUpUI() {
        buttonServerStart.setOnClickListener(this);
        buttonServerStop.setOnClickListener(this);
        buttonClientStart.setOnClickListener(this);
        buttonConnect.setOnClickListener(this);
        buttonDiscoveryStop.setOnClickListener(this);
        buttonDiscoveryStart.setOnClickListener(this);
        buttonConfigure.setOnClickListener(this);

        buttonClientStart.setVisibility(View.INVISIBLE);
        buttonServerStop.setVisibility(View.INVISIBLE);
        buttonServerStart.setVisibility(View.INVISIBLE);
        editTextTextInput.setVisibility(View.INVISIBLE);
        textViewReceivedDataStatus.setVisibility(View.INVISIBLE);
        textViewReceivedData.setVisibility(View.INVISIBLE);


        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                device = deviceListItems[i];
                Toast.makeText(activity, "Selected device :" + device.deviceName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void discoverPeers() {
        setDeviceList(new ArrayList<WifiP2pDevice>());
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                stateDiscovery = true;
                makeToast("peer discovery started");
                MyPeerListener myPeerListener = new MyPeerListener(WifiActivity.this);
                mManager.requestPeers(mChannel, myPeerListener);
            }

            @Override
            public void onFailure(int i) {
                stateDiscovery = false;
                if (i == WifiP2pManager.P2P_UNSUPPORTED) {
                    makeToast(" peer discovery failed :" + "P2P_UNSUPPORTED");
                } else if (i == WifiP2pManager.ERROR) {
                    makeToast(" peer discovery failed :" + "ERROR");
                } else if (i == WifiP2pManager.BUSY) {
                    makeToast(" peer discovery failed :" + "BUSY");
                }
            }
        });
    }

    private void stopPeerDiscover() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                stateDiscovery = false;
                makeToast("Peer Discovery stopped" );
            }
            @Override
            public void onFailure(int i) {
                makeToast("Stopping Peer Discovery failed" );
            }
        });

    }

    public void makeToast(String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    public void connect (final WifiP2pDevice device) {

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(WifiActivity.this. activity.getApplication(),"Connection successful with " + device.deviceName,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                if(reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    makeToast("Failed establishing connection: " + "P2P_UNSUPPORTED");
                }
                else if( reason == WifiP2pManager.ERROR) {
                    makeToast("Failed establishing connection: " + "ERROR");

                }
                else if( reason == WifiP2pManager.BUSY) {
                    makeToast("Failed establishing connection: " + "BUSY");

                }
            }
        });
    }

    public void setDeviceList(ArrayList<WifiP2pDevice> deviceDetails) {
        deviceListItems = new WifiP2pDevice[deviceDetails.size()];
        String[] deviceNames = new String[deviceDetails.size()];
        for(int i=0 ;i< deviceDetails.size(); i++){
            deviceNames[i] = deviceDetails.get(i).deviceName;
            deviceListItems[i] = deviceDetails.get(i);
        }
        mAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_1, android.R.id.text1,deviceNames);
        listViewDevices.setAdapter(mAdapter);
    }

    public void setStatusView(int status) {

        switch (status)
        {
            case Constants.DISCOVERY_INITIATED:
                stateDiscovery = true;
                textViewDiscoveryStatus.setText("DISCOVERY_INITIATED");
                break;
            case Constants.DISCOVERY_STOPPED:
                stateDiscovery = false;
                textViewDiscoveryStatus.setText("DISCOVERY_STOPPED");
                break;
            case Constants.P2P_WIFI_DISABLED:
                stateWifi = false;
                textViewWifiP2PStatus.setText("P2P_WIFI_DISABLED");
                buttonDiscoveryStart.setEnabled(false);
                buttonDiscoveryStop.setEnabled(false);
                break;
            case Constants.P2P_WIFI_ENABLED:
                stateWifi = true;
                textViewWifiP2PStatus.setText("P2P_WIFI_ENABLED");
                buttonDiscoveryStart.setEnabled(true);
                buttonDiscoveryStop.setEnabled(true);
                break;
            case Constants.NETWORK_CONNECT:
                stateConnection = true;
                makeToast("It's a connect");

                textViewConnectionStatus.setText("Connected");
                break;
            case Constants.NETWORK_DISCONNECT:
                stateConnection = false;
                textViewConnectionStatus.setText("Disconnected");
                makeToast("State is disconnected");
                break;
            default:
                Log.d(WifiActivity.TAG,"Unknown status");
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.main_activity_button_discover_start:
                if(!stateDiscovery) {
                    discoverPeers();
                }
                break;
            case R.id.main_activity_button_discover_stop:
                if(stateDiscovery){
                    stopPeerDiscover();
                }
                break;
            case R.id.main_activity_button_connect:

                if(device == null) {
                    Toast.makeText(activity,"Please discover and select a device",Toast.LENGTH_SHORT).show();
                    return;
                }
                connect(device);
                break;
            case R.id.main_activity_button_server_start:
                if(this.isServer){
                    this.chatServer = new ChatServer(Constants.WIFI_SOCKET_PORT);
                    this.chatServer.setUpdateListener(new OnUpdateListener() {
                        public void onUpdate(String obj) {
                            setReceivedText(obj);
                        }
                    });
                    this.chatServer.start();
                }
                else {
                    this.chatClient = new ChatClient(this.IP, Constants.WIFI_SOCKET_PORT);
                    this.chatClient.setUpdateListener(new OnUpdateListener() {
                        public void onUpdate(String obj) {
                            setReceivedText(obj);
                        }
                    });
                    this.chatClient.start();
                }
                break;
            case R.id.main_activity_button_server_stop:
                // TODO: STOP SERVER
                break;
            case R.id.main_activity_button_client_start:
                System.out.println("LKLLLKKL");
                MainActivity.getInstance().setupGame();
                break;
            case R.id.main_activity_button_configure:
                mManager.requestConnectionInfo(mChannel,this);
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        String hostAddress = wifiP2pInfo.groupOwnerAddress.getHostAddress();
        if (hostAddress == null) hostAddress= "host is null";

        if(IS_OWNER) System.out.println("I am Server");
        else System.out.println("I am Client");

        Log.d(this.TAG,"wifiP2pInfo.groupOwnerAddress.getHostAddress() " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        IP = wifiP2pInfo.groupOwnerAddress.getHostAddress();
        IS_OWNER = wifiP2pInfo.isGroupOwner;

        buttonClientStart.setVisibility(View.VISIBLE);
        editTextTextInput.setVisibility(View.VISIBLE);

        buttonServerStop.setVisibility(View.VISIBLE);
        buttonServerStart.setVisibility(View.VISIBLE);

        textViewReceivedData.setVisibility(View.VISIBLE);
        textViewReceivedDataStatus.setVisibility(View.VISIBLE);

        if(IS_OWNER) this.isServer = true;
        else this.isServer = false;
    }

    public void setReceivedText(final String data) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.getInstance().getCommand(data);
            }
        });
    }
}
