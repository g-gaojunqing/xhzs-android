package com.example.dzxh_app.Activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.view.MyInformDialog;
import com.example.dzxh_app.view.MyProcessDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BluetoothActivity extends BaseActivity {

    private MyApplication myApplication; //APP全局变量，蓝牙连接状态
    //蓝牙扫描
    private Button bluetooth_scan_bt;
    private BluetoothAdapter mBluetoothAdapter=null;
    private ListView bluetooth_device_lv; //蓝牙2.0列表控件
    private SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, Object>> bluetoothArrayList;
    private ArrayList<BluetoothDevice> mBluetoothDevices; //蓝牙设备列表
    private boolean isScanning=false;//是否正在扫描标记位


    //服务
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private Handler mDataHandler;  //接收服务回传的连接状态
    //连接
    private BluetoothDevice mSelect_Device;//用于信号强度检测
    private MyInformDialog mConnectDialog;
    private MyProcessDialog mProcessDialog; //连接提示弹窗
    private Handler mTimerHandler;//超时状态检测

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        //顶部状态栏返回按键
        Toolbar bluetooth_tb =findViewById(R.id.bluetooth_tb);
        bluetooth_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getLocationPermission(); //获取定位权限
        getBluetoothPermission();//获取蓝牙权限
        NumInit();      //数据初始化
        ButtonInit();   //按键初始化
        ViewInit();     //列表初始化
        SetHandler();   //设置服务回调函数
        ServiceInit();  //服务初始化
    }
    /**
     * 数据初始化
     */
    private void NumInit(){
        myApplication=(MyApplication)this.getApplication();
        mBluetoothDevices = new ArrayList<>();//蓝牙设备数组
        mTimerHandler=new Handler();
    }
    /**
     * 服务初始化
     */
    private void ServiceInit(){
        Intent intent = new Intent(this, CommunicationService.class);
        startService(intent);   //开启服务
        myServiceConnection = new MyServiceConnection();
        bindService(intent, myServiceConnection, BIND_AUTO_CREATE); //绑定服务
    }
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCommunicationIService = (CommunicationIService) service;//申请服务，回调使用
            mCommunicationIService.setHandler(mDataHandler);//数据回传
            myApplication.setAutoBluetoothState(false);//取消自动连接标记
            mCommunicationIService.callStopAutoConnectBluetooth(); //停止自动连接
            BluetoothInit();   //蓝牙初始化
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    /**
     * 按钮初始化
     */
    private void ButtonInit(){

        //扫描设备
        bluetooth_scan_bt = findViewById(R.id.bluetooth_scan_bt);
        bluetooth_scan_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapter==null){
                    BluetoothInit();
                    return;
                }
                //打开蓝牙
                if(!mBluetoothAdapter.isEnabled()){
                    myApplication.setBluetoothDevice(null,-1);
                    //弹出对话框提示用户是后打开
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 1);
                }else{
                    StartScan(); //扫描设备
                }
            }
        });
    }
    /**
     * 蓝牙列表初始化
     */
    private void ViewInit(){
        bluetoothArrayList = new ArrayList<HashMap<String, Object>>();
        //有蓝牙2.0已经连接，将其添加至列表
        if(myApplication.getBluetoothDevice()!=null){
            if(!mBluetoothDevices.contains(myApplication.getBluetoothDevice())){
                mBluetoothDevices.add(myApplication.getBluetoothDevice());
            }
        }
        for(int i=0;i<mBluetoothDevices.size();i++) {
            HashMap<String, Object> blueMap = new HashMap<String, Object>();
            blueMap.put("bluetooth_name", mBluetoothDevices.get(i).getName());
            blueMap.put("bluetooth_id",mBluetoothDevices.get(i).getAddress());
            if(mBluetoothDevices.get(i).equals(myApplication.getBluetoothDevice())){
                blueMap.put("bluetooth_connected",R.drawable.ic_check_black_24dp);
            }else {
                blueMap.put("bluetooth_connected",R.drawable.ic_null_24dp);
            }
            bluetoothArrayList.add(blueMap);
        }
        simpleAdapter = new SimpleAdapter(this, bluetoothArrayList,
                R.layout.item_bluetooth,
                new String[]{"bluetooth_name","bluetooth_id","bluetooth_connected"},
                new int[]{R.id.item_bluetooth_name_tv,R.id.item_bluetooth_id_tv,R.id.item_bluetooth_image});
        bluetooth_device_lv = findViewById(R.id.bluetooth_device_lv);
        bluetooth_device_lv.setAdapter(simpleAdapter); //列表配置
        setListViewHeightBasedOnChildren(bluetooth_device_lv);  //重置ListView大小
        bluetooth_device_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShowConnectDialog(mBluetoothDevices.get(position));
            }
        });
    }

    private void BluetoothInit(){
        if(!getLocationPermission()||!getBluetoothPermission()){
            return;
        }
        //是否支持蓝牙BLE
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,"当前设备不支持蓝牙",Toast.LENGTH_SHORT).show();
        }
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        //打开蓝牙
        if(!mBluetoothAdapter.isEnabled()){
            myApplication.setBluetoothDevice(null,-1);
            //弹出对话框提示用户是后打开
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }else{
            StartScan(); //扫描设备
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==1){
                if(mBluetoothAdapter.isEnabled()) {
                    //不加延时会立即停止扫描
                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            StartScan(); //扫描设备
                        }
                    },500);
                }
            }
        }
    }

    /**
     * 扫描设备
     */
    private void StartScan(){
        if(mBluetoothAdapter.isEnabled()) {
            ShowPairedBluetoothList(); //显示已经配对蓝牙
            if(mBluetoothAdapter.startDiscovery()){
                RegisterScanBroadcast();    //注册扫描回调广播
                isScanning=true;
                bluetooth_scan_bt.setEnabled(false); //禁用按钮
                bluetooth_scan_bt.setText("扫描中....");
                bluetooth_scan_bt.setTextColor(Color.rgb(30,144,255));
                mBluetoothDevices.clear();  //清除原设备
                BluetoothListRefresh();
            }else{
                Toast.makeText(BluetoothActivity.this,"扫描失败",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(BluetoothActivity.this,"蓝牙未打开",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 停止扫描
     */
    private void StopScan(){
        isScanning=false;
        unregisterReceiver(mBroadcastReceiver);
        mBluetoothAdapter.cancelDiscovery();
        bluetooth_scan_bt.setEnabled(true);  //解禁按钮
        bluetooth_scan_bt.setText("扫描设备");
        bluetooth_scan_bt.setTextColor(Color.rgb(0, 0, 238));
    }

    /**
     * 显示已经配对的设备
     */
    private void ShowPairedBluetoothList(){
        Set<BluetoothDevice> pairedDevices=mBluetoothAdapter.getBondedDevices();//获取已配对的设备
        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
        if(pairedDevices.size()!=0)
        {
            for (BluetoothDevice device : pairedDevices) {
                HashMap<String, Object> map=new HashMap<String, Object>();
                map.put("bluetooth_name",device.getName());
                map.put("bluetooth_id",device.getAddress());
                arrayList.add(map);
            }
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, arrayList,
                R.layout.item_bluetooth,
                new String[]{"bluetooth_name","bluetooth_id"},
                new int[]{R.id.item_bluetooth_name_tv,R.id.item_bluetooth_id_tv});
        ListView bluetooth_contented_lv = findViewById(R.id.bluetooth_contented_lv);
        bluetooth_contented_lv.setAdapter(simpleAdapter); //列表配置
        setListViewHeightBasedOnChildren(bluetooth_contented_lv);//重置ListView大小
    }

    /**
     * 注册扫描回调广播
     */
    private void RegisterScanBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//发现结束
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//发现结束
        registerReceiver(mBroadcastReceiver,filter);
    }
    private final BroadcastReceiver mBroadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if(!mBluetoothDevices.contains(device)){
                    if(!myApplication.getNameFilterState()||device.getName()!=null) { //蓝牙名称过滤未开启或蓝牙名称不为空
                        mBluetoothDevices.add(device);
                        BluetoothListRefresh();
                    }
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Toast.makeText(BluetoothActivity.this,mBluetoothDevices.size()+"个设备",Toast.LENGTH_SHORT).show();
                StopScan();
            }
        }
    };

    /**
     * 蓝牙列表刷新
     */
    private void BluetoothListRefresh(){
        bluetoothArrayList.clear();
        if(myApplication.getBluetoothDevice()!=null){
            if(!mBluetoothDevices.contains(myApplication.getBluetoothDevice())){
                mBluetoothDevices.add(myApplication.getBluetoothDevice());
            }
        }
        for(int i=0;i<mBluetoothDevices.size();i++) {
            HashMap<String, Object> blueMap = new HashMap<String, Object>();
            blueMap.put("bluetooth_name", mBluetoothDevices.get(i).getName());
            blueMap.put("bluetooth_id",mBluetoothDevices.get(i).getAddress());
            if(mBluetoothDevices.get(i).equals(myApplication.getBluetoothDevice())){
                blueMap.put("bluetooth_connected",R.drawable.ic_check_black_24dp);
            }else {
                blueMap.put("bluetooth_connected",R.drawable.ic_null_24dp);
            }
            bluetoothArrayList.add(blueMap);
        }
        simpleAdapter.notifyDataSetChanged();   //更新到列表
        setListViewHeightBasedOnChildren(bluetooth_device_lv);  //重置ListView大小
    }

    /**
     * 设备扫描回调,用于检测信号强弱
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(device.getAddress()!=null) {
                        if(mSelect_Device.equals(device)){
                            mConnectDialog.setInform2(Integer.toString(rssi));   //设置弹窗信号值
                        }
                    }
                }
            });
        }
    };

    /**
     * 蓝牙连接界面
     * @param bluetoothDevice 蓝牙设备
     */
    private void ShowConnectDialog(BluetoothDevice bluetoothDevice){
        mSelect_Device=bluetoothDevice;
        mBluetoothAdapter.startLeScan(mLeScanCallback);//开始扫描获取信号值
        mConnectDialog = new MyInformDialog(this);
        mConnectDialog.setCancelable(true); //点击屏幕外能取消
        mConnectDialog.show();
        mConnectDialog.setTitle(bluetoothDevice.getName());
        mConnectDialog.setInform3(bluetoothDevice.getAddress());
        if(bluetoothDevice.equals(myApplication.getBluetoothDevice())) {
            mConnectDialog.setButtonText("取消","断开连接");
        }else{
            mConnectDialog.setButtonText("取消","连接");
        }
        if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_UNKNOWN){
            mConnectDialog.setInform1("未知");
        }else if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_CLASSIC){
            mConnectDialog.setInform1("经典蓝牙");
        }else if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_LE){
            mConnectDialog.setInform1("低功耗蓝牙");
        }else if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_DUAL){
            mConnectDialog.setInform1("双模蓝牙");
        }

        mConnectDialog.setOnDialogClickListener(new MyInformDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if (view.getId() == R.id.layout_inform_dialog_yes){
                    if(bluetoothDevice.equals(myApplication.getBluetoothDevice())){
                            DisConnectDevice(myApplication.getBluetoothDevice());
                    }else{
                        if(myApplication.getBluetoothDevice()==null){
                            ConnectDevice(bluetoothDevice);
                        }else{
                            Toast.makeText(BluetoothActivity.this,"请先断开已连接设备",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    /**
     * 连接蓝牙
     * @param bluetoothDevice 蓝牙设备
     */
    private void ConnectDevice(BluetoothDevice bluetoothDevice){
        //检测蓝牙设备型号
        if(isScanning) {
            StopScan();
        }
        if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_CLASSIC) {
            ShowProcessDialog("正在连接....");
            mCommunicationIService.callConnectBluetooth2_0(bluetoothDevice);
        }else if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_LE) {
            ShowProcessDialog("正在连接....");
            mCommunicationIService.callConnectBluetooth4_0(bluetoothDevice);
        }else if(bluetoothDevice.getType()==BluetoothDevice.DEVICE_TYPE_DUAL) {
            ShowProcessDialog("正在连接....");
            mCommunicationIService.callConnectBluetooth2_0(bluetoothDevice);
        } else{
            Toast.makeText(BluetoothActivity.this,"无法连接蓝牙",Toast.LENGTH_SHORT).show();
        }
    }

    private void DisConnectDevice(BluetoothDevice bluetoothDevice){
        ShowProcessDialog("正在断开连接....");
        if(myApplication.getBluetoothType()==BluetoothDevice.DEVICE_TYPE_CLASSIC){
            mCommunicationIService.callDisConnectBluetooth2_0();
        }else if(myApplication.getBluetoothType()==BluetoothDevice.DEVICE_TYPE_LE){
            mCommunicationIService.callDisConnectBluetooth4_0();
        }else if(myApplication.getBluetoothType()==BluetoothDevice.DEVICE_TYPE_DUAL){
            mCommunicationIService.callDisConnectBluetooth2_0();
        }
    }
    /**
     * 连接提示弹窗
     * @param string 提示语句
     */
    private void ShowProcessDialog(String string){
        mProcessDialog = new MyProcessDialog(this);
        mProcessDialog.setCancelable(true);//点击屏幕外可取消
        mProcessDialog.show();//显示
        mProcessDialog.setTitle(string);
        //防止没有回调，超时检测
        mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //关掉提示
                if(mProcessDialog!=null){
                    if(mProcessDialog.isShowing()){
                        mProcessDialog.dismiss();
                    }
                }
                Toast.makeText(BluetoothActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
            }
        },15000);
    }

    /**
     * 用于接收来自Service的回调信息
     */
    private void SetHandler(){
        mDataHandler = new Handler(new Handler.Callback() {
            @SuppressLint("ShowToast")
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                mTimerHandler.removeMessages(0); //移除超时监控
                if(msg.obj!=null){
                    Toast.makeText(BluetoothActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
                }
                BluetoothListRefresh(); //更新蓝牙列表
                //关掉提示
                if(mProcessDialog!=null){
                    if(mProcessDialog.isShowing()){
                        mProcessDialog.dismiss();
                    }
                }
                return false;
            }
        });
    }
    @Override
    protected void onDestroy() {
        unbindService(myServiceConnection);
        if(isScanning){
            unregisterReceiver(mBroadcastReceiver);
            mBluetoothAdapter.cancelDiscovery();
        }
        //继续自动连接
        myApplication.setAutoBluetoothState(sp.getBoolean("AutoBluetoothState",false));//设置自动连接
        if(myApplication.getAutoBluetoothState()){
            mCommunicationIService.callStartAutoConnectBluetooth();
        }
        super.onDestroy();
    }
}

