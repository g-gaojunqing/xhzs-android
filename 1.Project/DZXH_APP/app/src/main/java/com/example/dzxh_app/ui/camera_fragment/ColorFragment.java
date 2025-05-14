package com.example.dzxh_app.ui.camera_fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
@SuppressLint("SetTextI18n")
public class ColorFragment extends Fragment {
    private CameraActivity cameraActivity;
    //控件
    private ImageView fragment_color_big_iv;
    private ImageView fragment_color_small_iv;
    private ImageView fragment_color_cursor_big_iv;
    private ImageView fragment_color_cursor_small_iv;
    private EditText fragment_color_width_et;
    private EditText fragment_color_height_et;
    private LinearLayout fragment_color_adjust_ll;//滑块
    private ArrayList<Button> buttons;
    private SeekBar fragment_color_process_sb;
    private SeekBar fragment_color_adjust_sb;
    private Drawable seekBarProcess;
    private TextView fragment_color_inform_tv;
    private ToggleButton fragment_color_start_tb;
    //图片处理相关
    private static Bitmap srcColorBitmap;
    private static Bitmap srcBinaryBitmap;
    private static byte[] bytesColorImage =null; //彩色图片数组
    private static byte[] bytesBinaryImage =null;  //二值化图片数组
    private static int imageLength, seekbarLength; //进度条刷新
    private int colorRxHeight, colorRxWidth;
    private int rxState =0, rxNum =0;  //接收数据标记位
    private static boolean changeFlag =false; //两幅图片交换位置标志位
    private int buttonChecked;//记录阈值调整被选中按钮
    private int[] colorThresholds;//颜色阈值
    private int[] colorMaxThresholds;//颜色阈值最大值,不能加 private
    //图像操作相关
    private Paint paint;//游标
    private Paint paintGrid;//网格
    private static int bigIVHeight, bigIVWidth;   //大图宽高
    private static float smallIVHeight, smallIVWidth; //小图宽高
    private static float cursorX, cursorY;  //使用int会造成误差
    private static float scaleXY =1; //图像缩放比例
    private static float offsetX =0, offsetY =0;   //图像偏移，用于图像拖动
    private int touchEvent;    //点击事件标志位，用于记录手势

