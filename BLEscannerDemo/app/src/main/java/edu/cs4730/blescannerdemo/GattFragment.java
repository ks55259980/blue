package edu.cs4730.blescannerdemo;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GattFragment extends Fragment {

    private BluetoothDevice device;
    private Context context;

    TextView name, logger;
    String TAG = "GattFrag";
    Button start;


    public GattFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_gatt, container, false);
        name = myView.findViewById(R.id.name);
        logger = myView.findViewById(R.id.gattlogger);
        start = myView.findViewById(R.id.start_gatt);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        return myView;
    }

    //handler which can update the screen and in this case show the html and messages.
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            logthis(msg.getData().getString("msg"));
            return true;
        }

    });

    /**
     * simple method to send messages to the handler.
     */
    public void sendmsg(String str) {
        //handler junk, because thread can't update screen!
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    public void setDevice(BluetoothDevice d) {
        device = d;
//
    }

    public void start() {
        Log.e("connect BTDevice",device.getName());
        name.setText(device.getName() + " " + device.getAddress());
        BluetoothGatt gatt = device.connectGatt(context, false, new BluetoothGattCallback() {
                    // 当连接状态发生改变
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        sendmsg("onConnectionStateChange");
                        super.onConnectionStateChange(gatt, status, newState);
                        if (newState == BluetoothGatt.STATE_CONNECTED)
                            gatt.discoverServices();

                    }

                    // 新的远程设备的服务,特征,描述发生改变时回调
                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        sendmsg("OnserviceDiscovered");
                        super.onServicesDiscovered(gatt, status);
                        //now we can start the characteristic
                        List<BluetoothGattService> services = gatt.getServices();
                        BluetoothGattCharacteristic characteristic = null;
                        for (BluetoothGattService service : services) {

                            sendmsg("UUID IS " + service.getUuid().toString());
                            for (BluetoothGattCharacteristic serviceCharacteristic : service.getCharacteristics()) {

                                characteristic = serviceCharacteristic;

                                sendmsg("char name is " + characteristic.toString());
                                boolean successfullyRead = gatt.readCharacteristic(characteristic);
                                sendmsg("Read characteristic " + successfullyRead);

                                List<BluetoothGattDescriptor> descriptors = serviceCharacteristic.getDescriptors();
                                sendmsg("descriptor size is " + descriptors.size());
                                for (BluetoothGattDescriptor descriptor : descriptors) {
                                    sendmsg("desc name is " + descriptor.toString());
                                    successfullyRead = gatt.readDescriptor(descriptor);
                                    sendmsg("Read descriptor " + successfullyRead);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        sendmsg("OnCharacteristicRead");
                        super.onCharacteristicRead(gatt, characteristic, status);
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            byte[] characteristicValue = characteristic.getValue();
                            sendmsg("char is " + characteristicValue.toString());

                        } else if (status == BluetoothGatt.GATT_READ_NOT_PERMITTED) {
                            sendmsg("No permitted to read a characteristic");
                        } else if (status == BluetoothGatt.GATT_FAILURE) {
                            sendmsg("failed to read a characteristic");
                        }
                    }

                    @Override
                    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                        sendmsg("onDescriptorRead");
                        super.onDescriptorRead(gatt, descriptor, status);
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            byte[] descriptorValue = descriptor.getValue();
                            sendmsg("Descriptor: " + descriptorValue.toString());
                        } else if (status == BluetoothGatt.GATT_READ_NOT_PERMITTED) {
                            sendmsg("No permitted to read a descriptor");
                        } else if (status == BluetoothGatt.GATT_FAILURE) {
                            sendmsg("failed to read a descriptor");
                        }
                    }
                }
        );


    }


    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

}
