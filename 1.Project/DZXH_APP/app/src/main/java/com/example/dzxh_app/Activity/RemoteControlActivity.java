package com.example.dzxh_app.Activity;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.util.RegexUtil;
import com.example.dzxh_app.view.MyOneEditDialog;
import com.example.dzxh_app.view.MyScrollView;
import com.example.dzxh_app.view.MyTwoEditDialog;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class RemoteControlActivity extends BaseActivity {

    private MyApplication myApplication; //全局变量
    private String myCharsetName; //字符编码格式

    //四个按钮
    private ArrayList<MyScrollView> remote_control_sv=new ArrayList<>();
    private final ArrayList<Button> remote_control_bt=new ArrayList<>();
    private final ArrayList<ToggleButton> remote_control_tb=new ArrayList<>();
    private boolean[] scrollIsButton=new boolean[4];
    //两个开关
    private final ArrayList<Switch> remote_control_sw=new ArrayList<>();
    private final ArrayList<EditText> remote_control_switch_et=new ArrayList<>();
    //定时时间设置
    private EditText remote_control_timer_et;
    private TextView remote_control_timer_tv;
    //方向按键与摇杆View
    private LinearLayout remote_control_left_rocker_ll;
    private LinearLayout remote_control_left_key_ll;
    private LinearLayout remote_control_right_rocker_ll;
    private LinearLayout remote_control_right_key_ll;
    private RadioButton remote_control_left_rocker_rb;
    private RadioButton remote_control_left_key_rb;
    private RadioButton remote_control_left_circular_rb;
    private RadioButton remote_control_left_square_rb;
    private RadioButton remote_control_right_rocker_rb;
    private RadioButton remote_control_right_key_rb;
    private RadioButton remote_control_right_circular_rb;
    private RadioButton remote_control_right_square_rb;
    boolean leftIsStick =true, rightIsStick =true;
    //方向按键
    private final ArrayList<ImageButton> remote_control_ib=new ArrayList<>();
    //按键与摇杆值
    private final byte[] buttonValue =new byte[4];
    private final byte[] switchValue=new byte[2];
    private final byte[] keyValue=new byte[8];
    private final int[] xValue=new int[]{100,100};
    private final int[] yValue=new int[]{100,100};
    //摇杆绘制
    private final ArrayList<ImageView> remote_control_rocker_iv=new ArrayList<>();
    private final ArrayList<Bitmap> stickBitmap =new ArrayList<>();
    private final ArrayList<Path> pathBack=new ArrayList<>();
    private final ArrayList<Paint> paintBack=new ArrayList<>();
    private final ArrayList<Path> pathFore=new ArrayList<>();
    private final ArrayList<Paint> paintFore=new ArrayList<>();
    //摇杆计算
    private final int[] xCenter=new int[2];
    private final int[] yCenter=new int[2];
    private final int[] radiusBack=new int[2];//背景半径
    private final int[] radiusFore=new int[2];//前景半径
    private final boolean[] moveFlag=new boolean[2];
    private final boolean[] joyStickIsCircular=new boolean[2];
    //陀螺仪传感器
    private SensorManager sensorManager;
    private Sensor magneticSensor;
    private Sensor accelerometerSensor;
    private boolean gyroscopeEnabled;
    //定时发送
    private Timer timer;
    private boolean timerEnabled;
    private int timerTime =100;
    //设置与文本接收框
    private boolean setEnabled;//设置标记位
    private TextView remote_control_rx_tv;
    private String stringRX="";
    private byte[] rxBytes;
    //服务
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //全屏显示
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(layoutParams);

        Toolbar remote_control_tb = findViewById(R.id.remote_control_tb);
        setSupportActionBar(remote_control_tb);
        remote_control_tb.setOverflowIcon(drawableIcon);
        ServiceInit();
        numInit();
        viewInit();
        buttonInit();
        setGesture(0);
        setGesture(1);
        GyroscopeInit();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //设置全屏显示
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        if (stickBitmap.size() != 0){
            return;
        }
        for (int r = 0; r < 2; r++) {
            int width = remote_control_rocker_iv.get(r).getWidth();
            int height = remote_control_rocker_iv.get(r).getHeight();
            xCenter[r] = width/2;
            yCenter[r] = height/2;
            if(xCenter[r] < yCenter[r]){
                radiusBack[r] =width/3;
                radiusFore[r] =width/6;
            }else{
                radiusBack[r] =height/3;
                radiusFore[r] =height/6;
            }
            stickBitmap.add(Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888));
            pathBack.get(r).reset();
            pathBack.get(r).addCircle(xCenter[r], yCenter[r], radiusBack[r], Path.Direction.CCW);

            pathFore.get(r).reset();
            pathFore.get(r).addCircle(xCenter[r], yCenter[r], radiusFore[r], Path.Direction.CCW);
            draw(r);
        }
        getData();//先把界面加载好在get数据，因为要先配置摇杆，再设置是否可见
        timerStart();
    }


    //服务初始化
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
    private void BroadcastReceiverInit() {
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter(
                "com.example.dzxh_app.content");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, filter,RECEIVER_EXPORTED); //注册广播开始接收
        }else{
            registerReceiver(myBroadcastReceiver, filter); //注册广播开始接收
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
            intent=new Intent(RemoteControlActivity.this,BluetoothActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_more_wifi){
            intent=new Intent(RemoteControlActivity.this,WifiActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_more_help){
            intent=new Intent(RemoteControlActivity.this, UserActivity.class);
            intent.putExtra("page",7);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    private void numInit(){
        myApplication =(MyApplication) this.getApplication();
        myCharsetName=myApplication.getCharsetName();

        for (int r = 0; r < 2; r++) {
            pathFore.add(new Path());
            paintFore.add(new Paint());
            paintFore.get(r).setColor(Color.rgb(180, 180, 180));
            paintFore.get(r).setStyle(Paint.Style.FILL);
            paintFore.get(r).setAntiAlias(true); //去除锯齿

            pathBack.add(new Path());
            paintBack.add(new Paint());
            paintBack.get(r).setColor(Color.rgb(200, 200, 200));
            paintBack.get(r).setStrokeWidth(6);
            paintBack.get(r).setStyle(Paint.Style.STROKE);
            paintBack.get(r).setAntiAlias(true);
        }
    }
    private void viewInit(){
        //按键与开关的ScrollView
        remote_control_sv.add(findViewById(R.id.remote_control_a_sv));
        remote_control_sv.add(findViewById(R.id.remote_control_b_sv));
        remote_control_sv.add(findViewById(R.id.remote_control_c_sv));
        remote_control_sv.add(findViewById(R.id.remote_control_d_sv));
        for (int i = 0; i < 4; i++) {
            remote_control_sv.get(i).setScrollEnable(false);
        }
        //摇杆与按键
        remote_control_left_rocker_ll=findViewById(R.id.remote_control_left_rocker_ll);
        remote_control_left_key_ll=findViewById(R.id.remote_control_left_key_ll);
        remote_control_right_rocker_ll=findViewById(R.id.remote_control_right_rocker_ll);
        remote_control_right_key_ll=findViewById(R.id.remote_control_right_key_ll);
        //摇杆ImageView
        remote_control_rocker_iv.add(findViewById(R.id.remote_control_left_rocker_iv));
        remote_control_rocker_iv.add(findViewById(R.id.remote_control_right_rocker_iv));
        //定时发送
        remote_control_timer_tv = findViewById(R.id.remote_control_timer_tv); //单位展示
        //打印信息文本框
        remote_control_rx_tv = findViewById(R.id.remote_control_rx_tv);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void buttonInit(){
        //陀螺仪
        ToggleButton remote_control_gyroscope_tb = findViewById(R.id.remote_control_gyroscope_tb);
        remote_control_gyroscope_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sensorManager.registerListener(mSensorEventListener, magneticSensor,
                            SensorManager.SENSOR_DELAY_GAME);
                    sensorManager.registerListener(mSensorEventListener, accelerometerSensor,
                            SensorManager.SENSOR_DELAY_GAME);
                    gyroscopeEnabled =true;
                }else{
                    for (int i = 0; i < 5; i++) {
                        X_average_values[i]=0;
                        Y_average_values[i]=0;
                    }
                    sensorManager.unregisterListener(mSensorEventListener);
                    if(leftIsStick){
                        refreshView(0,xCenter[0], yCenter[0]);
                    }else{
                        gyroscopeKey(0,0);
                    }
                    gyroscopeEnabled =false;
                }
            }
        });
        //定时发送
        ToggleButton remote_control_timer_tb = findViewById(R.id.remote_control_timer_tb);
        remote_control_timer_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                timerEnabled =isChecked;
            }
        });
        //定时时间输入框
        remote_control_timer_et = findViewById(R.id.remote_control_timer_et);
        remote_control_timer_et.setEnabled(false);  //不可输入
        remote_control_timer_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(RegexUtil.isNum(remote_control_timer_et.getText().toString())) {
                    timerTime =Integer.parseInt(remote_control_timer_et.getText().toString());
                }
            }
        });
        //按键
        remote_control_bt.add(findViewById(R.id.remote_control_a_bt));
        remote_control_bt.add(findViewById(R.id.remote_control_b_bt));
        remote_control_bt.add(findViewById(R.id.remote_control_c_bt));
        remote_control_bt.add(findViewById(R.id.remote_control_d_bt));
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            remote_control_bt.get(i).setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(!setEnabled){
                        byte[] bytes=new byte[3];
                        if(event.getAction()==MotionEvent.ACTION_DOWN){
                            buttonValue[finalI]=0x01;
                            sendByte((byte) ((0xA0+(0x10*finalI))| buttonValue[finalI]));
                        }else if(event.getAction()==MotionEvent.ACTION_UP){
                            buttonValue[finalI]=0x00;
                            sendByte((byte) ((0xA0+(0x10*finalI))| buttonValue[finalI]));
                        }
                    }else{
                        if(event.getAction()==MotionEvent.ACTION_UP){
                            showButtonEditDialog(finalI);
                        }
                    }
                    return false;
                }
            });
        }
        //切换按钮
        remote_control_tb.add(findViewById(R.id.remote_control_a_tb));
        remote_control_tb.add(findViewById(R.id.remote_control_b_tb));
        remote_control_tb.add(findViewById(R.id.remote_control_c_tb));
        remote_control_tb.add(findViewById(R.id.remote_control_d_tb));
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            remote_control_tb.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    byte[] bytes=new byte[3];
                    if(!setEnabled){
                        if(isChecked){
                            buttonValue[finalI]=1;
                        }else{
                            buttonValue[finalI]=0;
                        }
                        sendByte((byte) (((0xA0+(0x10*finalI))| buttonValue[finalI])));
                    }else{
                        showToggleEditDialog(finalI);
                    }
                }
            });
        }
        //开关
        remote_control_sw.add(findViewById(R.id.remote_control_e_sw));
        remote_control_sw.add(findViewById(R.id.remote_control_f_sw));
        for (int i = 0; i < 2; i++) {
            int finalI = i;
            remote_control_sw.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(!setEnabled){
                        if(isChecked){
                            switchValue[finalI]=0x01;
                        }else{
                           switchValue[finalI]=0x00;
                        }
                        sendByte((byte) (((0xE0+(0x10*finalI))| switchValue[finalI])));
                    }
                }
            });
        }
        //开关文本
        remote_control_switch_et.add(findViewById(R.id.remote_control_switch_e_et));
        remote_control_switch_et.add(findViewById(R.id.remote_control_switch_f_et));
        for (int i = 0; i < 2; i++) {
            remote_control_switch_et.get(i).setEnabled(false);
        }
        //方向按键
        remote_control_ib.add(findViewById(R.id.remote_control_left_up_ib));
        remote_control_ib.add(findViewById(R.id.remote_control_left_down_ib));
        remote_control_ib.add(findViewById(R.id.remote_control_left_left_ib));
        remote_control_ib.add(findViewById(R.id.remote_control_left_right_ib));
        remote_control_ib.add(findViewById(R.id.remote_control_right_up_ib));
        remote_control_ib.add(findViewById(R.id.remote_control_right_down_ib));
        remote_control_ib.add(findViewById(R.id.remote_control_right_left_ib));
        remote_control_ib.add(findViewById(R.id.remote_control_right_right_ib));
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            remote_control_ib.get(i).setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction()==MotionEvent.ACTION_DOWN){
                        remote_control_ib.get(finalI).setImageResource(R.drawable.ic_forward_black_98dp);
                        keyValue[finalI]=0x01;
                        sendByte((byte) ((0x10+(0x10*finalI))|keyValue[finalI]));
                    }else if(event.getAction()==MotionEvent.ACTION_UP){
                        remote_control_ib.get(finalI).setImageResource(R.drawable.ic_forward_black_90dp);
                        keyValue[finalI]=0x00;
                        sendByte((byte) ((0x10+(0x10*finalI))|keyValue[finalI]));
                    }
                    return true;
                }
            });
        }
        //左摇杆形状选择
        remote_control_left_circular_rb = findViewById(R.id.remote_control_left_circular_rb);
        remote_control_left_square_rb = findViewById(R.id.remote_control_left_square_rb);
        RadioGroup remote_control_left_shape_rg = findViewById(R.id.remote_control_left_shape_rg);
        remote_control_left_shape_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.remote_control_left_circular_rb){
                    joyStickIsCircular[0] =true;
                    setJoyStickShape(0,true);
                }else if(checkedId==R.id.remote_control_left_square_rb){
                    joyStickIsCircular[0] =false;
                    setJoyStickShape(0,false);
                }
            }
        });
        remote_control_left_shape_rg.setVisibility(View.GONE);
        //右摇杆形状选择
        remote_control_right_circular_rb = findViewById(R.id.remote_control_right_circular_rb);
        remote_control_right_square_rb = findViewById(R.id.remote_control_right_square_rb);
        RadioGroup remote_control_right_shape_rg = findViewById(R.id.remote_control_right_shape_rg);
        remote_control_right_shape_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.remote_control_right_circular_rb){
                    joyStickIsCircular[1] =true;
                    setJoyStickShape(1,true);
                }else if(checkedId==R.id.remote_control_right_square_rb){
                    joyStickIsCircular[1] =false;
                    setJoyStickShape(1,false);
                }
            }
        });
        remote_control_right_shape_rg.setVisibility(View.GONE);
        //左控件选择
        remote_control_left_rocker_rb = findViewById(R.id.remote_control_left_rocker_rb);
        remote_control_left_key_rb = findViewById(R.id.remote_control_left_key_rb);
        RadioGroup remote_control_left_select_rg = findViewById(R.id.remote_control_left_select_rg);
        remote_control_left_select_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.remote_control_left_rocker_rb){
                    leftIsStick =true;
                    remote_control_left_rocker_ll.setVisibility(View.VISIBLE);
                    remote_control_left_key_ll.setVisibility(View.GONE);
                    if(setEnabled) {
                        remote_control_left_shape_rg.setVisibility(View.VISIBLE);
                    }
                }else if(checkedId==R.id.remote_control_left_key_rb){
                    leftIsStick =false;
                    remote_control_left_rocker_ll.setVisibility(View.GONE);
                    remote_control_left_key_ll.setVisibility(View.VISIBLE);
                    remote_control_left_shape_rg.setVisibility(View.GONE);
                }
            }
        });
        remote_control_left_select_rg.setVisibility(View.GONE);
        //右控件选择
        remote_control_right_rocker_rb = findViewById(R.id.remote_control_right_rocker_rb);
        remote_control_right_key_rb = findViewById(R.id.remote_control_right_key_rb);
        RadioGroup remote_control_right_select_rg = findViewById(R.id.remote_control_right_select_rg);
        remote_control_right_select_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.remote_control_right_rocker_rb){
                    rightIsStick =true;
                    remote_control_right_rocker_ll.setVisibility(View.VISIBLE);
                    remote_control_right_key_ll.setVisibility(View.GONE);
                    if(setEnabled){
                        remote_control_right_shape_rg.setVisibility(View.VISIBLE);
                    }
                }else if(checkedId==R.id.remote_control_right_key_rb){
                    rightIsStick =false;
                    remote_control_right_rocker_ll.setVisibility(View.GONE);
                    remote_control_right_key_ll.setVisibility(View.VISIBLE);
                    remote_control_right_shape_rg.setVisibility(View.GONE);
                }
            }
        });
        remote_control_right_select_rg.setVisibility(View.GONE);
        //设置
        ToggleButton remote_control_set_tb = findViewById(R.id.remote_control_set_tb);
        remote_control_set_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //停止发送
                    timer.cancel();
                    //使能按键开关切换滑动
                    for (int i = 0; i < 4; i++) {
                        remote_control_sv.get(i).setScrollEnable(true);
                    }
                    //允许输入
                    for (int i = 0; i < 2; i++) {
                        remote_control_switch_et.get(i).setEnabled(true);
                    }
                    //定时设置
                    remote_control_timer_et.setText(timerTime +""); //显示当前定时时间
                    remote_control_timer_et.setEnabled(true); //使能输入
                    remote_control_timer_tv.setText("ms"); //显示单位
                    //功能切换单选选项显示
                    remote_control_left_select_rg.setVisibility(View.VISIBLE);
                    remote_control_right_select_rg.setVisibility(View.VISIBLE);
                    //形状切换单选选项显示
                    if(leftIsStick){
                        remote_control_left_shape_rg.setVisibility(View.VISIBLE);
                    }
                    if(rightIsStick){
                        remote_control_right_shape_rg.setVisibility(View.VISIBLE);
                    }
                    setEnabled =true;
                }else{
                    //禁止滑动
                    for (int i = 0; i < 4; i++) {
                        remote_control_sv.get(i).setScrollEnable(false);
                    }
                    //禁止输入
                    for (int i = 0; i < 2; i++) {
                        remote_control_switch_et.get(i).setEnabled(false);
                    }

                    //设置定时
                    remote_control_timer_et.setText("定时发送");
                    remote_control_timer_et.setEnabled(false);
                    remote_control_timer_tv.setText(null); //隐藏单位
                    //定时时间矫正
                    if(timerTime <10){
                        timerTime =10;
                        Toast.makeText(RemoteControlActivity.this,"定时时间已设置为最小值10ms",Toast.LENGTH_SHORT).show();
                    }
                    //隐藏单选
                    remote_control_left_select_rg.setVisibility(View.GONE);
                    remote_control_right_select_rg.setVisibility(View.GONE);
                    if(leftIsStick){
                        remote_control_left_shape_rg.setVisibility(View.GONE);
                    }
                    if(rightIsStick){
                        remote_control_right_shape_rg.setVisibility(View.GONE);
                    }
                    timerStart();
                    putData(); //保存定时发送时间间隔
                    setEnabled =false;
                }
            }
        });
    }

    /**
     * 设置按键名称
     * @param num 按键序号
     */
    private void showButtonEditDialog(int num){
        String[] abc=new String[]{"A","B","C","D"};
        MyOneEditDialog myOneEditDialog =new MyOneEditDialog(this);
        myOneEditDialog.setCancelable(true);
        myOneEditDialog.show();
        myOneEditDialog.setTitle("按键"+abc[num]);
        myOneEditDialog.setHint("按键名称");
        myOneEditDialog.setInformName("按键名称：");
        myOneEditDialog.setInform(remote_control_bt.get(num).getText().toString());
        myOneEditDialog.setOnDialogClickListener(new MyOneEditDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_edit_one_yes_bt){
                    String string= myOneEditDialog.getInform();
                    remote_control_bt.get(num).setText(string);
                }
            }
        });
    }
    /**
     * 开关文本设置
     * @param num 开关序号
     */
    private void showToggleEditDialog(int num){
        String[] abc=new String[]{"A","B","C","D"};
        MyTwoEditDialog myTwoEditDialog =new MyTwoEditDialog(this);
        myTwoEditDialog.setCancelable(true);
        myTwoEditDialog.show();
        myTwoEditDialog.setTitle("开关"+abc[num]);
        myTwoEditDialog.setInformName("开启名称：","关闭名称：");
        myTwoEditDialog.setHint("开启名称","关闭名称");
        myTwoEditDialog.setInform(remote_control_tb.get(num).getTextOn().toString(),remote_control_tb.get(num).getTextOff().toString());
        myTwoEditDialog.setOnDialogClickListener(new MyTwoEditDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_edit_two_yes_bt){
                    String string_on=myTwoEditDialog.getInform1();
                    String string_off=myTwoEditDialog.getInform2();
                    remote_control_tb.get(num).setTextOn(string_on);
                    remote_control_tb.get(num).setTextOff(string_off);
                    //上两个方法不能立即改变按钮文本
                    if(remote_control_tb.get(num).isChecked()){
                        remote_control_tb.get(num).setText(string_on);
                    }else{
                        remote_control_tb.get(num).setText(string_off);
                    }
                }
            }
        });
    }
    /**
     * 发送一个字节
     * @param b 需要发送的字节
     */
    private void sendByte(byte b){
        if(!timerEnabled){
            byte[] bytes=new byte[1];
            bytes[0]=b;
            try{
                mCommunicationIService.callWrite(bytes);
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this,"发送失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 定时发送初始化
     */
    private void timerStart(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(timerEnabled){
                    DecimalFormat decimalFormat =new DecimalFormat("000");
                    String string="";
                    if(leftIsStick){
                        string+=decimalFormat.format(xValue[0])+decimalFormat.format(yValue[0]);
                    }else{
                        string+=""+keyValue[0]+keyValue[1] + keyValue[2] + keyValue[3];
                    }
                    if(rightIsStick){
                        string+=decimalFormat.format(xValue[1])+decimalFormat.format(yValue[1]);
                    }else{
                        string+=""+ keyValue[4] + keyValue[5] + keyValue[6] + keyValue[7];
                    }
                    string+=""+buttonValue[0]+buttonValue[1]+buttonValue[2]+buttonValue[3];
                    string+=""+switchValue[0]+switchValue[1]+"\r\n";
                    mCommunicationIService.callWrite(string.getBytes());
                }
            }
        }, timerTime, timerTime);
    }
    private void gyroscopeKey(float x, float y)
    {
        if(keyValue[0]==0)
        {
            if((-15<x)&&(x<15)&&(y>30)){
                remote_control_ib.get(0).setImageResource(R.drawable.ic_forward_black_98dp);
                keyValue[0]=1;
                sendByte((byte) (0x10|keyValue[0]));
            }
        }else{
            if((-20>x)||(x>20)||(y<25)){
                remote_control_ib.get(0).setImageResource(R.drawable.ic_forward_black_90dp);
                keyValue[0]=0;
                sendByte((byte) (0x10|keyValue[0]));
            }
        }
        if(keyValue[1] ==0)
        {
            if((-15<x)&&(x<15)&&(y<-30)){
                remote_control_ib.get(1).setImageResource(R.drawable.ic_forward_black_98dp);
                keyValue[1] =1;
                sendByte((byte) (0x20|  keyValue[1]));
            }
        }else{
            if((-20>x)||(x>20)||(y>-25)){
                remote_control_ib.get(1).setImageResource(R.drawable.ic_forward_black_90dp);
                keyValue[1] =0;
                sendByte((byte) (0x20|  keyValue[1]));
            }
        }
        if(keyValue[2] ==0)
        {
            if((-15<y)&&(y<15)&&(x>30)){
                remote_control_ib.get(2).setImageResource(R.drawable.ic_forward_black_98dp);
                keyValue[2] =1;
                sendByte((byte) (0x30|  keyValue[2]));
            }
        }else{
            if((-20>y)||(y>20)||(x<25)){
                remote_control_ib.get(2).setImageResource(R.drawable.ic_forward_black_90dp);
                keyValue[2] =0;
                sendByte((byte) (0x30|  keyValue[2]));
            }
        }
        if(keyValue[3] ==0)
        {
            if((-15<y)&&(y<15)&&(x<-30)){
                remote_control_ib.get(3).setImageResource(R.drawable.ic_forward_black_98dp);
                keyValue[3] =1;
                sendByte((byte) (0x40|  keyValue[3]));
            }
        }else{
            if((-20>y)||(y>20)||(x>-25)){
                remote_control_ib.get(3).setImageResource(R.drawable.ic_forward_black_90dp);
                keyValue[3] =0;
                sendByte((byte) (0x40|  keyValue[3]));
            }
        }
    }
    /**
     * 显示左
     */
    private void draw(int r){
        Canvas canvas=new Canvas(stickBitmap.get(r));
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//不清除会留下旧轨迹
        canvas.drawPath(pathBack.get(r), paintBack.get(r));
        canvas.drawPath(pathFore.get(r), paintFore.get(r));
        remote_control_rocker_iv.get(r).setImageBitmap(stickBitmap.get(r));
    }

    private void setJoyStickShape(int s,boolean isCircular){
        pathBack.get(s).reset();
        if(isCircular){
            pathBack.get(s).addCircle(xCenter[s], yCenter[s], radiusBack[s], Path.Direction.CCW);
        }else{
            float left=xCenter[s]-radiusBack[s];
            float right=xCenter[s]+radiusBack[s];
            float top=yCenter[s]-radiusBack[s];
            float bottom=yCenter[s]+radiusBack[s];
            pathBack.get(s).addRect(left,top,right,bottom, Path.Direction.CW);
            Log.d("leftVale",":"+left+"r"+right+"t"+top+"b"+bottom);
        }
        draw(s);
    }

    /**
     * 刷新摇杆
     * @param x x
     * @param y y
     */
    private void refreshView(int r, float x, float y){
        pathFore.get(r).reset();
        pathFore.get(r).addCircle(x,y, radiusFore[r], Path.Direction.CCW);
        draw(r);
        int x_value=(int)((x- xCenter[r])*101/ radiusBack[r])+100;
        int y_value=(int)-((y- yCenter[r])*101/ radiusBack[r])+100;
        if(x_value>200)x_value=200;
        if(x_value<0)x_value=0;
        if(y_value>200)y_value=200;
        if(y_value<0)y_value=0;
        xValue[r]=x_value;yValue[r]=y_value;
        if(!timerEnabled) {
            byte[] bytes=new byte[4];
            if(r==0){
                bytes[0]=(byte)(0x10|(xValue[r]/16));
                bytes[1]=(byte)(0x20|(xValue[r]%16));
                bytes[2]=(byte)(0X30|(yValue[r]/16));
                bytes[3]=(byte)(0X40|(yValue[r]%16));
            }else{
                bytes[0]=(byte)(0x50|(xValue[r]/16));
                bytes[1]=(byte)(0x60|(xValue[r]%16));
                bytes[2]=(byte)(0X70|(yValue[r]/16));
                bytes[3]=(byte)(0X80|(yValue[r]%16));
            }
            mCommunicationIService.callWrite(bytes);
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setGesture(int r){
        remote_control_rocker_iv.get(r).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(abs(event.getX()- xCenter[r])< radiusFore[r] && abs(event.getY()- yCenter[r])< radiusFore[r]){
                            moveFlag[r] =true;
                            refreshView(r,event.getX(),event.getY());
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!moveFlag[r]) {
                            return true;
                        }
                        float x=event.getX()- xCenter[r];
                        float y=event.getY()- yCenter[r];
                        if(joyStickIsCircular[0]){
                            float dxy=(float) Math.sqrt(x*x+y*y);
                            if (!(dxy <= radiusBack[r])) {
                                x = x * radiusBack[r] / dxy;
                                y = y * radiusBack[r] / dxy;
                            }
                        }else{
                            if (abs(x) >radiusBack[r]) {
                                x= x* radiusBack[r] /abs(x);
                            }
                            if (abs(y) >radiusBack[r]) {
                                y= y* radiusBack[r]/abs(y);
                            }
                        }
                        refreshView(r,x+xCenter[r],y+yCenter[r]);

                        break;
                    case MotionEvent.ACTION_UP:
                        moveFlag[r] =false;
                        refreshView(r,xCenter[r], yCenter[r]);
                        break;
                }
                return true;  //返回false将无法检测move
            }
        });
    }

    float[] r;
    float[] accelerometerValues;
    float[] magneticValues;
    float[] values;
    float[] X_average_values;
    float[] Y_average_values;
    private void GyroscopeInit(){
        X_average_values=new float[7];
        Y_average_values=new float[7];
        //初始化数组
        accelerometerValues = new float[3];//用来保存加速度传感器的值
        magneticValues = new float[3];//用来保存地磁传感器的值
        r=new float[9];//保存旋转数据的数组
        values=new float[3];//保存方向数据
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);//获取传感器管理器
        assert sensorManager != null;
        magneticSensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//磁场传感器
        accelerometerSensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//加速度传感器
        //注册陀螺仪传感器，并设定传感器向应用中输出的时间间隔类型是SensorManager.SENSOR_DELAY_GAME(20000微秒)
        //SensorManager.SENSOR_DELAY_FASTEST(0微秒)：最快。最低延迟，一般不是特别敏感的处理不推荐使用，该模式可能在成手机电力大量消耗，由于传递的为原始数据，诉法不处理好会影响游戏逻辑和UI的性能
        //SensorManager.SENSOR_DELAY_GAME(20000微秒)：游戏。游戏延迟，一般绝大多数的实时性较高的游戏都是用该级别
        //SensorManager.SENSOR_DELAY_NORMAL(200000微秒):普通。标准延时，对于一般的益智类或EASY级别的游戏可以使用，但过低的采样率可能对一些赛车类游戏有跳帧现象
        //SensorManager.SENSOR_DELAY_UI(600000微秒):用户界面。一般对于屏幕方向自动旋转使用，相对节省电能和逻辑处理，一般游戏开发中不使用
    }

    private float xAngle,yAngle;
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
                float degreeX = (float) Math.toDegrees(values[1]);
                float degreeY =(float) Math.toDegrees(values[2]);
                if(abs(degreeX)<46) {
                    xAngle = xAverageFilter(degreeX);
                }
                if(abs(degreeY)<46) {
                    yAngle = yAverageFilter(degreeY);
                }
                if(leftIsStick){
                    float x= -xAngle* radiusBack[0] /45;   //转换到画布比例，将画布45等分，每份一度
                    float y= -yAngle* radiusBack[0] /45;   //转换到画布比例
                    if(joyStickIsCircular[0]){
                        float dxy=(float) Math.sqrt(x*x+y*y);
                        if (!(dxy <= radiusBack[0])) {
                            x = x * radiusBack[0] / dxy;
                            y = y * radiusBack[0] / dxy;
                        }
                    }else{
                        if (abs(x) >radiusBack[0]) {
                            x= x* radiusBack[0] /abs(x);
                        }
                        if (abs(y) >radiusBack[0]) {
                            y= y* radiusBack[0]/abs(y);
                        }
                    }
                    refreshView(0,x+ xCenter[0],y+ yCenter[0]);
                }else{
                    gyroscopeKey(xAngle,yAngle);
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    /**
     * X滤波
     * @param x 实时X值
     * @return  滤波后X值
     */
    private float xAverageFilter(float x){
        float X_sum=0;
        System.arraycopy(X_average_values, 1, X_average_values, 0, 6);
        X_average_values[6]=x;
        for (int i = 0; i < 7; i++) {
            X_sum+=X_average_values[i];
        }
        return X_sum/7;
    }

    /**
     * Y值滤波
     * @param y 实时Y值
     * @return 滤波后Y值
     */
    private float yAverageFilter(float y){
        float Y_sum=0;
        System.arraycopy(Y_average_values, 1, Y_average_values, 0, 6);
        Y_average_values[6]=y;
        for (int i = 0; i < 7; i++) {
            Y_sum+=Y_average_values[i];
        }
        return Y_sum/7;
    }

    /**
     * byte数组转字符串
     * @param bytes byte数组
     * @return  GBK字符串
     */
    private String BytesToString(byte[] bytes){
        String string=null;
        try {
            string = new String(bytes, myCharsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }
    private byte[] bytes_rx=new byte[64];
    private int rx_num;
    private void DealBytes(byte[] bytes){
        String string = null;
        try {
            string = new String(bytes,myCharsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        remote_control_rx_tv.setText(string);
    }
    /**
     *广播接收到数据回调
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes = intent.getByteArrayExtra("value");
            assert bytes != null;
            for (byte aByte : bytes) {
                bytes_rx[rx_num] = aByte;
                rx_num++;
                if (rx_num>=2) {
                    if (bytes_rx[rx_num - 1] == 0x0A && bytes_rx[rx_num - 2] == 0x0D) { //接收到0x0D0x0A
                        byte[] tmp=new byte[rx_num-2];
                        System.arraycopy(bytes_rx,0,tmp,0,rx_num-2);
                        DealBytes(tmp);
                        rx_num = 0;
                        bytes_rx = new byte[64];
                    }
                }
                if (rx_num >= 64) {
                    DealBytes(bytes_rx);
                    rx_num = 0;
                    bytes_rx = new byte[64];
                }
            }
        }
    }

    /**
     * 保存设置数据
     */
    private void putData(){
        //按钮与切换开关
        for (int i = 0; i < 4; i++) {
            sp.putString("remote_control_bt_text"+i,remote_control_bt.get(i).getText().toString());
            sp.putString("remote_control_tb_text_on"+i,remote_control_tb.get(i).getTextOn().toString());
            sp.putString("remote_control_tb_text_off"+i,remote_control_tb.get(i).getTextOff().toString());
            scrollIsButton[i]=remote_control_sv.get(i).getIsUP();
            sp.putBoolean("remote_control_scrollIsButton"+i,scrollIsButton[i]);
        }
        //开关
        for (int i = 0; i < 2; i++) {
            sp.putString("remote_control_switch_et_text"+i,remote_control_switch_et.get(i).getText().toString());
        }
        //定时时间
        sp.putInt("remote_control_timerTime", timerTime);
        //控件选择
        sp.putBoolean("remote_control_leftIsStick", leftIsStick);
        sp.putBoolean("remote_control_rightIsStick", rightIsStick);
        //摇杆形状
        sp.putBoolean("remote_control_joyStickIsCircular0", joyStickIsCircular[0]);
        sp.putBoolean("remote_control_joyStickIsCircular1", joyStickIsCircular[1]);
    }
    /**
     * 获取设置数据
     */
    private void getData(){
        //按键切换开关
        for (int i = 0; i < 4; i++) {
            remote_control_bt.get(i).setText(sp.getString("remote_control_bt_text"+i,"按键"));
            remote_control_tb.get(i).setTextOn(sp.getString("remote_control_tb_text_on"+i,"开启"));
            remote_control_tb.get(i).setTextOff(sp.getString("remote_control_tb_text_off"+i,"关闭"));
            //上两个方法不能立即改变按钮文本
            if(remote_control_tb.get(i).isChecked()){
                remote_control_tb.get(i).setText(sp.getString("remote_control_tb_text_on"+i,"开启"));
            }else{
                remote_control_tb.get(i).setText(sp.getString("remote_control_tb_text_off"+i,"关闭"));
            }
            scrollIsButton[i]=sp.getBoolean("remote_control_scrollIsButton"+i,true);
            remote_control_sv.get(i).setIsUp(scrollIsButton[i]);
            if(!scrollIsButton[i]){
                remote_control_sv.get(i).fullScroll(ScrollView.FOCUS_DOWN);
            }
        }
        //开关
        for (int i = 0; i < 2; i++) {
            remote_control_switch_et.get(i).setText(sp.getString("remote_control_switch_et_text"+i,"开关"));
        }
        //定时时间
        timerTime =sp.getInt("remote_control_timerTime",100);
        //控件选择
        leftIsStick =sp.getBoolean("remote_control_leftIsStick",true);
        rightIsStick =sp.getBoolean("remote_control_rightIsStick",false);
        //选择摇杆或方向按键
        if(leftIsStick){
            remote_control_left_rocker_rb.setChecked(true);//会触发按键
        }else{
            remote_control_left_key_rb.setChecked(true);
        }
        if(rightIsStick){
            remote_control_right_rocker_rb.setChecked(true);
        }else{
            remote_control_right_key_rb.setChecked(true);
        }
        joyStickIsCircular[0] =sp.getBoolean("remote_control_joyStickIsCircular0",true);
        joyStickIsCircular[1] =sp.getBoolean("remote_control_joyStickIsCircular1",true);
        //摇杆形状设定
        if(joyStickIsCircular[0]){
            remote_control_left_circular_rb.setChecked(true);//会触发按键
        }else{
            remote_control_left_square_rb.setChecked(true);
        }
        if(joyStickIsCircular[1]){
            remote_control_right_circular_rb.setChecked(true);//会触发按键
        }else{
            remote_control_right_square_rb.setChecked(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        putData();
        sensorManager.unregisterListener(mSensorEventListener); //注销传感器广播
        unbindService(myServiceConnection); //解绑服务
        unregisterReceiver(myBroadcastReceiver); //注销广播
        timer.cancel(); //取消定时器
    }
}
