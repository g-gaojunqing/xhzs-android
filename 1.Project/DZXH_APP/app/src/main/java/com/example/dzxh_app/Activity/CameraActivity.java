package com.example.dzxh_app.Activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import com.example.dzxh_app.R;
import com.example.dzxh_app.ui.camera_fragment.BinaryFragment;
import com.example.dzxh_app.ui.camera_fragment.ColorFragment;
import com.example.dzxh_app.ui.camera_fragment.GreyFragment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class CameraActivity extends BaseActivity implements View.OnClickListener {

    private int fragmentNum;

    private ColorFragment colorFragment;
    private GreyFragment greyFragment;
    private BinaryFragment binaryFragment;
    private Button camera_color_bt;
    private Button camera_grey_bt;
    private Button camera_binary_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);          //去除状态栏
        //实例化Fragment
        ButtonInit();
        GetData();
        FragmentInit();
        getStoragePermission();

    }

    private void ButtonInit(){
        camera_color_bt = findViewById(R.id.camera_color_bt);
        camera_color_bt.setOnClickListener(this);

        camera_grey_bt = findViewById(R.id.camera_grey_bt);
        camera_grey_bt.setOnClickListener(this);

        camera_binary_bt = findViewById(R.id.camera_binary_bt);
        camera_binary_bt.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        //按钮全部设置为未点击状态
        camera_color_bt.setBackgroundColor(0x3F3F3F);
        camera_grey_bt.setBackgroundColor(0x3F3F3F);
        camera_binary_bt.setBackgroundColor(0x3F3F3F);
        camera_color_bt.setTextColor(Color.WHITE);
        camera_grey_bt.setTextColor(Color.WHITE);
        camera_binary_bt.setTextColor(Color.WHITE);
        if(v.getId()==R.id.camera_color_bt) {
            fragmentNum = 1;
            camera_color_bt.setBackgroundResource(R.drawable.shape_camera_button);
            camera_color_bt.setTextColor(Color.BLACK);
            getSupportFragmentManager().beginTransaction().replace(R.id.camera_fl, colorFragment).commitAllowingStateLoss();
        }
        if(v.getId()==R.id.camera_grey_bt) {
            fragmentNum = 2;
            camera_grey_bt.setBackgroundResource(R.drawable.shape_camera_button);
            camera_grey_bt.setTextColor(Color.BLACK);
            getSupportFragmentManager().beginTransaction().replace(R.id.camera_fl, greyFragment).commitAllowingStateLoss();
        }
        if(v.getId()==R.id.camera_binary_bt) {
            fragmentNum = 3;
            camera_binary_bt.setBackgroundResource(R.drawable.shape_camera_button);
            camera_binary_bt.setTextColor(Color.BLACK);
            getSupportFragmentManager().beginTransaction().replace(R.id.camera_fl, binaryFragment).commitAllowingStateLoss();
        }
        PutData();
    }

    /**
     * 显示上次打开的Fragment
     */

    public void FragmentInit() {
        colorFragment=new ColorFragment();
        greyFragment=new GreyFragment();
        binaryFragment=new BinaryFragment();
        switch (fragmentNum) {
            case 1:
                camera_color_bt.setBackgroundResource(R.drawable.shape_camera_button);
                camera_color_bt.setTextColor(Color.BLACK);
                getSupportFragmentManager().beginTransaction().replace(R.id.camera_fl, colorFragment).commitAllowingStateLoss();
                break;
            case 2:
                camera_grey_bt.setBackgroundResource(R.drawable.shape_camera_button);
                camera_grey_bt.setTextColor(Color.BLACK);
                getSupportFragmentManager().beginTransaction().replace(R.id.camera_fl, greyFragment).commitAllowingStateLoss();
                break;
            case 3:
                camera_binary_bt.setBackgroundResource(R.drawable.shape_camera_button);
                camera_binary_bt.setTextColor(Color.BLACK);
                getSupportFragmentManager().beginTransaction().replace(R.id.camera_fl, binaryFragment).commitAllowingStateLoss();
                break;
        }
    }

    private void PutData(){
        sp.putInt("Camera_fragmentNum", fragmentNum);
    }
    private void GetData(){
        fragmentNum =sp.getInt("Camera_fragmentNum",1);//获得打开上次打开Fragment标号
    }
    /**
     * 保存已打开Fragment标号
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