    //服务相关
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private ContentReceiver mContentReceiver;
    private IntentFilter filter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color,container,false);
        NumInit();
        ViewInit(view);
        ButtonInit(view);
        getData();
        SetGesture();
        ServiceInit();
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
        mContentReceiver=new ContentReceiver();
        filter = new IntentFilter(
                "com.example.dzxh_app.content");
    }
    private void NumInit() {
        buttonChecked =-1;
        colorThresholds =new int[]{0,248,0,252,0,248};
        colorMaxThresholds =new int[]{248,248,252,252,248,248};

        buttons=new ArrayList<Button>();
        paint = new Paint();
        paint.setColor(0xFFE0E000);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(24);
        paint.setAntiAlias(true);
        paintGrid = new Paint();
        paintGrid.setColor(Color.GRAY);

        cameraActivity=(CameraActivity)getActivity();
    }
    private void ViewInit(View view){
        //阈值调整控件
        fragment_color_adjust_ll = view.findViewById(R.id.fragment_color_adjust_ll);
        fragment_color_adjust_ll.setVisibility(View.GONE);
        //宽高
        fragment_color_width_et = view.findViewById(R.id.fragment_color_width_et);
        fragment_color_height_et = view.findViewById(R.id.fragment_color_height_et);
        //小图
        fragment_color_small_iv = view.findViewById(R.id.fragment_color_small_iv);
        //大图
        fragment_color_big_iv = view.findViewById(R.id.fragment_color_big_iv);
        //对背景图设置
        if(!changeFlag){
            fragment_color_small_iv.setBackgroundColor(Color.WHITE);
            fragment_color_big_iv.setBackgroundColor(Color.BLACK);
        }else{
            fragment_color_small_iv.setBackgroundColor(Color.BLACK);
            fragment_color_big_iv.setBackgroundColor(Color.WHITE);
        }
        //小图游标
        fragment_color_cursor_small_iv = view.findViewById(R.id.fragment_color_cursor_small_iv);
        //大图游标
        fragment_color_cursor_big_iv = view.findViewById(R.id.fragment_color_cursor_big_iv);
        //左上角信息
        fragment_color_inform_tv = view.findViewById(R.id.fragment_color_inform_tv);
        //图片接收进程
        fragment_color_process_sb = view.findViewById(R.id.fragment_color_process_sb);
        //得到fragment中的控件大小的一种方法
        fragment_color_cursor_big_iv.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {

                //第一次打开界面
                if(bytesColorImage ==null){
                    smallIVHeight =fragment_color_cursor_small_iv.getHeight();
                    smallIVWidth =fragment_color_cursor_small_iv.getWidth();
                    bigIVHeight = fragment_color_cursor_big_iv.getHeight();
                    bigIVWidth = fragment_color_cursor_big_iv.getWidth();
                    cursorX = (float) bigIVWidth /2;
                    cursorY = (float) bigIVHeight /2;
                    //显示储存好的照片
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    srcColorBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.huihui,options);
                    srcColorBitmap=Bitmap.createScaledBitmap(srcColorBitmap,300,300,false);
                    srcBinaryBitmap=Bitmap.createBitmap(300, 300, Bitmap.Config.ALPHA_8);
                    scaleXY =2;
                    offsetX =(float) (bigIVWidth -600)/2;
                    offsetY =(float) (bigIVHeight -600)/2;
                    BitmapToBytes(srcColorBitmap);
                    DealBytes();
                }
                DrawCursor();//画游标
                DrawImage();
            }
        });
    }

    private void BitmapToBytes(Bitmap bitmap){
        bytesColorImage =new byte[2*300*300];
        bytesBinaryImage =new byte[300*300];
        int bytes = bitmap.getByteCount();  //获取数组大小
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);
        bytesColorImage = buffer.array();
    }

    private void ButtonInit(View view){
        fragment_color_width_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(RegexUtil.isNum(fragment_color_width_et.getText().toString())) {
                    colorRxWidth =Integer.parseInt(fragment_color_width_et.getText().toString());
                }else{
                    Toast.makeText(getActivity(),"输入不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        fragment_color_height_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(RegexUtil.isNum(fragment_color_height_et.getText().toString())) {
                    colorRxHeight =Integer.parseInt(fragment_color_height_et.getText().toString());
                }else{
                    Toast.makeText(getActivity(),"输入不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //开始or停止接收
        fragment_color_start_tb = view.findViewById(R.id.fragment_color_start_tb);
        fragment_color_start_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //设置宽高不可调
                    fragment_color_start_tb.setTextColor(Color.RED);
                    fragment_color_width_et.setEnabled(false);
                    fragment_color_width_et.setTextColor(Color.GRAY);
                    fragment_color_height_et.setEnabled(false);
                    fragment_color_height_et.setTextColor(Color.GRAY);
                    //宽高改变
                    if(colorRxWidth !=srcColorBitmap.getWidth()|| colorRxHeight !=srcColorBitmap.getHeight()){
                        if(colorRxWidth ==0){
                            colorRxWidth =8;
                        }
                        if(colorRxHeight ==0){
                            colorRxHeight =8;
                        }
                        putData();//记录新的宽高
                        bytesColorImage =new byte[colorRxWidth * colorRxHeight *2];//重新申请大图数组
                        for (int i = 0; i < colorRxWidth * colorRxHeight *2; i++) {
                            bytesColorImage[i]=(byte)0XFF;
                        }
                        bytesBinaryImage =new byte[colorRxWidth * colorRxHeight];//重新申请小图数组
                        srcColorBitmap = Bitmap.createBitmap(colorRxWidth, colorRxHeight, Bitmap.Config.RGB_565);//彩色图片
                        srcBinaryBitmap=Bitmap.createBitmap(colorRxWidth, colorRxHeight, Bitmap.Config.ALPHA_8);//黑白图片
                        scaleXY =1;//缩放比例置一
                        offsetX =(float) (bigIVWidth - colorRxWidth)/2;//偏移居中
                        offsetY =(float) (bigIVHeight - colorRxHeight)/2;//偏移居中
                        DrawCursor();
                        DealBytes();
                        DrawImage();
                    }
                    rxState =0;
                    rxNum =0;
                    fragment_color_width_et.setText(colorRxWidth +"");//有时候可能没有输入
                    fragment_color_height_et.setText(colorRxHeight +"");//有时候可能没有输入
                    //设置进度条参数
                    imageLength = bytesColorImage.length; //图像长度
                    seekbarLength = imageLength /100;
                    fragment_color_process_sb.setMax(imageLength);
                    fragment_color_process_sb.setProgress(rxNum);
                    //绑定广播
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requireActivity().registerReceiver(mContentReceiver, filter,Context.RECEIVER_EXPORTED); //绑定广播
                    }else{
                        requireActivity().registerReceiver(mContentReceiver, filter); //绑定广播
                    }
                }else {
                    //解绑广播
                    requireActivity().unregisterReceiver(mContentReceiver);
                    //设置宽高可调
                    fragment_color_start_tb.setTextColor(Color.BLACK);
                    fragment_color_width_et.setEnabled(true);
                    fragment_color_width_et.setTextColor(Color.BLACK);
                    fragment_color_height_et.setEnabled(true);
                    fragment_color_height_et.setTextColor(Color.BLACK);
                }
            }
        });
        //清除
        Button fragment_color_clear_bt = view.findViewById(R.id.fragment_color_clear_bt);
        fragment_color_clear_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Arrays.fill(bytesColorImage, (byte) 0xFF);
                DealBytes();
                DrawImage();
            }
        });
        //打开图片
        Button fragment_color_open_bt = view.findViewById(R.id.fragment_color_open_bt);
        fragment_color_open_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragment_color_start_tb.isChecked()) {
                    Toast.makeText(getActivity(), "正在接收，操作失败", Toast.LENGTH_SHORT).show();
                }else{
                    OpenFile();
                }
            }
        });
        //保存图片
        Button fragment_color_save_bt = view.findViewById(R.id.fragment_color_save_bt);
        fragment_color_save_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveFile();
            }
        });
        //阈值调整SeekBar
        fragment_color_adjust_sb = view.findViewById(R.id.fragment_color_adjust_sb);
        LayerDrawable layerDrawable = (LayerDrawable) fragment_color_adjust_sb.getProgressDrawable();
        seekBarProcess = layerDrawable.getDrawable(1);
        fragment_color_adjust_sb.setMax(255);
        fragment_color_adjust_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(buttonChecked!=-1&&fromUser){
                    buttons.get(buttonChecked).setText(Integer.toString(seekBar.getProgress()));
                    colorThresholds[buttonChecked] = seekBar.getProgress();
                    DealBinaryBytes();//重写灰度数组
                    if(!changeFlag){
                        DrawSmallImage(srcBinaryBitmap);
                    }else{
                        DrawBigImage(srcBinaryBitmap);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        //阈值调整增
        ImageButton fragment_color_add_ib = view.findViewById(R.id.fragment_color_add_ib);
        fragment_color_add_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked !=-1) {
                    if (colorThresholds[buttonChecked] < colorMaxThresholds[buttonChecked]) {
                        colorThresholds[buttonChecked]++;
                        buttons.get(buttonChecked).setText(colorThresholds[buttonChecked] + "");
                        fragment_color_adjust_sb.setProgress(colorThresholds[buttonChecked]);
                        DealBinaryBytes();//重写灰度数组
                        if(!changeFlag){
                            DrawSmallImage(srcBinaryBitmap);
                        }else{
                            DrawBigImage(srcBinaryBitmap);
                        }
                    }
                }
            }
        });
        //阈值调整减
        ImageButton fragment_color_remove_ib = view.findViewById(R.id.fragment_color_remove_ib);
        fragment_color_remove_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked !=-1) {
                    if (colorThresholds[buttonChecked]>0) {
                        colorThresholds[buttonChecked]--;
                        buttons.get(buttonChecked).setText(colorThresholds[buttonChecked] + "");
                        fragment_color_adjust_sb.setProgress(colorThresholds[buttonChecked]);
                        DealBinaryBytes();//重写二值化图片
                        if(!changeFlag){
                            DrawSmallImage(srcBinaryBitmap);
                        }else{
                            DrawBigImage(srcBinaryBitmap);
                        }
                    }
                }
            }
        });
        //红色下限
        Button fragment_color_rl_bt = view.findViewById(R.id.fragment_color_Rl_bt);
        buttons.add(fragment_color_rl_bt);
        fragment_color_rl_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==0){
                    RestButton(0);
                }else{
                    SetSeekBar(0,new LightingColorFilter(Color.RED,0x000000));
                }
            }
        });
        //红色上限
        Button fragment_color_rh_bt = view.findViewById(R.id.fragment_color_Rh_bt);
        buttons.add(fragment_color_rh_bt);
        fragment_color_rh_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==1){
                    RestButton(1);
                }else{
                    SetSeekBar(1,new LightingColorFilter(Color.RED,0xFF0000));
                }
            }
        });
        //绿色下限
        Button fragment_color_gl_bt = view.findViewById(R.id.fragment_color_Gl_bt);
        buttons.add(fragment_color_gl_bt);
        fragment_color_gl_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==2){
                    RestButton(2);
                }else{
                    SetSeekBar(2,new LightingColorFilter(Color.GREEN,0x000000));
                }
            }
        });
        //绿色上限
        Button fragment_color_gh_bt = view.findViewById(R.id.fragment_color_Gh_bt);
        buttons.add(fragment_color_gh_bt);
        fragment_color_gh_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==3){
                    RestButton(3);
                }else{
                    SetSeekBar(3,new LightingColorFilter(Color.GREEN,0x00FF00));
                }
            }
        });

        //蓝色下限
        Button fragment_color_bl_bt = view.findViewById(R.id.fragment_color_Bl_bt);
        buttons.add(fragment_color_bl_bt);
        fragment_color_bl_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(buttonChecked ==4){
                    RestButton(4);
               }else{
                    SetSeekBar(4,new LightingColorFilter(Color.BLUE,0x000000));
               }
            }
        });
        //蓝色上限
        Button fragment_color_bh_bt = view.findViewById(R.id.fragment_color_Bh_bt);
        buttons.add(fragment_color_bh_bt);
        fragment_color_bh_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==5){
                    RestButton(5);
                }else{
                    SetSeekBar(5,new LightingColorFilter(Color.BLUE,0x0000FF));
                }
            }
        });
    }
    private void RestButton(int num){
        buttons.get(num).setBackgroundColor(Color.parseColor("#2B2B2B"));//抬起这个按钮
        fragment_color_adjust_ll.setVisibility(View.GONE);
        buttonChecked =-1;
    }
    private void SetSeekBar(int num,ColorFilter colorFilter){
        if(buttonChecked !=-1){
            buttons.get(buttonChecked).setBackgroundColor(Color.parseColor("#2B2B2B"));//抬起上一个按钮
        }
        //设置此按钮
        buttons.get(num).setBackgroundColor(Color.parseColor("#111111"));//按下这个按钮
        //SeekBar颜色及进度
        seekBarProcess.setColorFilter(colorFilter);
        fragment_color_adjust_sb.setMax(colorMaxThresholds[num]);
        fragment_color_adjust_sb.setProgress(colorThresholds[num]);
        fragment_color_adjust_ll.setVisibility(View.VISIBLE);
        buttonChecked =num;
    }

    private void DrawInformation(){
        int width=srcColorBitmap.getWidth();
        int height=srcColorBitmap.getHeight();
        int cx,cy,rgb565,r,g,b;
        String string;
        cx=(int)((cursorX - offsetX)/ scaleXY);
        cy=(int)((cursorY - offsetY)/ scaleXY);
        if((cursorX - offsetX)<0||cx>=width||(cursorY - offsetY)<0||cy>=height){
            string="X:- Y:-\r\nR:- G:- B:-";
        }else{
            rgb565=((0XFF& bytesColorImage[2*(width*cy+cx)+1])<<8)+(0XFF& bytesColorImage[2*(width*cy+cx)]);
            r=(rgb565&0xF800)>>8;
            g=(rgb565&0x07E0)>>3;
            b=(rgb565&0x001F)<<3;
            string="X:"+cx+" Y:"+cy+"\r\n"+"R:"+r+" G:"+g+" B:"+b;
        }
        fragment_color_inform_tv.setText(string);
    }

    /**
     * 大图游标
     */
    private void DrawBigCursor(){
        Bitmap Cursor_Bitmap= Bitmap.createBitmap(bigIVWidth, bigIVHeight, Bitmap.Config.ARGB_8888);   //既有彩色，又可以达到透明不影响图片显示
        Canvas canvas=new Canvas(Cursor_Bitmap);
        //网格
        if(scaleXY >10) {
            float grid_offsetX= offsetX % scaleXY;
            float grid_offsetY= offsetY % scaleXY;
            float grid_width= bigIVWidth + scaleXY;
            float grid_height= bigIVHeight + scaleXY;
            for (float i = 0; i <=grid_width; i+= scaleXY) {
                canvas.drawLine(i+grid_offsetX,0,i+grid_offsetX, bigIVHeight, paintGrid);//X游标
            }
            for (float i = 0; i <=grid_height; i+= scaleXY) {
                canvas.drawLine(0,i+grid_offsetY, bigIVWidth,i+grid_offsetY, paintGrid); //Y游标
            }
        }
        canvas.drawLine(cursorX,0, cursorX, bigIVHeight,paint);//X游标
        canvas.drawLine(0, cursorY, bigIVWidth, cursorY,paint); //Y游标
        fragment_color_cursor_big_iv.setImageBitmap(Cursor_Bitmap);//添加到前景
    }

    /**
     * 小图游标
     */
    private void DrawSmallCursor() {
        int width=srcColorBitmap.getWidth();
        int height=srcColorBitmap.getHeight();
        float scale;
        Bitmap Cursor_Bitmap = Bitmap.createBitmap((int) smallIVWidth, (int) smallIVHeight, Bitmap.Config.ARGB_8888);   //既有彩色，又可以达到透明不影响图片显示
        Canvas canvas = new Canvas(Cursor_Bitmap);

        if(smallIVHeight *width> smallIVWidth *height){
            scale= smallIVWidth /width;
        }else{
            scale= smallIVHeight /height;
        }
        float cx=(cursorX - offsetX)/ scaleXY *scale+ smallIVWidth /2-width*scale/2;

        float cy=(cursorY - offsetY)/ scaleXY *scale+ smallIVHeight /2-height*scale/2;
        if(cx<0){
            cx=0;
        }else if(cx>= smallIVWidth){
            cx= smallIVWidth -1;
        }
        if(cy<0){
            cy=0;
        }else if(cy>= smallIVHeight){
            cy= smallIVHeight -1;
        }
        canvas.drawLine(cx,0,cx, smallIVHeight,paint);//X游标
        canvas.drawLine(0,cy, smallIVWidth,cy,paint); //Y游标
        fragment_color_cursor_small_iv.setImageBitmap(Cursor_Bitmap);
    }

    /**
     * @功能 将彩色图片数组转为图片
     * @调用 在彩色图片刷新后调用
     */
    private void DealColorBytes(){
        ByteBuffer buffer = ByteBuffer.wrap(bytesColorImage);
        srcColorBitmap.copyPixelsFromBuffer(buffer);
    }

    /**
     * @功能 二值化数组转为图片
     * @调用 阈值与彩色图片刷新后调用
     */
    private void DealBinaryBytes(){
        int len= bytesColorImage.length;
        int rgb565;
        int r,g,b;
        for (int i = 0; i <len; i+=2) {
            rgb565=((0XFF& bytesColorImage[i+1])<<8)+(0XFF& bytesColorImage[i]);
            r=(rgb565&0xF800)>>8;
            g=(rgb565&0x07E0)>>3;
            b=(rgb565&0x001F)<<3;
            if((r>=colorThresholds[0])&&(r<= colorThresholds[1])
                    &&(g>= colorThresholds[2])&&(g<= colorThresholds[3])
                    &&(b>= colorThresholds[4])&&(b<= colorThresholds[5])){
                bytesBinaryImage[i/2]=(byte)0X11;//白
            }else{
                bytesBinaryImage[i/2]=(byte)0XFF;//黑
            }
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytesBinaryImage);
        srcBinaryBitmap.copyPixelsFromBuffer(buffer);
    }
    private void DrawBigImage(Bitmap bitmap){
        Bitmap scaledBitmap;
        int srcWidth,srcHeight;
        int cutWidth,cut_X=0,cutHeight,cut_Y=0;
        srcWidth=bitmap.getWidth();
        srcHeight=bitmap.getHeight();
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
        matrix.postTranslate(dx,dy);
        fragment_color_big_iv.setImageMatrix(matrix);
        fragment_color_big_iv.setImageBitmap(scaledBitmap);
    }

    /**
     * 小图显示
     * @param bitmap 图片
     */
    private void DrawSmallImage(Bitmap bitmap){
        Bitmap scaledBitmap;
        float scale;//缩放比例
        float dx,dy;//起点
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        //右上角图像 确定缩放比例
        if(smallIVHeight *width> smallIVWidth *height){
            scale= smallIVWidth /width;
            dx=0;
            dy= smallIVHeight /2-height*scale/2;
        }else{
            scale= smallIVHeight /height;
            dx= smallIVWidth /2-width*scale/2;
            dy=0;
        }
        scaledBitmap=Bitmap.createScaledBitmap(bitmap,(int)(width*scale),(int)(height*scale),false);
        Matrix matrix = new Matrix();
        matrix.postTranslate(dx,dy);
        fragment_color_small_iv.setImageMatrix(matrix);
        fragment_color_small_iv.setImageBitmap(scaledBitmap);
    }

    /**
     * 写游标以及信息
     */
    private void DrawCursor(){
        DrawBigCursor();
        DrawSmallCursor();
        DrawInformation();
    }

    /**
     * 数组解析
     */
    private void DealBytes(){
        DealColorBytes();
        DealBinaryBytes();
        DrawInformation();
    }

    /**
     * 显示图像
     */
    private void DrawImage(){
        if(!changeFlag){
            DrawBigImage(srcColorBitmap);
            DrawSmallImage(srcBinaryBitmap);
        }else {
            DrawBigImage(srcBinaryBitmap);
            DrawSmallImage(srcColorBitmap);
        }
    }
    private float DownXed,DownYed,DownXYed,DownXYing,Y_Center,X_Center;
    @SuppressLint("ClickableViewAccessibility")
    private void SetGesture(){
        fragment_color_cursor_big_iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()& MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_DOWN:
                        if(Math.abs(event.getX()- cursorX)<50&&((bigIVHeight -event.getY())<100||event.getY()<100)){
                            touchEvent =1;      //X方向游标
                            DownXed=event.getX();
                        }else if(Math.abs(event.getY()- cursorY)<50&&((bigIVWidth -event.getX()<100||event.getX()<100))){
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
                        //两指中心
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
                            DrawImage();
                        }else if(touchEvent ==4){
                            //计算当前两指间距离
                            DownXYing=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                                    +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                            DownXYing=(float) Math.sqrt(DownXYing);
                            //以两指为中心放大
                            float tempOX=X_Center-(X_Center- offsetX)*DownXYing/DownXYed;
                            float tempOY=Y_Center-(Y_Center- offsetY)*DownXYing/DownXYed;
                            float tempXY = scaleXY *DownXYing/DownXYed;       //X方向比例
                            if(tempXY<200&&tempXY>0.1&&(colorRxWidth *tempXY>1)&&(colorRxHeight *tempXY>1)){
                                scaleXY =tempXY;
                                offsetX =tempOX;
                                offsetY =tempOY;
                                DownXYed = DownXYing;
                                DrawCursor();
                                DrawImage();
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
        fragment_color_small_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFlag =!changeFlag;
                if(!changeFlag){
                    fragment_color_small_iv.setBackgroundColor(Color.WHITE);
                    fragment_color_big_iv.setBackgroundColor(Color.BLACK);
                }else{
                    fragment_color_small_iv.setBackgroundColor(Color.BLACK);
                    fragment_color_big_iv.setBackgroundColor(Color.WHITE);
                }
                DrawImage();
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
     *广播接收到数据
     */
    public class ContentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] bytes=intent.getByteArrayExtra("value");
            assert bytes != null;
            for (byte aByte : bytes) {
                if (rxState == 4) {
                    bytesColorImage[rxNum] = aByte;
                    rxNum++;
                    if(rxNum % seekbarLength ==0) {
                        fragment_color_process_sb.setProgress(rxNum);
                    }
                    if (rxNum >= imageLength) {
                        rxState = 0;
                        rxNum = 0;
                        DealBytes();
                        DrawImage();
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
                        if (aByte == 0x01) {
                            rxState = 1;
                        } else {
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
        cameraActivity.sp.putInt("Color_colorRxWidth", colorRxWidth);
        cameraActivity.sp.putInt("Color_colorRxHeight", colorRxHeight);
        for (int i = 0; i < 6; i++) {
            cameraActivity.sp.putInt("Color_colorThresholds"+i, colorThresholds[i]);
        }
    }
    private void getData(){
        colorRxWidth =cameraActivity.sp.getInt("Color_colorRxWidth",300);
        fragment_color_width_et.setText(colorRxWidth +"");
        colorRxHeight =cameraActivity.sp.getInt("Color_colorRxHeight",300);
        fragment_color_height_et.setText(colorRxHeight +"");
        for (int i = 0; i < 6; i++) {
            colorThresholds[i]=cameraActivity.sp.getInt("Color_colorThresholds"+i,0);
            buttons.get(i).setText(colorThresholds[i]+"");
        }
    }
    /**
     * 保存图像
     */
    private void SaveFile(){
        makeDirs();//创建文件夹
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE,"图像.rgb565");

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File newFolder = new File(downloadDir, "XHZS/IMAGE/");
        Log.d("ColorFragment","Path:"+newFolder.getPath());
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,"content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2FXHZS%2FIMAGE%2F%E5%9B%BE%E5%83%8F.rgb565%20(1)");

        Log.d("ColorFragment","Uri:"+Uri.fromFile(newFolder).toString());
       // intent.addCategory(Intent.CATEGORY_OPENABLE);


        startActivityForResult(intent,1);
    }

    private void makeDirs(){
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File newFolder = new File(downloadDir, "学会助手/图像");
        if(newFolder.exists()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = requireActivity().getContentResolver();
            // 使用 MediaStore 在 Download 下创建子文件夹
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, "temp.text");
            values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/XHZS/IMAGE");
            try {
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    // 写入空内容并删除临时文件，仅保留文件夹
                    try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                        outputStream.write("".getBytes());
                    }
                    //resolver.delete(uri, null, null); // 删除临时文件
                    Log.d("ColorFragment", "文件夹已创建");
                }
            } catch (IOException e) {
                Log.d("ColorFragment", "文件夹创建失败");
                e.printStackTrace();
            }
        }else{
            if (newFolder.mkdirs()) {
                Log.d("CameraActivity", "文件夹已创建");
            } else {
                Log.d("CameraActivity", "文件夹创建失败");
            }
        }
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
            Log.d("ColorFragment","onActivityResult Uri:"+uri);
            if(requestCode==1){
                WriteFile(uri);
            }else if(requestCode==2){
                ReadFile(uri);
            }
        }else{
            Toast.makeText(getActivity(),"操作取消",Toast.LENGTH_SHORT).show();
        }
    }

    private void WriteFile(Uri uri){
        try{
            OutputStream outputStream =getActivity().getContentResolver().openOutputStream(uri);
            DecimalFormat decimalFormat = new DecimalFormat("000");
            String s=decimalFormat.format(srcColorBitmap.getWidth())+"#"+decimalFormat.format(srcColorBitmap.getHeight())+"#";
            assert outputStream != null;
            outputStream.write(s.getBytes());
            outputStream.write(bytesColorImage);
            outputStream.close();
            Toast.makeText(getActivity(),"保存成功",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"保存失败",Toast.LENGTH_SHORT).show();
        }
    }

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
            byte[] buffer = new byte[inputStream.available()];//inputStream.available() 表示要读取的文件中的数据长度
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
            if((buffer.length-8)!=2*Integer.parseInt(string_w)*Integer.parseInt(string_h)){
                Toast.makeText(getActivity(), "文件出错了", Toast.LENGTH_SHORT).show();
                return;
            }
            colorRxWidth =Integer.parseInt(string_w);
            colorRxHeight =Integer.parseInt(string_h);
            fragment_color_width_et.setText(colorRxWidth +"");
            fragment_color_height_et.setText(colorRxHeight +"");
            //显示图片
            bytesColorImage =new byte[2* colorRxWidth * colorRxHeight];
            System.arraycopy(buffer,8, bytesColorImage,0, bytesColorImage.length);
            bytesBinaryImage =new byte[colorRxWidth * colorRxHeight];
            srcColorBitmap = Bitmap.createBitmap(colorRxWidth, colorRxHeight, Bitmap.Config.RGB_565);
            srcBinaryBitmap=Bitmap.createBitmap(colorRxWidth, colorRxHeight, Bitmap.Config.ALPHA_8);
            scaleXY =1;
            offsetX =(bigIVWidth - colorRxWidth)/2;
            offsetY =(bigIVHeight - colorRxHeight)/2;
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
        getActivity().unbindService(myServiceConnection);
        if(fragment_color_start_tb.isChecked()){
            getActivity().unregisterReceiver(mContentReceiver);
        }
        putData();
    }
}
