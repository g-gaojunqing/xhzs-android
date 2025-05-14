package com.example.dzxh_app.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.dzxh_app.R;
import com.example.dzxh_app.ui.main_frament.CommunicateFragment;
import com.example.dzxh_app.ui.main_frament.HomeFragment;
import com.example.dzxh_app.ui.main_frament.ToolsFragment;
import com.example.dzxh_app.view.MySelectDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends BaseActivity {

    private HomeFragment homeFragment;
    private CommunicateFragment communicateFragment;
    private ToolsFragment toolsFragment;

    private int[] newVersion=new int[3]; //防止一直弹窗检测
    private final int[] myVersion=new int[3];

    private String updateUrl="https://www.bilibili.com/video/BV1sG4y167cn";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        homeFragment = new HomeFragment();
        communicateFragment = new CommunicateFragment();
        toolsFragment = new ToolsFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment, homeFragment).commitAllowingStateLoss(); //底部导航栏
        ButtonInit();   //按钮初始化
        getData();
        HandlerInit();
        getWebData();
    }

    /**
     * 退出提示
     */
    private void ShowSelectDialog(){
        MySelectDialog mySelectDialog=new MySelectDialog(this);
        mySelectDialog.show();
        mySelectDialog.setButtonText("忽略本次","查看更新");
        mySelectDialog.setText("哦吼，发现新版本【v"+newVersion[0]+"."+newVersion[1]+"."+newVersion[2]+"】\r\n"+"快来下载吧！");
        mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_select_yes_bt){
                    Intent intent = new Intent();
                    intent.setData(Uri.parse(updateUrl));//Url 要打开的网址
                    intent.setAction(Intent.ACTION_VIEW);
                    startActivity(intent);
                }else if(view.getId()==R.id.layout_dialog_select_no_bt){
                    if(newVersion[0]!=0){
                        putData();
                    }
                }
            }
        });
    }
    private int[] getVersion(String string){
        int[] version=new int[3];
        string=string.replace('.',',');
        String[] strings= string.split(",");
        if(strings.length!=3) {
            Log.d("MainActivity","数据量异常-------------------");
            return null;
        }
        for (int i = 0; i < 3; i++) {
            try {
                version[i]=Integer.parseInt(strings[i]);
            }catch (NumberFormatException e){
                Log.d("MainActivity","数字异常-------------------");
                return null;
            }catch (NullPointerException e){
                Log.d("MainActivity","没有检测到数-------------------");
                return null;
            }
        }
        return version;
    }

    private Handler handler;
    @SuppressLint("HandlerLeak")
    private void HandlerInit(){
        handler=new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what!=0){
                    return;
                }
                String string=msg.obj.toString();
                newVersion=getVersion(string);
                if(newVersion==null){
                    return;
                }
                Log.d("MainActivity","newVersion:"+newVersion[0]+"."+newVersion[1]+"."+newVersion[2]);
                if((newVersion[0]>myVersion[0])||(newVersion[1]>myVersion[1])||(newVersion[2]>myVersion[2])){
                    ShowSelectDialog();
                }
            }
        };
    }

    private void handlerSendMessage(int what,String string){
        Message message = new Message();
        //封装message包
        message.what =what;   //收到消息标志位
        message.obj =string;
        handler.sendMessage(message);    //发送message到handler
    }

    private void getWebData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url=new URL("https://www.bilibili.com/video/BV1sG4y167cn");
                    HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(5000);
                    int code=httpURLConnection.getResponseCode();
                    Log.d("MainActivity","code:"+code);
                    if(code==200){
                        Log.d("MainActivity","获取成功");
                        InputStream inputStream=httpURLConnection.getInputStream();
                        byte[] bytes=new byte[1024];
                        inputStream.read(bytes);
                        httpURLConnection.disconnect();
                        String string=new String(bytes);
                        Log.d("MainActivity",string);
                        //提取版本信息
                        int index=string.indexOf("学会助手v");
                        if(index==-1){
                            return;
                        }
                        String version_str=string.substring(index);
                        int s=version_str.indexOf("v");
                        int e=version_str.indexOf("》");
                        if((s==-1)||(e==-1)){
                            return;
                        }
                        version_str=version_str.substring(s+1,e);

                        Log.d("MainActivity",version_str);
                        //提取更新视频地址
                        index=string.indexOf("版本更新：");
                        if(index==-1){
                            return;
                        }
                        String update_str= string.substring(index);
                        s=update_str.indexOf("：");
                        e=update_str.indexOf("，");
                        if((s==-1)||(e==-1)){
                            return;
                        }
                        updateUrl=update_str.substring(s+1,e);
                        Log.d("MainActivity","updateUrl:"+updateUrl);
                        handlerSendMessage(0,version_str);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("MainActivity","检查失败");
                }
            }
        }).start();
    }

    private void ButtonInit(){
        BottomNavigationView nav_view = findViewById(R.id.nav_view);
        nav_view.setSelectedItemId(R.id.navigation_home);
        nav_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        nav_view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.navigation_communicate) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, communicateFragment).commit();
                }
                if(menuItem.getItemId()==R.id.navigation_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,homeFragment).commit();
                }
                if(menuItem.getItemId()==R.id.navigation_tools) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,toolsFragment).commit();
                }
                return true;
            }
        });
    }

    private void putData(){
        sp.putInt("main_myVersion0",newVersion[0]);
        sp.putInt("main_myVersion1",newVersion[1]);
        sp.putInt("main_myVersion2",newVersion[2]);
    }

    private void getData(){
        int[] nowVersion={1,4,1};
        myVersion[0]=sp.getInt("main_myVersion0",nowVersion[0]);
        myVersion[1]=sp.getInt("main_myVersion1",nowVersion[1]);
        myVersion[2]=sp.getInt("main_myVersion2",nowVersion[2]);
        //如果以前点了忽略，将数据更新到本最新版本
        boolean update=false;
        if(myVersion[0]<nowVersion[0]){
            update=true;
        }else if(myVersion[0]==nowVersion[0]){
            if(myVersion[1]<nowVersion[1]){
                update=true;
            }else if(myVersion[1]==nowVersion[1]){
                if(myVersion[2]<nowVersion[2]){
                    update=true;
                }
            }
        }
        if(update){
            sp.putInt("main_myVersion0",nowVersion[0]);
            sp.putInt("main_myVersion1",nowVersion[1]);
            sp.putInt("main_myVersion2",nowVersion[2]);
            System.arraycopy(nowVersion, 0, myVersion, 0, 3);
        }
        Log.d("MainActivity","myVersion:"+myVersion[0]+"."+myVersion[1]+"."+myVersion[2]);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true); //将任务退到后台
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
