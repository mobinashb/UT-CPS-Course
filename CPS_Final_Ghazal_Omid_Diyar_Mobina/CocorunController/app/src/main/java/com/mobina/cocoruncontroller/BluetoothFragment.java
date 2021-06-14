package com.mobina.cocoruncontroller;

import androidx.fragment.app.Fragment;

public class BluetoothFragment extends Fragment {
//    private BluetoothAdapter mBluetoothAdapter = null;
//    private String mConnectedDeviceName = null;
//    private StringBuffer mOutStringBuffer;
//    private BluetoothService mService = null;
//    Set<BluetoothDevice> pairedDevices;
//    ArrayAdapter adapterPairedDevices;
//
//    Timer timer;
//
//    public BluetoothFragment(){
//        super(R.layout.fragment_bluetooth);
//        System.out.println("SALAM BluetoothFragment");
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState){
//        super.onCreate(savedInstanceState);
//
//        System.out.println("SALAM onCreate");
//
//        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        FragmentActivity activity = getActivity();
//        if (this.mBluetoothAdapter == null) {
//            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
//            activity.finish();
//        }
//        System.out.println("Bye onCreate");
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        System.out.println("SALAM onStart");
//        if (this.mBluetoothAdapter == null) {
//            return;
//        }
//
//        if (!this.mBluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, BluetoothConfig.REQUEST_ENABLE_BT);
//            // Otherwise, setup the chat session
//        }
//        else {
//            if (this.mService == null) initialize();
//            adapterPairedDevices = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.support_simple_spinner_dropdown_item);
//            initializeDevices();
//            showDialog();
//        }
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//               double steerAngle = Math.asin(((MainActivity1)getActivity()).getGameRotationTheta().y) * 360;
//                String command = "";
//                if (steerAngle > -5 && steerAngle < 5)
//                  command = "N";
//                else if (steerAngle >= 5 && steerAngle < 65)
//                {
//                  command = String.format("R%d", (int) ((steerAngle - 5)/12)+1);
//                }
//                else if (steerAngle <= -5 && steerAngle > -60)
//                  command = String.format("L%d", (int) (( (-steerAngle) - 5)/12)+1);
//                else
//                  command = "N";
//                sendMessage(command);
//            }
//        }, 1000, 2 * GameConfig.REFRESH_INTERVAL);
//        System.out.println("Bye onStart");
//    }
//
//    @Override
//    public synchronized void onResume() {
//        super.onResume();
//        System.out.println("SALAM onResume");
//        if (mService != null) {
//            if (mService.getState() == BluetoothService.STATE_NONE) {
//                mService.start();
//            }
//        }
//        System.out.println("Bye onResume");
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        System.out.println("SALAM onDestroy");
//        if (mService != null) mService.stop();
//        System.out.println("Bye onDestroy");
//    }
//
//    // The Handler that gets information back from the BluetoothService
//    @SuppressLint("HandlerLeak")
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            System.out.println(msg);
//            switch (msg.what) {
//                case BluetoothConfig.MESSAGE_STATE_CHANGE:
//                    switch (msg.arg1) {
//                        case BluetoothService.STATE_CONNECTED:
//                            Toast.makeText(getActivity(), "connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                            break;
//                        case BluetoothService.STATE_CONNECTING:
//                            Toast.makeText(getActivity(), "connecting", Toast.LENGTH_SHORT).show();
//                            break;
//                        case BluetoothService.STATE_LISTEN:
//                        case BluetoothService.STATE_NONE:
//                            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
//                            break;
//                    }
//                    break;
//                case BluetoothConfig.MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    break;
//                case BluetoothConfig.MESSAGE_READ:
//                    byte[] readBuf = (byte[]) msg.obj;
//                    // construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    Toast.makeText(getActivity(), readMessage, Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothConfig.MESSAGE_DEVICE_NAME:
//                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(BluetoothConfig.DEVICE_NAME);
//                    Toast.makeText(getActivity().getApplicationContext(), "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothConfig.MESSAGE_TOAST:
//                    Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(BluetoothConfig.TOAST),
//                            Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//    };
//
//    private void initialize() {
//        System.out.println("SALAM initialize");
//        this.mService = new BluetoothService(getActivity(), mHandler);
//        this.mOutStringBuffer = new StringBuffer("");
//        System.out.println("Bye initialize");
//    }
//
//    public void initializeDevices()
//    {
//        System.out.println("SALAM initializeDevices");
//        this.pairedDevices = this.mBluetoothAdapter.getBondedDevices();
//        if (this.pairedDevices.size() > 0) {
//
//            for (BluetoothDevice device : this.pairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                this.adapterPairedDevices.add(deviceName + "\n" + deviceHardwareAddress);
//                System.out.println(deviceName + "\t" + deviceHardwareAddress);
//            }
//        }
//        System.out.println("Bye initializeDevices");
//    }
//
//    private void showDialog() {
//        System.out.println("SALAM showDialog");
//        AlertDialog.Builder builderSingle = new AlertDialog.Builder((MainActivity1) getActivity());
//        builderSingle.setTitle("Select a device: ");
//
//        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        builderSingle.setAdapter(adapterPairedDevices, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Object[] objects = pairedDevices.toArray();
//                BluetoothDevice device = (BluetoothDevice) objects[which];
//                mService.connect(device);
//                Toast.makeText(((MainActivity1) getActivity()).getApplicationContext(),"device chosen "+device.getName(),Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//        });
//        builderSingle.show();
//        System.out.println("Bye showDialog");
//    }
//
//    private void sendMessage(String message) {
//
//        if (mService == null)
//            return;
//        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
//            return;
//        }
//        if (message.length() > 0) {
//            byte[] send = message.getBytes();
//            mService.write(send);
//            mOutStringBuffer.setLength(0);
//        }
//    }
}
