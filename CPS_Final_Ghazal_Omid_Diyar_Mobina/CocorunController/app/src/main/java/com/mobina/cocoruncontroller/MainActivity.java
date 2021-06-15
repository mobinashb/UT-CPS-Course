package com.mobina.cocoruncontroller;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobina.cocoruncontroller.core.Wifi.ChatClient;
import com.mobina.cocoruncontroller.core.Wifi.ChatServer;
import com.mobina.cocoruncontroller.core.Wifi.Constants;
import com.mobina.cocoruncontroller.core.Wifi.MyPeerListener;
import com.mobina.cocoruncontroller.core.Wifi.ServiceDiscovery;
import com.mobina.cocoruncontroller.core.Wifi.WifiBroadcastReceiver;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "===MainActivity";
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
    Button buttonClientStop;
    Button buttonServerStop;
    Button buttonConfigure;
    EditText editTextTextInput;

    ServiceDiscovery serviceDisvcoery;

    ListView listViewDevices;
    TextView textViewDiscoveryStatus;
    TextView textViewWifiP2PStatus;
    TextView textViewConnectionStatus;
    TextView textViewReceivedData;
    TextView textViewReceivedDataStatus;
    public static String IP = null;
    public static boolean IS_OWNER = false;

    static boolean  stateDiscovery = false;
    static boolean stateWifi = false;
    public static boolean stateConnection = false;

    boolean isServer;
    ChatServer chatServer;
    ChatClient chatClient;
//    private ChatConnection connection;

//    ServerSocketThread serverSocketThread;

    ArrayAdapter mAdapter;
    WifiP2pDevice[] deviceListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceDisvcoery = new ServiceDiscovery();
        setUpUI();
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, this);

