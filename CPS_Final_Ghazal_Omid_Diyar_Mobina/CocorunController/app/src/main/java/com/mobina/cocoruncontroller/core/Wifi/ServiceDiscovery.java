package com.mobina.cocoruncontroller.core.Wifi;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;


public class ServiceDiscovery {
    private static final String TAG = "===ServiceDiscovery";
    private static final int SERVER_PORT = 4545;

    private WifiP2pDnsSdServiceRequest serviceRequest;
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public void discoverService(WifiP2pManager manager, WifiP2pManager.Channel channel) {


        manager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                        Log.d(ServiceDiscovery.TAG,"=========================yessssssssss");
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    @Override
                    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice device) {
                        Log.d(TAG,device.deviceName + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
                    }
                });
        
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d(ServiceDiscovery.TAG,"Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d(ServiceDiscovery.TAG,"Failed adding service discovery request");
                    }
                });
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(ServiceDiscovery.TAG,"Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                Log.d(ServiceDiscovery.TAG,"Service discovery failed");

            }
        });
    }


    public void startRegistrationAndDiscovery(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(ServiceDiscovery.TAG,"Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                Log.d(ServiceDiscovery.TAG,"Failed to add a service");
            }
        });
    }
}
