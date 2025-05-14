package com.example.dzxh_app.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.util.RegexUtil;
import com.example.dzxh_app.util.UriToPath;
import com.example.dzxh_app.view.MyFourEditDialog;
import com.example.dzxh_app.view.MyHintDialog;
import com.example.dzxh_app.view.MyOneEditDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SetTextI18n")
public class OscilloscopeActivity extends BaseActivity {
    //控件
    private ArrayList<EditText> editTexts;
    private Button oscilloscope_data_type_bt;
    private Button oscilloscope_channel_num_bt;
    private RadioButton oscilloscope_color_rb;
    private boolean oscilloscopeBackColor;
    private ArrayList<CheckBox> checkBoxes;
    private ToggleButton oscilloscope_start_tb;
    private ImageView oscilloscope_iv;
    private TextView oscilloscope_rx_num_tv;

    //数据接收
    private Timer timer;
    private int oscilloscopeDataType;
    private int oscilloscopeChannelNum;
    private byte[] bytesData;  //接收到的数据
    private int dataLength;
    private int rxState, rxNum; //接收状态，接收数量
    private long[] oscilloscopeLongNum; //储存整数
    private float[] oscilloscopeFloatNum;//储存float
    private double[] oscilloscopeDoubleNum;//储存double

    //画背景
    private ArrayList<Paint> paints;  //画笔配置
    private ArrayList<Path> paths;  //储存路径
    private Paint paintWall1, paintWall2, paintWall3, paintWall4, paintText;
    private Path pathWall1, pathWall2, pathWall3, pathWall4;
    private int mDrawColor;
    //数据储存
    private static double[] dataArray1, dataArray2, dataArray3, dataArray4;//4组数据
    private static ArrayList<double[]> arrayDoubles;
    private static int dataNum =0;//记录传输数据数
    private boolean[] seriesVisible;  //波形是否可见
    //显示参数
    private int xMax, yMax; //最大值
    private int ivWidth;  //窗口宽度
    private static double scaleX =10, scaleY =1; //*缩放比例，越大图像越大,每隔ScaleX画一点 *零线
    private static double offsetY =0;//图像偏移Y，用于图像拖动
    private static double offsetX =0;//图像偏移X，用于图像拖动
    private static int multiplier =0;  //标记刻度放大次数，图像越大值越大
    private static double zoom =1; //刻度缩放，在原始单位根据缩放分为多少份
    private static double[] dataScales;//刻度值
    private float yDiv, yDivO;//当前两根虚线之间像素距离，两根虚线间原始像素距离
    private float yDivRem;//刻度偏移
    private boolean refreshFlag;
    private static Bitmap osBitmap;

    //参数调节
    private ArrayList<Button> variableButtons;
    private SeekBar oscilloscope_adjust_sb;
    private Button oscilloscope_variable_array_bt;
    private int arrayChecked=0;
    private int buttonChecked=-1;//记录阈值调整被选中按钮