//        serverSocketThread = new ServerSocketThread();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpIntentFilter();
        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
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
        buttonDiscoveryStart = findViewById(R.id.main_activity_button_discover_start);
        buttonDiscoveryStop = findViewById(R.id.main_activity_button_discover_stop);
        buttonConnect = findViewById(R.id.main_activity_button_connect);
        buttonServerStart = findViewById(R.id.main_activity_button_server_start);
        buttonServerStop = findViewById(R.id.main_activity_button_server_stop);
        buttonClientStart = findViewById(R.id.main_activity_button_client_start);
        buttonClientStop = findViewById(R.id.main_activity_button_client_stop);
        buttonConfigure = findViewById(R.id.main_activity_button_configure);
        listViewDevices = findViewById(R.id.main_activity_list_view_devices);
        textViewConnectionStatus = findViewById(R.id.main_activiy_textView_connection_status);
        textViewDiscoveryStatus = findViewById(R.id.main_activiy_textView_dicovery_status);
        textViewWifiP2PStatus = findViewById(R.id.main_activiy_textView_wifi_p2p_status);
        textViewReceivedData = findViewById(R.id.main_acitivity_data);
        textViewReceivedDataStatus = findViewById(R.id.main_acitivity_received_data);

        editTextTextInput = findViewById(R.id.main_acitivity_input_text);

        buttonServerStart.setOnClickListener(this);
        buttonServerStop.setOnClickListener(this);
        buttonClientStart.setOnClickListener(this);
        buttonClientStop.setOnClickListener(this);
        buttonConnect.setOnClickListener(this);
        buttonDiscoveryStop.setOnClickListener(this);
        buttonDiscoveryStart.setOnClickListener(this);
        buttonConfigure.setOnClickListener(this);

        buttonClientStop.setVisibility(View.INVISIBLE);
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
                Toast.makeText(MainActivity.this,"Selected device :"+ device.deviceName ,Toast.LENGTH_SHORT).show();
            }
        });




    }

    private void discoverPeers()
    {
//        Log.d(MainActivity.TAG,"discoverPeers()");
        setDeviceList(new ArrayList<WifiP2pDevice>());
        System.out.println("HIHI");
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                System.out.println("HIHI1");
                stateDiscovery = true;
//                Log.d(MainActivity.TAG,"peer discovery started");
                makeToast("peer discovery started");
                MyPeerListener myPeerListener = new MyPeerListener(MainActivity.this);
                mManager.requestPeers(mChannel,myPeerListener);

            }

            @Override
            public void onFailure(int i) {
                System.out.println("HIHI2");
                stateDiscovery = false;
                if (i == WifiP2pManager.P2P_UNSUPPORTED) {
//                    Log.d(MainActivity.TAG," peer discovery failed :" + "P2P_UNSUPPORTED");
                    makeToast(" peer discovery failed :" + "P2P_UNSUPPORTED");

                } else if (i == WifiP2pManager.ERROR) {
//                    Log.d(MainActivity.TAG," peer discovery failed :" + "ERROR");
                    makeToast(" peer discovery failed :" + "ERROR");

                } else if (i == WifiP2pManager.BUSY) {
//                    Log.d(MainActivity.TAG," peer discovery failed :" + "BUSY");
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
//                Log.d(MainActivity.TAG,"Peer Discovery stopped");
                makeToast("Peer Discovery stopped" );
                //buttonDiscoveryStop.setEnabled(false);

            }

            @Override
            public void onFailure(int i) {
//                Log.d(MainActivity.TAG,"Stopping Peer Discovery failed");
                makeToast("Stopping Peer Discovery failed" );
                //buttonDiscoveryStop.setEnabled(true);

            }
        });

    }

    public void makeToast(String msg) {
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }

    public void connect (final WifiP2pDevice device) {
        // Picking the first device found on the network.

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

//        Log.d(MainActivity.TAG,"Trying to connect : " +device.deviceName);
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
//                Log.d(MainActivity.TAG, "Connected to :" + device.deviceName);
                Toast.makeText(getApplication(),"Connection successful with " + device.deviceName,Toast.LENGTH_SHORT).show();
                //setDeviceList(new ArrayList<WifiP2pDevice>());
            }

            @Override
            public void onFailure(int reason) {
                if(reason == WifiP2pManager.P2P_UNSUPPORTED) {
//                    Log.d(MainActivity.TAG, "P2P_UNSUPPORTED");
                    makeToast("Failed establishing connection: " + "P2P_UNSUPPORTED");
                }
                else if( reason == WifiP2pManager.ERROR) {
//                    Log.d(MainActivity.TAG, "Conneciton falied : ERROR");
                    makeToast("Failed establishing connection: " + "ERROR");

                }
                else if( reason == WifiP2pManager.BUSY) {
//                    Log.d(MainActivity.TAG, "Conneciton falied : BUSY");
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
//            System.out.println(deviceListItems[i]);
        }
        mAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,android.R.id.text1,deviceNames);
        listViewDevices.setAdapter(mAdapter);
    }

    public void setStatusView(int status) {

        switch (status)
        {
            case Constants.DISCOVERY_INITATITED:
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
                Log.d(MainActivity.TAG,"Unknown status");
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.main_activity_button_discover_start:
                System.out.println("LKLKLKLKL");
                if(!stateDiscovery) {
                    System.out.println("bnbn");
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
                    Toast.makeText(MainActivity.this,"Please discover and select a device",Toast.LENGTH_SHORT).show();
                    return;
                }
                connect(device);
                break;
            case R.id.main_activity_button_server_start:
                if(this.isServer){
                    this.chatServer = new ChatServer(8888);
                    this.chatServer.setUpdateListener(new ChatServer.OnUpdateListener() {
                        public void onUpdate(String obj) {
                            setReceivedText(obj);
                        }
                    });
                    this.chatServer.execute();
                }
                else {
                    this.chatClient = new ChatClient(MainActivity.this.IP, 8888);
                    this.chatClient.setUpdateListener(new ChatClient.OnUpdateListener() {
                        public void onUpdate(String obj) {
                            setReceivedText(obj);
                        }
                    });
                    this.chatClient.execute();
                }
//                if(this.isServer) {
//                    System.out.println("I AM SERVER AND OWNER");
//                    this.chatServer = new ChatServer();
//                    this.chatServer.setUpdateListener(new ChatServer.OnUpdateListener() {
//                        public void onUpdate(String obj) {
//                            setReceivedText(obj);
//                        }
//                    });
//                    this.chatServer.execute();
//                }
//                else{
//                    System.out.println("I AM CLIENT");
//                    this.chatClient = new ChatClient();
//                    this.chatClient.setUpdateListener(new ChatClient.OnUpdateListener() {
//                        public void onUpdate(String obj) {
//                            setReceivedText(obj);
//                        }
//                    });
//                    this.chatClient.execute();
//                }
//                serverSocketThread = new ServerSocketThread();
//                serverSocketThread. setUpdateListener(new ServerSocketThread.OnUpdateListener() {
//                    public void onUpdate(String obj) {
//                        setReceivedText(obj);
//                    }
//                });
//                serverSocketThread.execute();
                break;
            case R.id.main_activity_button_server_stop:
//                if(serverSocketThread != null) {
//                    serverSocketThread.setInterrupted(true);
//                } else {
//                    Log.d(MainActivity.TAG,"serverSocketThread is null");
//                }
                //makeToast("Yet to do...");
                break;
            case R.id.main_activity_button_client_start:
                //serviceDisvcoery.startRegistrationAndDiscovery(mManager,mChannel);
                String dataToSend = editTextTextInput.getText().toString();
                System.out.println("I am Sending : " + dataToSend);
                if(this.isServer) this.chatServer.sendNewMsg(dataToSend);
                else this.chatClient.sendNewMsg(dataToSend);
//                ClientSocket clientSocket = new ClientSocket(dataToSend);
//                clientSocket.execute();
                break;
            case R.id.main_activity_button_configure:
                mManager.requestConnectionInfo(mChannel,this);
                break;
            case R.id.main_activity_button_client_stop:
                makeToast("Yet to do");
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        String hostAddress= wifiP2pInfo.groupOwnerAddress.getHostAddress();
        if (hostAddress == null) hostAddress= "host is null";

        //makeToast("Am I group owner : " + String.valueOf(wifiP2pInfo.isGroupOwner));
        //makeToast(hostAddress);
        Log.d(MainActivity.TAG,"wifiP2pInfo.groupOwnerAddress.getHostAddress() " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        IP = wifiP2pInfo.groupOwnerAddress.getHostAddress();
        IS_OWNER = wifiP2pInfo.isGroupOwner;
        System.out.println(IS_OWNER);
        System.out.println(wifiP2pInfo.groupOwnerAddress);

        buttonClientStop.setVisibility(View.VISIBLE);
        buttonClientStart.setVisibility(View.VISIBLE);
        editTextTextInput.setVisibility(View.VISIBLE);

        buttonServerStop.setVisibility(View.VISIBLE);
        buttonServerStart.setVisibility(View.VISIBLE);

        textViewReceivedData.setVisibility(View.VISIBLE);
        textViewReceivedDataStatus.setVisibility(View.VISIBLE);

        if(IS_OWNER) {
//            buttonClientStop.setVisibility(View.GONE);
//            buttonClientStart.setVisibility(View.GONE);
//            editTextTextInput.setVisibility(View.VISIBLE);
//
//            buttonServerStop.setVisibility(View.VISIBLE);
//            buttonServerStart.setVisibility(View.VISIBLE);
//
//            textViewReceivedData.setVisibility(View.VISIBLE);
//            textViewReceivedDataStatus.setVisibility(View.VISIBLE);
            this.isServer = true;
        } else {
//            buttonClientStop.setVisibility(View.VISIBLE);
//            buttonClientStart.setVisibility(View.VISIBLE);
//            editTextTextInput.setVisibility(View.VISIBLE);
//            buttonServerStop.setVisibility(View.GONE);
//            buttonServerStart.setVisibility(View.GONE);
//            textViewReceivedData.setVisibility(View.GONE);
//            textViewReceivedDataStatus.setVisibility(View.GONE);
            this.isServer = false;
        }

        makeToast("Configuration Completed");
    }

    public void setReceivedText(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewReceivedData.setText(data);
            }
        });
    }
}
