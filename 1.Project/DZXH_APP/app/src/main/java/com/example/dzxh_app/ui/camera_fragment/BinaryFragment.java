package com.example.dzxh_app.ui.camera_fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.dzxh_app.Activity.CameraActivity;
import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.util.RegexUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SetTextI18n")
public class BinaryFragment extends Fragment {

    private CameraActivity cameraActivity;
    //控件
    private ImageView fragment_binary_big_iv;
    private ImageView fragment_binary_cursor_iv;
    private EditText fragment_binary_width_et;
    private EditText fragment_binary_height_et;
    private TextView fragment_binary_inform_tv;
    private SeekBar fragment_binary_process_sb;
    private ToggleButton fragment_binary_start_tb;
    private ToggleButton fragment_binary_order_tb;
    //图像处理
    private int rxState, rxNum;//接收状态，接收数量
    private static Bitmap srcBinaryBitmap; //图片
    private static byte[] bytesBinaryImage; //图片数组
    private static int imageLength, seekbarLength; //进度条刷新
    private int binaryRxHeight; //图片高
    private int binaryRxWidth; //图片宽
    private Timer timer; //刷新定时
    private boolean refreshFlag; //刷新标记位
    private boolean orderFlag; //顺序标记位
    //图像操作
    private Paint paint;//游标
    private Paint paintGrid; //网格
    private static int bigIVWidth=0,bigIVHeight=0; //图片显示窗口宽度高度
    private static float cursorX, cursorY;//游标位置 #使用int会造成误差
    private static float scaleXY; //图像缩放比例
    private static float offsetX, offsetY;//图像偏移，用于图像拖动
    private int touchEvent;
    //服务相关
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private ContentReceiver mContentReceiver;
    private IntentFilter filter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_binary,container,false);

        NumInit();
        ViewInit(view);
        ButtonInit(view);
        SetGesture();
        ServiceInit();
        TimerInit();
        getData();
        return view;
    }
    private void ServiceInit(){
        Intent intent = new Intent(getActivity(), CommunicationService.class);
        requireActivity().startService(intent);
        myServiceConnection = new MyServiceConnection();
        getActivity().bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
        BroadcastReceiverInit();
    }
    private void BroadcastReceiverInit() {
        mContentReceiver= new ContentReceiver();
        filter = new IntentFilter(
                "com.example.dzxh_app.content");
    }
    private void NumInit() {
        refreshFlag =false;
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(24);
        paint.setAntiAlias(true);     //去除锯齿

        paintGrid = new Paint();
        paintGrid.setColor(Color.GRAY);
        cameraActivity=(CameraActivity)getActivity();
    }
    private void ViewInit(View view){
        //大图
        fragment_binary_big_iv = view.findViewById(R.id.fragment_binary_big_iv);
        //游标
        fragment_binary_cursor_iv = view.findViewById(R.id.fragment_binary_cursor_iv);
        //进度条
        fragment_binary_process_sb = view.findViewById(R.id.fragment_binary_process_sb);

        fragment_binary_inform_tv = view.findViewById(R.id.fragment_binary_inform_tv);
        //得到fragment中的控件大小的一种方法
        fragment_binary_cursor_iv.post(new Runnable() {
            @Override
            public void run() {
                //检测是否第一次打开
                if(bytesBinaryImage==null){
                    bigIVHeight = fragment_binary_cursor_iv.getHeight();
                    bigIVWidth = fragment_binary_cursor_iv.getWidth();
                    cursorX = (float)bigIVWidth /2;
                    cursorY = (float)bigIVHeight /2;
                    //加载预览图片
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                    srcBinaryBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.huihui_grey,options);
                    srcBinaryBitmap=Bitmap.createScaledBitmap(srcBinaryBitmap,304,304,false);
                    scaleXY =2; //缩放为2倍
                    offsetX =(float)(bigIVWidth -608)/2; //X偏移量，设置为居中
                    offsetY =(float)(bigIVHeight -608)/2;//Y偏移量
                    BitmapToBytes(srcBinaryBitmap);
                    byte temp;
                    if(orderFlag){
                        for (int i = 0; i < bytesBinaryImage.length; i+=8) {
                            for (int j = 0; j < 4; j++) {
                                temp= bytesBinaryImage[i+7-j];
                                bytesBinaryImage[i+7-j]= bytesBinaryImage[i+j];
                                bytesBinaryImage[i+j]=temp;
                            }
                        }
                    }
                    DealBytes();
                }
                DrawCursor();
                DrawImage();
            }
        });
    }

    /**
     * 图片转为数组
     * @param bitmap 输入图片
     */
    private void BitmapToBytes(Bitmap bitmap){
        int grey;
        bytesBinaryImage =new byte[304*304];
        int bytes = bitmap.getByteCount(); //获得像素数量
        ByteBuffer buffer = ByteBuffer.allocate(bytes); //分配缓冲区
        bitmap.copyPixelsToBuffer(buffer);
        bytesBinaryImage = buffer.array();
        //二值化图片数组
        for (int i = 0; i < bytesBinaryImage.length; i++) {
            grey=0XFF& bytesBinaryImage[i];
            if(grey<=218){
                bytesBinaryImage[i]=(byte)0XFF;//黑
            }else{
                bytesBinaryImage[i]=(byte)0X11;//白
            }
        }
    }
    private void ButtonInit(View view){
        fragment_binary_width_et = view.findViewById(R.id.fragment_binary_width_et);
        fragment_binary_width_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(RegexUtil.isNum(fragment_binary_width_et.getText().toString())){
                    binaryRxWidth =Integer.parseInt(fragment_binary_width_et.getText().toString());
                }else {
                    Toast.makeText(getActivity(),"输入不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        fragment_binary_height_et = view.findViewById(R.id.fragment_binary_height_et);
        fragment_binary_height_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(RegexUtil.isNum(fragment_binary_height_et.getText().toString())){
                    binaryRxHeight =Integer.parseInt(fragment_binary_height_et.getText().toString());
                }else {
                    Toast.makeText(getActivity(),"输入不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //开始接收
        fragment_binary_start_tb = view.findViewById(R.id.fragment_binary_start_tb);
        fragment_binary_start_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //设置宽高不可调
                    fragment_binary_start_tb.setTextColor(Color.RED);
                    fragment_binary_width_et.setEnabled(false);
                    fragment_binary_width_et.setTextColor(Color.GRAY);
                    fragment_binary_height_et.setEnabled(false);
                    fragment_binary_height_et.setTextColor(Color.GRAY);
                    //宽高改变
                    if(binaryRxWidth !=srcBinaryBitmap.getWidth()|| binaryRxHeight !=srcBinaryBitmap.getHeight()){
                        if(binaryRxWidth %8!=0){
                            binaryRxWidth = binaryRxWidth - binaryRxWidth %8+8; //不是8的倍数自动补8
                            fragment_binary_width_et.setText(binaryRxWidth +"");
                        }
                        if(binaryRxWidth ==0){
                            binaryRxWidth =8;
                        }
                        if(binaryRxHeight ==0){
                            binaryRxHeight =8;
                        }
                        putData();
                        bytesBinaryImage =new byte[binaryRxWidth * binaryRxHeight];
                        srcBinaryBitmap=Bitmap.createBitmap(binaryRxWidth, binaryRxHeight, Bitmap.Config.ALPHA_8);
                        Arrays.fill(bytesBinaryImage, (byte) 0XFF);
                        scaleXY =1;
                        offsetX =(float) (bigIVWidth - binaryRxWidth)/2;
                        offsetY =(float) (bigIVHeight - binaryRxHeight)/2;
                        DrawCursor();
                        DealBytes();
                        DrawImage();
                    }
                    rxState =0;
                    rxNum =0; //开始接收置零
                    fragment_binary_width_et.setText(binaryRxWidth +"");//有时候可能没有输入
                    fragment_binary_height_et.setText(binaryRxHeight +"");//有时候可能没有输入
                    //设置进度条
                    imageLength = bytesBinaryImage.length;
                    seekbarLength = imageLength /100;
                    fragment_binary_process_sb.setMax(imageLength);
                    fragment_binary_process_sb.setProgress(rxNum);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requireActivity().registerReceiver(mContentReceiver, filter,Context.RECEIVER_EXPORTED); //绑定广播
                    }else{
                        requireActivity().registerReceiver(mContentReceiver, filter); //绑定广播
                    }
                }else {
                    requireActivity().unregisterReceiver(mContentReceiver); //解绑广播
                    //设置宽高可调
                    fragment_binary_start_tb.setTextColor(Color.BLACK);
                    fragment_binary_width_et.setEnabled(true);
                    fragment_binary_width_et.setTextColor(Color.BLACK);
                    fragment_binary_height_et.setEnabled(true);
                    fragment_binary_height_et.setTextColor(Color.BLACK);
                }
            }
        });
        Button fragment_binary_clear_bt = view.findViewById(R.id.fragment_binary_clear_bt);
        fragment_binary_clear_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < bytesBinaryImage.length; i++) {
                    bytesBinaryImage[i]=(byte)0xFF;
                }
                if(!refreshFlag){
                    DealBytes();
                    DrawImage();
                }
            }
        });
        fragment_binary_order_tb = view.findViewById(R.id.fragment_binary_order_tb);
        fragment_binary_order_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                byte temp;
                if(isChecked&&!orderFlag){  //避免在GetData中在开Order_flag=true情况下运行此函数
                    orderFlag =true; //开倒序
                    for (int i = 0; i < bytesBinaryImage.length; i+=8) {
                        for (int j = 0; j < 4; j++) {
                            temp= bytesBinaryImage[i+7-j];
                            bytesBinaryImage[i+7-j]= bytesBinaryImage[i+j];
                            bytesBinaryImage[i+j]=temp;
                       }
                    }
                    if(!refreshFlag){
                        DealBytes();
                        DrawImage();
                    }
                }else if(!isChecked&& orderFlag){
                    orderFlag =false;
                    for (int i = 0; i < bytesBinaryImage.length; i+=8) {
                        for (int j = 0; j < 4; j++) {
                            temp= bytesBinaryImage[i+7-j];
                            bytesBinaryImage[i+7-j]= bytesBinaryImage[i+j];
                            bytesBinaryImage[i+j]=temp;
                        }
                    }
                    if(!refreshFlag){
                        DealBytes();
                        DrawImage();
                    }
                }
            }
        });
        //打开图片
        Button fragment_grey_open_bt = view.findViewById(R.id.fragment_binary_open_bt);
        fragment_grey_open_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragment_binary_start_tb.isChecked()) {
                    Toast.makeText(getActivity(), "正在接收，操作失败", Toast.LENGTH_SHORT).show();
                }else{
                    OpenFile();
                }
            }
        });
        Button fragment_binary_save_bt = view.findViewById(R.id.fragment_binary_save_bt);
        fragment_binary_save_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveFile();
            }
        });
    }

    /**
     * 像素信息
     */
    private void DrawInform(){
        int width=srcBinaryBitmap.getWidth();
        int height=srcBinaryBitmap.getHeight();
        int cx,cy;
        String string;
        cx=(int)((cursorX - offsetX)/ scaleXY);
        cy=(int)((cursorY - offsetY)/ scaleXY);
        if((cursorX - offsetX)<0||cx>=width||(cursorY - offsetY)<0||cy>=height){
            string="X:- Y:-\r\nV:-";
        }else{
            if(bytesBinaryImage[width*cy+cx]==0x11) {
                string="X:"+cx+" Y:"+cy+"\r\n"+"V:1";
            }else{
                string="X:"+cx+" Y:"+cy+"\r\n"+"V:0";
            }
        }
        fragment_binary_inform_tv.setText(string);
    }

    /**
     * 绘制游标与网格
     */
    private void DrawCursor() {
        Bitmap Cursor_Bitmap= Bitmap.createBitmap(bigIVWidth, bigIVHeight, Bitmap.Config.ARGB_8888);   //既有彩色，又可以达到透明不影响图片显示
        Canvas canvas=new Canvas(Cursor_Bitmap);
        canvas.drawLine(cursorX,0, cursorX, bigIVHeight,paint);//X游标
        canvas.drawLine(0, cursorY, bigIVWidth, cursorY,paint); //Y游标
        //网格
        if(scaleXY >10) {
            float grid_offsetX= offsetX % scaleXY;
            float grid_offsetY= offsetY % scaleXY;
            float grid_width= bigIVWidth + scaleXY;
            float grid_height= bigIVHeight + scaleXY;
            for (float i = 0; i <=grid_width ; i+= scaleXY) {
                canvas.drawLine(i+grid_offsetX,0,i+grid_offsetX, bigIVHeight, paintGrid);//X游标
            }
            for (float i = 0; i <=grid_height; i+= scaleXY) {
                canvas.drawLine(0,i+grid_offsetY, bigIVWidth,i+grid_offsetY, paintGrid); //Y游标
            }
        }
        fragment_binary_cursor_iv.setImageBitmap(Cursor_Bitmap);//添加到前景
        DrawInform();
    }
    /**
     * @功能 将灰度图片数组转为图片
     * @调用 在灰度图片刷新后调用
     */
    private void DealBytes(){
        ByteBuffer buffer = ByteBuffer.wrap(bytesBinaryImage);
        srcBinaryBitmap.copyPixelsFromBuffer(buffer);
        DrawInform();
    }

    private void DrawImage(){
        DrawBigImage(srcBinaryBitmap);
    }

    /**
     * 将图片显示在窗口中
     * @param bitmap 图片
     */
    private void DrawBigImage(Bitmap bitmap){
        Bitmap scaledBitmap;
        int srcWidth,srcHeight;
        int cut_X=0,cutWidth,cut_Y=0,cutHeight;
        srcWidth=bitmap.getWidth();
        srcHeight=bitmap.getHeight();
        //图片过大，进行裁剪
        if(srcWidth* scaleXY > bigIVWidth || srcHeight* scaleXY > bigIVHeight) {
            if(srcWidth* scaleXY > bigIVWidth){
                if(offsetX <0) {
                    cut_X = (int)(-offsetX / scaleXY);
                    if(cut_X>(srcWidth-1)){
                        cut_X=srcWidth-1;
                    }
                }
                cutWidth=(int)((double) bigIVWidth / scaleXY)+2;//加2防止后部黑边
                if((cutWidth+cut_X)>srcWidth){
                    cutWidth= srcWidth-cut_X;
                }
            }else{
                cutWidth= srcWidth;
            }
            if(srcHeight* scaleXY > bigIVHeight){
                if(offsetY <0) {
                    cut_Y= (int)(-offsetY / scaleXY);
                    if(cut_Y>(srcHeight-1)){
                        cut_Y=srcHeight-1;
                    }
                }
                cutHeight=(int)((double) bigIVHeight / scaleXY)+2;//加2防止后部黑边
                if((cutHeight+cut_Y)> srcHeight) {
                    cutHeight= srcHeight-cut_Y;
                }
            }else{
                cutHeight= srcHeight;
            }
            //裁剪图片
            Bitmap cutBitmap=Bitmap.createBitmap(bitmap,cut_X,cut_Y,cutWidth,cutHeight,new Matrix(),false);
            //放大图片，且不添加模糊边缘，以保证清晰的像素
            int width=(int)(cutWidth* scaleXY);
            int height=(int)(cutHeight* scaleXY);
            scaledBitmap=Bitmap.createScaledBitmap(cutBitmap,width,height,false);
        }else{
            //放大图片，且不添加模糊边缘，以保证清晰的像素
            int width=(int)(srcWidth* scaleXY);
            int height=(int)(srcHeight* scaleXY);
            scaledBitmap=Bitmap.createScaledBitmap(bitmap,width,height,false);
        }

        Matrix matrix = new Matrix();
        float dx= offsetX + scaleXY *cut_X;
        float dy= offsetY + scaleXY *cut_Y;
        matrix.postTranslate(dx,dy);//设置图片显示起点
        fragment_binary_big_iv.setImageMatrix(matrix);
        fragment_binary_big_iv.setImageBitmap(scaledBitmap);
        refreshFlag =false;//防止频繁刷新
    }

    private float DownXed,DownYed,DownXYed,DownXYing,Y_Center,X_Center;
    @SuppressLint("ClickableViewAccessibility")
    private void SetGesture(){
        fragment_binary_cursor_iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()& MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_DOWN:
                        if(Math.abs(event.getX()- cursorX)<50&&((bigIVHeight -event.getY()<100||event.getY()<100))){
                            touchEvent =1;      //X方向游标
                            DownXed=event.getX();
                        }else if(Math.abs(event.getY()- cursorY)<50&&((bigIVWidth -event.getX())<100||event.getX()<100)){
                            touchEvent =2;      //Y方向游标
                            DownYed=event.getY();
                        }else{
                            touchEvent =3;//平移事件
                            DownXed=event.getX();
                            DownYed=event.getY();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        touchEvent =4; //缩放事件
                        //计算起始两指距离
                        DownXYed=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                                +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                        DownXYed=(float) Math.sqrt((double) DownXYed);
                        //二指中心
                        Y_Center=(event.getY(0)+event.getY(1))/2;
                        X_Center=(event.getX(0)+event.getX(1))/2;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(touchEvent ==1){
                            cursorX =event.getX();
                            if(cursorX <0){
                                cursorX =0;
                            }else if(cursorX > bigIVWidth){
                                cursorX = bigIVWidth;
                            }
                            DrawCursor();
                        }else if(touchEvent ==2){
                            cursorY =event.getY();
                            if(cursorY <0){
                                cursorY =0;
                            }else if(cursorY > bigIVHeight){
                                cursorY = bigIVHeight;
                            }
                            DrawCursor();
                        }else if(touchEvent ==3){
                            offsetX +=event.getX()-DownXed;
                            offsetY +=event.getY()-DownYed;
                            DownXed=event.getX();
                            DownYed=event.getY();
                            DrawCursor();
                            if(!refreshFlag){
                                DrawImage();
                            }
                        }else if(touchEvent ==4){
                            DownXYing=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                                    +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                            DownXYing=(float) Math.sqrt(DownXYing);
                            //以两指为中心放大
                            float tempOX=X_Center-(X_Center- offsetX)*DownXYing/DownXYed;
                            float tempOY=Y_Center-(Y_Center- offsetY)*DownXYing/DownXYed;
                            float tempXY =  ( scaleXY * DownXYing / DownXYed);       //X方向比例
                            if(tempXY<200&&tempXY>0.1&&(binaryRxHeight *tempXY>1)&&(binaryRxWidth *tempXY>1)){
                                scaleXY =tempXY;
                                offsetX =tempOX;
                                offsetY =tempOY;
                                DownXYed = DownXYing;
                                DrawCursor();
                                if(!refreshFlag){
                                    DealBytes();
                                    DrawImage();
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        touchEvent =0;  //不写就会出现程序卡死
                        break;
                }
                return true;
            }
        });
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
     * 防止刷新速率过高
     */
    private void TimerInit(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(refreshFlag){
                    refreshFlag =false;
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DealBytes();
                            DrawImage();
                        }
                    });
                }
            }
        },50,50);
    }
    /**
     *广播接收到数据
     */
    public class ContentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes=intent.getByteArrayExtra("value");
            assert bytes != null;
            for (byte aByte : bytes) {
                if (rxState == 4) {
                    if (orderFlag) {  //倒序
                        for (int j = 0; j < 8; j++) {
                            if ((aByte & (0x01 << j)) == 0) {
                                bytesBinaryImage[rxNum] = (byte) 0XFF;
                            } else {
                                bytesBinaryImage[rxNum] = 0X11;
                            }
                            rxNum++;
                        }
                    } else {  //正序
                        for (int j = 0; j < 8; j++) {
                            if ((aByte & (0x80 >> j)) == 0) {
                                bytesBinaryImage[rxNum] = (byte) 0XFF;
                            } else {
                                bytesBinaryImage[rxNum] = 0X11;
                            }
                            rxNum++;
                        }
                    }
                    if(rxNum % seekbarLength ==0){
                        fragment_binary_process_sb.setProgress(rxNum);
                    }
                    if (rxNum >= imageLength) {
                        rxState = 0;
                        rxNum = 0;
                        refreshFlag =true;
                    }
                } else if (rxState == 3) {
                    if (aByte == 0x07) {
                        rxState = 4;
                        rxNum =0;
                    } else {
                        if (aByte == 0x01) {
                            rxState = 1;
                        } else {
                            rxState = 0;
                        }
                    }
                } else if (rxState == 2) {
                    if (aByte == 0x08) {
                        rxState = 3;
                    } else {
                        if (aByte == 0x01) {
                            rxState = 1;
                        } else {
                            rxState = 0;
                        }
                    }
                } else if (rxState == 1) {
                    if (aByte == 0x09) {
                        rxState = 2;
                    } else {
                        if (aByte != 0x01) {
                            rxState = 0;
                        }
                    }
                } else if (rxState == 0) {
                    if (aByte == 0x01) {
                        rxState = 1;
                    }
                }
            }
        }
    }
    private void putData(){
        cameraActivity.sp.putInt("Binary_binaryRxWidth", binaryRxWidth);
        cameraActivity.sp.putInt("Binary_binaryRxHeight", binaryRxHeight);
        cameraActivity.sp.putBoolean("Binary_orderFlag", orderFlag);
    }
    private void getData(){
        binaryRxWidth =cameraActivity.sp.getInt("Binary_binaryRxWidth",304);
        fragment_binary_width_et.setText(binaryRxWidth +"");
        binaryRxHeight =cameraActivity.sp.getInt("Binary_binaryRxHeight",304);
        fragment_binary_height_et.setText(binaryRxHeight +"");
        orderFlag =cameraActivity.sp.getBoolean("Binary_orderFlag",false);
        fragment_binary_order_tb.setChecked(orderFlag);
    }
    /**
     * 保存数据到txt文件
     */
    private void SaveFile(){
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        }
        Objects.requireNonNull(intent).setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE,"图像.bin");
        startActivityForResult(intent,1);
    }
    /**
     * 打开文件
     */
    private void OpenFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            Uri uri = Objects.requireNonNull(data).getData();//得到uri，将uri转化成路径
            if(requestCode==1){
                WriteFile(uri);
            }else if(requestCode==2){
                ReadFile(uri);
            }
        }else{
            Toast.makeText(getActivity(),"操作取消",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 保存图像
     * @param uri uri
     */
    private void WriteFile( Uri uri){
        try {
            OutputStream outputStream = requireActivity().getContentResolver().openOutputStream(uri);
            DecimalFormat decimalFormat = new DecimalFormat("000");
            String s=decimalFormat.format(srcBinaryBitmap.getWidth())+"#"+decimalFormat.format(srcBinaryBitmap.getHeight())+"#";
            Objects.requireNonNull(outputStream).write(s.getBytes());
            outputStream.write(bytesBinaryImage);
            outputStream.close();
            Toast.makeText(getActivity(),"保存成功",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"保存失败",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 读图像
     * @param uri uri
     */
    private void ReadFile(Uri uri){
        byte[] bytes_w,bytes_h;
        String string_w,string_h;
        try {
            InputStream inputStream= requireActivity().getContentResolver().openInputStream(uri);
            assert inputStream != null;
            if(inputStream.available()==0){
                Toast.makeText(getActivity(), "空文件", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] buffer = new byte[inputStream.available()];//in.available() 表示要读取的文件中的数据长度
            inputStream.read(buffer);  //将文件中的数据读到buffer中
            inputStream.close();
            bytes_w=new byte[3];
            System.arraycopy(buffer,0,bytes_w,0,3);
            string_w=new String(bytes_w);
            if(!RegexUtil.isNum(string_w)){
                Toast.makeText(getActivity(), "文件解析失败", Toast.LENGTH_SHORT).show();
                return;
            }
            bytes_h=new byte[3];
            System.arraycopy(buffer,4,bytes_h,0,3);
            string_h=new String(bytes_h);
            if(!RegexUtil.isNum(string_h)){
                Toast.makeText(getActivity(), "文件解析失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if((buffer.length-8)!=Integer.parseInt(string_w)*Integer.parseInt(string_h)){
                Toast.makeText(getActivity(), "文件出错了", Toast.LENGTH_SHORT).show();
                return;
            }
            binaryRxWidth =Integer.parseInt(string_w);
            binaryRxHeight =Integer.parseInt(string_h);
            fragment_binary_width_et.setText(binaryRxWidth +"");
            fragment_binary_height_et.setText(binaryRxHeight +"");

            //显示图片
            bytesBinaryImage =new byte[binaryRxWidth * binaryRxHeight];
            System.arraycopy(buffer,8, bytesBinaryImage,0, bytesBinaryImage.length);
            srcBinaryBitmap = Bitmap.createBitmap(binaryRxWidth, binaryRxHeight, Bitmap.Config.ALPHA_8);
            scaleXY =1;
            offsetX =(bigIVWidth - binaryRxWidth)/2;
            offsetY =(bigIVHeight - binaryRxHeight)/2;
            DrawCursor();
            DealBytes();
            DrawImage();
            Toast.makeText(getActivity(), "图片导入成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "图片导入失败", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "图片导入失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unbindService(myServiceConnection);
        if(fragment_binary_start_tb.isChecked()){
            getActivity().unregisterReceiver(mContentReceiver);
        }
        timer.cancel();
        putData();
    }
}
