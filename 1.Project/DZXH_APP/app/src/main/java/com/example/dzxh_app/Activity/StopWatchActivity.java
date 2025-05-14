package com.example.dzxh_app.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class StopWatchActivity extends BaseActivity {

    private TextView stop_watch_min1_tv;
    private TextView stop_watch_min2_tv;
    private TextView stop_watch_sec1_tv;
    private TextView stop_watch_sec2_tv;
    private TextView stop_watch_mil1_tv;
    private TextView stop_watch_mil2_tv;
    private Button stop_watch_reset_bt;
    private ToggleButton stop_watch_start_tb;

    private static long mTimeStandard, mTimEd, mTimeIng;
    private static int mil, sec, min;
    private static boolean startFlag;

    private static int countTimes; //计次次数
    private static String[] itemStrings1;  //时间
    private static String[] itemStrings2; //增量

    private Timer timer;//定时发送

    private final String[] STRING_NUM = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    //服务相关
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_watch);
        setStatusBarColor(Color.parseColor("#000000"), this); //设置状态栏颜色
        Toolbar stop_watch_back_tb =findViewById(R.id.stop_watch_tb);
        setSupportActionBar(stop_watch_back_tb);
        stop_watch_back_tb.setOverflowIcon(drawableIcon);
        stop_watch_back_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        NumInit();
        ViewInit();
        ButtonInit();
        ServiceInit();
        TimerRun();
    }
    private void ServiceInit(){
        Intent intent = new Intent(StopWatchActivity.this, CommunicationService.class);
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
     * 广播接收初始化
     */
    private void BroadcastReceiverInit() {
        myBroadcastReceiver= new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter(
                "com.example.dzxh_app.content");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, filter,RECEIVER_EXPORTED); //绑定广播
        }else{
            registerReceiver(myBroadcastReceiver, filter); //绑定广播
        }
    }

    private void NumInit() {
        if(countTimes ==0){
            itemStrings1 =new String[500];
            itemStrings2 =new String[500];
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
            intent=new Intent(StopWatchActivity.this,BluetoothActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_more_wifi){
            intent=new Intent(StopWatchActivity.this,WifiActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_more_help){
            intent=new Intent(StopWatchActivity.this, UserActivity.class);
            intent.putExtra("page",9);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    private void ViewInit() {
        stop_watch_min1_tv = findViewById(R.id.stop_watch_min1_tv);
        stop_watch_min2_tv = findViewById(R.id.stop_watch_min2_tv);

        stop_watch_sec1_tv = findViewById(R.id.stop_watch_sec1_tv);
        stop_watch_sec2_tv = findViewById(R.id.stop_watch_sec2_tv);

        stop_watch_mil1_tv = findViewById(R.id.stop_watch_mil1_tv);
        stop_watch_mil2_tv = findViewById(R.id.stop_watch_mil2_tv);
        stop_watch_min1_tv.setText(STRING_NUM[min / 10]);
        stop_watch_min2_tv.setText(STRING_NUM[min % 10]);
        stop_watch_sec1_tv.setText(STRING_NUM[sec / 10]);
        stop_watch_sec2_tv.setText(STRING_NUM[sec % 10]);
        stop_watch_mil1_tv.setText(STRING_NUM[mil / 10]);
        stop_watch_mil2_tv.setText(STRING_NUM[mil % 10]);

        ListViewRefresh();
    }

    private void ButtonInit() {
        stop_watch_start_tb = findViewById(R.id.stop_watch_start_tb);
        stop_watch_start_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    WatchRun();
                } else {
                    WatchStop();
                }
            }
        });
        stop_watch_reset_bt = findViewById(R.id.stop_watch_reset_bt);
        stop_watch_reset_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //没有开始 #复位
                if (!startFlag) {
                    WatchReset();
                }else{
                    WatchTimes();
                }
            }
        });
        if (startFlag) {
            stop_watch_reset_bt.setText("计次");
            stop_watch_start_tb.setChecked(true);
        }
    }

    /**
     * 定时刷新秒数
     */
    private void TimerRun(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(startFlag){
                    mTimeIng = SystemClock.elapsedRealtime()/10;
                    int dTime = (int) (mTimeIng - mTimeStandard);
                    min = dTime % 360000 / 6000;
                    sec = dTime % 6000 / 100;
                    mil = dTime % 100;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stop_watch_min1_tv.setText(STRING_NUM[min / 10]);
                            stop_watch_min2_tv.setText(STRING_NUM[min % 10]);
                            stop_watch_sec1_tv.setText(STRING_NUM[sec / 10]);
                            stop_watch_sec2_tv.setText(STRING_NUM[sec % 10]);
                            stop_watch_mil1_tv.setText(STRING_NUM[mil / 10]);
                            stop_watch_mil2_tv.setText(STRING_NUM[mil % 10]);
                        }
                    });
                }
            }
        }, 0, 10);
    }

    /**
     * 开始计时
     */
    private void WatchRun(){
        mTimeStandard = SystemClock.elapsedRealtime()/10 - 6000 * min-100 * sec-mil;
        startFlag = true;
        stop_watch_reset_bt.setText("计次");
    }

    /**
     * 暂停
     */
    private void WatchStop(){
        startFlag = false;
        stop_watch_reset_bt.setText("复位");
    }

    /**
     * 计次操作
     */
    private void WatchTimes(){
        DecimalFormat decimalFormat =new DecimalFormat("00");
        if(countTimes <500){
            for (int i = countTimes; i>=1; i--) {
                itemStrings1[i]= itemStrings1[i-1];
                itemStrings2[i]= itemStrings2[i-1];
            }
            int mTime = (int) (mTimeIng - mTimeStandard);
            int d_min = mTime % 360000 / 6000;
            int d_sec = mTime % 6000 / 100;
            int d_mil = mTime % 100;
            itemStrings1[0]=decimalFormat.format(d_min)+":"
                    +decimalFormat.format(d_sec)+"."
                    +decimalFormat.format(d_mil);
            int dTime = (int) (mTime - mTimEd);
            d_min = dTime % 360000 / 6000;
            d_sec = dTime % 6000 / 100;
            d_mil = dTime % 100;
            itemStrings2[0]="+"+decimalFormat.format(d_min)+":"
                    +decimalFormat.format(d_sec)+"."
                    +decimalFormat.format(d_mil);
            mTimEd =mTime;
            countTimes++;
        }
        ListViewRefresh();
    }
    private void WatchReset(){
        min = 0;
        sec = 0;
        mil = 0;
        stop_watch_min1_tv.setText(STRING_NUM[0]);
        stop_watch_min2_tv.setText(STRING_NUM[0]);
        stop_watch_sec1_tv.setText(STRING_NUM[0]);
        stop_watch_sec2_tv.setText(STRING_NUM[0]);
        stop_watch_mil1_tv.setText(STRING_NUM[0]);
        stop_watch_mil2_tv.setText(STRING_NUM[0]);
        for (int i = 0; i< countTimes; i++) {
            if(itemStrings1[i]!=null){
                itemStrings1[i]=null;
                itemStrings2[i]=null;
            }else {
                break;
            }
        }
        countTimes =0;
        mTimEd =0;
        ListViewRefresh();
    }

    /**
     * 计次列表更新
     */
    private void ListViewRefresh() {
        //计次列表
        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < countTimes; i++) {
            if(itemStrings1[i]!=null){
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("name","计次"+(countTimes -i));
                map.put("num1", itemStrings1[i]);
                map.put("num2", itemStrings2[i]);
                arrayList.add(map);
            }else {
                break;
            }
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, arrayList,
                R.layout.item_stop_watch,
                new String[]{"name", "num1", "num2"},
                new int[]{R.id.item_stop_watch_tv1, R.id.item_stop_watch_tv2, R.id.item_stop_watch_tv3});
        ListView stop_watch_times_lv = findViewById(R.id.stop_watch_times_lv);

        stop_watch_times_lv.setAdapter(simpleAdapter);//列表配置
    }
    /**
     *广播接收到数据
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes = intent.getByteArrayExtra("value");
            assert bytes != null;
            for (byte aByte : bytes) {
                if (aByte == 0X32) {
                   if (startFlag) {
                        WatchTimes();
                    }
                } else if (aByte == 0X31) {
                    if (!startFlag) {
                        WatchRun();
                        stop_watch_start_tb.setChecked(true);
                    }
                } else if (aByte == 0X33) {
                    if (startFlag) {
                        WatchStop();
                        stop_watch_start_tb.setChecked(false);
                    }
                } else if (aByte == 0X34) {
                    if (!startFlag) {
                        WatchReset();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        unbindService(myServiceConnection);
        unregisterReceiver(myBroadcastReceiver);
    }
}
