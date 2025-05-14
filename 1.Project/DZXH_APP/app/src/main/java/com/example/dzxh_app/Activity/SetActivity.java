package com.example.dzxh_app.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.InputQueue;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.view.MyProcessDialog;
import com.example.dzxh_app.view.MySelectDialog;

import org.xmlpull.v1.XmlSerializer;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SetActivity extends BaseActivity {

    private MyApplication myApplication;
    private Switch set_auto_sw;
    private Switch set_name_sw;
    private Switch set_auto_wifi_sw;
    private RadioButton set_charset_gbk_rb;
    private RadioButton set_charset_utf8_rb;

    private String updateUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        Toolbar stop_watch_back_tb =findViewById(R.id.set_tb);
        stop_watch_back_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        NumInit();
        ViewInit();
        ButtonInit();
        ListViewInit1();

        HandlerInit();
        GetData();
    }

    private void NumInit(){
        myApplication=(MyApplication) this.getApplication();
    }

    private void ViewInit(){
        //获取手机分辨率

    }

    private void ButtonInit(){
        set_auto_sw = findViewById(R.id.set_auto_sw);
        set_auto_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myApplication.setAutoBluetoothState(isChecked);
                PutData();
            }
        });

        set_name_sw = findViewById(R.id.set_name_sw);
        set_name_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myApplication.setNameFilterState(isChecked);
                PutData();
            }
        });

        set_auto_wifi_sw = findViewById(R.id.set_auto_wifi_sw);
        set_auto_wifi_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myApplication.setAutoWifiState(isChecked);
                PutData();
            }
        });

        //字符编码格式设置
        RadioGroup set_charset_rg = findViewById(R.id.set_charset_rg);
        set_charset_gbk_rb = findViewById(R.id.set_charset_gbk_rb);
        set_charset_utf8_rb = findViewById(R.id.set_charset_utf8_rb);
        set_charset_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.set_charset_gbk_rb){
                    myApplication.setCharsetName("GBK");
                }else if(checkedId==R.id.set_charset_utf8_rb){
                    myApplication.setCharsetName("UTF8");
                }
                PutData();
            }
        });

    }

    private void ListViewInit1(){
        ArrayList<HashMap<String, Object>> SetListItem = new ArrayList<HashMap<String, Object>>();
        for(int i=0;i<2;i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            if(i==0) {
                map.put("set_name", "关于");
            }else{
                map.put("set_name","检查更新" );
            }
            SetListItem.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, SetListItem,
                R.layout.item_set,
                new String[]{"set_name"},
                new int[]{R.id.item_set_name});
        ListView set_lv1 = findViewById(R.id.set_lv1);

        set_lv1.setAdapter(simpleAdapter); //列表配置
        setListViewHeightBasedOnChildren(set_lv1);//重置ListView大小
        //点击事件
        set_lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=null;
                if(position==0){
                    intent = new Intent(SetActivity.this, AboutActivity.class);
                    startActivity(intent);
                }else {
                    ShowProcessDialog("检查中...");
                    getWebDta();
                }
            }
        });
    }

    private MyProcessDialog mProcessDialog;
    private void ShowProcessDialog(String string){
        mProcessDialog = new MyProcessDialog(this);
        mProcessDialog.setCancelable(true);//点击屏幕外可取消
        mProcessDialog.show();//显示
        mProcessDialog.setTitle(string);
    }

    private int[] getVersion(String string){
        int[] version=new int[3];
        string=string.replace('.',',');
        String[] strings= string.split(",");
        if(strings.length!=3) {
            Log.d("SetActivity","数据量异常-------------------");
            return null;
        }
        for (int i = 0; i < 3; i++) {
            try {
                version[i]=Integer.parseInt(strings[i]);
            }catch (NumberFormatException e){
                Log.d("SetActivity","数字异常-------------------");
                return null;
            }catch (NullPointerException e){
                Log.d("SetActivity","没有检测到数-------------------");
                return null;
            }
        }
        return version;
    }

    /**
     * 退出提示
     */
    private void ShowSelectDialog(){
        MySelectDialog mySelectDialog=new MySelectDialog(this);
        mySelectDialog.show();
        mySelectDialog.setButtonText("取消","查看");
        mySelectDialog.setText("发现新版本！");
        mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_select_yes_bt){
                    Intent intent = new Intent();
                    intent.setData(Uri.parse(updateUrl));//Url 要打开的网址
                    intent.setAction(Intent.ACTION_VIEW);
                    startActivity(intent);
                }
            }
        });
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
                    Toast.makeText(SetActivity.this, "检查失败Error"+msg.what, Toast.LENGTH_SHORT).show();//连接成功
                    if(mProcessDialog!=null){
                        if(mProcessDialog.isShowing()){
                            mProcessDialog.dismiss();
                        }
                    }
                    return;
                }

                String string=msg.obj.toString();
                if(getVersion(string)==null){
                    Toast.makeText(SetActivity.this, "检查失败getVersion", Toast.LENGTH_SHORT).show();//连接成功
                    if(mProcessDialog!=null){
                        if(mProcessDialog.isShowing()){
                            mProcessDialog.dismiss();
                        }
                    }
                    return;
                }
                boolean newAPP=false;
                int[] now_version={1,4,1};
                int[] new_version=getVersion(string);
                Log.d("SetActivity",new_version[0]+"."+new_version[1]+"."+new_version[2]);
                if((new_version[0]>now_version[0])||(new_version[1]>now_version[1])||(new_version[2]>now_version[2])){
                    newAPP=true;
                }
                //关掉提示
                if(mProcessDialog!=null){
                    if(mProcessDialog.isShowing()){
                        mProcessDialog.dismiss();
                    }
                }
                if(newAPP){
                    ShowSelectDialog();
                }else{
                    Toast.makeText(SetActivity.this, "没有发现新版本", Toast.LENGTH_SHORT).show();//连接成功
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

    private void getWebDta(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url=new URL("https://www.bilibili.com/video/BV1sG4y167cn\n");
                    HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(5000);
                    int code=httpURLConnection.getResponseCode();
                    Log.d("SetActivity","code:"+code);
                    if(code==200){
                        Log.d("SetActivity","获取成功");
                        InputStream inputStream=httpURLConnection.getInputStream();
                        byte[] bytes=new byte[1024];
                        inputStream.read(bytes);
                        httpURLConnection.disconnect();
                        String string=new String(bytes);
                        Log.d("SetActivity",string);
                        //提取版本信息
                        int index=string.indexOf("学会助手v");
                        if(index==-1){
                            handlerSendMessage(2,null);
                            return;
                        }
                        String version_str=string.substring(index);
                        int s=version_str.indexOf("v");
                        int e=version_str.indexOf("》");
                        if((s==-1)||(e==-1)){
                            handlerSendMessage(3,null);
                            return;
                        }
                        version_str=version_str.substring(s+1,e);

                        Log.d("SetActivity",version_str);
                        //提取更新视频地址
                        index=string.indexOf("版本更新：");
                        if(index==-1){
                            handlerSendMessage(4,null);
                            return;
                        }
                        String update_str= string.substring(index);
                        s=update_str.indexOf("：");
                        e=update_str.indexOf("，");
                        if((s==-1)||(e==-1)){
                            handlerSendMessage(5,null);
                            return;
                        }
                        updateUrl=update_str.substring(s+1,e);
                        Log.d("SetActivity","updateUrl:"+updateUrl);
                        handlerSendMessage(0,version_str);
                    }else{
                        handlerSendMessage(6,null);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("SetActivity","检查失败");
                    handlerSendMessage(1,null);
                }
            }
        }).start();
    }


    private void PutData(){
        sp.putBoolean("AutoBluetoothState",myApplication.getAutoBluetoothState());
        sp.putBoolean("NameFilterState",myApplication.getNameFilterState());
        sp.putBoolean("AutoWifiState",myApplication.getAutoWifiState());
        sp.putString("CharsetName",myApplication.getCharsetName());
    }

    private void GetData(){
        myApplication.setAutoBluetoothState(sp.getBoolean("AutoBluetoothState",false));
        myApplication.setNameFilterState(sp.getBoolean("NameFilterState",false));
        myApplication.setAutoWifiState(sp.getBoolean("AutoWifiState",false));
        myApplication.setCharsetName(sp.getString("CharsetName","GBK"));

        set_auto_sw.setChecked(myApplication.getAutoBluetoothState());
        set_name_sw.setChecked(myApplication.getNameFilterState());
        set_auto_wifi_sw.setChecked(myApplication.getAutoWifiState());
        if(myApplication.getCharsetName().equals("GBK"))
        {
            set_charset_gbk_rb.setChecked(true);
        }else if(myApplication.getCharsetName().equals("UTF8"))
        {
            set_charset_utf8_rb.setChecked(true);
        }

    }
}
