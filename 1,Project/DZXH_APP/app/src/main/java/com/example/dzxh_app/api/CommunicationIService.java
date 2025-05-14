package com.example.dzxh_app.api;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;


public interface CommunicationIService {

    public void callConnectBluetooth2_0(BluetoothDevice bluetoothDevice);
    public void callDisConnectBluetooth2_0();

    public void callConnectBluetooth4_0(BluetoothDevice bluetoothDevice);
    public void callDisConnectBluetooth4_0();

    public void callConnectWifi(String address,int port);
    public void callDisConnectWifi();

    public void callWrite(byte[] bytes);

    public void setHandler(Handler handler);

    public void callStartAutoConnectBluetooth();
    public void callStopAutoConnectBluetooth();

    public void callStartAutoConnectWifi();
    public void callStopAutoConnectWifi();

}
