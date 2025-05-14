package com.example.dzxh_app.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.view.GPSBoard;
import com.example.dzxh_app.view.MySelectDialog;
import com.example.dzxh_app.view.MyTwoEditDialog;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressLint("SetTextI18n")
public class GPSActivity extends BaseActivity {


    private TextView gps_my_longitude_tv;
    private TextView gps_my_latitude_tv;
    private TextView gps_target_longitude_tv;
    private TextView gps_target_latitude_tv;
    private ToggleButton gps_start_scan_tb;
    private TextView gps_yaw_angle_tv;
    private GPSBoard gps_scan_gb;
    private ImageView gps_arrowhead_iv; //蓝色箭头

    private boolean autoFlag; //是否跟随

    private boolean gpsMode;    //是否开启GPS模式
    private SensorManager sensorManager; //传感器
    private int zValue; //Z轴，指南针的值
    private float mLongitude,mLatitude,tLongitude,tLatitude;//经度纬度
    private int mX,mY,tX,tY;//坐标
    private boolean txFlag;
    private Timer timer;
    //服务
    private static CommunicationIService mCommunicationIService;
    MyServiceConnection myServiceConnection;
    private MyBroadcastReceiver myBroadcastReceiver;
    private IntentFilter filter;

    private CheckBox gps_auto_cb;
    private CheckBox gps_gps_mode_cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        //三个点
        Toolbar gps_tb =findViewById(R.id.gps_tb);
        setSupportActionBar(gps_tb);
        gps_tb.setOverflowIcon(drawableIcon); //三个点颜色
        gps_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });//返回按键

        ViewInit();
        ButtonInit();
        ServiceInit();
        GyroscopeInit();
        TimerInit();
        GetData();
        getLocationPermission();//获得位置权限;
    }
    private void ServiceInit(){
        Intent intent = new Intent(this, CommunicationService.class);
        startService(intent);
        myServiceConnection = new MyServiceConnection();
        bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
        BroadcastReceiverInit();
    }
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCommunicationIService = (CommunicationIService) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    /**
     * 数据接收广播注册
     */
    private void BroadcastReceiverInit() {
        myBroadcastReceiver=new MyBroadcastReceiver();
        filter = new IntentFilter(
                "com.example.dzxh_app.content");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, filter,RECEIVER_EXPORTED); //绑定广播
        }else{
            registerReceiver(myBroadcastReceiver, filter); //绑定广播
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_more, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.menu_more_blue) {
            gps_scan_gb.setStart(false);
            intent=new Intent(GPSActivity.this,BluetoothActivity.class);
            startActivityForResult(intent,0);
        }else if(item.getItemId() == R.id.menu_more_wifi){
            gps_scan_gb.setStart(false);
            intent=new Intent(GPSActivity.this,WifiActivity.class);
            startActivityForResult(intent,2);
        }else if(item.getItemId() == R.id.menu_more_help){
            gps_scan_gb.setStart(false);
            intent=new Intent(GPSActivity.this, UserActivity.class);
            intent.putExtra("page",10);
            startActivityForResult(intent,3);
        }
        return super.onOptionsItemSelected(item);
    }

    private void ViewInit(){
        //扫描盘
        gps_scan_gb = findViewById(R.id.gps_scan_gb);
        gps_scan_gb.setStart(true);
        //经度
        gps_my_longitude_tv = findViewById(R.id.gps_my_longitude_tv);
        gps_target_longitude_tv = findViewById(R.id.gps_target_longitude_tv);
        //纬度
        gps_my_latitude_tv = findViewById(R.id.gps_my_latitude_tv);
        gps_target_latitude_tv = findViewById(R.id.gps_target_latitude_tv);
        //偏航角
        gps_yaw_angle_tv = findViewById(R.id.gps_yaw_angle_tv);
        //北
        gps_arrowhead_iv = findViewById(R.id.gps_arrowhead_iv);

    }

    private void ButtonInit(){
        //扫描按钮
        gps_start_scan_tb = findViewById(R.id.gps_start_scan_tb);
        gps_start_scan_tb.setChecked(true);
        gps_start_scan_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    gps_scan_gb.setStart(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        registerReceiver(myBroadcastReceiver, filter,RECEIVER_EXPORTED);
                    }else{
                        registerReceiver(myBroadcastReceiver, filter);
                    }
                }else{
                    gps_scan_gb.setStart(false);
                    unregisterReceiver(myBroadcastReceiver);
                }
            }
        });
        //开始发送
        ToggleButton gps_start_send_tb = findViewById(R.id.gps_start_send_tb);
        gps_start_send_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                txFlag = isChecked;
                if(isChecked){
                    if(!gpsMode){
                        Toast.makeText(GPSActivity.this, "GPS模式下才可发送", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //自动跟随
        gps_auto_cb = findViewById(R.id.gps_auto_cb);
        gps_auto_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    autoFlag =true;
                }else{
                    autoFlag =false;
                    gps_scan_gb.setYaw_angle(0);   //回到0位置
                    gps_arrowhead_iv.setRotation(0);    //回到0位置
                }
                PutData();
            }
        });
        //模式切换
        gps_gps_mode_cb = findViewById(R.id.gps_gps_mode_cb);
        gps_gps_mode_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    gpsMode =true;
                    LocationStart();
                    DecimalFormat decimalFormat =new DecimalFormat("000.000000");
                    gps_target_longitude_tv.setText("经度:"+decimalFormat.format(tLongitude));
                    gps_target_latitude_tv.setText("纬度:"+decimalFormat.format(tLatitude));
                    gps_scan_gb.setTarget_XY((tLongitude-mLongitude)*1000000,(tLatitude-mLatitude)*1000000);
                }else{
                    gpsMode =false;
                    XYUpdates();
                    gps_target_longitude_tv.setText("X:"+tX);
                    gps_target_latitude_tv.setText("Y:"+tY);
                    gps_scan_gb.setTarget_XY(tX-mX,tY-mY);
                }
                PutData();
            }
        });

        //我的位置
        TextView gpa_my_location_tv = findViewById(R.id.gpa_my_location_tv);
        gpa_my_location_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!gpsMode) {
                    ShowEditDialog();
                }
            }
        });
    }

    /**
     * 輸入彈窗
     */
    private void ShowEditDialog(){
        final MyTwoEditDialog myTwoEditDialog = new MyTwoEditDialog(this);
        myTwoEditDialog.setCancelable(true); //点击屏幕外能取消
        myTwoEditDialog.show();
        myTwoEditDialog.setTitle("我的位置");
        myTwoEditDialog.setInformName("X：","Y：");
        myTwoEditDialog.setHint("X","Y");
        myTwoEditDialog.setInform(mX+"",mY+"");
        myTwoEditDialog.setOnDialogClickListener(new MyTwoEditDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_edit_two_yes_bt) {
                    try {
                        mX = Integer.parseInt(myTwoEditDialog.getInform1().trim());
                    }catch (NumberFormatException e){
                        Toast.makeText(GPSActivity.this, "输入X不符合规范", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    try {
                        mY = Integer.parseInt(myTwoEditDialog.getInform2().trim());
                    }catch (NumberFormatException e){
                        Toast.makeText(GPSActivity.this, "输入Y不符合规范", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    XYUpdates();
                    PutData();
                }
            }
        });
    }

    /**定时发送函数*/
    private void TimerInit(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(txFlag && gpsMode){
                    DecimalFormat decimalFormat =new DecimalFormat("000000000");
                    String string=decimalFormat.format(mLongitude*1000000)
                            +decimalFormat.format(mLatitude*1000000)+"\r\n";
                    mCommunicationIService.callWrite(string.getBytes());
                }
            }
        }, 1000, 1000);
    }

    /**
     * 定位初始化
     */
    private void LocationStart(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!Objects.requireNonNull(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ShowSelectDialog();
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(GPSActivity.this,"没有获得定位权限",Toast.LENGTH_SHORT).show();
           return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, //指定GPS定位的提供者
                500,           //间隔时间
                0.2f,      //位置间隔0.2米
                new LocationListener() {        //监听信息改变
                    @Override
                    public void onLocationChanged(Location location) {  //信息发生改变
                        if(gpsMode){
                            LocationUpdates(location);
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {     //GPS状态发生改变

                    }

                    @Override
                    public void onProviderEnabled(String provider) {        //定位提供者启动时改变

                    }

                    @Override
                    public void onProviderDisabled(String provider) {  //定位关闭回调

                    }
                }
        );
        Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LocationUpdates(location);
    }

    /**定位更新*/
    private void LocationUpdates(Location location){
        if(location!=null){
            mLongitude=(float) location.getLongitude();
            mLatitude=(float)location.getLatitude();
            gps_scan_gb.setTarget_XY((tLongitude-mLongitude)*1000000,(tLatitude-mLatitude)*1000000);
        }else {
            Toast.makeText(this, "没用获取到定位信息", Toast.LENGTH_SHORT).show();
        }
        DecimalFormat decimalFormat =new DecimalFormat("000.000000");
        gps_my_longitude_tv.setText("经度:"+decimalFormat.format(mLongitude));
        gps_my_latitude_tv.setText("纬度:"+decimalFormat.format(mLatitude));
    }
    /**自定义定位更新*/
    private void XYUpdates(){
        gps_my_longitude_tv.setText("X:"+mX);
        gps_my_latitude_tv.setText("Y:"+mY);
        gps_scan_gb.setTarget_XY(tX-mX,tY-mY);
    }

    /**
     * 开启GPS提示窗口
     */
    private void ShowSelectDialog(){
        MySelectDialog mySelectDialog=new MySelectDialog(this);
        mySelectDialog.show();
        mySelectDialog.setButtonText("取消","现在开启");
        mySelectDialog.setText("GPS未开启，请开启后重试");
        mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_select_yes_bt){
                    // 转到手机设置界面，用户设置GPS
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }
    /**陀螺仪传感器*/
    float[] accelerometerValues;
    float[] magneticValues;
    float[] r;
    float[] values;
    float[] Z_average_values;
    private void GyroscopeInit(){
        Z_average_values=new float[20];
        //初始化数组
        accelerometerValues = new float[3];//用来保存加速度传感器的值
        magneticValues = new float[3];//用来保存地磁传感器的值保存旋转数据的数组
        r=new float[9];//
        values=new float[3];//保存方向数据
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);//获取传感器管理器
        Sensor magneticSensor = Objects.requireNonNull(sensorManager)
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//磁场传感器
        Sensor accelerometerSensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//加速度传感器

        sensorManager.registerListener(mSensorEventListener, magneticSensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(mSensorEventListener, accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    private SensorEventListener mSensorEventListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone();
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues=event.values.clone();
                // r从这里返回
                SensorManager.getRotationMatrix(r, null, accelerometerValues, magneticValues);
                //values从这里返回
                SensorManager.getOrientation(r, values);
                //提取数据
                float degreeZ = (float) Math.toDegrees(values[0]);
                if(degreeZ<0){degreeZ=360+degreeZ;}

                if((degreeZ-zValue)>180){
                    degreeZ-=360;
                }else if((degreeZ-zValue)<-180){
                    degreeZ+=360;
                }
                zValue =ZAverage_filter(degreeZ);
                gps_yaw_angle_tv.setText(zValue+"°");
                if(autoFlag){
                    gps_arrowhead_iv.setRotation(-zValue);
                    gps_scan_gb.setYaw_angle(zValue);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private int ZAverage_filter(float x){
        float Z_sum=0;
        System.arraycopy(Z_average_values, 1, Z_average_values, 0, 19);
        Z_average_values[19]=x;
        for (int i = 0; i < 20; i++) {
            Z_sum+=Z_average_values[i];
        }
        int angle=(int)Z_sum/20;
        if(angle>=360){
            angle-=360;
            for (int i = 0; i <20; i++) {
                Z_average_values[i]-=360;
            }
        }else if(angle<0){
            angle+=360;
            for (int i = 0; i <20; i++) {
                Z_average_values[i]+=360;
            }
        }
        return angle;
    }

    private void DealBytes(){
        for (int i = 0; i < bytes_data.length; i+=4) {
            int int32=((0XFF&bytes_data[i+3])<<24)|((0XFF&bytes_data[i+2])<<16)
                    |((0XFF&bytes_data[i+1])<<8)|(0XFF&bytes_data[i]);
            if(gpsMode){
                if(i==0) {
                    tLongitude = Float.intBitsToFloat(int32);
                }else{
                    tLatitude= Float.intBitsToFloat(int32);
                }
            }else {
                if(i==0){
                    tX=int32;
                }else{
                    tY=int32;
                }
            }
        }
        if(gpsMode){
            DecimalFormat decimalFormat =new DecimalFormat("000.000000");
            gps_target_longitude_tv.setText("经度:"+decimalFormat.format(tLongitude));
            gps_target_latitude_tv.setText("纬度:"+decimalFormat.format(tLatitude));
            gps_scan_gb.setTarget_XY((tLongitude-mLongitude)*1000000,(tLatitude-mLatitude)*1000000);
        }else{
            gps_target_longitude_tv.setText("X:"+tX);
            gps_target_latitude_tv.setText("Y:"+tY);
            gps_scan_gb.setTarget_XY(tX-mX,tY-mY);
        }
    }
    private final byte[] bytes_data=new byte[8];
    private int RX_state;
    private int RX_num;
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes=intent.getByteArrayExtra("value");
            assert bytes != null;
            for (byte aByte : bytes) {
                if (RX_state == 2) {   //帧头接收成功
                    if (RX_num < bytes_data.length) {
                        bytes_data[RX_num] = aByte;
                        RX_num++;
                        if (RX_num >= bytes_data.length) { //数据接收完成
                            RX_state = 3;
                            RX_num = 0;
                        }
                    }
                } else if (RX_state == 3) {  //数据接收完成
                    if ((0XFF & aByte) == 0xFC) {  //帧尾FC正确
                        RX_state = 4;
                    } else { //帧尾错误
                        if (aByte == 0x03) {  //是帧头03
                            RX_state = 1; //接收到第一个帧头
                        } else { //啥也不是，重新开始
                            RX_state = 0;
                        }
                    }
                } else if (RX_state == 4) { //第一个帧尾正确
                    if (aByte == 0x03) { //第二个帧尾正确，接收成功
                        RX_state = 0;
                        if(bytes_data.length==8){
                            DealBytes();
                        }
                    } else { //第二个帧尾不正确
                        RX_state = 0;
                    }
                } else if (RX_state == 1) {  //接收到第一个帧头
                    if ((0XFF & aByte) == 0xFC) { //第二个帧头正确
                        RX_state = 2; //开始接收数据
                    } else {
                        if (aByte != 0x03) { //第二个帧头错误，也不是第一个帧头
                            RX_state = 0;
                        }
                    }
                } else if (RX_state == 0) { //帧头没有接收到
                    if (aByte == 0x03) {
                        RX_state = 1;
                    }
                }
            }
        }
    }
    private void PutData(){
        sp.putInt("GPS_mX",mX);
        sp.putInt("GPS_mY",mY);
        sp.putBoolean("GPS_gpsMode", gpsMode);
        sp.putBoolean("GPS_autoFlag", autoFlag);

    }
    private void GetData(){
        mX=sp.getInt("GPS_mX",0);
        mY=sp.getInt("GPS_mY",0);
        gpsMode =sp.getBoolean("GPS_gpsMode",false);
        gps_gps_mode_cb.setChecked(gpsMode);
        if(!gpsMode){
            XYUpdates();
            DealBytes();
        }
        autoFlag =sp.getBoolean("GPS_autoFlag",false);
        gps_auto_cb.setChecked(autoFlag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        gps_scan_gb.setStart(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PutData();
        timer.cancel();
        unbindService(myServiceConnection);
        if(gps_start_scan_tb.isChecked()){
            unregisterReceiver(myBroadcastReceiver);
        }
        sensorManager.unregisterListener(mSensorEventListener);
    }
}
