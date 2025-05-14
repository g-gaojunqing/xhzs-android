package com.example.dzxh_app.ui.main_frament;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dzxh_app.Activity.MainActivity;
import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.util.MyApplication;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

@SuppressLint("SetTextI18n")
public class CommunicateFragment extends Fragment {

    private MainActivity mainActivity;
    private MyApplication myApplication; //全局变量
    //控件
    private TextView communicate_rx_tv;
    private EditText communicate_tx_et;
    private TextView communicate_rx_num_tv;
    private TextView communicate_tx_num_tv;
    private ScrollView communicate_rx_sv;
    private ToggleButton communicate_rx_char_tb;
    private ToggleButton communicate_tx_char_tb;
    private ToggleButton communicate_rx_start_tb;
    //数据
    private int communicateRxNum;//接收数量
    private int communicateTxNum;//发送数量
    private String charString ="";//字符格式显示数据记录
    private String hexString ="";//十六进制显示数据记录
    private boolean rxHexFlag;//接收十六进制标志 true：十六进制接收按钮打开
    private boolean txHexFlag;//发送十六进制标志 true：十六进制发送按钮打开
    private String myCharsetName; //字符编码格式

    private boolean refreshFlag;//刷新标志
    private Timer timer;//定时器，用于定时刷新接收的数据
    private int hexLength =0;//记录已经刷新的字符数量

    //数据收发服务与广播
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private MyBroadcastReceiver myBroadcastReceiver; //数据接收广播
    private IntentFilter filter; //广播接收标签

