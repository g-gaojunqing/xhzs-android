package com.example.dzxh_app.Activity;

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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.api.MusicIService;
import com.example.dzxh_app.api.MusicService;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.util.UriToPath;
import com.example.dzxh_app.view.MyInformDialog;
import com.example.dzxh_app.view.MySelectDialog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@SuppressLint("StaticFieldLeak")
public class SpeechActivity extends BaseActivity {
    //音乐播放栏
    private  static ToggleButton speech_stop_tb;
    private  TextView speech_audio_name_tv;
    private  static TextView speech_time_tv;
    private  static SeekBar speech_sb;
    //音乐播放
    private static String[] musics=new String[31]; //音频路径存储
    private int itemSelected;
    private static String musicPlaying =null;//正在播放的音频名称
    //音频列表
    private LinearLayout speech_menu_ll;
    private final ArrayList<HashMap<String, Object>> ListItems = new ArrayList<>();
    private ListView speech_audio_lv;
    private SimpleAdapter simpleAdapter;
    //文本接收
    private TextToSpeech textToSpeech;
    private  static TextView speech_rx_tv;
    private  static ScrollView speech_rx_sl;
    private String myCharsetName; //字符编码格式
    //音乐服务相关
    private MyMusicServiceConnection myMusicServiceConnection;
    private static MusicIService musicIService;  //中间人对象
    //数据服务相关
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        Toolbar speech_tb =findViewById(R.id.speech_tb);
        setSupportActionBar(speech_tb);
        speech_tb.setOverflowIcon(drawableIcon);
        speech_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        NumInit();
        ViewInit();
        ButtonInit();
        SpeechInit();
        ListViewInit();
        ServiceInit();
        getStoragePermission();
    }
    private void ServiceInit(){
        //音乐播放器
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        myMusicServiceConnection = new MyMusicServiceConnection();
        bindService(intent, myMusicServiceConnection, BIND_AUTO_CREATE);
        //通信服务
        Intent intent2 = new Intent(this, CommunicationService.class);
        startService(intent2);
        myServiceConnection = new MyServiceConnection();
        bindService(intent2, myServiceConnection, BIND_AUTO_CREATE);
        BroadcastReceiverInit();
    }
    private void BroadcastReceiverInit() {
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter(
                "com.example.dzxh_app.content");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, filter,RECEIVER_EXPORTED); //绑定广播
        }else{
            registerReceiver(myBroadcastReceiver, filter); //绑定广播
        }
    }
    private void NumInit(){
        MyApplication myApplication = (MyApplication) Objects.requireNonNull(this).getApplication();
        myCharsetName=myApplication.getCharsetName();
        getData();
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
            intent=new Intent(SpeechActivity.this,BluetoothActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_more_wifi){
            intent=new Intent(SpeechActivity.this,WifiActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_more_help){
            intent=new Intent(SpeechActivity.this, UserActivity.class);
            intent.putExtra("page",10);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    private void ViewInit(){
        speech_audio_lv = findViewById(R.id.speech_audio_lv);
        speech_rx_sl = findViewById(R.id.speech_rx_sl);
        speech_rx_tv = findViewById(R.id.speech_rx_tv);
        speech_audio_name_tv = findViewById(R.id.speech_audio_name_tv);
        speech_audio_name_tv.setText(musicPlaying);
        speech_time_tv = findViewById(R.id.speech_time_tv);
        speech_menu_ll = findViewById(R.id.speech_menu_ll);
        speech_menu_ll.setVisibility(View.GONE);
    }

    private void ButtonInit(){
        //清除文本按钮
        ImageButton speech_clear_ib = findViewById(R.id.speech_clear_ib);
        speech_clear_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speech_rx_tv.setText(null);
            }
        });
        //暂停按钮
        speech_stop_tb = findViewById(R.id.speech_stop_tb);
        speech_stop_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    musicIService.callRePlayMusic();
                }else{
                    musicIService.callPauseMusic();
                }
            }
        });
        //菜单显示按钮
        ToggleButton speech_menu_tb = findViewById(R.id.speech_menu_tb);
        speech_menu_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    speech_menu_ll.setVisibility(View.VISIBLE);
                }else{
                    speech_menu_ll.setVisibility(View.GONE);
                }
            }
        });
        //音乐播放进度条
        speech_sb = findViewById(R.id.speech_sb);
        speech_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicIService.callSeekTo(seekBar.getProgress());
            }
        });
        //添加文件按钮
        ImageButton speech_add_file_ib = findViewById(R.id.speech_add_file_ib);
        speech_add_file_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ListItems.size()<30){
                    OpenFile();
                    itemSelected =ListItems.size();
                }else{
                    Toast.makeText(SpeechActivity.this,"列表已满",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //清空文件按钮
        Button speech_clear_menu_bt = findViewById(R.id.speech_clear_menu_bt);
        speech_clear_menu_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowSelectDialog(1);
            }
        });
    }
    /**
     * 语音播放初始化
     */
    private void SpeechInit(){
        textToSpeech=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("SpeechActivity","status:"+status);

                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setPitch(1.0f);//方法用来控制音调
                    textToSpeech.setSpeechRate(1.0f);//用来控制语速
                } else {
                    Toast.makeText(SpeechActivity.this, "数据丢失或不支持语音播报", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 开始播报
     */
    private void StartSpeech(String data) {
        textToSpeech.speak(data,//输入中文，若不支持的设备则不会读出来
                TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * 列表初始化
     */
    private void ListViewInit(){
        ListItems.clear();
        for (int i=0;i<30;i++) {
            if(musics[i]!=null){
                HashMap<String, Object> map=new HashMap<String, Object>();
                File mFile = new File(musics[i]);
                map.put("item_name",""+(i+1));
                map.put("audio_name",mFile.getName());
                if(i<15){
                    map.put("item_byte","0x0"+Integer.toHexString(i+1));
                }else{
                    map.put("item_byte","0x"+Integer.toHexString(i+1));
                }
                ListItems.add(map);
            }else{
                break;
            }
        }
        simpleAdapter = new SimpleAdapter(this,ListItems,
                R.layout.item_speech,
                new String[]{"item_name","audio_name","item_byte"},
                new int[]{R.id.item_speech_tv1,R.id.item_speech_tv2,R.id.item_speech_tv3});
        //列表配置
        speech_audio_lv.setAdapter(simpleAdapter);

        speech_audio_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemSelected =position;
                ShowSpeechDialog();
            }
        });
        speech_audio_lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemSelected =position;
                ShowSelectDialog(2);
                return true;
            }
        });
    }

    /**
     * 列表更新
     */
    private void ListViewRefresh(){
        ListItems.clear();
        for (int i=0;i<30;i++) {
            if(musics[i]!=null){
                HashMap<String, Object> map=new HashMap<String, Object>();
                File mFile = new File(musics[i]);
                map.put("item_name",""+(i+1));
                if(i<15){
                    map.put("item_byte","0x0"+Integer.toHexString(i+1));
                }else{
                    map.put("item_byte","0x"+Integer.toHexString(i+1));
                }
                map.put("audio_name",mFile.getName());
                ListItems.add(map);
            }else{
                break;
            }
        }
        simpleAdapter.notifyDataSetChanged();
        putData();
    }

    /**
     * 播放音乐
     * @param num 播放序列
     */
    private void PlayMusic(int num){
        File file=new File(musics[num]);
        musicIService.callPlayMusic(musics[num]);
        speech_stop_tb.setChecked(true);
        speech_audio_name_tv.setText(file.getName());
        musicPlaying =file.getName();
        TextViewRefresh("[系统]正在播放音频"+(num+1)+"\r\n");
    }

    /**
     * 接收框刷新
     * @param s 文本
     */
    private static void TextViewRefresh(String s){
        speech_rx_tv.append(s);
        speech_rx_sl.fullScroll(ScrollView.FOCUS_DOWN);
    }

    /**
     * 显示音频信息
     */
    private void ShowSpeechDialog(){
        MyInformDialog MyInformDialog = new MyInformDialog(this);
        MyInformDialog.setCancelable(true); //点击屏幕外能取消
        MyInformDialog.show();
        File file=new File(musics[itemSelected]);
        MyInformDialog.setTitle("音频"+(itemSelected +1));
        MyInformDialog.setName("文件","时长","触发字符");
        MyInformDialog.setButtonText("更换文件","试听");
        MyInformDialog.setInform1(file.getName());
        MyInformDialog.setInform2(musicIService.callGetDuration(musics[itemSelected]));
        if(itemSelected <15){
            MyInformDialog.setInform3("0x0"+Integer.toHexString(itemSelected +1));//小于F时，
        }else{
            MyInformDialog.setInform3("0x"+Integer.toHexString(itemSelected +1));
        }
        MyInformDialog.setOnDialogClickListener(new MyInformDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_inform_dialog_no) {
                    OpenFile();
                    ListViewRefresh();
                }else if(view.getId()==R.id.layout_inform_dialog_yes){
                    if (file.exists()) {
                        PlayMusic(itemSelected);
                    } else {
                        Toast.makeText(SpeechActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * 提示框
     * @param mode 提示内容
     */
    private void ShowSelectDialog(int mode){
        MySelectDialog mySelectDialog = new MySelectDialog(this);
        mySelectDialog.setCancelable(true); //点击屏幕外能取消
        mySelectDialog.show();
        if(mode==1){
            mySelectDialog.setText("确定要清空列表吗?");
            mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
                @Override
                public void OnClick(View view) {
                    if(view.getId()==R.id.layout_dialog_select_yes_bt){
                        musics =new String[31];
                        ListViewRefresh();
                    }
                }
            });
        }else if(mode==2){
            mySelectDialog.setText("是否要删除此项?");
            mySelectDialog.setButtonText("取消","删除");
            mySelectDialog.setButtonColor(0XFF6A5AFF,Color.RED);
            mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
                @Override
                public void OnClick(View view) {
                    if(view.getId()==R.id.layout_dialog_select_yes_bt){
                        for (int i = itemSelected; i <30; i++) {
                            musics[i]= musics[i+1];
                            if(musics[i]==null){
                                break;
                            }
                        }
                        ListViewRefresh();
                    }
                }
            });
        }

    }

    public static Handler music_handler=new Handler(){

        public void handleMessage(android.os.Message msg) {
            //获取我们携带的数据
            Bundle data = msg.getData();
            //获取歌曲的总时长和当前进度
            int duration = data.getInt("duration");
            int currentPosition = data.getInt("currentPosition");
            String string_dur =data.getString("string_dur");
            //设置seekbar的进度
            speech_sb.setMax(duration);
            speech_sb.setProgress(currentPosition);
            speech_time_tv.setText(string_dur);
            if((currentPosition/200)>=(duration/200)){
                TextViewRefresh("[系统]播放结束\r\n");
                speech_stop_tb.setChecked(false);
                musicIService.callSeekTo(0);
            }
        }
    };

    /**
     * 打卡txt文件
     */
    private void OpenFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        File file = new File(Environment.getExternalStorageDirectory()+"/学会助手");
        if(file.exists()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:" + "%2f学会助手%2f");
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            }
        }
        startActivityForResult(intent,1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK) {
                assert data != null;
                Uri uri = data.getData();//得到uri，将uri转化成路径
                Log.d("onActivityResult",uri+"--------------------");
                String img_path=UriToPath.getFilePathByUri(this,uri);
                Log.d("onActivityResult",img_path+"--------------------");
                musics[itemSelected]=img_path;
                ListViewRefresh();
                if((itemSelected +=1)==ListItems.size()){
                    speech_audio_lv.setSelection(ListItems.size()-1);//将添加滑到底部
                }
            }
        }
    }
    /**
     * 监听音乐服务状态
     */
    private static class MyMusicServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
             musicIService = (MusicIService) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    /**
     * 通信服务
     */
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCommunicationIService = (CommunicationIService) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    private byte[] bytes_rx=new byte[102];
    private int rx_num;
    private void DealBytes(){
        byte[] bytes=new byte[rx_num-2];
        System.arraycopy(bytes_rx,0,bytes,0,rx_num-2);
        if(bytes.length==1&&bytes[0]<=30){
            if(bytes[0]>0&&bytes[0]<=(ListItems.size())){
                PlayMusic(bytes[0]-1);
            }else {
                Toast.makeText(this,"没有找到文件",Toast.LENGTH_SHORT).show();
            }
        }else if(bytes.length>0){
            String string = null;
            try {
                string = new String(bytes,myCharsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            TextViewRefresh(string+"\r\n");
            StartSpeech(string);
        }
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
                bytes_rx[rx_num] = aByte;
                rx_num++;
                if (rx_num>=2) {
                    if (bytes_rx[rx_num - 1] == 0x0A && bytes_rx[rx_num - 2] == 0x0D) { //接收到0x0D0x0A
                        DealBytes();
                        rx_num = 0;
                        bytes_rx = new byte[102];
                    }
                }
                if (rx_num >= 102) {
                    rx_num = 0;
                    bytes_rx = new byte[102];
                }
            }
        }
    }
    private void putData(){
        for (int i = 0; i < 30; i++) {
            sp.putString("Speech_musics"+i, musics[i]);
            if(musics[i]==null){
                break;
            }
        }
    }
    private void getData(){
        for (int i = 0; i < 30; i++) {
            musics[i]=sp.getString("Speech_musics"+i,null);
            if(musics[i]==null){
                break;
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        putData();
        musicIService.callPauseMusic(); //停止播放
        textToSpeech.stop(); // 不管是否正在朗读TTS都被打断
        textToSpeech.shutdown(); // 关闭，释放资源
        unbindService(myServiceConnection);
        unbindService(myMusicServiceConnection);
        unregisterReceiver(myBroadcastReceiver);
    }
}
