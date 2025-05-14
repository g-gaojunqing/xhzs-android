package com.example.dzxh_app.util;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

public class MyApplication extends Application {
    //记录已连接设备
    private BluetoothDevice BluetoothDevice=null; //已连接的蓝牙设备
    private int BluetoothType=0;

    private String WifiDevice=null; //已连接的蓝牙设备

    private boolean autoBluetoothState =false; //自动连接标记
    private boolean autoWifiState =false; //自动连接标记

    private boolean NameFilterState=false;//是否过滤没有名字的设备
    private String CharsetName="GBK";//编码格式

    private int[] myVersion=new int[]{1,3,0};

    @Override
    public void onCreate() {
        super.onCreate();
    }
    /**蓝牙*/
    public void setBluetoothDevice(BluetoothDevice b,int type){
        this.BluetoothDevice=b;
        if(BluetoothDevice==null){
            this.BluetoothType=-1;
        }else{
            this.BluetoothType=type;
        }

    }
    public BluetoothDevice getBluetoothDevice(){
        return this.BluetoothDevice;
    }

    public int getBluetoothType(){
        return this.BluetoothType;
    }

    /**wifi*/
    public void setWifiDevice(String s) {
        this.WifiDevice = s;
    }
    public String getWifiDevice(){
        return this.WifiDevice;
    }
    /**自动连接*/
    public void setAutoBluetoothState(boolean b){
        autoBluetoothState =b;
    }
    public boolean getAutoBluetoothState(){
        return autoBluetoothState;
    }
    public void setAutoWifiState(boolean b){
        autoWifiState =b;
    }
    public boolean getAutoWifiState(){
        return autoWifiState;
    }
    /**滤除没有名字设备*/
    public void setNameFilterState(boolean b){
        NameFilterState=b;
    }
    public boolean getNameFilterState(){
        return NameFilterState;
    }
    /**字符编码格式*/
    public void setCharsetName(String s){
        CharsetName=s;
    }
    public String getCharsetName(){
        return CharsetName;
    }
    public void setMyVersion(int[] version){
        myVersion=version;
    }
    public int[] getMyVersion(){
        return  myVersion;
    }


}