    private long[][] variableValue =new long[6][4];
    private long[][] variableMax =new long[6][4];
    private long[][] variableMin =new long[6][4];
    private int[][] variableScale=new int[6][4];
    private String[][] variableName=new String[6][4];
    private ArrayList<CheckBox> variableCheckBoxes;
    private boolean[][] variableUsed;  //参数是否可用
    private EditText oscilloscope_adjust_timer_et;
    private Timer txTimer;
    private boolean txTimerEnabled;
    private int txTimerTime =1000;
    //服务
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private MyBroadcastReceiver myBroadcastReceiver;
    private IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oscilloscope);
        Log.d("Oscilloscope","开始-------------------");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);          //去除状态栏
        //三个点
        Toolbar oscilloscope_tb =findViewById(R.id.oscilloscope_tb);
        setSupportActionBar(oscilloscope_tb);
        oscilloscope_tb.setOverflowIcon(drawableIcon); //三个点颜色

        NumInit();
        SeriesInit();
        ViewInit();
        ButtonInit();
        ServiceInit();
        SetGesture();
        GetData();
        getStoragePermission();
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
            intent=new Intent(OscilloscopeActivity.this,BluetoothActivity.class);
            startActivityForResult(intent,0);
        }else if(item.getItemId() == R.id.menu_more_wifi){
            intent=new Intent(OscilloscopeActivity.this,WifiActivity.class);
            startActivityForResult(intent,2);
        }else if(item.getItemId() == R.id.menu_more_help){
            intent=new Intent(OscilloscopeActivity.this, UserActivity.class);
            intent.putExtra("page",2);
            startActivityForResult(intent,3);
        }
        return super.onOptionsItemSelected(item);
    }

    private void NumInit(){
        editTexts =new ArrayList<>();
        checkBoxes=new ArrayList<>();
        oscilloscopeBackColor =true;
        oscilloscopeLongNum =new long[4]; //储存整数
        oscilloscopeFloatNum =new float[4];//储存浮点数
        oscilloscopeDoubleNum =new double[4];//储存浮点数
        refreshFlag = false;
        mDrawColor= Color.rgb(30,30,30);
        seriesVisible =new boolean[4];

        variableName=new String[6][4];
        variableValue =new long[6][4];
        variableMax =new long[6][4];
        variableMin =new long[6][4];
        variableScale=new int[6][4];
        variableUsed=new boolean[6][4];
        variableButtons =new ArrayList<>();
        variableCheckBoxes =new ArrayList<>();
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
    //接收广播
    private void BroadcastReceiverInit() {
        myBroadcastReceiver=new MyBroadcastReceiver();
        filter = new IntentFilter("com.example.dzxh_app.content");
    }
    private void ButtonInit(){
        //开始接收
        oscilloscope_start_tb = findViewById(R.id.oscilloscope_start_tb);
        oscilloscope_start_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int len= oscilloscopeChannelNum;
                if(isChecked){
                    oscilloscope_start_tb.setTextColor(Color.RED);
                    oscilloscope_data_type_bt.setEnabled(false);
                    oscilloscope_channel_num_bt.setEnabled(false);
                    if(oscilloscopeDataType==0){
                        len=256;
                    }else if(oscilloscopeDataType ==1|| oscilloscopeDataType ==2){
                        len= oscilloscopeChannelNum;
                    }else if(oscilloscopeDataType ==3|| oscilloscopeDataType ==4){
                        len=2* oscilloscopeChannelNum;
                    }else if(oscilloscopeDataType ==5|| oscilloscopeDataType ==6|| oscilloscopeDataType ==7){
                        len=4* oscilloscopeChannelNum;
                    }else if(oscilloscopeDataType ==8){
                        len=8* oscilloscopeChannelNum;
                    }
                    bytesData =new byte[len];
                    dataLength = bytesData.length;
                    rxState =0;
                    rxNum =0;//清零防止上次接收数据溢出数组
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        registerReceiver(myBroadcastReceiver, filter,RECEIVER_EXPORTED); //注册广播开始接收
                    }else{
                        registerReceiver(myBroadcastReceiver, filter); //注册广播开始接收
                    }
                }else{
                    unregisterReceiver(myBroadcastReceiver);
                    oscilloscope_start_tb.setTextColor(Color.BLACK);
                    oscilloscope_data_type_bt.setEnabled(true);
                    oscilloscope_channel_num_bt.setEnabled(true);
                }
            }
        });
        //清除数据
        Button oscilloscope_delete_bt = findViewById(R.id.oscilloscope_delete_bt);
        oscilloscope_delete_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Clear();
            }
        });
        //导入
        Button oscilloscope_input_bt = findViewById(R.id.oscilloscope_input_bt);
        oscilloscope_input_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(oscilloscope_start_tb.isChecked()) {
                    Toast.makeText(OscilloscopeActivity.this, "正在接收，操作失败", Toast.LENGTH_SHORT).show();
                }else{
                    OpenFile();
                }
            }
        });
        //导出
        Button oscilloscope_output_bt = findViewById(R.id.oscilloscope_output_bt);
        oscilloscope_output_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataNum !=0){
                    SaveFile();
                }else{
                    Toast.makeText(OscilloscopeActivity.this,"没有发现数据",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //数据1
        CheckBox oscilloscope_ser1_cb = findViewById(R.id.oscilloscope_ser1_cb);
        checkBoxes.add(oscilloscope_ser1_cb);
        oscilloscope_ser1_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetSeries_visible(0, oscilloscope_ser1_cb.isChecked());
            }
        });
        oscilloscope_ser1_cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowEditDialog(0);
                return true;
            }
        });
        //数据2
        CheckBox oscilloscope_ser2_cb = findViewById(R.id.oscilloscope_ser2_cb);
        checkBoxes.add(oscilloscope_ser2_cb);
        oscilloscope_ser2_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetSeries_visible(1, oscilloscope_ser2_cb.isChecked());
            }
        });
        oscilloscope_ser2_cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowEditDialog(1);
                return true;
            }
        });
        //数据3
        CheckBox oscilloscope_ser3_cb = findViewById(R.id.oscilloscope_ser3_cb);
        checkBoxes.add(oscilloscope_ser3_cb);
        oscilloscope_ser3_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetSeries_visible(2, oscilloscope_ser3_cb.isChecked());
            }
        });
        oscilloscope_ser3_cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowEditDialog(2);
                return true;
            }
        });
        //数据4
        CheckBox oscilloscope_ser4_cb = findViewById(R.id.oscilloscope_ser4_cb);
        checkBoxes.add(oscilloscope_ser4_cb);
        oscilloscope_ser4_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetSeries_visible(3, oscilloscope_ser4_cb.isChecked());
            }
        });
        oscilloscope_ser4_cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowEditDialog(3);
                return true;
            }
        });
        //屏幕反白
        oscilloscope_color_rb = findViewById(R.id.oscilloscope_color_rb);
        oscilloscope_color_rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oscilloscopeBackColor = !oscilloscopeBackColor;
                oscilloscope_color_rb.setChecked(oscilloscopeBackColor);
                SetDrawColor(oscilloscopeBackColor);
                PutData();
            }
        });
        //X增
        ImageButton oscilloscope_x_up_ib = findViewById(R.id.oscilloscope_x_up_ib);
        oscilloscope_x_up_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetScaleX(true);
            }
        });
        //X减
        ImageButton oscilloscope_x_down_ib = findViewById(R.id.oscilloscope_x_down_ib);
        oscilloscope_x_down_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetScaleX(false);
            }
        });
        //Y增
        ImageButton oscilloscope_y_up_ib = findViewById(R.id.oscilloscope_y_up_ib);
        oscilloscope_y_up_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetScaleY(true);
            }
        });
        //Y减
        ImageButton oscilloscope_y_down_ib = findViewById(R.id.oscilloscope_y_down_ib);
        oscilloscope_y_down_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetScaleY(false);
            }
        });
        //数据类型
        oscilloscope_data_type_bt = findViewById(R.id.oscilloscope_data_type_bt);
        oscilloscope_data_type_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataTypeList();
            }
        });
        //通道数
        oscilloscope_channel_num_bt = findViewById(R.id.oscilloscope_channel_num_bt);
        oscilloscope_channel_num_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelNumList();
            }
        });
        HorizontalScrollView oscilloscope_hsv = findViewById(R.id.oscilloscope_hsv);
        oscilloscope_adjust_timer_et = findViewById(R.id.oscilloscope_adjust_timer_et);
        oscilloscope_adjust_timer_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(RegexUtil.isNum(oscilloscope_adjust_timer_et.getText().toString())) {
                    txTimerTime =Integer.parseInt(oscilloscope_adjust_timer_et.getText().toString());
                }
            }
        });
        //实时发送
        LinearLayout oscilloscope_adjust_timer_ll=findViewById(R.id.oscilloscope_adjust_timer_ll);
        oscilloscope_adjust_timer_ll.setVisibility(View.GONE);
        Switch oscilloscope_adjust_instant_sw = findViewById(R.id.oscilloscope_adjust_timer_sw);
        oscilloscope_adjust_instant_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(txTimerTime<20){
                        txTimerTime=20;
                        oscilloscope_adjust_timer_et.setText("20");
                    }
                    sp.putInt("Oscilloscope_txTimerTime",txTimerTime); //保存
                    txTimerEnabled=true;
                    oscilloscope_adjust_timer_et.setEnabled(false);
                    txTimerInit();
                }else{
                    txTimerEnabled=false;
                    oscilloscope_adjust_timer_et.setEnabled(true);
                    txTimer.cancel();
                }
            }
        });

        //调参按钮
        LinearLayout oscilloscope_adjust_ll = findViewById(R.id.oscilloscope_adjust_ll);
        oscilloscope_adjust_ll.setVisibility(View.GONE);
        ToggleButton oscilloscope_adjust_tb = findViewById(R.id.oscilloscope_adjust_tb);
        oscilloscope_adjust_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    oscilloscope_adjust_timer_ll.setVisibility(View.VISIBLE);
                    oscilloscope_adjust_ll.setVisibility(View.VISIBLE);
                    oscilloscope_hsv.fullScroll(ScrollView.FOCUS_DOWN);
                    if(oscilloscope_adjust_instant_sw.isChecked()){
                        txTimerEnabled=true;
                        txTimerInit();
                    }
                } else{
                    oscilloscope_adjust_timer_ll.setVisibility(View.GONE);
                    oscilloscope_adjust_ll.setVisibility(View.GONE);
                    oscilloscope_hsv.fullScroll(ScrollView.FOCUS_UP);
                    if(oscilloscope_adjust_instant_sw.isChecked()){
                        txTimerEnabled=false;
                        txTimer.cancel();
                    }
                }
            }
        });
        //参数1
        CheckBox oscilloscope_variable1_cb = findViewById(R.id.oscilloscope_variable1_cb);
        variableCheckBoxes.add(oscilloscope_variable1_cb);
        oscilloscope_variable1_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetVariableUsed(0,oscilloscope_variable1_cb.isChecked());
            }
        });
        oscilloscope_variable1_cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowFourEditDialog(0);
                return true;
            }
        });
        //参数2
        CheckBox oscilloscope_variable2_cb = findViewById(R.id.oscilloscope_variable2_cb);
        variableCheckBoxes.add(oscilloscope_variable2_cb);
        oscilloscope_variable2_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetVariableUsed(1,oscilloscope_variable2_cb.isChecked());
            }
        });
        oscilloscope_variable2_cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowFourEditDialog(1);
                return true;
            }
        });
        //参数3
        CheckBox oscilloscope_variable3_cb = findViewById(R.id.oscilloscope_variable3_cb);
        variableCheckBoxes.add(oscilloscope_variable3_cb);
        oscilloscope_variable3_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetVariableUsed(2,oscilloscope_variable3_cb.isChecked());
            }
        });
        oscilloscope_variable3_cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowFourEditDialog(2);
                return true;
            }
        });
        //参数4
        CheckBox oscilloscope_variable4_cb = findViewById(R.id.oscilloscope_variable4_cb);
        variableCheckBoxes.add(oscilloscope_variable4_cb);
        oscilloscope_variable4_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetVariableUsed(3,oscilloscope_variable4_cb.isChecked());
            }
        });
        oscilloscope_variable4_cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowFourEditDialog(3);
                return true;
            }
        });
        //参数1
        Button oscilloscope_variable1_bt = findViewById(R.id.oscilloscope_variable1_bt);
        variableButtons.add(oscilloscope_variable1_bt);
        oscilloscope_variable1_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==0){
                    RestButton(0);
                }else{
                    SetSeekBar(0);
                }
            }
        });
        //参数2
        Button oscilloscope_variable2_bt = findViewById(R.id.oscilloscope_variable2_bt);
        variableButtons.add(oscilloscope_variable2_bt);
        oscilloscope_variable2_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==1){
                    RestButton(1);
                }else{
                    SetSeekBar(1);
                }
            }
        });
        //参数3
        Button oscilloscope_variable3_bt = findViewById(R.id.oscilloscope_variable3_bt);
        variableButtons.add(oscilloscope_variable3_bt);
        oscilloscope_variable3_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==2){
                    RestButton(2);
                }else{
                    SetSeekBar(2);
                }
            }
        });
        //参数4
        Button oscilloscope_variable4_bt = findViewById(R.id.oscilloscope_variable4_bt);
        variableButtons.add(oscilloscope_variable4_bt);
        oscilloscope_variable4_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==3){
                    RestButton(3);
                }else{
                    SetSeekBar(3);
                }
            }
        });
        //拖动条
        int[] scales=new int[]{1,10,100,1000,10000,100000};
        oscilloscope_adjust_sb = findViewById(R.id.oscilloscope_adjust_sb);
        oscilloscope_adjust_sb.setMax(2000);
        oscilloscope_adjust_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if((buttonChecked!=-1)&&fromUser) {
                    variableValue[arrayChecked][buttonChecked] =
                            (variableMax[arrayChecked][buttonChecked] - variableMin[arrayChecked][buttonChecked]) * progress / 2000
                                    + variableMin[arrayChecked][buttonChecked];
                    if (variableScale[arrayChecked][buttonChecked] == 0){
                        variableButtons.get(buttonChecked).setText(" "+variableValue[arrayChecked][buttonChecked]);
                    }else{
                        variableButtons.get(buttonChecked).setText(" "+(double)variableValue[arrayChecked][buttonChecked]/scales[variableScale[arrayChecked][buttonChecked]]);
                    }
                    sp.putLong("oscilloscope_variable_value"+arrayChecked+buttonChecked, variableValue[arrayChecked][buttonChecked]);//保存数据
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //加号
        ImageButton oscilloscope_adjust_add_ib=findViewById(R.id.oscilloscope_adjust_add_ib);
        oscilloscope_adjust_add_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked!=-1){
                    variableValue[arrayChecked][buttonChecked]++;
                    if(variableValue[arrayChecked][buttonChecked]>variableMax[arrayChecked][buttonChecked]) {
                        variableValue[arrayChecked][buttonChecked]=variableMax[arrayChecked][buttonChecked];
                    }
                    if (variableScale[arrayChecked][buttonChecked] == 0){
                        variableButtons.get(buttonChecked).setText(" "+variableValue[arrayChecked][buttonChecked]);
                    }else{
                        variableButtons.get(buttonChecked).setText(" "+(double)variableValue[arrayChecked][buttonChecked]/scales[variableScale[arrayChecked][buttonChecked]]);
                    }
                    //刷新进度条
                    long progress=(variableValue[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked])
                            *2000/(variableMax[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked]);
                    oscilloscope_adjust_sb.setProgress((int)progress);
                    sp.putLong("oscilloscope_variable_value"+arrayChecked+buttonChecked, variableValue[arrayChecked][buttonChecked]);//保存数据
                }
            }
        });
        //减号
        ImageButton oscilloscope_adjust_reduce_ib=findViewById(R.id.oscilloscope_adjust_reduce_ib);
        oscilloscope_adjust_reduce_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked!=-1) {
                    variableValue[arrayChecked][buttonChecked]--;
                    if(variableValue[arrayChecked][buttonChecked]<variableMin[arrayChecked][buttonChecked]){
                        variableValue[arrayChecked][buttonChecked]=variableMin[arrayChecked][buttonChecked];
                    }
                    if (variableScale[arrayChecked][buttonChecked] == 0){
                        variableButtons.get(buttonChecked).setText(" "+variableValue[arrayChecked][buttonChecked]);
                    }else{
                        variableButtons.get(buttonChecked).setText(" "+(double)variableValue[arrayChecked][buttonChecked]/scales[variableScale[arrayChecked][buttonChecked]]);
                    }
                    //刷新进度条
                    long progress=(variableValue[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked])
                            *2000/(variableMax[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked]);
                    oscilloscope_adjust_sb.setProgress((int)progress);
                    sp.putLong("oscilloscope_variable_value"+arrayChecked+buttonChecked, variableValue[arrayChecked][buttonChecked]);//保存数据
                }
            }
        });
        //变量组选择
        oscilloscope_variable_array_bt = findViewById(R.id.oscilloscope_variable_array_bt);
        oscilloscope_variable_array_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VariableChannelList();
            }
        });
        //发送按钮
        Button oscilloscope_send_bt = findViewById(R.id.oscilloscope_send_bt);
        oscilloscope_send_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVariable();
            }
        });
    }

    private void SetVariableUsed(int num,boolean b) {
        variableUsed[arrayChecked][num]=b;
        PutData();
    }

    private void ShowFourEditDialog(int num){
        MyFourEditDialog myFourEditDialog =new MyFourEditDialog(this);
        myFourEditDialog.setCancelable(true);
        myFourEditDialog.show();
        myFourEditDialog.setTitle("参数"+(num+1));
        myFourEditDialog.setInformName("参数名：","最大值：","最小值：","小数点后位数：");
        myFourEditDialog.setHint("参数名称","最大值：-100000~100000","最小值：-100000~100000","小数点后位数：0~5");
        myFourEditDialog.setInform(variableName[arrayChecked][num],
                (variableMax[arrayChecked][num]/(int)Math.pow(10,variableScale[arrayChecked][num])+""),
                (variableMin[arrayChecked][num]/(int)Math.pow(10,variableScale[arrayChecked][num])+""),
                variableScale[arrayChecked][num]+"");
        myFourEditDialog.setOnDialogClickListener(new MyFourEditDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_edit_four_yes_bt){
                    variableName[arrayChecked][num]=myFourEditDialog.getInform1();
                    try {
                        int scale=Integer.parseInt(myFourEditDialog.getInform4());
                        if(scale>5||scale<0){
                            Toast.makeText(OscilloscopeActivity.this, "输入小数点后位数不符合规范", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        variableValue[arrayChecked][num]=variableValue[arrayChecked][num]*(int)Math.pow(10,scale)/(int)Math.pow(10,variableScale[arrayChecked][num]);
                        variableScale[arrayChecked][num]=scale;
                    }catch (NumberFormatException e){
                        Toast.makeText(OscilloscopeActivity.this, "输入小数点后位数不符合规范", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }
                    try {
                        variableMax[arrayChecked][num]=Integer.parseInt(myFourEditDialog.getInform2())*(int)Math.pow(10,variableScale[arrayChecked][num]);
                    }catch (NumberFormatException e){
                        Toast.makeText(OscilloscopeActivity.this, "输入最大值不符合规范", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }
                    try {
                        variableMin[arrayChecked][num]=Integer.parseInt(myFourEditDialog.getInform3())*(int)Math.pow(10,variableScale[arrayChecked][num]);
                    }catch (NumberFormatException e){
                        Toast.makeText(OscilloscopeActivity.this, "输入最小值不符合规范", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }
                    //检验数据最大值是否小于最小值
                    if(variableMax[arrayChecked][num]<=variableMin[arrayChecked][num]){
                        variableMax[arrayChecked][num]=variableMin[arrayChecked][num]+1;
                    }
                    //刷新数据名称
                    variableCheckBoxes.get(num).setText(variableName[arrayChecked][num] + "");
                    //限制数据大小

                    if(variableValue[arrayChecked][num]>variableMax[arrayChecked][num]) {
                        variableValue[arrayChecked][num]=variableMax[arrayChecked][num];
                    }else if(variableValue[arrayChecked][num]<variableMin[arrayChecked][num]){
                        variableValue[arrayChecked][num]=variableMin[arrayChecked][num];
                    }
                    if (variableScale[arrayChecked][num] == 0){
                        variableButtons.get(num).setText(" "+variableValue[arrayChecked][num]+"");
                    }else{
                        variableButtons.get(num).setText(" "+(double)variableValue[arrayChecked][num]/Math.pow(10,variableScale[arrayChecked][num])+"");
                    }
                    //刷新进度条
                    if(num==buttonChecked)
                    {
                        long progress=(variableValue[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked])
                            *2000/(variableMax[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked]);
                        oscilloscope_adjust_sb.setProgress((int)progress);
                    }
                    PutData();
                }
            }
        });
    }

    private void RestButton(int num){
        variableButtons.get(num).setBackgroundColor(Color.TRANSPARENT);//抬起这个按钮
        buttonChecked =-1;
    }

    private void SetSeekBar(int num){
        //抬起上一个按钮
        if(buttonChecked !=-1) {
            variableButtons.get(buttonChecked).setBackgroundColor(Color.TRANSPARENT);
        }
        //设置此按钮
        buttonChecked =num;
        variableButtons.get(num).setBackgroundColor(Color.parseColor("#87101010"));//按下这个按钮
        long progress=(variableValue[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked])
                *2000/(variableMax[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked]);
        oscilloscope_adjust_sb.setProgress((int)progress);
    }

    private void txTimerInit(){
        txTimer = new Timer();
        txTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SendVariable();
            }
        }, txTimerTime, txTimerTime);
    }

    private void ViewInit(){

        EditText oscilloscope_ser1_et = findViewById(R.id.oscilloscope_ser1_et);
        editTexts.add(oscilloscope_ser1_et);
        EditText oscilloscope_ser2_et = findViewById(R.id.oscilloscope_ser2_et);
        editTexts.add(oscilloscope_ser2_et);
        EditText oscilloscope_ser3_et = findViewById(R.id.oscilloscope_ser3_et);
        editTexts.add(oscilloscope_ser3_et);
        EditText oscilloscope_ser4_et = findViewById(R.id.oscilloscope_ser4_et);
        editTexts.add(oscilloscope_ser4_et);
        oscilloscope_rx_num_tv=findViewById(R.id.oscilloscope_rx_num_tv);

        oscilloscope_iv = findViewById(R.id.oscilloscope_iv);
        oscilloscope_iv.post(new Runnable() {
            @Override
            public void run() {
                ivWidth =oscilloscope_iv.getWidth();
                yMax =oscilloscope_iv.getHeight();
                if(osBitmap ==null){
                    offsetY = yMax /2;
                    osBitmap = Bitmap.createBitmap(ivWidth, yMax, Bitmap.Config.RGB_565);
                    arrayDoubles =new ArrayList<>();
                    dataArray1 = new double[3000000];//可储存300万数
                    arrayDoubles.add(dataArray1);
                    dataArray2 = new double[3000000];//可储存300万数
                    arrayDoubles.add(dataArray2);
                    dataArray3 = new double[3000000];//可储存300万数
                    arrayDoubles.add(dataArray3);
                    dataArray4 = new double[3000000];//可储存300万数
                    arrayDoubles.add(dataArray4);
                    dataScales = new double[11];
                }
                yDivO = yMax /10; //网格原始间隔
                paintText.setTextSize(yMax /25); //接收数量字体
                DrawWall();
                TimerInit();
                refreshFlag =true;
            }
        });
    }

    /**
     * 设置按键名称
     * @param num 按键序号
     */
    private void ShowEditDialog(int num){
        MyOneEditDialog myOneEditDialog =new MyOneEditDialog(this);
        myOneEditDialog.setCancelable(true);
        myOneEditDialog.show();
        myOneEditDialog.setTitle("波形"+(num+1));
        myOneEditDialog.setInformName("波形名称：");
        myOneEditDialog.setHint("波形名称");
        myOneEditDialog.setInform(checkBoxes.get(num).getText().toString());
        myOneEditDialog.setOnDialogClickListener(new MyOneEditDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_edit_one_yes_bt){
                    checkBoxes.get(num).setText(myOneEditDialog.getInform());
                    PutData();
                }
            }
        });
    }

    /**
     * 数据类型列表
     */
    private final String[] items = {"字符串","int8_t", "uint8_t", "int16_t", "uint16_t","int32_t", "uint32_t", "float","double"};
    private void DataTypeList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle("数据类型");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                oscilloscope_data_type_bt.setText(items[which]);
                oscilloscopeDataType =which;
                PutData();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    /**
     * 通道数量列表
     */
    private void ChannelNumList() {
        String[] items = {"1", "2", "3", "4"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle("通道数量");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                oscilloscopeChannelNum =which+1;
                oscilloscope_channel_num_bt.setText(oscilloscopeChannelNum +"");
                for (int i = 0; i < 4; i++) {
                    editTexts.get(i).setText(null);
                }
                PutData();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 通道数量列表
     */
    private void VariableChannelList() {
        String[] items = {"1","2","3","4","5","6"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle("变量组选择");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                arrayChecked =which;
                oscilloscope_variable_array_bt.setText("CH"+(which+1));
                for (int i = 0; i < 4; i++) {
                    variableCheckBoxes.get(i).setChecked(variableUsed[arrayChecked][i]);
                    variableCheckBoxes.get(i).setText(variableName[arrayChecked][i] + "");
                    if (variableScale[arrayChecked][i] == 0){
                        variableButtons.get(i).setText(" "+variableValue[arrayChecked][i]);
                    }else{
                        variableButtons.get(i).setText(" "+(double)variableValue[arrayChecked][i]/Math.pow(10,variableScale[arrayChecked][i]));
                    }
                }
                if(buttonChecked!=-1){
                    long progress=(variableValue[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked])
                            *2000/(variableMax[arrayChecked][buttonChecked]- variableMin[arrayChecked][buttonChecked]);
                    oscilloscope_adjust_sb.setProgress((int)progress);
                }
                PutData();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    void SendVariable()
    {
        StringBuilder string_builder= new StringBuilder();

        for (int i = 0; i < 4; i++) {
            if(variableCheckBoxes.get(i).isChecked()) {
                string_builder.append("c").append(arrayChecked+1).append(i+1).append(":");
                string_builder.append(variableButtons.get(i).getText().toString().trim()).append(",");
            }
        }
        String string=string_builder+"\0\r\n";
        mCommunicationIService.callWrite(string.getBytes());
    }

    /**
     * 波形初始化
     */
    private void SeriesInit(){
        paths=new ArrayList<Path>();
        paints=new ArrayList<Paint>();
        //虚线
        paintWall1 = new Paint();
        pathWall1 = new Path();
        paintWall1.setColor(Color.GRAY);
        paintWall1.setStyle(Paint.Style.STROKE);
        paintWall1.setPathEffect(new DashPathEffect(new float[] {5, 5}, 1));
        //零线
        paintWall2 = new Paint();
        pathWall2 = new Path();
        paintWall2.setColor(Color.GRAY);
        paintWall2.setStrokeWidth(3);
        paintWall2.setStyle(Paint.Style.STROKE);
        //边框
        paintWall3 = new Paint();
        pathWall3 = new Path();
        paintWall3.setColor(Color.GRAY);
        paintWall3.setStrokeWidth(2);
        paintWall3.setStyle(Paint.Style.STROKE);
        //格尺
        paintWall4 = new Paint();
        pathWall4 = new Path();
        paintWall4.setColor(mDrawColor);
        paintWall4.setStrokeWidth(2);
        paintWall4.setStyle(Paint.Style.FILL);
        //数字
        paintText = new Paint();
        paintText.setColor(Color.YELLOW);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextSize(30);
        paintText.setAntiAlias(true);     //去除锯齿

        //四条波形
        // 波形1
        Paint paint1 = new Paint();
        Path path1 = new Path();
        paint1.setColor(Color.BLUE);
        paint1.setStrokeWidth(3);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        paints.add(paint1);
        paths.add(path1);
        //波形2
        Paint paint2 = new Paint();
        Path path2 = new Path();
        paint2.setColor(0XFF00DD00);
        paint2.setStrokeWidth(3);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setAntiAlias(true);
        paints.add(paint2);
        paths.add(path2);
        //波形3
        Paint paint3 = new Paint();
        Path path3 = new Path();
        paint3.setColor(Color.RED);
        paint3.setStrokeWidth(3);
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setAntiAlias(true);
        paints.add(paint3);
        paths.add(path3);
        //波形4
        Paint paint4 = new Paint();
        Path path4 = new Path();
        paint4.setColor(0xFF7B68B4);
        paint4.setStrokeWidth(3);
        paint4.setStyle(Paint.Style.STROKE);
        paint4.setAntiAlias(true);
        paints.add(paint4);
        paths.add(path4);
        for (int i = 0; i <4; i++) {
            paths.get(i).moveTo(0,0);
        }
    }

    /**
     * 显示所有路径
     */
    private void draw(){
        Canvas canvas = new Canvas(osBitmap);
        canvas.drawColor(mDrawColor);
        canvas.drawPath(pathWall1, paintWall1);
        canvas.drawPath(pathWall2, paintWall2);
        for(int i = 0; i< oscilloscopeChannelNum; i++){
            if(seriesVisible[i]) {
                canvas.drawPath(paths.get(i), paints.get(i));
            }
        }
        canvas.drawPath(pathWall4, paintWall4);
        canvas.drawPath(pathWall3, paintWall3);
        for (int i = 0; i < 11; i++) {
            float temp= yDivRem + yDiv *i;
            canvas.drawText(dataScales[i]+"", xMax +10,temp, paintText);
        }
        oscilloscope_iv.setImageBitmap(osBitmap);
        oscilloscope_rx_num_tv.setText("RX:"+ dataNum);
    }

    /**
     * 清空波形
     */
    private void Clear(){
        for (int i = 0; i < 4; i++) {
            paths.get(i).reset();
            paths.get(i).moveTo(0,0);
        }
        dataNum =0;
        refreshFlag =true;
    }

    /**
     * 得到窗口X显示最大值（减去刻度位置）
     */
    private void GetX_max(){
        float stringWidth_max;
        stringWidth_max=0;
        for (int i = 0; i < 11; i++) {
            if((yDivRem + yDiv *i)< yMax){
                float stringWidth= paintText.measureText(dataScales[i]+"  ");
                if(stringWidth_max<stringWidth){
                    stringWidth_max=stringWidth;
                }
            }
        }
        xMax = ivWidth -(int)stringWidth_max;
    }

    /**
     * 设置网格行间距，放大3个一循环
     */
    private void SetY_div(){
        if(multiplier %3==0) {
            zoom =Math.pow(10,(int)(multiplier /3));
        }else if(multiplier %3==1) {
            zoom =2*Math.pow(10,(int)(multiplier /3));
        }else if(multiplier %3==2) {
            zoom =5*Math.pow(10,(int)(multiplier /3));
        }else if(multiplier %3==-1) {
            zoom =0.5*Math.pow(10,(int)(multiplier /3));
        }else if(multiplier %3==-2) {
            zoom =0.2*Math.pow(10,(int)(multiplier /3));
        }
        yDiv = (float)(scaleY * yDivO / zoom); //计算得到刻度宽度
    }
    /**
     * 画图表
     */
    private void DrawWall(){
        pathWall1.reset();
        pathWall2.reset();
        pathWall3.reset();
        pathWall4.reset();
        yDiv =(float) (scaleY * yDivO / zoom);
        if(yDiv >2* yDivO){
            //不是处于比例2xx到5xx的过程
            if(multiplier %3!=1&& multiplier %3!=-2){
                multiplier++;
                SetY_div();
            }
        }
        if(yDiv >2.5* yDivO) {
            multiplier++;
            SetY_div();
        }
        if(yDiv < yDivO){
            multiplier--;
            SetY_div();
        }
        //计算坐标值
        for (int i =0; i <11; i++){
            if(multiplier %3==0){
                if(multiplier >0) {
                    dataScales[i] = ((int) (offsetY / yDiv) - i)/Math.pow(10,(int)(multiplier /3));
                }else{
                    dataScales[i] = ((int) (offsetY / yDiv) - i)*Math.pow(10, (int)(-multiplier /3));
                }
            }else if(multiplier %3==1){
                dataScales[i]=((int)(offsetY / yDiv)-i)/(2*Math.pow(10,(int)((multiplier -1)/3)));
            }else if(multiplier %3==-1){
                dataScales[i]=((int)(offsetY / yDiv)-i)*2*Math.pow(10,(int)(-(multiplier +1)/3));
            }else if(multiplier %3==2){
                dataScales[i]=((int)(offsetY / yDiv)-i)/(5*Math.pow(10,(int)((multiplier -2)/3)));
            }else if(multiplier %3==-2){
                dataScales[i]=((int)(offsetY / yDiv)-i)*5*Math.pow(10,(int)((-(multiplier +2)/3)));
            }
        }
        yDivRem =(float)(offsetY % yDiv);
        //虚线
        GetX_max();//由于刻度尺标号长度变化，波形显示区域也随之变化
        for (int i = 0; i <11; i++) {
            pathWall1.moveTo(0, yDivRem + yDiv *i);
            pathWall1.lineTo(xMax, yDivRem + yDiv *i);
        }
        //格尺
        pathWall3.moveTo(xMax,0);
        pathWall3.lineTo(xMax, yMax);
        for (int i = 0; i <11; i++) {
            pathWall3.moveTo(xMax, yDivRem + yDiv *i);
            pathWall3.lineTo(xMax +10, yDivRem + yDiv *i);
        }
        pathWall3.addRect(0,0, ivWidth, yMax,Path.Direction.CCW);//方框
        pathWall4.addRect(xMax,0, ivWidth, yMax,Path.Direction.CCW); //覆盖多余曲线
        //零线
        pathWall2.moveTo(0, (float) offsetY);
        pathWall2.lineTo(xMax +10, (float) offsetY);
    }
    /**
     *添加数据
     */
    private void AddData(double data1,double data2,double data3,double data4) {
        if(dataNum <3000000){
            dataArray1[dataNum]=data1;
            dataArray2[dataNum]=data2;
            dataArray3[dataNum]=data3;
            dataArray4[dataNum]=data4;
            dataNum++;
            offsetX =(float) scaleX *(dataNum -1)- xMax;//置最大，实时显示数据
            refreshFlag =true;
        }else if(dataNum ==3000000){
            DisHintDialog();
            dataNum++;
        }
    }

    /**
     * 接收已满提示
     */
    private void DisHintDialog(){
        MyHintDialog myHintDialog = new MyHintDialog(this );
        myHintDialog.setCancelable(false);//点击屏幕外可取消
        myHintDialog.show();
        myHintDialog.setText("接收数据已达上线");
        myHintDialog.setOnDialogClickListener(new MyHintDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
            }
        });
    }

    /**
     * 获得数组张最大值
     * @param doubles 数组
     * @return 最大值
     */
    private double GetMax(double[] doubles){
        int len=doubles.length;
        double max=doubles[0];
        for (int i = 1; i < len; i++) {
            if(doubles[i]>max){
                max=doubles[i];
            }
        }
        return max;
    }

    /**
     * 获得数组中最小值
     * @param doubles 数组
     * @return 最小值
     */
    private double GetMin(double[] doubles){
        int len=doubles.length;
        double min=doubles[0];
        for (int i = 1; i < len; i++) {
            if(doubles[i]<min){
                min=doubles[i];
            }
        }
        return min;
    }

    /**
     * 将数值限制到-100000000至100000000
     * @param d 原始值
     * @return 限位值
     */
    private double Limiting(double d)
    {
        if(d>100000000){
            d=100000000;
        }else if(d<-100000000){
            d=-100000000;
        }
        return d;
    }
    /**
     *刷新波形
     */
    private void RefreshWave() {
        double X,Y;
        double[] min=new double[4];
        double[] max =new double[4];
        //清除原路径
        for (int i = 0; i < oscilloscopeChannelNum; i++) {
            paths.get(i).reset();
        }
        //显示长度没有超过屏幕
        if(scaleX *(dataNum -1)< xMax) {
            offsetX =0; //为后续缩放做准备
        }
        //偏移不可小于零
        if(offsetX <0) {
            offsetX =0;
        }
        //防止小于零卡死
        int start=(int)(offsetX / scaleX);//第一个刷新数据数组中位置
        int end=(int)((offsetX + xMax)/ scaleX +2);//最后一个刷新数组中位置，多刷一个，再加一表示数量
        if(end> dataNum) end= dataNum; //超出数量,限位
        int dx=(int)(2f/ scaleX);   //两个像素内显示数据量
        if(dx<3){
            for (int i =start; i<end; i++) {
                X=i* scaleX - offsetX;
                for (int j = 0; j < oscilloscopeChannelNum; j++) {
                    Y=Limiting(-scaleY * yDivO * arrayDoubles.get(j)[i]+ offsetY);  //Y值限位
                    paths.get(j).lineTo((float)X,(float)Y);
                }
            }
        }else{
            double[] doubles=new double[dx];
            int rem=(end-start)%dx;//余数
            end-=rem; //减去后面余数
            for (int i =start; i <end; i+=dx) {
                X=i* scaleX - offsetX;
                for (int j = 0; j < oscilloscopeChannelNum; j++) {
                    System.arraycopy(arrayDoubles.get(j),i,doubles,0,dx);
                    min[j]=GetMin(doubles);
                    Y=Limiting(-scaleY * yDivO *min[j]+ offsetY); //Y值限位
                    paths.get(j).lineTo((float)X,(float)Y);
                    //Y值限位
                    max[j]=GetMax(doubles);
                    Y=Limiting(-scaleY * yDivO *max[j]+ offsetY);//Y值限位
                    paths.get(j).lineTo((float)X,(float)Y);
                }
            }
            //有余数
            if(rem>0){
                doubles=new double[rem];
                X=end* scaleX - offsetX;
                for (int j = 0; j < oscilloscopeChannelNum; j++) {
                    System.arraycopy(arrayDoubles.get(j),end,doubles,0,rem);
                    min[j]=GetMin(doubles);
                    Y=Limiting(-scaleY * yDivO *min[j]+ offsetY);//Y值限位
                    paths.get(j).lineTo((float)X,(float)Y);
                    max[j]=GetMax(doubles);
                    Y=Limiting(-scaleY * yDivO *max[j]+ offsetY);  //Y值限位
                    paths.get(j).lineTo((float)X,(float)Y);
                }
            }
        }
        draw();
    }
    /**
     *  设置画布颜色
     */
    private void SetDrawColor(boolean color){
        if(color){
            mDrawColor=Color.rgb(30,30,30);//黑
            paintText.setColor(Color.YELLOW);
            paintWall3.setColor(Color.GRAY);
            paintWall4.setColor(mDrawColor);
            oscilloscope_rx_num_tv.setTextColor(Color.YELLOW);
        }
        else{
            mDrawColor=Color.rgb(240,240,240);//白
            paintText.setColor(Color.BLUE);
            paintWall3.setColor(Color.BLACK);
            paintWall4.setColor(mDrawColor);
            oscilloscope_rx_num_tv.setTextColor(Color.BLUE);
        }
        refreshFlag =true;
    }

    /**
     * 设置波形是否可见
     * @param series 波形编号
     * @param life  是否可见
     */
    private void SetSeries_visible(int series,boolean life){
        seriesVisible[series]=life;
        refreshFlag =true;
        PutData();
    }

    private double DownX,DownY,DownXYed,DownXYing,Y_Center,X_Center;
    private int Two_flag;
    /**
     * 按钮调整X轴
     * @param dir 方向
     */
    private void SetScaleX(boolean dir){
        double tempX;
        double tempOX;
        if(dir){
            tempX = scaleX *2;
             tempOX =(offsetX +(double) xMax /2)*2-(double) xMax /2;      //新的X方向偏移
        }else{
            tempX = scaleX /2;
             tempOX =(offsetX +(double) xMax /2)/2-(double) xMax /2;      //新的X方向偏移
        }
        if (tempX < 1000 && tempX > 0.00001) {
            scaleX = tempX;
            offsetX =tempOX;
            DrawWall();
            refreshFlag =true;
        }
    }
    /**
     * 外部调整Y轴
     */
    private void SetScaleY(boolean dir){
        double tempY;
        double tempO;
        if(dir){
            tempY = scaleY *2;
            tempO=(offsetY -(double) yMax /2)*2+(double) yMax /2;
        }else{
            tempY = scaleY /2;
            tempO=(offsetY -(double) yMax /2)/2+(double) yMax /2;
        }
        if (tempY < 100000000 && tempY > 0.000000000001 && tempO<100000000 && tempO>-100000000) {
            scaleY = tempY;
            offsetY =tempO;
            DrawWall();
            refreshFlag =true;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetGesture(){
        oscilloscope_iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()& MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Two_flag=1;
                        DownX=event.getX();
                        DownY=event.getY();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        Two_flag=2;
                        DownXYed=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                                +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                        DownXYed=Math.sqrt(DownXYed);
                        Y_Center=(event.getY(0)+event.getY(1))/2;
                        X_Center=(event.getX(0)+event.getX(1))/2;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(Two_flag==2){
                            DownXYing=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                                    +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                            DownXYing=Math.sqrt(DownXYing);
                            //以两指为中心放大算法
                            double tempX =  ( scaleX * DownXYing / DownXYed);       //X方向比例
                            double tempY =  ( scaleY * DownXYing / DownXYed);       //Y方向比例
                            double tempOY  =(offsetY -Y_Center)*DownXYing/DownXYed+Y_Center;      //新的Y方向偏移
                            double tempOX  =(offsetX +X_Center)*DownXYing/DownXYed-X_Center;      //新的X方向偏移
                            //稍微限制一下缩放平移比例
                            if (tempX < 1000&& tempX > 0.00001
                                    && tempY < 100000000&& tempY > 0.000000000001
                                    &&tempOY<100000000&&tempOY>-100000000){
                                scaleX = tempX ;
                                scaleY =  tempY;
                                offsetY =tempOY;
                                offsetX =tempOX;
                                DownXYed = DownXYing;
                                DrawWall();
                                //防止与定时器刷新冲突
                                if(!refreshFlag) {
                                    RefreshWave();
                                }
                            }
                        }else if(Two_flag==1) {
                            offsetX -= event.getX() - DownX;
                            if(offsetX <0){
                                offsetX =0;
                            }
                            offsetY += event.getY() - DownY;
                            DownX = event.getX();
                            DownY = event.getY();
                            DrawWall();
                            if(!refreshFlag) {
                                RefreshWave();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        Two_flag=0;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 定时刷新
     */
    private void TimerInit(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(refreshFlag) {
                            if(oscilloscopeDataType ==0){
                                for (int i = 0; i < oscilloscopeChannelNum; i++) {
                                    editTexts.get(i).setText(oscilloscopeDoubleNum[i] + "");
                                }
                            }else if(oscilloscopeDataType <7){
                                for (int i = 0; i < oscilloscopeChannelNum; i++) {
                                    editTexts.get(i).setText(oscilloscopeLongNum[i] + "");
                                }
                            }else if(oscilloscopeDataType ==7){
                                for (int i = 0; i < oscilloscopeChannelNum; i++) {
                                    editTexts.get(i).setText(oscilloscopeFloatNum[i] + "");
                                }
                            }else if(oscilloscopeDataType ==8){
                                for (int i = 0; i < oscilloscopeChannelNum; i++) {
                                    editTexts.get(i).setText(oscilloscopeDoubleNum[i] + "");
                                }
                            }
                            RefreshWave();
                            refreshFlag = false;
                        }
                    }
                });
            }
        },80,80);
    }

    /**
     * 数据处理
     */
    private void DealBytes(byte[] bytes){
        switch (oscilloscopeDataType){
            case 1:
                for (int i = 0; i < oscilloscopeChannelNum; i++) {
                    byte int8= bytes[i];
                    oscilloscopeLongNum[i]=int8;
                }break;
            case 2:
                for (int i = 0; i < oscilloscopeChannelNum; i++) {
                    int u8=0XFF& bytes[i];
                    oscilloscopeLongNum[i]=u8;
                }break;
            case 3:
                for (int i = 0; i < bytes.length; i+=2) {
                    short int16=(short)(((0xFF& bytes[i+1])<<8)|(0xFF& bytes[i]));//顺序不能颠倒
                    oscilloscopeLongNum[i/2]=int16;
                }break;
            case 4:
                for (int i = 0; i < bytes.length; i+=2) {
                    int u16=((0xFF& bytes[i+1])<<8)|(0xFF& bytes[i]);
                    oscilloscopeLongNum[i/2]=u16;
                }break;
            case 5:
                for (int i = 0; i < bytes.length; i+=4) {
                    int int32=((0XFF& bytes[i+3])<<24)|((0XFF& bytes[i+2])<<16)
                            |((0XFF& bytes[i+1])<<8)|(0XFF& bytes[i]);
                    oscilloscopeLongNum[i/4]=int32;
                }break;
            case 6:
                for (int i = 0; i < bytes.length; i+=4) {
                    long u32=((long)(0XFF& bytes[i+3])<<24)|((long)(0XFF& bytes[i+2])<<16)
                            |((long)(0XFF& bytes[i+1])<<8)|(long)(0XFF& bytes[i]);
                    oscilloscopeLongNum[i/4]=u32;
                }break;
            case 7:
                for (int i = 0; i < bytes.length; i+=4) {
                    int int32=((0XFF& bytes[i+3])<<24)|((0XFF& bytes[i+2])<<16)
                            |((0XFF& bytes[i+1])<<8)|(0XFF& bytes[i]);
                    float f=Float.intBitsToFloat(int32);
                    oscilloscopeFloatNum[i/4]=f;
                }break;
            case 8:
                for (int i = 0; i < bytes.length; i+=8) {
                    long long64=((long)(0XFF& bytes[i+7])<<56)|((long)(0XFF& bytes[i+6])<<48)
                            |((long)(0XFF& bytes[i+5])<<40)|((long)(0XFF& bytes[i+4])<<32)
                            |((long)(0XFF& bytes[i+3])<<24)|((long)(0XFF& bytes[i+2])<<16)
                            |((long)(0XFF& bytes[i+1])<<8)|(long)(0XFF& bytes[i]);
                    double d=Double.longBitsToDouble(long64);
                    oscilloscopeDoubleNum[i/8]=d;
                }break;
        }
        if(oscilloscopeDataType <7){
            AddData(oscilloscopeLongNum[0],
                    oscilloscopeLongNum[1],
                    oscilloscopeLongNum[2],
                    oscilloscopeLongNum[3]);
        }else if(oscilloscopeDataType ==7){
            AddData(oscilloscopeFloatNum[0],
                    oscilloscopeFloatNum[1],
                    oscilloscopeFloatNum[2],
                    oscilloscopeFloatNum[3]);
        } else if(oscilloscopeDataType ==8){
            AddData(oscilloscopeDoubleNum[0],
                    oscilloscopeDoubleNum[1],
                    oscilloscopeDoubleNum[2],
                    oscilloscopeDoubleNum[3]);
        }
    }
    /**
     * 数据处理
     */
    private void DealString(){
        byte[] bytes=new byte[rxNum];
        System.arraycopy(bytesData,0,bytes,0,bytes.length); //裁剪数据
        String string=new String(bytes);
        String[] strings= string.split(",");
        if(strings.length!=oscilloscopeChannelNum) {
            Log.d("Oscilloscope","数据量异常-------------------");
            return;
        }
        for (int i = 0; i < oscilloscopeChannelNum; i++) {
            try {
                oscilloscopeDoubleNum[i]=Double.parseDouble(strings[i]);
            }catch (NumberFormatException e){
                Log.d("Oscilloscope","数字异常-------------------");
                return;
            }catch (NullPointerException e){
                Log.d("Oscilloscope","没有检测到数-------------------");
                return;
            }
        }
        AddData(oscilloscopeDoubleNum[0],
                oscilloscopeDoubleNum[1],
                oscilloscopeDoubleNum[2],
                oscilloscopeDoubleNum[3]);
    }
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes=intent.getByteArrayExtra("value");
            assert bytes != null;
            if(oscilloscopeDataType!=0){
                for (byte aByte : bytes) {
                    if (rxState == 2) {
                        bytesData[rxNum] = aByte;
                        rxNum++;
                        if (rxNum >= dataLength) {
                            rxState = 3;
                            rxNum = 0;
                        }
                    } else if (rxState == 3) {
                        if ((0XFF & aByte) == 0xFC) {
                            rxState = 4;
                        } else {
                            if (aByte == 0x03) {
                                rxState = 1;
                            } else {
                                rxState = 0;
                            }
                        }
                    } else if (rxState == 4) {
                        if (aByte == 0x03) {
                            rxState = 0;
                            DealBytes(bytesData);
                        } else {
                            rxState = 0;
                        }
                    } else if (rxState == 1) {
                        if ((0XFF & aByte) == 0xFC) {
                            rxState = 2;
                            rxNum =0;
                        } else {
                            if (aByte != 0x03) {
                                rxState = 0;
                            }
                        }
                    } else if (rxState == 0) {
                        if (aByte == 0x03) {
                            rxState = 1;
                        }
                    }
                }
            }else{
                for (byte aByte : bytes) {
                    if (rxState == 2) {
                        if(aByte!=0x0d){
                            bytesData[rxNum] = aByte;
                            rxNum++;
                            if (rxNum >= 255) {
                                Log.d("Oscilloscope","数据量超标-------------------");
                                rxState=0;
                                rxNum = 0;
                            }
                        }else{
                            rxState=3;
                        }
                    }
                    else if (rxState == 3) {
                        rxState=0;
                        DealString();
                        rxNum = 0;
                    }
                    else if (rxState == 1) {
                        if (aByte == ':') {
                            rxState = 2;
                            rxNum =0;
                        } else {
                            if (aByte != 'w') {
                                rxState = 0;
                            }
                        }
                    } else if (rxState == 0) {
                        if (aByte == 'w') {
                            rxState = 1;
                        }
                    }
                }
            }
        }
    }
    /**
     * 将文件中byte转为double
     * @param bytes byte数组
     * @return double数组
     */
    private double[] BytesToDoubles(byte[] bytes){
        int len=bytes.length;
        double[] doubles=new double[len/8];
        for (int i = 0; i < len; i+=8) {
            long long64=((long)(0XFF&bytes[i+7])<<56)|((long)(0XFF&bytes[i+6])<<48)
                    |((long)(0XFF&bytes[i+5])<<40)|((long)(0XFF&bytes[i+4])<<32)
                    |((long)(0XFF&bytes[i+3])<<24)|((long)(0XFF&bytes[i+2])<<16)
                    |((long)(0XFF&bytes[i+1])<<8)|(long)(0XFF&bytes[i]);
            doubles[i/8]=Double.longBitsToDouble(long64);
        }
        return doubles;
    }
    /**
     * 将double转为byte便于储存
     * @param doubles double数组
     * @return byte数组
     */
    private byte[] DoublesToBytes(double[] doubles,int num){
        byte[] bytes=new byte[8*num];
        for (int i = 0; i < num; i++) {
            long long64 = Double.doubleToLongBits(doubles[i]);
            bytes[8*i] = (byte) (long64 & 0xff);
            bytes[8*i+1] = (byte) ((long64 >> 8) & 0xff);
            bytes[8*i+2] = (byte) ((long64 >> 16) & 0xff);
            bytes[8*i+3] = (byte) ((long64 >> 24) & 0xff);
            bytes[8*i+4] = (byte) ((long64 >> 32) & 0xff);
            bytes[8*i+5] = (byte) ((long64 >> 40) & 0xff);
            bytes[8*i+6] = (byte) ((long64 >> 48) & 0xff);
            bytes[8*i+7] = (byte) ((long64 >> 56) & 0xff);
        }
        return bytes;
    }

    /**
     * 保存数据到文件
     */
    private void SaveFile(){
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        }
        Objects.requireNonNull(intent).setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE,"波形.wave");
        File file = new File(Environment.getExternalStorageDirectory()+"/学会助手/虚拟示波器");
        if(file.exists()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:" + "%2f学会助手%2f虚拟示波器%2f");
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            }
        }
        startActivityForResult(intent,1);
    }
    /**
     * 打开文件
     */
    private void OpenFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        File file = new File(Environment.getExternalStorageDirectory()+"/学会助手");
        startActivityForResult(intent,2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            Uri uri = Objects.requireNonNull(data).getData();//得到uri，将uri转化成路径
            if(requestCode==1){
                WriteFile(uri);
            }else if(requestCode==2){
                ReadFile(uri);
            }
        }else{
            //Toast.makeText(this,"操作取消",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 将数据写到文件
     * @param uri uri
     */
    private void WriteFile(Uri uri){
        try {
            OutputStream outputStream =getContentResolver().openOutputStream(Objects.requireNonNull(uri));
            DecimalFormat decimalFormat = new DecimalFormat("0000000");
            String string= oscilloscopeChannelNum +"#"+decimalFormat.format(dataNum)+"#";
            Objects.requireNonNull(outputStream).write(string.getBytes());
            for (int i = 0; i < oscilloscopeChannelNum; i++) {
                outputStream.write(DoublesToBytes(arrayDoubles.get(i), dataNum));
            }
            outputStream.close();
            Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"保存失败",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从文件中读出数据
     * @param uri uri
     */
    private void ReadFile(Uri uri){
        byte[] bytes_num;
        String string_num;
        try {
            InputStream inputStream=getContentResolver().openInputStream(uri);
            assert inputStream != null;
            if(inputStream.available()==0){
                Toast.makeText(this, "空文件", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] buffer = new byte[inputStream.available()];//InputStream.available() 表示要读取的文件中的数据长度
            inputStream.read(buffer);//读数据
            inputStream.close();//关数据流
            //判断第一位是否储存的通道数
            if((buffer[0]<'0')||(buffer[0]>'4')){
                Toast.makeText(this, "文件解析失败", Toast.LENGTH_SHORT).show();
                return;
            }
            bytes_num=new byte[7];   //储存数据数量
            System.arraycopy(buffer,2,bytes_num,0,7);   //提取数据数量
            string_num=new String(bytes_num);   //数据数量转字符串
            //判断是否全为数字
            if(!RegexUtil.isNum(string_num)){
                Toast.makeText(this, "文件解析失败", Toast.LENGTH_SHORT).show();
                return;
            }
            //数据数量不对
            if(((buffer.length-10)!=8*(buffer[0]-0x30)*Integer.parseInt(string_num))||(Integer.parseInt(string_num)>3000000)){
                Toast.makeText(this, "文件出错了", Toast.LENGTH_SHORT).show();
                return;
            }
            //检验完毕
            oscilloscopeChannelNum =buffer[0]-0x30;//得到通道数量
            dataNum =Integer.parseInt(string_num);//得到数据数量
            oscilloscope_channel_num_bt.setText(oscilloscopeChannelNum +"");
            //显示波形
            byte[] bytes;
            for (int i = 0; i < oscilloscopeChannelNum; i++) {
                bytes=new byte[8* dataNum];
                System.arraycopy(buffer,10+8*i* dataNum,bytes,0,bytes.length);
                System.arraycopy(BytesToDoubles(bytes),0, arrayDoubles.get(i),0, dataNum);
            }
            refreshFlag =true;
            Toast.makeText(this, "数据导入成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "数据导入失败", Toast.LENGTH_SHORT).show();
        }  catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "数据导入失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void PutData(){
        for (int i = 0; i < 4; i++) {
            sp.putBoolean("oscilloscope_ser_cb"+i,checkBoxes.get(i).isChecked());
        }
        for (int i = 0; i < 4; i++) {
            sp.putString("oscilloscope_ser_cb_text"+i,checkBoxes.get(i).getText().toString());
        }
        sp.putBoolean("oscilloscope_color_rb",oscilloscope_color_rb.isChecked()); //背景颜色
        sp.putInt("oscilloscope_data_type", oscilloscopeDataType); //数据类型
        sp.putInt("oscilloscope_channel_num", oscilloscopeChannelNum); //通道数量

        sp.putInt("oscilloscope_array_checked",arrayChecked);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                sp.putString("oscilloscope_variable_name"+i+j,variableName[i][j]);
                sp.putLong("oscilloscope_variable_value"+i+j, variableValue[i][j]);
                sp.putLong("oscilloscope_variable_max"+i+j, variableMax[i][j]);
                sp.putLong("oscilloscope_variable_min"+i+j, variableMin[i][j]);
                sp.putInt("oscilloscope_variable_scale"+i+j,variableScale[i][j]);
                sp.putBoolean("oscilloscope_variable_used"+i+j,variableUsed[i][j]);
            }
        }
        sp.putInt("Oscilloscope_txTimerTime",txTimerTime);
    }

    private void GetData(){
        for (int i = 0; i < 4; i++) {
            seriesVisible[i] =sp.getBoolean("oscilloscope_ser_cb"+i,true);
        }
        for (int i = 0; i < 4; i++) {
            String s=sp.getString("oscilloscope_ser_cb_text"+i,"波形"+(i+1));
            checkBoxes.get(i).setText(s);
        }
        oscilloscopeBackColor =sp.getBoolean("oscilloscope_color_rb",true);
        oscilloscopeDataType =sp.getInt("oscilloscope_data_type",6);
        oscilloscope_data_type_bt.setText(items[oscilloscopeDataType]);
        oscilloscopeChannelNum =sp.getInt("oscilloscope_channel_num",4);
        oscilloscope_channel_num_bt.setText(oscilloscopeChannelNum +"");

        arrayChecked=sp.getInt("oscilloscope_array_checked",0);
        oscilloscope_variable_array_bt.setText("CH"+(arrayChecked+1));
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                variableName[i][j]=sp.getString("oscilloscope_variable_name"+i+j,"参数"+(j+1));
                variableValue[i][j]=sp.getLong("oscilloscope_variable_value"+i+j,0);
                variableMax[i][j]=sp.getLong("oscilloscope_variable_max"+i+j,1000);
                variableMin[i][j]=sp.getLong("oscilloscope_variable_min"+i+j,0);
                variableScale[i][j]=sp.getInt("oscilloscope_variable_scale"+i+j,0);
                variableUsed[i][j]=sp.getBoolean("oscilloscope_variable_used"+i+j,true);
            }
        }

        for (int i = 0; i < 4; i++) {
            checkBoxes.get(i).setChecked(seriesVisible[i]);
        }
        refreshFlag =true;
        oscilloscope_color_rb.setChecked(oscilloscopeBackColor);
        SetDrawColor(oscilloscopeBackColor);
        for (int i = 0; i < 4; i++) {
            variableCheckBoxes.get(i).setChecked(variableUsed[arrayChecked][i]);
            variableCheckBoxes.get(i).setText(variableName[arrayChecked][i]+"");
            if (variableScale[arrayChecked][i] == 0){
                variableButtons.get(i).setText(" "+variableValue[arrayChecked][i]);
            }else{
                variableButtons.get(i).setText(" "+(double)variableValue[arrayChecked][i]/Math.pow(10,variableScale[arrayChecked][i]));
            }
        }
        txTimerTime=sp.getInt("Oscilloscope_txTimerTime",100);
        oscilloscope_adjust_timer_et.setText(txTimerTime+"");
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(myServiceConnection);
        if(oscilloscope_start_tb.isChecked()){  //注册广播的情况下注销广播
            unregisterReceiver(myBroadcastReceiver);
        }
        timer.cancel();
        if(txTimerEnabled){
            txTimer.cancel();
        }
        PutData();
    }
}