    private Toast mToast;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_communicate, container, false);
        onCreate(savedInstanceState);
        NumInit();
        ViewInit(root);
        ButtonInit(root);
        ServiceInit();
        TimerInit();
        GetData();
        return root;
    }
    /**
     * 初始化数据
     */
    private void NumInit(){
        mainActivity = (MainActivity)getActivity();
        myApplication = (MyApplication) requireActivity().getApplication();
        myCharsetName=myApplication.getCharsetName();
    }
    /**
     * 初始化服务
     */
    private void ServiceInit(){
        Intent intent = new Intent(getActivity(), CommunicationService.class);
        requireActivity().startService(intent);
        myServiceConnection = new MyServiceConnection();
        getActivity().bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
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
     * 广播接收
     */
    private void BroadcastReceiverInit() {
        myBroadcastReceiver=new MyBroadcastReceiver();
        filter = new IntentFilter(
                "com.example.dzxh_app.content");
    }

    /**
     * 功能：初始化文本
     * 输入：root
     */
    private void ViewInit(View root){
        //接收区
        communicate_rx_tv = root.findViewById(R.id.communicate_rx_tv);
        communicate_rx_sv = root.findViewById(R.id.communicate_rx_sv);
        //字符串模式 | 十六进制模式
        if(!rxHexFlag){
            communicate_rx_tv.setText(charString);
        }else{
            communicate_rx_tv.setText(hexString);
            hexLength = hexString.length();
        }
        //接收数量
        communicate_rx_num_tv = root.findViewById(R.id.communicate_rx_num_tv);
        communicate_rx_num_tv.setText("RX:"+ communicateRxNum);
        //发送数量
        communicate_tx_num_tv = root.findViewById(R.id.communicate_tx_num_tv);
        communicate_tx_num_tv.setText("TX:"+ communicateTxNum);
    }

    /**
     * 功能：初始化按钮
     * 输入： root
     */
    private void ButtonInit(View root){
        //输入框
        communicate_tx_et=root.findViewById(R.id.communicate_tx_et);
        communicate_tx_et.addTextChangedListener(new TextWatcher() {
            String before_string=null;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before_string=s.toString();//记录新内容输入之前的内容
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("Communicate",s.toString()+",start:"+start+"before:"+before+"count:"+count);
                if(txHexFlag){
                    String edited = communicate_tx_et.getText().toString();
                    String regEx = "[^0-9 a-fA-F]"; //只能输入0-9a-fA-F和空格
                    String editing = Pattern.compile(regEx).matcher(edited).replaceAll("");  //删掉不符合规范的字符
                    if(!editing.equals(edited)){
                        Toast.makeText(getActivity(),"请输入正确格式0-9,a-f,A-F",Toast.LENGTH_SHORT).show();
                        communicate_tx_et.setText(before_string); //设置更改前EditText的字符
                        communicate_tx_et.setSelection(start); //重写设置新的光标所在位置
                    }
                }else if(before==0&&count==1){
                    if(s.toString().charAt(start)=='\n'){
                        StringBuilder stringBuilder=new StringBuilder(s.toString());
                        stringBuilder.insert(start,'\r');
                        communicate_tx_et.setText(stringBuilder.toString());
                        communicate_tx_et.setSelection(start+2); //重写设置新的光标所在位置
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        //发送按钮
        Button communicate_tx_bt = root.findViewById(R.id.communicate_tx_bt);
        communicate_tx_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes;
                if((myApplication.getWifiDevice()!=null)||(myApplication.getBluetoothDevice()!=null)){
                    if(txHexFlag){
                        bytes=HexStringToBytes(communicate_tx_et.getText().toString());//转为字符串发送
                    }else{
                        bytes=StringToBytes(communicate_tx_et.getText().toString());
                    }
                    mCommunicationIService.callWrite(bytes);
                    communicateTxNum +=bytes.length;
                    communicate_tx_num_tv.setText("TX:"+ communicateTxNum);
                }else{
                    if(mToast!=null){
                        mToast.cancel();
                    }
                    mToast=Toast.makeText(getActivity(),"设备未连接",Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }
        });
        //接收按钮
        communicate_rx_start_tb = root.findViewById(R.id.communicate_rx_start_tb);
        communicate_rx_start_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requireActivity().registerReceiver(myBroadcastReceiver, filter,Context.RECEIVER_EXPORTED);//绑定接收
                    }else{
                        requireActivity().registerReceiver(myBroadcastReceiver, filter);//绑定接收
                    }
                    communicate_rx_start_tb.setTextColor(Color.RED);
                }else{
                    requireActivity().unregisterReceiver(myBroadcastReceiver);//解绑接收
                    communicate_rx_start_tb.setTextColor(Color.BLACK);
                }
            }
        });
        //清除接收
        Button communicate_clear_rx_bt = root.findViewById(R.id.communicate_clear_rx_bt);
        communicate_clear_rx_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communicateRxNum =0;
                communicate_rx_num_tv.setText("RX:"+ communicateRxNum);
                communicate_rx_tv.setText(null);
                charString ="";
                hexString ="";
                hexLength =0;
            }
        });
        //清除发送
        Button communicate_clear_tx_bt = root.findViewById(R.id.communicate_clear_tx_bt);
        communicate_clear_tx_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communicateTxNum =0;
                communicate_tx_num_tv.setText("TX:"+ communicateTxNum);
                communicate_tx_et.setText(null);
            }
        });
        //接收进制
        communicate_rx_char_tb = root.findViewById(R.id.communicate_rx_character_tb);
        communicate_rx_char_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){ //字符串模式
                    rxHexFlag =true;
                    communicate_rx_tv.setText(hexString);
                    hexLength = hexString.length();
                }else{
                    rxHexFlag =false;
                    communicate_rx_tv.setText(charString);
                }
                communicate_rx_sv.fullScroll(ScrollView.FOCUS_UP);//滚动条滚动到最上面
                PutData();//保存设置数据
            }
        });
        //发送进制
        communicate_tx_char_tb = root.findViewById(R.id.communicate_tx_char_tb);
        communicate_tx_char_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    txHexFlag =true;
                    String string=communicate_tx_et.getText().toString();
                    byte[] bytes=StringToBytes(string);
                    communicate_tx_et.setText(BytesToHexString(bytes));
                }else{
                    txHexFlag =false;
                    String string=communicate_tx_et.getText().toString();
                    byte[] bytes=HexStringToBytes(string);
                    communicate_tx_et.setText(BytesToString(bytes));
                }
                communicate_tx_et.setSelection(communicate_tx_et.getText().toString().length());    //游标放到最后
                PutData();//保存设置数据
            }
        });
        //保存接收
        Button communicate_save_bt = root.findViewById(R.id.communicate_save_bt);
        communicate_save_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveFile();
            }
        });
    }

    /**
     * 将byte数组转为十六进制字符串
     * @param bytes byte数组
     * @return  十六进制字符串
     */
    String stringHex ="0123456789ABCDEF";
    private String BytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            stringBuilder.append(stringHex.charAt((aByte & 0xf0) >> 4));
            stringBuilder.append(stringHex.charAt((aByte & 0x0f))).append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * 十六进制数转为byte数组
     * @param s 十六进制字符串
     * @return  byte数组
     */
    private byte[] HexStringToBytes(String s) {
        s=s.replaceAll(" ","");
        int len=s.length()/2;
        byte[] bytes=new byte[len];
        StringBuilder stringBuilder;
        for(int i=0;i<len;i++){
            stringBuilder = new StringBuilder();
            stringBuilder.append(s.charAt(2*i));
            stringBuilder.append(s.charAt(2*i+1));
            bytes[i]=(byte)Integer.parseInt(stringBuilder.toString(),16);//将字符串作为16进制解析
        }
        return bytes;
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

    /**
     * 字符串进制数转为byte数组
     * @param s 字符串
     * @return  byte数组
     */
    private byte[] StringToBytes(String s) {
        byte[] bytes=new byte[]{};
        try {
            bytes=s.getBytes(myCharsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bytes;
    }
    /**
     * 名称：定时器初始化
     * 功能：100ms刷新一次数据，避免刷新频率过高发生卡顿
     */
    private void TimerInit(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(refreshFlag){ //有新数据
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int string_length;
                            string_length= hexString.length();
                            String hex_substring= hexString.substring(hexLength,string_length);//直接更新字符串汉字错误率较高，使用十六进制数据刷新字符串
                            byte[] bytes=HexStringToBytes(hex_substring);
                            String char_substring=BytesToString(bytes);
                            charString +=char_substring;//更新字符串
                            if(!rxHexFlag){
                                communicate_rx_tv.append(char_substring);//添加到文本框
                            }else{
                                communicate_rx_tv.append(hex_substring);//添加到文本框
                            }
                            hexLength =string_length;//保存本次更新长度
                            communicate_rx_num_tv.setText("RX:"+ communicateRxNum);
                            communicate_rx_sv.fullScroll(ScrollView.FOCUS_DOWN);//滚动条滚动到最下面
                            refreshFlag =false;
                        }
                    });
                }
            }
        },100,100);
    }

    /**
     *广播接收到数据
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes=intent.getByteArrayExtra("value");
            assert bytes != null;
            //Char_string+=BytesToString(bytes); //直接这里刷新字符串汉字容易出现解析错误，在刷新界面时再更新
            hexString +=BytesToHexString(bytes);
            communicateRxNum +=bytes.length; //接收数量
            refreshFlag =true; //设置刷新标志位
        }
    }
    /**
     * 保存数据到txt文件
     */
    private void SaveFile(){
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        }
        assert intent != null;
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE,"串口.txt");
        startActivityForResult(intent,1);
    }

    /**
     * 保存文件后的回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                try {
                    Uri uri=data.getData();
                    OutputStream outputStream = requireActivity().getContentResolver().openOutputStream(uri);
                    outputStream.write(communicate_rx_tv.getText().toString().getBytes());
                    outputStream.close();
                    Toast.makeText(getActivity(),"保存成功",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),"保存失败",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getActivity(),"取消保存",Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 保存设置数据
     */
    private void PutData(){
        mainActivity.sp.putBoolean("communicate_rx_char_tb",communicate_rx_char_tb.isChecked());//存储接收进制
        mainActivity.sp.putBoolean("communicate_tx_char_tb",communicate_tx_char_tb.isChecked());//存储发送进制
    }
    /**
     * 读出设置数据
     */
    private void GetData(){
        communicate_rx_char_tb.setChecked(mainActivity.sp.getBoolean("communicate_rx_char_tb",false));//读取接收进制
        communicate_tx_char_tb.setChecked(mainActivity.sp.getBoolean("communicate_tx_char_tb",false));//读取发送进制
    }
    @Override
    public void onDestroy() {
        requireActivity().unbindService(myServiceConnection);
        //正在接收数据
        if(communicate_rx_start_tb.isChecked()){
            getActivity().unregisterReceiver(myBroadcastReceiver); //注销广播接收
        }
        timer.cancel();//结束定时器
        PutData();//保存设置数据
        super.onDestroy();
    }
}

