package com.example.dzxh_app.api;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.dzxh_app.util.Constants;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.view.MyHintDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CommunicationService extends Service {
    private Intent mIntent;
    private MyApplication myApplication;//APP全局变量
    private Handler mHandler; //发送信息至activity
    //自动连接
    private Timer mScanTimer; //定时扫描
    private BluetoothAdapter mBluetoothAdapter;
    private boolean blueScanning =false; //记录是否正在扫描，避免广播重复解绑
    private final Handler mAutoHandler=new Handler(); //检测自动连接是否成功
    private Timer mWifiTimer; //定时扫描
    private BluetoothDevice mAutoBluetoothDevice;
    private String mAutoAddress;
    private int mAutoPort;

    private String wifiName;
    private boolean wifiConnecting;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyCommunicationBinder();
    }

    @Override
    public void onCreate() {
        myApplication=(MyApplication) this.getApplication();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("CommunicationService","服务开始了");
        ContentBroadcastInit();// 传递接收到的数据
        RegisterActionBroadcast();//监听蓝牙是否断开的广播
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    /**
     * 传递接收到的数据
     */
    private void ContentBroadcastInit(){
        mIntent = new Intent();
        mIntent.setAction("com.example.dzxh_app.content");
    }

    /**
     * 写数据
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void WriteChar(byte[] bytes){
        int type=myApplication.getBluetoothType();
        if(type==BluetoothDevice.DEVICE_TYPE_CLASSIC){
            WriteBluetooth2_0(bytes);
        }else if(type==BluetoothDevice.DEVICE_TYPE_LE){
            WriteBluetooth4_0(bytes);
        }else if(type==BluetoothDevice.DEVICE_TYPE_DUAL){
            WriteBluetooth2_0(bytes);
        }
        if(myApplication.getWifiDevice()!=null){
            WriteWifi(bytes);
        }
    }
    /**
     * 注册蓝牙断开监控广播
     */
    private void RegisterActionBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mActionBroadcastReceiver, filter);
    }
    private  BroadcastReceiver mActionBroadcastReceiver=new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert bluetoothDevice != null;
                if(bluetoothDevice.equals(myApplication.getBluetoothDevice())){
                    myApplication.setBluetoothDevice(null,-1);
                    SendMessage(Constants.DEVICE_ERROR_DISCONNECT,"蓝牙连接断开了");
                    //自动连接开 蓝牙4.0 wifi未连接
                    if(myApplication.getAutoBluetoothState()){
                        mAutoBluetoothDevice =bluetoothDevice; //用于自动连接
                        StartAutoConnectBluetooth();
                    }else{
                        DisHintDialog("蓝牙连接断开了");
                    }
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void DisHintDialog(String string){
        MyHintDialog myHintDialog = new MyHintDialog(getApplicationContext());
        myHintDialog.setCancelable(false);//点击屏幕外可取消
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){//6.0 　　　　　　
            Objects.requireNonNull(myHintDialog.getWindow()).setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else {
            Objects.requireNonNull(myHintDialog.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        myHintDialog.show();
        myHintDialog.setText(string);
        myHintDialog.setOnDialogClickListener(new MyHintDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
            }
        });
    }

    /**
     * 开始自动连接
     */
    private void StartAutoConnectBluetooth(){
        //蓝牙是否可用
        if(mBluetoothAdapter.isEnabled()){
            if(mBluetoothAdapter.startDiscovery()) {
                RegisterScanBroadcast();
                blueScanning =true;
            }
        }else{
            return;
        }
        //每12S重新扫描一次
        mScanTimer = new Timer();
        mScanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (myApplication.getAutoBluetoothState()&&(myApplication.getBluetoothDevice()==null)){
                    mBluetoothAdapter.cancelDiscovery();
                    mBluetoothAdapter.startDiscovery();
                }else{
                    //停止扫描
                    if(blueScanning){//有可能在蓝牙连接界面被手动叫停
                        mBluetoothAdapter.cancelDiscovery();
                        unregisterReceiver(mScanBroadcastReceiver);
                        blueScanning =false;
                        this.cancel();
                    }
                }
            }
        },12000,12000);
    }

    /**
     * 停止自动连接
     */
    private void StopAutoConnectBluetooth(){
        mAutoHandler.removeMessages(0);//停止连接是否成功的检测
        //防止多次注销广播程序卡死（已经开始连接后进入连接页面再次调用此函数）
        if(blueScanning){
            mBluetoothAdapter.cancelDiscovery();
            unregisterReceiver(mScanBroadcastReceiver);
            blueScanning =false;
            mScanTimer.cancel();
        }
    }

    /**
     *  注册广播，监控扫描状态
     */
    private void RegisterScanBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//发现结束
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//发现结束
        registerReceiver(mScanBroadcastReceiver,filter);
    }
    private final BroadcastReceiver mScanBroadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice bluetoothDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert bluetoothDevice != null;
                if(bluetoothDevice.equals(mAutoBluetoothDevice)){
                    StopAutoConnectBluetooth();
                    if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                        ConnectBluetooth2_0(bluetoothDevice);
                    }else if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_LE) {
                        ConnectBluetooth4_0(bluetoothDevice);
                    }else if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_DUAL){
                        ConnectBluetooth2_0(bluetoothDevice);
                    }
                    //若未连接成功，重新开始自动连接
                    mAutoHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (myApplication.getAutoBluetoothState()&&mAutoBluetoothDevice!=null){
                                StartAutoConnectBluetooth();
                            }
                        }
                    },15000);
                }
            }
        }
    };

    private void StartAutoConnectWifi() {
        //每5S重新连接
        wifiConnecting=true;
        mWifiTimer = new Timer();
        mWifiTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(myApplication.getAutoWifiState()&&(mAutoAddress!=null)){
                    if(GetWifiName().equals(wifiName)){
                        ConnectWifi(mAutoAddress,mAutoPort);
                    }
                }else{
                    if(wifiConnecting) {
                        wifiConnecting=false;
                        this.cancel();
                    }
                }
            }
        },5000,5000);
    }

    private void StopAutoConnectWifi() {
        if (wifiConnecting){
            wifiConnecting=false;
            mWifiTimer.cancel();
        }
    }

    /**
     * 发送信息到Activity
     * @param type 状态
     * @param string 语句
     */
    private void SendMessage(int type,String string){
        if (mHandler != null){
            Message message = mHandler.obtainMessage();
            message.what =1;
            message.obj=string;
            mHandler.sendMessage(message);
        }
        if(type==Constants.DEVICE_BLUETOOTH_CONNECT_SUCCEED){
            mAutoBluetoothDevice=null;
        }
        if(type==Constants.DEVICE_WIFI_CONNECT_SUCCEED){
            mAutoAddress=null;
            mAutoPort=0;
        }
    }

    /*********************************************************** 以下为蓝牙2.0 ******************************************************************/
    ReceiverData receiverData;
    private static BluetoothSocket mBluetoothSocket=null;
    private static OutputStream  outputStream ;
    private void ConnectBluetooth2_0(BluetoothDevice bluetoothDevice){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//经典蓝牙的连接UUID
                } catch (IOException e) {
                    e.printStackTrace();
                    myApplication.setBluetoothDevice(null,-1);
                    SendMessage(Constants.DEVICE_CONNECT_FAIL,"蓝牙连接失败");
                    return;
                }
                try {
                    mBluetoothSocket.connect();
                    receiverData=new ReceiverData();
                    receiverData.start();
                    outputStream = mBluetoothSocket.getOutputStream();
                    myApplication.setBluetoothDevice(bluetoothDevice,BluetoothDevice.DEVICE_TYPE_CLASSIC);
                    SendMessage(Constants.DEVICE_BLUETOOTH_CONNECT_SUCCEED,"蓝牙连接成功<经典蓝牙>");
                } catch (Exception e) {
                    e.printStackTrace();
                    myApplication.setBluetoothDevice(null,-1);
                    SendMessage(Constants.DEVICE_CONNECT_FAIL,"蓝牙连接失败");
                }
            }
        }).start();
    }

    private void DisConnectBluetooth2_0(){
        myApplication.setBluetoothDevice(null,-1); //清除已连接2.0设备
        try {
            mBluetoothSocket.close();
            SendMessage(Constants.DEVICE_DISCONNECT,null); //回传信息
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 开启接收线程
     */
    private class ReceiverData extends Thread{
        InputStream inputStream=null;
        private ReceiverData(){
            try {
                inputStream = mBluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            int len;
            byte[] buffer=new byte[1024];
            byte[] bytes;
            try {
                while(true){
                    len=inputStream.read(buffer);
                    if(len!=0){
                        bytes=new byte[len];
                        System.arraycopy(buffer,0,bytes,0,len);
                        mIntent.putExtra("value",bytes);
                        sendBroadcast(mIntent);
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**写数据*/

    public void WriteBluetooth2_0(byte[] bytes){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write(bytes);
                    outputStream.flush();
                } catch (Exception e) {
                    Log.d("CommunicationService","连接断开了");
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /*********************************************************** 以下为蓝牙4.0 ******************************************************************/

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic,mCharacteristicTX;
    private ExecutorService mExecutorService;
    private final Handler mTimeHandler = new Handler();
    private BluetoothDevice bluetoothDevice4_0;
    private volatile boolean sendDataSign = true;//发送的标志位

    private UUID UUID_read_chara;
    private UUID UUID_read_service;
    private UUID UUID_write_chara;
    private UUID UUID_write_service;
    private UUID UUID_notify_chara;
    private UUID UUID_notify_service;
    private UUID UUID_indicate_chara;
    private UUID UUID_indicate_service;

    public final static UUID UUID_SERVICE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CHAR =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_NOTIFY_DESCRIPTOR =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**连接设备*/

    private void ConnectBluetooth4_0(BluetoothDevice bluetoothDevice){
        bluetoothDevice4_0=bluetoothDevice;
        if(mBluetoothGatt!=null){
            mBluetoothGatt.close();
            mBluetoothGatt=null;
        }
        mBluetoothGatt = bluetoothDevice.connectGatt(CommunicationService.this, false, bluetoothGattCallback); //这里使用自动连接可能会影响连接成功率与速度
    }
    /**断开蓝牙4.0*/
    private void DisConnectBluetooth4_0(){
        if(mBluetoothGatt!=null){
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt=null;
        }
        myApplication.setBluetoothDevice(null,-1); //清除已连接4.0设备
        SendMessage(Constants.DEVICE_DISCONNECT,null);
    }

    private final BluetoothGattCallback bluetoothGattCallback =new BluetoothGattCallback() {
        /**断开或链接 状态发生变化时使用*/
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(status == 133) {
                myApplication.setBluetoothDevice(null,-1);
                SendMessage(Constants.DEVICE_CONNECT_FAIL,"蓝牙连接失败 error:133");
            }
            if(newState== BluetoothProfile.STATE_CONNECTED){
                mTimeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothGatt.discoverServices();//扫描服务
                    }
                },1000);//坑：设置延迟时间过短，很可能发现不了服务
                mTimeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myApplication.setBluetoothDevice(null,-1);
                        SendMessage(Constants.DEVICE_CONNECT_FAIL,"蓝牙连接失败 error:未发现服务");
                        mBluetoothGatt.close();
                    }
                },5000);
            }else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                if(mBluetoothGatt!=null){
                    mBluetoothGatt.close();
                }
            }
        }
        /**
         * 发现服务以及获得其他属性，如write和read
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status==BluetoothGatt.GATT_SUCCESS){
                mTimeHandler.removeCallbacksAndMessages(null);//停止回调检测等待
                GetUUID();
                BluetoothGattService service=mBluetoothGatt.getService(UUID_SERVICE);
                mCharacteristic =service.getCharacteristic(UUID_CHAR);
                mCharacteristicTX=service.getCharacteristic(UUID_CHAR);
                if(mCharacteristicTX!=null){
                    Log.d("CommunicationService","mCharacteristicTX build success");
                }else{
                    Log.d("CommunicationService","mCharacteristicTX build fail");
                }
                gatt.setCharacteristicNotification(mCharacteristic,true);
                mTimeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothGattDescriptor mBluetoothGattDescriptor=mCharacteristic.getDescriptor(UUID_NOTIFY_DESCRIPTOR);
                        if(mBluetoothGattDescriptor!=null){
                            mBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(mBluetoothGattDescriptor);//设置这个才能监听模块数据
                        }
                    }
                },200);
            }
        }
        /**读监听*/
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }
        /**设置监听成功后会调用此函数*/
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            myApplication.setBluetoothDevice(bluetoothDevice4_0,BluetoothDevice.DEVICE_TYPE_LE);
            SendMessage(Constants.DEVICE_BLUETOOTH_CONNECT_SUCCEED,"蓝牙连接成功<低功耗蓝牙>");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
        /**
         *写操作回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                sendDataSign = true;//等到发送数据回调成功才可以继续发送
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            mIntent.putExtra("value",characteristic.getValue());
            sendBroadcast(mIntent);
        }
    };

    private void GetUUID(){
        List<BluetoothGattService> bluetoothGattServices= mBluetoothGatt.getServices();
        for (BluetoothGattService bluetoothGattService:bluetoothGattServices){
            List<BluetoothGattCharacteristic> characteristics=bluetoothGattService.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic:characteristics){
                int charaProp = characteristic.getProperties();
                Log.d("CommunicationService","charaProp:"+Integer.toHexString(charaProp));
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    UUID_read_chara =characteristic.getUuid();
                    UUID_read_service =bluetoothGattService.getUuid();
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) >0) {
                    UUID_write_chara =characteristic.getUuid();
                    UUID_write_service =bluetoothGattService.getUuid();
                    Log.d("CommunicationService","PROPERTY_WRITE1:"+UUID_write_chara.toString());
                    Log.d("CommunicationService","PROPERTY_WRITE2:"+UUID_write_service.toString());
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                    UUID_write_chara =characteristic.getUuid();
                    UUID_write_service =bluetoothGattService.getUuid();
                    Log.d("CommunicationService","PROPERTY_WRITE_NO_RESPONSE:"+UUID_write_chara.toString());
                    Log.d("CommunicationService","PROPERTY_WRITE_NO_RESPONSE:"+UUID_write_service.toString());
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    UUID_notify_chara =characteristic.getUuid();
                    UUID_notify_service =bluetoothGattService.getUuid();
                    Log.d("CommunicationService","PROPERTY_NOTIFY1:"+UUID_notify_chara.toString());
                    Log.d("CommunicationService","PROPERTY_NOTIFY2:"+UUID_notify_service.toString());
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    UUID_indicate_chara =characteristic.getUuid();
                    UUID_indicate_service =bluetoothGattService.getUuid();
                }
            }
        }
    }

    /**将byte[]数组分包为List byte数组*/
    private List<byte[]> getSendDataByte(byte[] bytes){
        List<byte[]> listSendData = new ArrayList<>();
        int[] sendDataLength = new int[2];
        sendDataLength[0]=bytes.length/20;
        sendDataLength[1]=bytes.length%20;
        for(int i=0;i<sendDataLength[0];i++) {
            byte[] dataFor20 = new byte[20];
            System.arraycopy(bytes, i * 20, dataFor20, 0, 20);
            listSendData.add(dataFor20);
        }
        if(sendDataLength[1]>0) {
            byte[] lastData = new byte[sendDataLength[1]];
            System.arraycopy(bytes, sendDataLength[0] * 20, lastData, 0, sendDataLength[1]);
            listSendData.add(lastData);
        }
        return listSendData;
    }

    /**BLE一次至多20字符*/
    private void WriteBluetooth4_0(byte[] bytes){
        if (mExecutorService == null){
            mExecutorService = Executors.newFixedThreadPool(1);
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if(bytes.length<=20){
                        if(mCharacteristicTX!=null){
                            mCharacteristicTX.setValue(bytes);
                            sendDataSign = !mBluetoothGatt.writeCharacteristic(mCharacteristicTX);//蓝牙发送数据，一次顶多20字节
                        }else{
                            Log.d("CommunicationService","mCharacteristicTX==null");
                        }

                    }else{
                        List<byte[]> sendDataArray = getSendDataByte(bytes);
                        int number = 0;
                        for (byte[] sendData : sendDataArray) {
                            while (!sendDataSign) {
                                number++;
                                Thread.sleep(2);
                                if (number == 50) {
                                    sendDataSign = true;
                                    return;
                                }
                            }
                            mCharacteristicTX.setValue(sendData);
                            sendDataSign = !mBluetoothGatt.writeCharacteristic(mCharacteristicTX);//蓝牙发送数据，一次顶多20字节
                            number = 0;
                        }
                    }
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
            }
        };
        mExecutorService.execute(runnable);
    }

    /*********************************************************** 以下为wifi ******************************************************************/
    private static Socket mSocket;
    private static OutputStream outStream;
    private static String mAddress;
    private static int mPort;

    private String GetWifiName(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return wifiManager.getConnectionInfo().getSSID();
    }

    /**
     * 连接wifi
     * @param address 地址
     * @param port 端口
     */
    private void ConnectWifi(String address,int port){
        mAddress=address;
        mPort=port;
        try {
           if(mSocket!=null){
               mSocket.close();
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SocketConnect socketConnect= new SocketConnect();
        socketConnect.start();
    }

    /**
     * 断开连接
     */
    private void DisConnectWifi(){
        try {
            myApplication.setWifiDevice(null);
            SendMessage(Constants.DEVICE_DISCONNECT,null);
            mSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class SocketConnect extends Thread{
        InputStream inStream;
        byte[] buffer=new byte[1024];
        byte[] bytes;
        int len;
        @Override
        public void run() {
            super.run();
            try {
                mSocket=null;
                mSocket = new Socket(mAddress,mPort);//address为要连接的服务端地址，port为端口号
                if(mSocket==null){
                    SendMessage(Constants.DEVICE_CONNECT_FAIL,"wifi连接失败 error:1");
                    return;
                }
                myApplication.setWifiDevice(mAddress+mPort);
                SendMessage(Constants.DEVICE_WIFI_CONNECT_SUCCEED,"wifi连接成功");
                wifiName=GetWifiName();
                outStream=mSocket.getOutputStream();//获取输出流
                inStream =mSocket.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
                SendMessage(Constants.DEVICE_CONNECT_FAIL,"wifi连接失败");
                return;
            }
            try {
                while(true){
                    len=inStream.read(buffer);
                    if(len>0){
                        bytes=new byte[len];
                        System.arraycopy(buffer,0,bytes,0,len);
                        mIntent.putExtra("value",bytes);
                        sendBroadcast(mIntent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if(myApplication.getWifiDevice()!=null) {
                    myApplication.setWifiDevice(null);
                    SendMessage(Constants.DEVICE_ERROR_DISCONNECT, "wifi连接断开了");
                    if(myApplication.getAutoWifiState()){
                        mAutoAddress=mAddress;
                        mAutoPort=mPort;
                        StartAutoConnectWifi();
                    }
                }
            }
        }
    }

    /**
     * 发送数据
     * @param bytes 要发送的数据
     */
    public void WriteWifi(byte[] bytes){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outStream.write(bytes);
                    outStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class MyCommunicationBinder extends Binder implements CommunicationIService {

        @Override
        public void callConnectBluetooth2_0(BluetoothDevice bluetoothDevice) {
            ConnectBluetooth2_0(bluetoothDevice);
        }

        @Override
        public void callDisConnectBluetooth2_0() {
            DisConnectBluetooth2_0();
        }

        @Override
        public void callConnectBluetooth4_0(BluetoothDevice bluetoothDevice) {
            ConnectBluetooth4_0(bluetoothDevice);
        }

        @Override
        public void callDisConnectBluetooth4_0() {
            DisConnectBluetooth4_0();
        }

        @Override
        public void callConnectWifi(String address, int port) {
            ConnectWifi(address,port);
        }

        @Override
        public void callDisConnectWifi() {
            DisConnectWifi();
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void callWrite(byte[] bytes) {
            WriteChar(bytes);
        }

        @Override
        public void setHandler(Handler handler) {
            mHandler=handler;
        }

        @Override
        public void callStartAutoConnectBluetooth() {
            if(mAutoBluetoothDevice!=null){
                StartAutoConnectBluetooth();
            }
        }

        @Override
        public void callStopAutoConnectBluetooth() {
            StopAutoConnectBluetooth();
        }

        @Override
        public void callStartAutoConnectWifi() {
            if(mAutoAddress!=null){
                StartAutoConnectWifi();
            }
        }

        @Override
        public void callStopAutoConnectWifi() {
            StopAutoConnectWifi();
        }
    }
}
