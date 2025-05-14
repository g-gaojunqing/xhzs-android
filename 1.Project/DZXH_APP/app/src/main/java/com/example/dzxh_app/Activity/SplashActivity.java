package com.example.dzxh_app.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.dzxh_app.R;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.view.MySelectDialog;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends BaseActivity {

    private static boolean isOpen=false;
    private boolean appIsUpdate=false;

    private int[] myVersion=new int[3];
    private int[] newVersion=new int[3];
    private String updateUrl;
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setStatusBarColor(Color.parseColor("#FFFFFF"), this); //设置状态栏颜色

        myApplication=(MyApplication)this.getApplication();
        GetData();
        Log.d("SplashActivity","isOpen-------------------"+isOpen);
        if(!isOpen){
            isOpen=true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(2);
                }
            },2000);
       }else{
            next();
       }
    }
    private Handler handler =new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            next();
        }
    };
    private void next() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void GetData(){
        myApplication.setAutoBluetoothState(sp.getBoolean("AutoBluetoothState",false));//自动连接
        myApplication.setNameFilterState(sp.getBoolean("NameFilterState",false));
        myApplication.setAutoWifiState(sp.getBoolean("AutoWifiState",false));//自动连接
        myApplication.setCharsetName(sp.getString("CharsetName","GBK"));
    }
}
