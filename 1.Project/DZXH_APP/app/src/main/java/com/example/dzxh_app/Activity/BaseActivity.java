package com.example.dzxh_app.Activity;

import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.dzxh_app.R;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.util.SharedPreferencesUtil;

import java.io.File;

@SuppressLint({"SetTextI18n", "Registered"})
public class BaseActivity extends AppCompatActivity {

    public SharedPreferencesUtil sp;
    public Drawable drawableIcon;
    private static MyApplication myApplication;
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        sp = SharedPreferencesUtil.getInstance(getApplicationContext());

        drawableIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.abc_ic_menu_overflow_material);
        drawableIcon.setColorFilter(new LightingColorFilter(Color.WHITE,0x000000));

    }
    /**
     * 设置ListView的大小
     */
    public void setListViewHeightBasedOnChildren(ListView listView){
        ListAdapter listAdapter = listView.getAdapter();
        //如果没用项目
        if(listAdapter==null){
            return;
        }
        int totalHeight=0;
        for(int i=0;i<listAdapter.getCount();i++){
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0,0);
            totalHeight+=listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height=totalHeight+(listView.getDividerHeight()*(listAdapter.getCount()-1));

        listView.setLayoutParams(params);
    }

    /**r
     * 设置状态栏颜色
     *
     * @param statusColor
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(int statusColor, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            //取消设置Window半透明的Flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加Flag把状态栏设为可绘制模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏为透明
            window.setStatusBarColor(statusColor);
        }
    }



    /**获取定位权限*/
    public boolean getLocationPermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            int REQUEST_CODE_CONTACT = 102;
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BaseActivity","申请权限："+str);
                    //申请权限
                    this.requestPermissions(permissions,REQUEST_CODE_CONTACT);
                    return false;
                }else{
                    Log.d("BaseActivity","已有权限："+str);
                }
            }
        }
        return true;
    }
    /**储存权限*/
    public boolean getStoragePermission() {
        int REQUEST_CODE_CONTACT = 102;
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //验证是否许可权限
        for (String str : permissions) {
            if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                Log.d("BaseActivity","申请权限："+str);
                //申请权限
                this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                return false;
            }else{
                Log.d("BaseActivity","已有权限："+str);
            }
        }
        return true;
    }
    /**获取蓝牙权限*/
    public boolean getBluetoothPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            int REQUEST_CODE_CONTACT = 102;
            String[] permissions = new String[0];
            permissions = new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BaseActivity","申请权限："+str);
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return false;
                }else{
                    Log.d("BaseActivity","已有权限："+str);
                }
            }
            return true;
        }
        return true;
    }
}
