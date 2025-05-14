package com.example.dzxh_app.Activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.view.MyInformDialog;
import com.example.dzxh_app.view.MyProcessDialog;
import com.example.dzxh_app.view.MySelectDialog;
import com.example.dzxh_app.view.MyTwoEditDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class WifiActivity extends BaseActivity {
    private MyApplication myApplication;//全局变量
    //列表
    private final ArrayList<String> addressList=new ArrayList<>();
    private final ArrayList<String> portList=new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> listItems=new ArrayList<>();
    private SimpleAdapter simpleAdapter;
    //服务
    private static CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private Handler mDataHandler;
    private MyProcessDialog mProcessDialog;
    private final Handler mTimerHandler=new Handler();//连接超时监测

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        Toolbar wifi_tb =findViewById(R.id.wifi_tb);
        setSupportActionBar(wifi_tb);
        wifi_tb.setOverflowIcon(drawableIcon);
        wifi_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        NumInit();
        ViewInit();
        SetHandler();
        ServiceInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wifi, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_wifi_add) {
            ShowEditDialog();
        }else{
            ShowClearDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void NumInit(){
        myApplication=(MyApplication)this.getApplication();
    }

    private void ViewInit(){
        getData();
        for (int i=0;i<addressList.size();i++) {
            HashMap<String, Object> map=new HashMap<String, Object>();
            map.put("server_ip",addressList.get(i));
            map.put("server_port",portList.get(i));
            if((addressList.get(i)+portList.get(i)).equals(myApplication.getWifiDevice())){
                map.put("server_connect",R.drawable.ic_check_black_24dp);
            }else{
                map.put("server_connect",null);
            }
            listItems.add(map);
        }
        simpleAdapter = new SimpleAdapter(this, listItems,
                R.layout.item_wifi,
                new String[]{"server_ip","server_port","server_connect"},
                new int[]{R.id.item_wifi_ip_tv,R.id.item_wifi_port_tv,R.id.item_wifi_image});
        ListView wifi_device_lv = findViewById(R.id.wifi_device_lv);
        //列表配置
        wifi_device_lv.setAdapter(simpleAdapter);
        wifi_device_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShowInformDialog(position);
            }
        });
        wifi_device_lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ShowDeleteDialog(position);
                return true;
            }
        });
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
            mCommunicationIService.setHandler(mDataHandler);
            myApplication.setAutoWifiState(false); //设置自动连接标记位
            mCommunicationIService.callStopAutoConnectWifi();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    private void ListViewRefresh(){
        listItems.clear();
        for (int i=0;i<addressList.size();i++) {
            HashMap<String, Object> map=new HashMap<String, Object>();
            if((addressList.get(i)+portList.get(i)).equals(myApplication.getWifiDevice())){
                map.put("server_connect",R.drawable.ic_check_black_24dp);
            }else{
                map.put("server_connect",R.drawable.ic_null_24dp);
            }
            map.put("server_ip",addressList.get(i));
            map.put("server_port",portList.get(i));
            listItems.add(map);
        }
        simpleAdapter.notifyDataSetChanged();
        putData();
    }

    /**
     * 添加设备弹窗
     */
    private void ShowEditDialog(){
        MyTwoEditDialog myTwoEditDialog = new MyTwoEditDialog(this);
        myTwoEditDialog.setCancelable(true); //点击屏幕外能取消
        myTwoEditDialog.show();
        myTwoEditDialog.setTitle("添加服务端");
        myTwoEditDialog.setInformName(null,null);
        myTwoEditDialog.setHint("IP地址","端口号");
        myTwoEditDialog.setButtonText("取消","添加");
        myTwoEditDialog.setOnDialogClickListener(new MyTwoEditDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_edit_two_yes_bt){
                    String regEx = "[^0-9.]"; //只能输入0-9.
                    String edited=myTwoEditDialog.getInform1();
                    if(edited.isEmpty()) {
                        Toast.makeText(WifiActivity.this, "输入IP地址不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String editing = Pattern.compile(regEx).matcher(edited).replaceAll("");  //删掉不符合规范的字符
                    if(!editing.equals(edited)){
                        Toast.makeText(WifiActivity.this,"输入IP地址不符合规范",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    regEx = "[^0-9]"; //只能输入0-9a-fA-F和空格
                    edited=myTwoEditDialog.getInform2();
                    if(edited.isEmpty()) {
                        Toast.makeText(WifiActivity.this, "输入端口号不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    editing = Pattern.compile(regEx).matcher(edited).replaceAll("");
                    if(!editing.equals(edited)){
                        Toast.makeText(WifiActivity.this,"输入端口号不符合规范",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addressList.add(myTwoEditDialog.getInform1());
                    portList.add(myTwoEditDialog.getInform2());
                    ListViewRefresh();
                }
            }
        });
    }

    /**
     * 删除提示弹窗
     * @param num 删除项目序号
     */
    private void ShowDeleteDialog(int num){
        MySelectDialog mySelectDialog=new MySelectDialog(this);
        mySelectDialog.show();
        mySelectDialog.setButtonText("取消","删除");
        mySelectDialog.setButtonColor(0XFF6A5AFF, Color.RED);
        mySelectDialog.setText("是否要删除此项?");
        mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_select_yes_bt){
                    if((addressList.get(num)+portList.get(num)).equals(myApplication.getWifiDevice())){
                        mCommunicationIService.callDisConnectWifi();
                    }
                    addressList.remove(num);
                    portList.remove(num);
                    ListViewRefresh();
                }
            }
        });
    }

    /**
     * 清除列表提醒
     */
    private void ShowClearDialog(){
        MySelectDialog mySelectDialog=new MySelectDialog(this);
        mySelectDialog.show();
        mySelectDialog.setButtonText("取消","确定");
        mySelectDialog.setText("确定要清空列表?");
        mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_select_yes_bt){
                    addressList.clear();
                    portList.clear();
                    ListViewRefresh();
                    putData();
                }
            }
        });
    }

    /**
     * 连接弹窗
     * @param num 要连接设备序号
     */
    private void ShowInformDialog(int num){
        MyInformDialog myInformDialog=new MyInformDialog(this);
        myInformDialog.setCancelable(true); //点击屏幕外能取消
        myInformDialog.show();
        myInformDialog.setTitle("服务端信息");
        myInformDialog.setName("状态","IP地址","端口号");
        myInformDialog.setButtonText("取消","连接");
        if((addressList.get(num)+portList.get(num)).equals(myApplication.getWifiDevice())){
            myInformDialog.setInform1("已连接");
            myInformDialog.setButtonText("取消","断开连接");
        }else{
            myInformDialog.setInform1("未连接");
            myInformDialog.setButtonText("取消","连接");
        }
        myInformDialog.setInform1("未连接");
        myInformDialog.setInform2(addressList.get(num));
        myInformDialog.setInform3(portList.get(num));
        myInformDialog.setOnDialogClickListener(new MyInformDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_inform_dialog_yes){
                    if((addressList.get(num)+portList.get(num)).equals(myApplication.getWifiDevice())){
                            ShowProcessDialog("正在断开连接....");
                            mCommunicationIService.callDisConnectWifi();
                    }else{
                        if(myApplication.getWifiDevice()==null){
                            ShowProcessDialog("正在连接....");
                            mCommunicationIService.callConnectWifi(addressList.get(num),Integer.valueOf(portList.get(num)));
                        }else{
                            Toast.makeText(WifiActivity.this,"请先断开已连接设备",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    /**
     * 连接提示
     * @param string 提示语句
     */
    private void ShowProcessDialog(String string){
        mProcessDialog = new MyProcessDialog(this);
        mProcessDialog.setCancelable(true);//点击屏幕外可取消
        mProcessDialog.show();//显示
        mProcessDialog.setTitle(string);
        /*防止没有回调程序卡死*/
        mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mProcessDialog!=null){
                    if(mProcessDialog.isShowing()){
                        mProcessDialog.dismiss();
                    }
                    Toast.makeText(WifiActivity.this,"连接超时，请检查热点是否连接",Toast.LENGTH_SHORT).show();
                }
            }
        },5000);
    }

    /**
     * 接收服务数据回传
     */
    private void SetHandler(){
        mDataHandler = new Handler(new Handler.Callback() {
            @SuppressLint("ShowToast")
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                mTimerHandler.removeMessages(0);
                if(msg.obj!=null) {
                    Toast.makeText(WifiActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
                }
                ListViewRefresh();
                if(mProcessDialog!=null){
                    if(mProcessDialog.isShowing()){
                        mProcessDialog.dismiss();
                    }
                }
                return false;
            }
        });
    }

    /**
     * 存储数据
     */
    private void putData(){
        sp.putInt("Wifi_addressListSize",addressList.size());
        for (int i = 0; i < addressList.size(); i++) {
            sp.putString("Wifi_addressList"+i,addressList.get(i));
            sp.putString("Wifi_portList"+i,portList.get(i));
        }
    }

    /**
     * 获取数据
     */
    private void getData(){
        int len=sp.getInt("Wifi_addressListSize",0);
        for (int i = 0; i < len; i++) {
            addressList.add(sp.getString("Wifi_addressList"+i,""));
            portList.add(sp.getString("Wifi_portList"+i,""));
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(myServiceConnection);//解绑服务
        //继续自动连接
        myApplication.setAutoWifiState(sp.getBoolean("AutoWifiState",false));
        if(myApplication.getAutoWifiState()){
            mCommunicationIService.callStartAutoConnectWifi();
        }
        super.onDestroy();
    }
}
