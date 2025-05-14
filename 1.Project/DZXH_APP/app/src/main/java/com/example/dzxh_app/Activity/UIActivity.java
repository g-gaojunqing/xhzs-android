package com.example.dzxh_app.Activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.core.content.ContextCompat;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.util.MyApplication;
import com.example.dzxh_app.view.JoyStickView;
import com.example.dzxh_app.view.LineChartView;
import com.example.dzxh_app.view.MySeekBar;
import com.example.dzxh_app.view.MySelectDialog;
import com.example.dzxh_app.view.MySwitch;
import com.example.dzxh_app.view.MyUIDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Objects;
@SuppressLint("SetTextI18n")
public class UIActivity extends BaseActivity  {

    //窗口
    private ConstraintLayout mConstraintLayout =null;
    private ConstraintLayout ui_back_cl =null;
    private TextView ui_tv;
    private int screenWidth,screenHeight;
    private int windowWidth,windowHeight;
    private float windowWidthScale=1,windowHeightScale=1,windowScale=1;
    private float windowWidthOffset,windowHeightOffset;
    private static boolean fillFlag;

    private String myCharsetName; //字符编码格式
    //服务相关
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private MyBroadcastReceiver myBroadcastReceiver;
    private ImageView imageView;
    private static int windowColor=Color.WHITE;
    private static Bitmap bitmap;
    private static String wallpaperPath;
    private static boolean screenOrientation=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui);
        Log.d("UIActivity","onCreate------------------------");
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //全屏显示
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(layoutParams);

        //三个点
        Toolbar ui_tb = findViewById(R.id.ui_tb);
        setSupportActionBar(ui_tb);
        //三个点颜色
        Drawable drawable= ContextCompat.getDrawable(getApplicationContext(), R.drawable.abc_ic_menu_overflow_material);
        drawable.setColorFilter(new LightingColorFilter(Color.GRAY,0x000000));
        ui_tb.setOverflowIcon(drawable);

        GetData();
        NumInit();
        ViewInit();
        ServiceInit();
        getStoragePermission();

    }

    private Handler handler =new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("UIActivity","onWindowFocusChanged------------------------");
        //设置全屏显示
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        //获取屏幕分辨率
        mConstraintLayout.post(new Runnable() {
            @Override
            public void run() {
                screenWidth = mConstraintLayout.getWidth();
                screenHeight = mConstraintLayout.getHeight();
                Log.d("UIActivity","screenHeight"+screenHeight);
                if(windowWidth==0){
                    ui_tv.setText(screenWidth +"x"+ screenHeight);
                    Log.d("UIActivity","windowWidth==0------------------------");
                    setBackgroundView(0,0, screenWidth, screenHeight);
                }else{
                    ui_tv.setText(windowWidth +"x"+ windowHeight);
                    Log.d("UIActivity","windowWidth!=0------------------------");
                    setBackgroundView(0,0, windowWidth, windowHeight);
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ui, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.menu_ui_blue) {
            intent=new Intent(UIActivity.this,BluetoothActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_ui_wifi){
            intent=new Intent(UIActivity.this,WifiActivity.class);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_ui_help){
            intent=new Intent(UIActivity.this, UserActivity.class);
            intent.putExtra("page",4);
            startActivity(intent);
        }else if(item.getItemId() == R.id.menu_ui_background){
            ShowSetDialog();

        }
        return super.onOptionsItemSelected(item);
    }
    private void NumInit(){
        MyApplication myApplication = (MyApplication) Objects.requireNonNull(this).getApplication();
        myCharsetName=myApplication.getCharsetName();
    }

    private void ViewInit(){
        mConstraintLayout = findViewById(R.id.ui_cl);
        ui_back_cl = findViewById(R.id.ui_back_cl);
        ui_tv = findViewById(R.id.ui_tv);
    }

    private void ServiceInit(){
        Intent intent = new Intent(this, CommunicationService.class);
        startService(intent);
        myServiceConnection = new MyServiceConnection();
        bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
        doRegisterReceiver();
    }

    private void doRegisterReceiver() {
        myBroadcastReceiver= new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter(
                "com.example.dzxh_app.content");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, filter,RECEIVER_EXPORTED); //注册广播开始接收
        }else{
            registerReceiver(myBroadcastReceiver, filter); //注册广播开始接收
        }
    }

    private ConstraintLayout.LayoutParams getLayoutParams(int x,int y,int w,int h){
        ConstraintLayout.LayoutParams layoutParam;

        if(fillFlag){
            Log.d("UIActivity","getLayoutParams fillFlag------------------------true");
            layoutParam = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.FILL_PARENT, ConstraintLayout.LayoutParams.FILL_PARENT);
            layoutParam.setMargins((int)(x*windowWidthScale),(int)(y*windowHeightScale),
                    (int)(screenWidth -(x+w)*windowWidthScale),(int)( screenHeight -(y+h)*windowHeightScale));
        }else{
            Log.d("UIActivity","getLayoutParams fillFlag------------------------false");
            layoutParam = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.FILL_PARENT, ConstraintLayout.LayoutParams.FILL_PARENT);
            layoutParam.setMargins((int)(windowWidthOffset+x*windowScale),(int)(windowHeightOffset+y*windowScale),
                    (int)(screenWidth -(x+w)*windowScale-windowWidthOffset),(int)(screenHeight -(y+h)*windowScale-windowHeightOffset));
            Log.d("UIActivity","getLayoutParams"+"windowHeightOffset"+windowHeightOffset);
            Log.d("UIActivity","getLayoutParams"+"windowScale"+windowScale);
            Log.d("UIActivity","getLayoutParams"+"screenHeight"+screenHeight);
        }
        return  layoutParam;
    }

    private void setBackgroundView(int x,int y,int w,int h)
    {
        ui_back_cl.removeAllViews();
        imageView = new ImageView(this);
        imageView.setLayoutParams(getLayoutParams(x,y,w,h));
        ui_back_cl.addView(imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
        }else{
            imageView.setBackgroundColor(windowColor);
        }
    }
    /**
     * 打卡txt文件
     */
    private void OpenFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,1);
    }
    /**
     * 创建文件夹
     */
    private boolean mkDirs(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String path=this.getExternalFilesDir(null).getAbsolutePath()+"/学会助手/UI";
            File file=new File(path);
            if(!file.exists()){
                if(file.mkdirs()){
                    Log.d("UIActivity","UI文件创建成功----------------");
                    Log.d("UIActivity",path);
                    return true;
                }else{
                    Log.d("UIActivity","UI文件创建失败----------------");
                    Log.d("UIActivity",path);
                    return false;
                }
            }else{
                Log.d("UIActivity","UI文件已存在----------------");
                Log.d("UIActivity",path);
                return true;
            }
        }else{
            Log.d("UIActivity","内存异常----------------");
            return false;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK) {
                assert data != null;
                Uri uri = data.getData();//得到uri，将uri转化成路径
                InputStream inputStream= null;
                try {
                    inputStream = getContentResolver().openInputStream(Objects.requireNonNull(uri));
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    bitmap=BitmapFactory.decodeStream(inputStream,null,options);
                    Log.d("wh","w"+imageView.getWidth()+"h"+imageView.getHeight()+"imageView"+"--------------------------------------");
                    //按照适当等比例压缩图片
                    assert bitmap != null;
                    float dstScale=Math.max((float) imageView.getWidth()/bitmap.getWidth(),(float)imageView.getHeight()/bitmap.getHeight());
                    if(dstScale<1){
                        bitmap=Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*dstScale),(int)(bitmap.getHeight()*dstScale),false);
                    }
                    imageView.setImageBitmap(bitmap);
                    //将图片另存
                    if(mkDirs()){
                        wallpaperPath=this.getExternalFilesDir(null).getAbsolutePath()+"/学会助手/UI/wallpaper.png";
                        File file=new File(wallpaperPath);
                        FileOutputStream fileOutputStream=new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        PutData();
                    }
                    Log.d("wh","w"+bitmap.getWidth()+"h"+bitmap.getHeight()+"c:"+bitmap.getByteCount()+"--------------------------------------");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    /**
     * 清除窗口
     */
    private void clearWindow(){
        mConstraintLayout.removeAllViews();
    }

    /**
     * 设置窗口颜色
     * @param bytes 数据信息
     */
    private void setWindowColor(byte[] bytes){
        int color=((0XFF&bytes[3])<<24)|((0XFF&bytes[2])<<16)|((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        windowColor=color;
        imageView.setBackgroundColor(color);
        bitmap=null;
        wallpaperPath=null;
        PutData();
    }
    /**
     * 设置窗口宽高
     * @param bytes 数据信息
     */
    private void setWindowSize(byte[] bytes){
        int width=(((0XFF&bytes[1])<<8)|(0XFF&bytes[0]));
        int height=(((0XFF&bytes[3])<<8)|(0XFF&bytes[2]));
        screenWidth =mConstraintLayout.getWidth();
        screenHeight =mConstraintLayout.getHeight();
        Log.d("UIActivity","screenHeight"+screenHeight);
        windowWidth=width;
        windowHeight=height;
        fillFlag=(bytes[4]!=0);
        if(fillFlag){
            windowWidthScale=(float) screenWidth /width;
            windowHeightScale=(float) screenHeight /height;
            Log.d("UIActivity","windowWidthScale:"+windowWidthScale+"windowHeightScale:"+windowHeightScale);
        }else{
            windowScale=Math.min((float) screenWidth /width,(float) screenHeight /height);
            windowWidthOffset=(screenWidth -width*windowScale)/2;
            windowHeightOffset=(screenHeight -height*windowScale)/2;
            Log.d("UIActivity","windowScale:"+windowScale);
        }
        setBackgroundView(0,0,windowWidth,windowHeight);
        ui_tv.setText(width+"x"+height);
    }

    //设置竖屏

    private void setWindowOrientation(){
        Log.d("UIActivity","screenOrientation------------------------"+screenOrientation);
        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            screenOrientation=false;
            PutData();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Log.d("UIActivity","screenOrientation1------------------------"+screenOrientation);
        }else{
            screenOrientation=true;
            PutData();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Log.d("UIActivity","screenOrientation2------------------------"+screenOrientation);
        }
    }
    /**
     * 添加一个TextView控件
     * @param bytes 数据信息
     */
    private void addTextView(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[4];//1 x,2 y,3 w,4 h,
        for (int i = 0; i < 8; i+=2) {
            int int32=((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/2]=int32;
        }
        TextView textView=new TextView(this);
        textView.setId(id);
        textView.setIncludeFontPadding(false);  //清除控件周围空白
        textView.setLayoutParams(getLayoutParams(data[0],data[1],data[2],data[3]));
        mConstraintLayout.addView(textView);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(0XFFF0F0F0);
    }
    /**
     * 设置TextView文本内容
     * @param bytes 数据信息
     */
    private void setTextViewStyle(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[5];// 0 size,1 text color,2 gravity,3 background,4 rotation
        for (int i = 0; i < 20; i+=4) {
            int int32=((0XFF&bytes[i+5])<<24)|((0XFF&bytes[i+4])<<16)
                    |((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/4]=int32;
        }
        try {
            TextView textView= mConstraintLayout.findViewById(id);
            if(data[0]!=0) textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,data[0]*windowHeightScale);
            if(data[1]!=0) textView.setTextColor(data[1]);
            if(data[2]!=0) textView.setGravity(data[2]);
            if(data[3]!=0) textView.setBackgroundColor(data[3]);
            if(data[4]!=0) textView.setRotation(data[4]);
        }catch (Exception e){
//          Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置TextView文本内容
     * @param bytes 数据信息
     */
    private void setTextViewText(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        String string = null;
        try {
            string=new String(bytes,2,bytes.length-2,myCharsetName);//裁掉id，使用GBK解析
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        try {
            TextView textView= mConstraintLayout.findViewById(id);
            textView.setText(string);
        }catch (Exception e){
//          Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * TextView添加文本
     * @param bytes 数据信息
     */
    private void addTextViewText(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        String string = null;
        try {
            string=new String(bytes,2,bytes.length-2,myCharsetName);//使用GBK解析
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        try {
            TextView textView= mConstraintLayout.findViewById(id);
            textView.append(string);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    /**
     * 设置TextView字体大小
     * @param bytes 数据信息
     */
    private void setTextViewTextSize(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int size=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            TextView textView= mConstraintLayout.findViewById(id);
            if(fillFlag){
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,size*windowHeightScale);
            }else{
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,size*windowScale);
            }
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置TextView字体颜色
     * @param bytes 数据信息
     */
    private void setTextViewTextColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            TextView textView= mConstraintLayout.findViewById(id);
            textView.setTextColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置TextView字体颜色
     * @param bytes 数据信息
     */
    private void setTextViewTextGravity(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int gravity=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            TextView textView= mConstraintLayout.findViewById(id);
            textView.setGravity(gravity);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置TextView背景颜色
     * @param bytes 数据信息
     */
    private void setTextViewBackgroundColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            TextView textView= mConstraintLayout.findViewById(id);
            textView.setBackgroundColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置TextView背景颜色
     * @param bytes 数据信息
     */
    private void setTextViewRotation(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int rotation=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            TextView textView= mConstraintLayout.findViewById(id);
            textView.setRotation(rotation);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 添加一个Button
     * @param bytes 数据信息
     */
    private void addButton(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[4];//1 x,2 y,3 w,4 h,
        for (int i = 0; i < 8; i+=2) {
            int int32=((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/2]=int32;
        }
        Button button=new Button(this);
        button.setId(id);
        button.setLayoutParams(getLayoutParams(data[0],data[1],data[2],data[3]));
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String string;
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    string="b"+id+":"+"1"+"\r\n";
                    mCommunicationIService.callWrite(string.getBytes());
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    string="b"+id+":"+"0"+"\r\n";
                    mCommunicationIService.callWrite(string.getBytes());
                }
                return false;
            }
        });
        mConstraintLayout.addView(button);
        button.setBackgroundTintList(ColorStateList.valueOf(0xFF6480FF));
        button.setTextColor(Color.WHITE);
    }
    /**
     * 设置按钮文本
     * @param bytes 数据信息
     */
    private void setButtonStyle(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[6];// 0 color 1 text size,2 text color,3 gravity,4 background 5 rotation,
        for (int i = 0; i < 24; i+=4) {
            int int32=((0XFF&bytes[i+5])<<24)|((0XFF&bytes[i+4])<<16)
                    |((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/4]=int32;
        }
        try {
            Button button= mConstraintLayout.findViewById(id);
            if(data[0]!=0) button.setBackgroundTintList(ColorStateList.valueOf(data[0]));
            if(data[1]!=0) button.setTextSize(TypedValue.COMPLEX_UNIT_PX,data[1]*windowHeightScale);
            if(data[2]!=0) button.setTextColor(data[2]);
            if(data[3]!=0) button.setGravity(data[3]);
            if(data[4]!=0) button.setBackgroundColor(data[4]);
            if(data[5]!=0) button.setRotation(data[5]);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 设置按钮文本
     * @param bytes 数据信息
     */
    private void setButtonText(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        String string = null;
        try {
            string=new String(bytes,2,bytes.length-2,myCharsetName);//使用GBK解析
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        try {
            Button button= mConstraintLayout.findViewById(id);
            button.setText(string);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setButtonTextSize(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int size=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            Button button= mConstraintLayout.findViewById(id);
            if(fillFlag){
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX,size*windowHeightScale);
            }else{
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX,size*windowScale);
            }
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setButtonTextColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            Button button= mConstraintLayout.findViewById(id);
            button.setTextColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setButtonTextGravity(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int gravity=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            Button button= mConstraintLayout.findViewById(id);
            button.setGravity(gravity);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置Button背景颜色
     * @param bytes 数据信息
     */
    private void setButtonColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            Button button= mConstraintLayout.findViewById(id);
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 设置Button背景颜色
     * @param bytes 数据信息
     */
    private void setButtonRotation(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int rotation=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            Button button= mConstraintLayout.findViewById(id);
            button.setRotation(rotation);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置Button背景颜色
     * @param bytes 数据信息
     */
    private void setButtonBackgroundColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            Button button= mConstraintLayout.findViewById(id);
            button.setBackgroundColor(color);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 添加一个EditView控件
     * @param bytes 数据信息
     */
    private void addEditText(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int x=((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        int y=((0XFF&bytes[5])<<8)|(0XFF&bytes[4]);
        int w=((0XFF&bytes[7])<<8)|(0XFF&bytes[6]);
        int h=((0XFF&bytes[9])<<8)|(0XFF&bytes[8]);
        EditText editText=new EditText(this);
        editText.setId(id);
        editText.setLayoutParams(getLayoutParams(x,y,w,h));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string="e"+id+":"+editText.getText().toString()+"\r\n";
                try {
                    mCommunicationIService.callWrite(string.getBytes(myCharsetName));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        mConstraintLayout.addView(editText);
        editText.setTextColor(Color.BLACK);
    }
    /**
     * 设置EditView文本内容
     * @param bytes 数据信息
     */
    private void setEditTextStyle(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[5];// 0 text size,1 text color,2 gravity,3 background,4 rotation
        for (int i = 0; i < 20; i+=4) {
            int int32=((0XFF&bytes[i+5])<<24)|((0XFF&bytes[i+4])<<16)
                    |((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/4]=int32;
        }
        try {
            EditText editText= mConstraintLayout.findViewById(id);
            if(data[0]!=0)editText.setTextSize(TypedValue.COMPLEX_UNIT_PX,data[0]*windowHeightScale);
            if(data[1]!=0)editText.setTextColor(data[1]);
            if(data[2]!=0)editText.setGravity(data[2]);
            if(data[3]!=0)editText.setBackgroundColor(data[3]);//在透明时有横线
            if(data[4]!=0)editText.setRotation(data[4]);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 设置EditView文本内容
     * @param bytes 数据信息
     */
    private void setEditTextText(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        String string = null;
        try {
            string=new String(bytes,2,bytes.length-2,myCharsetName);//使用GBK解析
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        try {
            EditText editText= mConstraintLayout.findViewById(id);
            editText.setText(string);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置EditView提示
     * @param bytes 数据信息
     */
    private void setEditTextHint(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        String string = null;
        try {
            string=new String(bytes,2,bytes.length-2,myCharsetName);//使用GBK解析
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        try {
            EditText editText= mConstraintLayout.findViewById(id);
            editText.setHint(string);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setEditTextTextSize(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int size=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            EditText editText= mConstraintLayout.findViewById(id);
            if(fillFlag){
                editText.setTextSize(TypedValue.COMPLEX_UNIT_PX,size*windowHeightScale);
            }else{
                editText.setTextSize(TypedValue.COMPLEX_UNIT_PX,size*windowScale);
            }
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setEditTextTextColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            EditText editText= mConstraintLayout.findViewById(id);
            editText.setTextColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setEditTextTextGravity(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int gravity=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            EditText editText= mConstraintLayout.findViewById(id);
            editText.setGravity(gravity);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setEditTextBackgroundColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            EditText editText= mConstraintLayout.findViewById(id);
            editText.setBackgroundColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setEditTextRotation(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int rotation=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            EditText editText= mConstraintLayout.findViewById(id);
            editText.setRotation(rotation);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 添加一个ToggleButton
     * @param bytes 控件信息
     */
    private void addSwitch(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int x=((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        int y=((0XFF&bytes[5])<<8)|(0XFF&bytes[4]);
        int w=((0XFF&bytes[7])<<8)|(0XFF&bytes[6]);
        int h=((0XFF&bytes[9])<<8)|(0XFF&bytes[8]);
        MySwitch mySwitch=new MySwitch(this);
        mySwitch.setId(id);
        mySwitch.setLayoutParams(getLayoutParams(x,y,w,h));
        mySwitch.setOnCheckedChangeListener(new MySwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChange(View v, boolean isChecked) {
                String string;
                if(isChecked){
                    string="s"+id+":"+"1"+"\r\n";
                }else{
                    string="s"+id+":"+"0"+"\r\n";
                }
                mCommunicationIService.callWrite(string.getBytes());
            }
        });
        mConstraintLayout.addView(mySwitch);
    }
    /**
     * 设置ToggleButton关闭文本内容
     * @param bytes 控件信息
     */
    private void setSwitchStyle(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[3];// 0 id,1 background,2 rotation,
        for (int i = 0; i < 12; i+=4) {
            int int32=((0XFF&bytes[i+5])<<24)|((0XFF&bytes[i+4])<<16)
                    |((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/4]=int32;
        }
        try {
            MySwitch mySwitch= mConstraintLayout.findViewById(id);
            if(data[0]!=0) mySwitch.setColor(data[0]);
            if(data[1]!=0) mySwitch.setBackgroundColor(data[1]);
            if(data[2]!=0) mySwitch.setRotation(data[2]);

        }catch (Exception e){
//            Toast.makeText(this,"没有找到id为"+id+"的开关",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setSwitchChecked(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int isChecked=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            MySwitch mySwitch= mConstraintLayout.findViewById(id);
            mySwitch.setChecked(isChecked != 0);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setSwitchColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            MySwitch sw= mConstraintLayout.findViewById(id);
            sw.setColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setSwitchBackgroundColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            MySwitch sw= mConstraintLayout.findViewById(id);
            sw.setBackgroundColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setSwitchRotation(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int rotation=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            MySwitch sw= mConstraintLayout.findViewById(id);
            sw.setRotation(rotation);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 添加一个SeekBar
     * @param bytes 控件信息
     */
    private void addSeekBar(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int x=((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        int y=((0XFF&bytes[5])<<8)|(0XFF&bytes[4]);
        int w=((0XFF&bytes[7])<<8)|(0XFF&bytes[6]);
        int h=((0XFF&bytes[9])<<8)|(0XFF&bytes[8]);
        MySeekBar seekBar=new MySeekBar(this);
        seekBar.setId(id);
        seekBar.setLayoutParams(getLayoutParams(x,y,w,h));

        seekBar.setOnSeekBarChangeListener(new MySeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(View v, int progress) {
                String string="S"+id+":"+progress+"\r\n";
                mCommunicationIService.callWrite(string.getBytes());
            }
        });
        mConstraintLayout.addView(seekBar);
    }
    /**
     * 设置SeekBar最大值
     * @param bytes 控件信息
     */
    private void setSeekBarStyle(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[4];// 0 max,1 color,2 background,3 rotation,
        for (int i = 0; i < 16; i+=4) {
            int int32=((0XFF&bytes[i+5])<<24)|((0XFF&bytes[i+4])<<16)
                    |((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/4]=int32;
        }
        try {
            MySeekBar seekBar= mConstraintLayout.findViewById(id);
            if(data[0]!=0)seekBar.setMax(data[0]);
            if(data[1]!=0)seekBar.setColor(data[1]);
            if(data[2]!=0) seekBar.setBackgroundColor(data[2]);
            if(data[3]!=0) seekBar.setRotation(data[3]);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到id为"+id+"的进度条",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置SeekBar最大值
     * @param bytes 控件信息
     */
    private void setSeekBarMax(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int max=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            MySeekBar seekBar= mConstraintLayout.findViewById(id);
            seekBar.setMax(max);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到id为"+id+"的进度条",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 设置SeekBar当前值
     * @param bytes 控件信息
     */
    private void setSeekBarProcess(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int process=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            MySeekBar seekBar= mConstraintLayout.findViewById(id);
            seekBar.setProgress(process);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到id为"+id+"的进度条",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setSeekBarColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            MySeekBar seekBar= mConstraintLayout.findViewById(id);
            seekBar.setColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setSeekBarBackgroundColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            MySeekBar seekBar= mConstraintLayout.findViewById(id);
            seekBar.setBackgroundColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setSeekBarRotation(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int rotation=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            SeekBar seekBar= mConstraintLayout.findViewById(id);
            seekBar.setRotation(rotation);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 添加一个SeekBar
     * @param bytes 控件信息
     */
    private void addLineChart(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int x=((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        int y=((0XFF&bytes[5])<<8)|(0XFF&bytes[4]);
        int w=((0XFF&bytes[7])<<8)|(0XFF&bytes[6]);
        int h=((0XFF&bytes[9])<<8)|(0XFF&bytes[8]);
        LineChartView lineChartView=new LineChartView(this);
        lineChartView.setId(id);
       lineChartView.setLayoutParams(getLayoutParams(x,y,w,h));
        mConstraintLayout.addView(lineChartView);
    }

    private void setLineChartStyle(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[6];//0 x,1 y_min,2 y_max,3 line_color,4 chart_color,5 background
        for (int i = 0; i < 24; i+=4) {
            int int32=((0XFF&bytes[i+5])<<24)|((0XFF&bytes[i+4])<<16)
                    |((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/4]=int32;
        }
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            if(data[0]!=0) lineChartView.setXMax(data[0]);
            if(data[1]!=0&&data[2]!=0)lineChartView.setYMax(Float.intBitsToFloat(data[1]),Float.intBitsToFloat(data[2]));
            else if(data[2]!=0)lineChartView.setYMax(0,Float.intBitsToFloat(data[2]));
            else if(data[1]!=0)lineChartView.setYMax(Float.intBitsToFloat(data[1]),100);
            else lineChartView.setYMax(0,100);
            if(data[3]!=0) lineChartView.setLineColor(data[3]);
            if(data[4]!=0) lineChartView.setChartColor(data[4]);
            if(data[5]!=0) lineChartView.setBackgroundColor(data[5]);
        }catch (Exception e){
//          Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void addLineChartData(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        long long64=((long)(0XFF& bytes[9])<<56)|((long)(0XFF& bytes[8])<<48)
                |((long)(0XFF& bytes[7])<<40)|((long)(0XFF& bytes[6])<<32)
                |((long)(0XFF& bytes[5])<<24)|((long)(0XFF& bytes[4])<<16)
                |((long)(0XFF& bytes[3])<<8)|(long)(0XFF& bytes[2]);
        double d=Double.longBitsToDouble(long64);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.addData(d);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartClear(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.clearData();
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartXZoomIn(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setScaleX(true);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartXZoomOut(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setScaleX(false);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartYZoomIn(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setScaleY(true);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartYZoomOut(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setScaleY(false);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartXAxis(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int max=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setXMax(max);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartYAxis(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int min=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        int max=((0XFF&bytes[9])<<24)|((0XFF&bytes[8])<<16)|((0XFF&bytes[7])<<8)|(0XFF&bytes[6]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setYMax(Float.intBitsToFloat(min),Float.intBitsToFloat(max));
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartLineColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setLineColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartChartColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setChartColor(color);

        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setLineChartBackgroundColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            LineChartView lineChartView= mConstraintLayout.findViewById(id);
            lineChartView.setBackgroundColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 添加一个SeekBar
     * @param bytes 控件信息
     */
    private void addJoyStick(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int x=((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        int y=((0XFF&bytes[5])<<8)|(0XFF&bytes[4]);
        int w=((0XFF&bytes[7])<<8)|(0XFF&bytes[6]);
        int h=((0XFF&bytes[9])<<8)|(0XFF&bytes[8]);
        JoyStickView joyStickView=new JoyStickView(this);
        joyStickView.setId(id);
        joyStickView.setLayoutParams(getLayoutParams(x,y,w,h));
        joyStickView.setOnDragListener(new JoyStickView.OnDragListener() {
            @Override
            public boolean onDrag(View v, int x, int y) {
                DecimalFormat decimalFormat =new DecimalFormat("000");
                String string="j"+id+":"+decimalFormat.format(x)+","+decimalFormat.format(y)+"\r\n";
                mCommunicationIService.callWrite(string.getBytes());
                return true;
            }
        });
        mConstraintLayout.addView(joyStickView);
    }

    private void setJoyStickStyle(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int[] data=new int[3];// 0 shape,1 color,2 background
        for (int i = 0; i < 12; i+=4) {
            int int32=((0XFF&bytes[i+5])<<24)|((0XFF&bytes[i+4])<<16)
                    |((0XFF&bytes[i+3])<<8)|(0XFF&bytes[i+2]);
            data[i/4]=int32;
        }
        try {
            JoyStickView joyStickView= mConstraintLayout.findViewById(id);
            if(data[0]!=0) joyStickView.setStickColor(data[0]);
            if(data[1]!=0) joyStickView.setStickShape(data[1]);
            if(data[2]!=0) joyStickView.setBackgroundColor(data[2]);
        }catch (Exception e){
//          Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setJoyStickColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            JoyStickView joyStickView= mConstraintLayout.findViewById(id);
            joyStickView.setStickColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setJoyStickShape(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int shape=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            JoyStickView joyStickView= mConstraintLayout.findViewById(id);
            joyStickView.setStickShape(shape);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 设置button字体大小
     * @param bytes 数据信息
     */
    private void setJoyStickBackgroundColor(byte[] bytes){
        int id=((0XFF&bytes[1])<<8)|(0XFF&bytes[0]);
        int color=((0XFF&bytes[5])<<24)|((0XFF&bytes[4])<<16)|((0XFF&bytes[3])<<8)|(0XFF&bytes[2]);
        try {
            JoyStickView joyStickView= mConstraintLayout.findViewById(id);
            joyStickView.setBackgroundColor(color);
        }catch (Exception e){
//            Toast.makeText(this,"没有找到d为"+id+"的文本框",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**
     * 服务
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

    /**
     * 数据处理
     */
    private void DealBytes(){
        byte[] bytes=new byte[rx_num-3];//减去命令与帧尾
        System.arraycopy(rx_bytes,1,bytes,0,bytes.length); //得到裁掉命令和帧尾的数据
        int length=rx_num-3;
        byte view=(byte) (0XF0&rx_bytes[0]);
        if(view==0x00){
            switch (rx_bytes[0]){
                case 0x00: {clearWindow();break;}
                case 0x01: {if(length>=4) setWindowColor(bytes);break;}
                case 0x02: {if(length>=5) setWindowSize(bytes);break;}
            }
        }else if(view==0x10){
            switch (rx_bytes[0]){
                case 0x10: {if(length>=10) addTextView(bytes);break;}
                case 0x11: {if(length>=22) setTextViewStyle(bytes);break;}
                case 0x12: {if(length>=2) setTextViewText(bytes);break;}
                case 0x13: {if(length>=2) addTextViewText(bytes);break;}
                case 0x14: {if(length>=6) setTextViewTextSize(bytes);break;}
                case 0x15: {if(length>=6) setTextViewTextColor(bytes);break;}
                case 0x16: {if(length>=6) setTextViewTextGravity(bytes);break;}
                case 0x17: {if(length>=6) setTextViewBackgroundColor(bytes);break;}
                case 0x18: {if(length>=6) setTextViewRotation(bytes);break;}
            }
        }else if(view==0x20){
            switch (rx_bytes[0]){
                case 0x20: {if(length>=10) addButton(bytes);break;}
                case 0x21: {if(length>=26) setButtonStyle(bytes);break;}
                case 0x22: {if(length>=4) setButtonText(bytes);break;}
                case 0x23: {if(length>=6) setButtonColor(bytes);break;}
                case 0x24: {if(length>=6) setButtonTextSize(bytes);break;}
                case 0x25: {if(length>=6) setButtonTextColor(bytes);break;}
                case 0x26: {if(length>=6) setButtonTextGravity(bytes);break;}
                case 0x27: {if(length>=6) setButtonBackgroundColor(bytes);break;}
                case 0x28: {if(length>=6) setButtonRotation(bytes);break;}
            }
        }else if(view==0x30){
            switch (rx_bytes[0]){
                case 0x30: {if(length>=10) addEditText(bytes);break;}
                case 0x31: {if(length>=22) setEditTextStyle(bytes);break;}
                case 0x32: {if(length>=2) setEditTextText(bytes);break;}
                case 0x33: {if(length>=2) setEditTextHint(bytes);break;}
                case 0x34: {if(length>=6) setEditTextTextSize(bytes);break;}
                case 0x35: {if(length>=6) setEditTextTextColor(bytes);break;}
                case 0x36: {if(length>=6) setEditTextTextGravity(bytes);break;}
                case 0x37: {if(length>=6) setEditTextBackgroundColor(bytes);break;}
                case 0x38: {if(length>=6) setEditTextRotation(bytes);break;}
            }
        }else if(view==0x40){
            switch (rx_bytes[0]){
                case 0x40: {if(length>=10) addSwitch(bytes);break;}
                case 0x41: {if(length>=14) setSwitchStyle(bytes);break;}
                case 0x42: {if(length>=6) setSwitchChecked(bytes);break;}
                case 0x43: {if(length>=6) setSwitchColor(bytes);break;}
                case 0x44: {if(length>=6) setSwitchBackgroundColor(bytes);break;}
                case 0x45: {if(length>=6) setSwitchRotation(bytes);break;}
            }
        }else if(view==0x50){
            switch (rx_bytes[0]){
                case 0x50: {if(length>=10) addSeekBar(bytes);break;}
                case 0x51: {if(length>=18) setSeekBarStyle(bytes);break;}
                case 0x52: {if(length>=6) setSeekBarMax(bytes);break;}
                case 0x53: {if(length>=6) setSeekBarProcess(bytes);break;}
                case 0x54: {if(length>=6) setSeekBarColor(bytes);break;}
                case 0x55: {if(length>=6) setSeekBarBackgroundColor(bytes);break;}
                case 0x56: {if(length>=6) setSeekBarRotation(bytes);break;}
            }
        }else if(view==0x60){
            switch (rx_bytes[0]){
                case 0x60: {if(length>=10) addLineChart(bytes);break;}
                case 0x61: {if(length>=26) setLineChartStyle(bytes);break;}
                case 0x62: {if(length>=10) addLineChartData(bytes);break;}
                case 0x63: {if(length>=2) setLineChartClear(bytes);break;}
                case 0x64: {if(length>=6) setLineChartXAxis(bytes);break;}
                case 0x65: {if(length>=10) setLineChartYAxis(bytes);break;}
                case 0x66: {if(length>=6) setLineChartLineColor(bytes);break;}
                case 0x67: {if(length>=6) setLineChartChartColor(bytes);break;}
                case 0x68: {if(length>=6) setLineChartBackgroundColor(bytes);break;}
            }
        }else if(view==0x70){
            switch (rx_bytes[0]){
                case 0x70: {if(length>=10) addJoyStick(bytes);break;}
                case 0x71: {if(length>=14) setJoyStickStyle(bytes);break;}
                case 0x72: {if(length>=6) setJoyStickColor(bytes);break;}
                case 0x73: {if(length>=6) setJoyStickShape(bytes);break;}
                case 0x74: {if(length>=6) setJoyStickBackgroundColor(bytes);break;}
            }
        }
    }
    /**
     *广播接收到数据
     */
    private int rx_num;
    private byte[] rx_bytes;
    private final byte[] start_bytes=new byte[2];
    private int rx_state;
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes=intent.getByteArrayExtra("value");
            assert bytes != null;
            for (byte aByte : bytes) {
                if (rx_state == 0) {
                    start_bytes[0] = start_bytes[1];
                    start_bytes[1] = aByte;
                    if (start_bytes[0] == 0x01 && start_bytes[1] == 0x09) {
                        rx_state = 1;
                    }
                } else if (rx_state == 1) {
                    int num = 0xFF & aByte ;
                    if (num != 0) {   //有固定个数
                        rx_bytes = new byte[num+3];//加上命令与帧尾
                        rx_state = 2;
                    } else {
                        rx_bytes = new byte[256];//最多可接收256
                        rx_state = 3;
                    }
                    rx_num = 0; //准备开始接收
                } else if (rx_state == 2) {
                    rx_bytes[rx_num] = aByte;
                    rx_num++;
                    //数据接收完成
                    if (rx_num == rx_bytes.length) {
                        //数据正确，处理数据
                        if( rx_bytes[rx_num - 2] == 0x08 && rx_bytes[rx_num - 1] == 0x07 ) {
                            DealBytes();
                        }
                        rx_num=0;
                        rx_state = 0;
                    }
                } else if (rx_state == 3) {
                    rx_bytes[rx_num] = aByte;
                    rx_num++;
                    if(rx_num>4) //命令+ID+帧尾，至少5个
                    {
                        if (rx_bytes[rx_num - 2] == 0x08 && rx_bytes[rx_num - 1] == 0x07) {
                            DealBytes();
                            rx_num=0;
                            rx_state = 0;
                        } else if (rx_num == 256) {
                            rx_num=0;
                            rx_state = 0;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            ShowBackDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void ShowBackDialog(){
        MySelectDialog mySelectDialog=new MySelectDialog(this);
        mySelectDialog.show();
        mySelectDialog.setButtonText("取消","退出");
        mySelectDialog.setText("退出将清空界面，是否退出？");
        mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_select_yes_bt){
                    finish();
                }
            }
        });
    }

    private void ShowSetDialog(){
        MyUIDialog myUIDialog=new MyUIDialog(this);
        myUIDialog.show();
        myUIDialog.setOnDialogClickListener(new MyUIDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_ui_back_bt){
                }else if(view.getId()==R.id.layout_dialog_ui_orientation_bt){
                    setWindowOrientation();
                }else if(view.getId()==R.id.layout_dialog_ui_wall_bt){
                    OpenFile();
                }
            }
        });
    }
    private void PutData(){
        sp.putString("UI_wallpaperPath",wallpaperPath);
        sp.putBoolean("UI_screenOrientation",screenOrientation);
    }

    private void GetData() {
        wallpaperPath=sp.getString("UI_wallpaperPath", null);
        if(wallpaperPath!=null){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565; //不会对PNG格式生效
            File file=new File(wallpaperPath);
            if(file.exists()){
                bitmap=BitmapFactory.decodeFile(wallpaperPath,options);
            }
        }
        screenOrientation=sp.getBoolean("UI_screenOrientation", true);
        if(screenOrientation){
            if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }else{
            if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(myServiceConnection);
        unregisterReceiver(myBroadcastReceiver);
    }
}
