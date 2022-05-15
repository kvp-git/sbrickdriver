package com.example.sbrickdriver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

public class BtLE
{
    public Context context;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BtLECallbacks btLECallbacks;
    private BluetoothDevice btDevice;
    private BluetoothGatt btGatt;
    private BluetoothGattCallback btGattCallback;
    private BluetoothGattService serviceControl;
    private BluetoothGattCharacteristic characteristicCommand;
    private boolean btReady;

    public BtLE(Context context, BtLECallbacks btLECallbacks)
    {
        this.context = context;
        this.btLECallbacks = btLECallbacks;
        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btReady = false;
    }

    public boolean connect(Context context, String address)
    {
        btDevice = btAdapter.getRemoteDevice(address);
        if (btDevice == null)
            return false;
        btGattCallback = new BluetoothGattCallback()
        {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
            {
                String s;
                switch(newState)
                {
                    case BluetoothProfile.STATE_CONNECTED:
                        s = "connected";
                        if(!btGatt.discoverServices())
                            disconnect();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        s = "disconnected";
                        disconnected();
                        break;
                    default:
                        s = "" + newState;
                        break;
                }
                Log.e("BtLE", "onConnectionStateChange status=" + status + " newState=" + s);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status)
            {
                Log.e("BtLE", "onServicesDiscovered status=" + status);
                serviceControl = btGatt.getService(UUID.fromString("4dc591b0-857c-41de-b5f1-15abda665b0c"));
                if (serviceControl == null)
                    disconnect();
                characteristicCommand = serviceControl.getCharacteristic(UUID.fromString("2b8cbcc-0e25-4bda-8790-a15f53e6010f"));
                if (characteristicCommand == null)
                    disconnect();
                btReady = true;
                Log.e("BtLE", "READY!");
                btLECallbacks.connected();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
            {
                //Log.d("BtLE", "onCharacteristicRead(" + status + ")");
                // TODO!!! status and error handling
                btLECallbacks.readDone(status, characteristic.getValue());
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
            {
                //Log.e("BtLE", "onCharacteristicWrite(" + status + ")");
                // TODO!!! status and error handling
                btLECallbacks.writeDone(status);
            }
        };
        btGatt = btDevice.connectGatt(context, true, btGattCallback, BluetoothDevice.TRANSPORT_LE);
        return btGatt.connect();
    }

    public boolean disconnected()
    {
        btReady = false;
        if (btGatt == null)
            return false;
        btLECallbacks.disconnected();
        return true;
    }

    public boolean disconnect()
    {
        btReady = false;
        if (btGatt == null)
            return false;
        btGatt.disconnect();
        btLECallbacks.disconnected();
        btGatt = null;
        return true;
    }

    public boolean isReady()
    {
        return btReady;
    }

    public boolean writeCommand(byte[] command)
    {
        if (characteristicCommand == null)
            return false;
        if(!characteristicCommand.setValue(command))
            return false;
        return btGatt.writeCharacteristic(characteristicCommand);
    }

    public boolean readResult()
    {
        if (characteristicCommand == null)
            return false;
        return btGatt.readCharacteristic(characteristicCommand);
    }

}

