package com.example.dzxh_app.Activity;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.view.ClockBoard;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ClockActivity extends BaseActivity {
    //控件
    private ClockBoard timer_clock_cb;
    private TextView timer_hour_tv;
    private TextView timer_day_tv;
    //定时器
    private Timer timerView; //定时刷新界面
    private Timer timerTx;  //定时发送
    //字符存储
    private  String[] stringsNum;
    private  String[] stringsWeek;
    //时间获取处理
    private Calendar calendar; //获取时间日期方法
    private  int year,month,day,week,hour_day,hour,minute,second,millisecond;//日期时间
    private boolean txFlag;
    //服务
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        setStatusBarColor(Color.BLACK, this);
        Toolbar clock_tb =findViewById(R.id.clock_tb);
        setSupportActionBar(clock_tb);
        clock_tb.setOverflowIcon(drawableIcon);
        clock_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ServiceInit();
        NumInit();
        ViewInit();
        ButtonInit();
        TimerTxInit();
        TimerViewInit();
    }


    private void NumInit(){
        txFlag =false;
        stringsNum = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8","9"};
        stringsWeek = new String[]{"天","一", "二", "三", "四", "五", "六",};
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
            timerView.cancel(); //旋转屏占用较多资源，先释放，不然会卡顿
            intent=new Intent(ClockActivity.this,BluetoothActivity.class);
            startActivityForResult(intent,0);
        }else if(item.getItemId() == R.id.menu_more_wifi){
            timerView.cancel(); //旋转屏占用较多资源，先释放，不然会卡顿
            intent=new Intent(ClockActivity.this,WifiActivity.class);
            startActivityForResult(intent,2);
        }else if(item.getItemId() == R.id.menu_more_help){
            timerView.cancel(); //旋转屏占用较多资源，先释放，不然会卡顿
            intent=new Intent(ClockActivity.this, UserActivity.class);
            intent.putExtra("page",9);
            startActivityForResult(intent,3);
        }
        return super.onOptionsItemSelected(item);
    }

    private void ViewInit(){
        timer_clock_cb = findViewById(R.id.timer_clock_cb);
        timer_hour_tv = findViewById(R.id.timer_hour_tv);
        timer_day_tv = findViewById(R.id.timer_day_tv);
    }

    private void ButtonInit(){
        ToggleButton clock_tx_tb = findViewById(R.id.clock_tx_tb);
        clock_tx_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                txFlag = isChecked;
            }
        });
    }

    /**
     * 定时发送
     */
    private void TimerTxInit(){
        timerTx =new Timer();
        timerTx.schedule(new TimerTask() {
            @Override
            public void run() {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH)+1;
                day = calendar.get(Calendar.DAY_OF_MONTH);
                week = calendar.get(Calendar.DAY_OF_WEEK)-1;
                hour_day = calendar.get(Calendar.HOUR_OF_DAY);
                hour = calendar.get(Calendar.HOUR);
                minute = calendar.get(Calendar.MINUTE);
                second = calendar.get(Calendar.SECOND);
                millisecond=calendar.get(Calendar.MILLISECOND)/100;
                if(txFlag){
                    SendData(year,month, day , week, hour_day, minute, second,millisecond);
                }
            }
        },200,100);
    }

    /**
     * 定时更新时间
     */
    private void TimerViewInit(){
        timerView = new Timer();
        timerView.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timer_hour_tv.setText(stringsNum[hour_day/10] + stringsNum[hour_day%10] + ":" + stringsNum[minute/10] + stringsNum[minute%10]);
                        timer_day_tv.setText(month+ "月" + day + "日" + " 星期" + stringsWeek[week]);
                        timer_clock_cb.Refresh_View(month, day , week, hour, minute, second);//更新转盘
                    }
                });
            }
        }, 200, 200);
    }

    private void SendData(int year,int month,int day,int week,int hour,int minute,int second,int mil){
        DecimalFormat decimalFormat4 =new DecimalFormat("0000");
        DecimalFormat decimalFormat2 =new DecimalFormat("00");
        DecimalFormat decimalFormat1 =new DecimalFormat("0");
        String string=decimalFormat4.format(year)+decimalFormat2.format(month)+decimalFormat2.format(day)+decimalFormat2.format(week)
                +decimalFormat2.format(hour)+decimalFormat2.format(minute)+decimalFormat2.format(second)
                +decimalFormat1.format(mil)+"\r\n";
        try {
            mCommunicationIService.callWrite(string.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 服务初始化
     */
    private void ServiceInit(){
        Intent intent = new Intent(this, CommunicationService.class);
        startService(intent);
        myServiceConnection = new MyServiceConnection();
        bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TimerViewInit();
    }

    @Override
    protected void onDestroy() {
        timerTx.cancel();
        timerView.cancel();
        unbindService(myServiceConnection);
        super.onDestroy();
    }
}
