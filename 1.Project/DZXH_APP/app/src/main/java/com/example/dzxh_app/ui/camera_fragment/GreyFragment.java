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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
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
import java.util.Objects;
@SuppressLint("SetTextI18n")
public class GreyFragment extends Fragment {
    private CameraActivity cameraActivity;
    //控件
    private TextView fragment_grey_inform_tv;
    private ToggleButton fragment_grey_start_tb;
    private ImageView fragment_grey_big_iv;
    private ImageView fragment_grey_small_iv;
    private ImageView fragment_grey_cursor_big_iv;
    private ImageView fragment_grey_cursor_small_iv;
    private ArrayList<Button> buttons;
    private EditText fragment_grey_width_et;
    private EditText fragment_grey_height_et;
    private LinearLayout fragment_grey_adjust_ll;
    private SeekBar fragment_grey_adjust_sb;
    private SeekBar fragment_grey_process_sb;
    private Switch fragment_grey_light_sw;
    //图片处理
    private static int bigIVHeight, bigIVWidth;
    private static float smallIVHeight, smallIVWidth;
    private static byte[] bytesGreyImage =null;
    private static byte[] bytesBinaryImage =null;
    private static int imageLength, seekbarLength; //进度条刷新
    private static Bitmap srcGreyBitmap;
    private static Bitmap srcBinaryBitmap;
    private int greyRxHeight, greyRxWidth;
    private int rxState =0, rxNum =0; //接收数据标记位
    private int buttonChecked;
    private int[] greyThresholds;
    private static boolean changeFlag =false;
    private boolean reverseFlag; //亮度取反

    //图片操作
    private Paint paint; //游标
    private Paint paintGrid; //网格
    private int touchEvent;
    private static float scaleXY =1;//图像缩放
    private static float offsetX =0, offsetY =0;//图像偏移，用于图像拖动
    private static float cursorX, cursorY;//使用int会造成误差

    //服务相关
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;
    private ContentReceiver mContentReceiver;
    private IntentFilter filter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grey,container,false);
        NumInit();
        ViewInit(view);
        ButtonInit(view);
        SetGesture();
        ServiceInit();
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
        mContentReceiver=new ContentReceiver();
        filter = new IntentFilter(
                "com.example.dzxh_app.content");
    }

    private void NumInit() {
        buttonChecked =-1;
        greyThresholds =new int[]{255,0};
        buttons=new ArrayList<Button>();

        paint = new Paint();
        paint.setColor(Color.BLUE);
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
        fragment_grey_adjust_ll = view.findViewById(R.id.fragment_grey_adjust_ll);
        fragment_grey_adjust_ll.setVisibility(View.GONE);
        //接收图像宽
        fragment_grey_width_et=view.findViewById(R.id.fragment_grey_width_et);
        //接收图像高
        fragment_grey_height_et=view.findViewById(R.id.fragment_grey_height_et);
        //小图
        fragment_grey_small_iv = view.findViewById(R.id.fragment_grey_small_iv);
        //大图
        fragment_grey_big_iv = view.findViewById(R.id.fragment_grey_big_iv);
        //小图游标
        fragment_grey_cursor_small_iv = view.findViewById(R.id.fragment_grey_cursor_small_iv);
        //大图游标
        fragment_grey_cursor_big_iv = view.findViewById(R.id.fragment_grey_cursor_big_iv);
        //左上角信息
        fragment_grey_inform_tv = view.findViewById(R.id.fragment_grey_inform_tv);
        //图片接收进程
        fragment_grey_process_sb = view.findViewById(R.id.fragment_grey_process_sb);
        //得到fragment中的控件大小的一种方法
        fragment_grey_cursor_big_iv.post(new Runnable() {
            @Override
            public void run() {
                //第一次打开界面
                if(bytesGreyImage ==null){
                    smallIVHeight =fragment_grey_cursor_small_iv.getHeight();
                    smallIVWidth =fragment_grey_cursor_small_iv.getWidth();
                    bigIVHeight = fragment_grey_cursor_big_iv.getHeight();
                    bigIVWidth = fragment_grey_cursor_big_iv.getWidth();
                    cursorX = (float) bigIVWidth /2;
                    cursorY = (float) bigIVHeight /2;
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                    srcGreyBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.huihui_grey,options);
                    srcGreyBitmap=Bitmap.createScaledBitmap(srcGreyBitmap,300,300,false);
                    srcBinaryBitmap=Bitmap.createBitmap(300, 300, Bitmap.Config.ALPHA_8);
                    scaleXY =2;
                    offsetX =(float)(bigIVWidth -600)/2;
                    offsetY =(float)(bigIVHeight -600)/2;
                    BitmapToBytes(srcGreyBitmap);
                    DealBytes();
                }
                DrawCursor();//画游标
                DrawImage();
            }
        });
    }

    /**
     * 图片转数组
     * @param bitmap 图片
     */
    private void BitmapToBytes(Bitmap bitmap){
        srcGreyBitmap=bitmap;
        bytesGreyImage =new byte[300*300];
        bytesBinaryImage =new byte[300*300];
        int bytes = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);
        bytesGreyImage = buffer.array();
        if(reverseFlag){
            for (int i = 0; i < bytesGreyImage.length; i++) {
                bytesGreyImage[i]=(byte) (~bytesGreyImage[i]);
            }
            DealBytes();
            DrawImage();
        }
    }
    private void ButtonInit(View view){
        //宽
        fragment_grey_width_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(RegexUtil.isNum(fragment_grey_width_et.getText().toString())){
                    greyRxWidth =Integer.parseInt(fragment_grey_width_et.getText().toString());
                }else {
                    Toast.makeText(getActivity(),"输入不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //高
        fragment_grey_height_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(RegexUtil.isNum(fragment_grey_height_et.getText().toString())){
                    greyRxHeight =Integer.parseInt(fragment_grey_height_et.getText().toString());
                }else {
                    Toast.makeText(getActivity(),"输入不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //开始停止接收
        fragment_grey_start_tb = view.findViewById(R.id.fragment_grey_start_tb);
        fragment_grey_start_tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //设置宽高不可调
                    fragment_grey_start_tb.setTextColor(Color.RED);
                    fragment_grey_width_et.setEnabled(false);
                    fragment_grey_width_et.setTextColor(Color.GRAY);
                    fragment_grey_height_et.setEnabled(false);
                    fragment_grey_height_et.setTextColor(Color.GRAY);
                    //宽高改变
                    if(greyRxWidth !=srcGreyBitmap.getWidth()|| greyRxHeight !=srcGreyBitmap.getHeight()){
                        if(greyRxWidth ==0){
                            greyRxWidth =8;
                        }
                        if(greyRxHeight ==0){
                            greyRxHeight =8;
                        }
                        putData();
                        bytesGreyImage =new byte[greyRxWidth * greyRxHeight];
                        for (int i = 0; i < bytesGreyImage.length; i++) {
                            bytesGreyImage[i]=(byte)0XFF;
                        }
                        bytesBinaryImage =new byte[greyRxWidth * greyRxHeight];
                        srcGreyBitmap = Bitmap.createBitmap(greyRxWidth, greyRxHeight, Bitmap.Config.ALPHA_8);
                        srcBinaryBitmap=Bitmap.createBitmap(greyRxWidth, greyRxHeight, Bitmap.Config.ALPHA_8);
                        scaleXY =1;
                        offsetX =(float) (bigIVWidth - greyRxWidth)/2;
                        offsetY =(float) (bigIVHeight - greyRxHeight)/2;
                        DrawCursor();
                        DealBytes();
                        DrawImage();
                    }
                    rxState =0;
                    rxNum =0;
                    fragment_grey_width_et.setText(greyRxWidth +"");//有时候可能没有输入
                    fragment_grey_height_et.setText(greyRxHeight +"");//有时候可能没有输入
                    //进度条
                    imageLength = bytesGreyImage.length; //图像长度
                    seekbarLength = imageLength /100;
                    fragment_grey_process_sb.setMax(imageLength);
                    fragment_grey_process_sb.setProgress(rxNum);
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
                    fragment_grey_start_tb.setTextColor(Color.BLACK);
                    fragment_grey_width_et.setEnabled(true);
                    fragment_grey_width_et.setTextColor(Color.BLACK);
                    fragment_grey_height_et.setEnabled(true);
                    fragment_grey_height_et.setTextColor(Color.BLACK);
                }
            }
        });

        //清除图片
        Button fragment_grey_clear_bt = view.findViewById(R.id.fragment_grey_clear_bt);
        fragment_grey_clear_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < bytesGreyImage.length; i++) {
                    bytesGreyImage[i]=(byte)0XFF;
                }
                DealBytes();
                DrawImage();
            }
        });
        //打开图片
        Button fragment_grey_open_bt = view.findViewById(R.id.fragment_grey_open_bt);
        fragment_grey_open_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragment_grey_start_tb.isChecked()) {
                    Toast.makeText(getActivity(), "正在接收，操作失败", Toast.LENGTH_SHORT).show();
                }else{
                    OpenFile();
                }
            }
        });
        //保存图片
        Button fragment_gery_save_bt = view.findViewById(R.id.fragment_grey_save_bt);
        fragment_gery_save_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveFile();
            }
        });
        //是否反白
        fragment_grey_light_sw = view.findViewById(R.id.fragment_grey_light_sw);
        fragment_grey_light_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //因为显示实质是数值越大越黑，所以这里与开关调换
                if(isChecked&& reverseFlag){
                    putData();
                    reverseFlag =false;
                    for (int i = 0; i < bytesGreyImage.length; i++) {
                        bytesGreyImage[i]=(byte) (~bytesGreyImage[i]);
                    }
                    DealBytes();
                    DrawImage();
                }else if(!isChecked&&!reverseFlag){
                    putData();
                    reverseFlag =true;
                    for (int i = 0; i < bytesGreyImage.length; i++) {
                        bytesGreyImage[i]=(byte) (~bytesGreyImage[i]);
                    }
                    DealBytes();
                    DrawImage();
                }
            }
        });
        //调整阈值进度条
        fragment_grey_adjust_sb = view.findViewById(R.id.fragment_grey_adjust_sb);
        fragment_grey_adjust_sb.setMax(255);
        fragment_grey_adjust_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(buttonChecked!=-1&&fromUser){
                    buttons.get(buttonChecked).setText(Integer.toString(seekBar.getProgress()));
                    greyThresholds[buttonChecked] = seekBar.getProgress();
                    DealBytes();
                    DrawImage();
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
        ImageButton fragment_grey_add_ib = view.findViewById(R.id.fragment_grey_add_ib);
        fragment_grey_add_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked !=-1) {
                    if (greyThresholds[buttonChecked] < 255) {
                        greyThresholds[buttonChecked]++;
                        buttons.get(buttonChecked).setText(greyThresholds[buttonChecked] + "");
                        fragment_grey_adjust_sb.setProgress(greyThresholds[buttonChecked]);
                        DealBytes();
                        DrawImage();
                    }
                }
            }
        });
        //阈值调整减
        ImageButton fragment_grey_remove_ib = view.findViewById(R.id.fragment_grey_remove_ib);
        fragment_grey_remove_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked !=-1) {
                    if (greyThresholds[buttonChecked]>0) {
                        greyThresholds[buttonChecked]--;
                        buttons.get(buttonChecked).setText(greyThresholds[buttonChecked] + "");
                        fragment_grey_adjust_sb.setProgress(greyThresholds[buttonChecked]);
                        DealBytes();
                        DrawImage();
                    }
                }
            }
        });
        //上限增
        Button fragment_grey_addH_bt = view.findViewById(R.id.fragment_grey_addH_bt);
        fragment_grey_addH_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (greyThresholds[0]<255) {
                    greyThresholds[0]++;
                    buttons.get(0).setText(greyThresholds[0] + "");
                    if(buttonChecked ==0) {
                        fragment_grey_adjust_sb.setProgress(greyThresholds[0]);
                    }
                    DealBytes();
                    DrawImage();
                }
            }
        });
        //上限减
        Button fragment_grey_removeH_bt = view.findViewById(R.id.fragment_grey_removeH_bt);
        fragment_grey_removeH_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (greyThresholds[0]>0) {
                    greyThresholds[0]--;
                    buttons.get(0).setText(greyThresholds[0] + "");
                    if(buttonChecked ==0) {
                        fragment_grey_adjust_sb.setProgress(greyThresholds[0]);
                    }
                    DealBytes();
                    DrawImage();
                }
            }
        });
        //下线增
        Button fragment_grey_addL_bt = view.findViewById(R.id.fragment_grey_addL_bt);
        fragment_grey_addL_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (greyThresholds[1]<255) {
                    greyThresholds[1]++;
                    buttons.get(1).setText(greyThresholds[1] + "");
                    if(buttonChecked ==1) {
                        fragment_grey_adjust_sb.setProgress(greyThresholds[1]);
                    }
                    DealBytes();
                    DrawImage();
                }
            }
        });
        //下限减
        Button fragment_grey_removeL_bt = view.findViewById(R.id.fragment_grey_removeL_bt);
        fragment_grey_removeL_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (greyThresholds[1]>0) {
                    greyThresholds[1]--;
                    buttons.get(1).setText(greyThresholds[1] + "");
                    if(buttonChecked ==1) {
                        fragment_grey_adjust_sb.setProgress(greyThresholds[1]);
                    }
                    DealBytes();
                    DrawImage();
                }
            }
        });
        //阈值上限
        Button fragment_grey_h_bt = view.findViewById(R.id.fragment_grey_H_bt);
        buttons.add(fragment_grey_h_bt);
        fragment_grey_h_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==0){ //已经按下此按钮
                    RestButton(0);
                }else{     //没有按下此按钮
                    SetSeekBar(0);
                }
            }
        });
        //阈值下限
        Button fragment_grey_l_bt = view.findViewById(R.id.fragment_grey_L_bt);
        buttons.add(fragment_grey_l_bt);
        fragment_grey_l_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonChecked ==1){ //已经按下此按钮
                    RestButton(1);
                }else{      //没有按下此按钮
                    SetSeekBar(1);
                }
            }
        });
    }
    private void RestButton(int num){
        buttons.get(num).setBackgroundColor(Color.parseColor("#FFFFFF"));//抬起这个按钮
        fragment_grey_adjust_ll.setVisibility(View.GONE); //进度条不可见
        buttonChecked =-1; //-1意思没有阈值在调整
    }
    private void SetSeekBar(int num){
        if(buttonChecked !=-1){
            buttons.get(buttonChecked).setBackgroundColor(Color.parseColor("#FFFFFF"));//抬起上一个按钮
        }
        //设置此按钮
        buttonChecked =num;
        buttons.get(buttonChecked).setBackgroundColor(Color.parseColor("#BBBBBB"));//按下这个按钮
        //SeekBar进度
        fragment_grey_adjust_sb.setProgress(Integer.parseInt(buttons.get(buttonChecked).getText().toString()));
        fragment_grey_adjust_ll.setVisibility(View.VISIBLE);
    }

    /**
     * 显示信息
     */
    private void DrawInform(){
        int width=srcGreyBitmap.getWidth();
        int height=srcGreyBitmap.getHeight();
        int cx,cy,grey;
        String string;
        cx=(int)((cursorX - offsetX)/ scaleXY);
        cy=(int)((cursorY - offsetY)/ scaleXY);
        if((cursorX - offsetX)<0||cx>=width||(cursorY - offsetY)<0||cy>=height){
            string="X:- Y:-\r\nG:-";
        }else{
            if(!reverseFlag){ //反向未开
                grey=0XFF& bytesGreyImage[width*cy+cx];
            }else{ //开了反向
                grey=0XFF&~bytesGreyImage[width*cy+cx];
            }
            string="X:"+cx+" Y:"+cy+"\r\n"+"G:"+grey;
        }
        fragment_grey_inform_tv.setText(string);
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
            for (float i = 0; i <=grid_width ; i+= scaleXY) {
                canvas.drawLine(i+grid_offsetX,0,i+grid_offsetX, bigIVHeight, paintGrid);//X游标
            }
            for (float i = 0; i <=grid_height; i+= scaleXY) {
                canvas.drawLine(0,i+grid_offsetY, bigIVWidth,i+grid_offsetY, paintGrid); //Y游标
            }
        }
        canvas.drawLine(cursorX,0, cursorX, bigIVHeight,paint);//X游标
        canvas.drawLine(0, cursorY, bigIVWidth, cursorY,paint); //Y游标
        fragment_grey_cursor_big_iv.setImageBitmap(Cursor_Bitmap);//添加到前景
    }

    /**
     * 小图游标
     */
    private void DrawSmallCursor() {
        int width=srcGreyBitmap.getWidth();
        int height=srcGreyBitmap.getHeight();
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
        fragment_grey_cursor_small_iv.setImageBitmap(Cursor_Bitmap);
    }


    /**
     * @功能 将灰度图片数组转为图片
     * @调用 在灰度图片刷新后调用
     */
    private void DealGreyBytes(){
        ByteBuffer buffer = ByteBuffer.wrap(bytesGreyImage);
        srcGreyBitmap.copyPixelsFromBuffer(buffer);
    }

    /**
     * @功能 二值化数组转为图片
     * @调用 阈值与灰度图片刷新后调用
     */
    private void DealBinaryBytes(){
        int len= bytesGreyImage.length;
        int grey;
        for (int i = 0; i <len; i++) {
            if(!reverseFlag){
                grey=0XFF& bytesGreyImage[i];
            }else{
                grey=0XFF&~bytesGreyImage[i];
            }
            if((grey<= greyThresholds[0])&&(grey>= greyThresholds[1])){
                bytesBinaryImage[i]=(byte)0X11;//白
            }else{
                bytesBinaryImage[i]=(byte)0XFF;//黑
            }
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytesBinaryImage);
        srcBinaryBitmap.copyPixelsFromBuffer(buffer);
    }

    /**
     * 显示大图
     * @param bitmap 图片
     */
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
        fragment_grey_big_iv.setImageMatrix(matrix);
        fragment_grey_big_iv.setImageBitmap(scaledBitmap);
    }

    /**
     * 显示小图片
     * @param bitmap 图片
     */
    private void DrawSmallImage(Bitmap bitmap){
        Bitmap scaledBitmap;
        float scale;
        float dx,dy;
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();

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
        fragment_grey_small_iv.setImageMatrix(matrix);
        fragment_grey_small_iv.setImageBitmap(scaledBitmap);
    }
    /**
     * 数组解析
     */
    private void DealBytes(){
        DealGreyBytes();
        DealBinaryBytes();
        DrawInform();
    }

    /**
     * 显示游标及信息
     */
    private void DrawCursor(){
        DrawBigCursor();
        DrawSmallCursor();
        DrawInform();
    }

    /**
     * 显示图片
     */
    private void DrawImage(){
        if(!changeFlag){
            DrawBigImage(srcGreyBitmap);
            DrawSmallImage(srcBinaryBitmap);
        }else {
            DrawBigImage(srcBinaryBitmap);
            DrawSmallImage(srcGreyBitmap);
        }
    }
    private float DownXed,DownYed,DownXYed,DownXYing,Y_Center,X_Center;
    @SuppressLint("ClickableViewAccessibility")
    private void SetGesture() {
        fragment_grey_cursor_big_iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (Math.abs(event.getX() - cursorX) < 50 && ((bigIVHeight - event.getY()) < 100||event.getY()<100)) {
                            touchEvent = 1;      //X方向游标
                            DownXed = event.getX();
                        } else if (Math.abs(event.getY() - cursorY) < 50 && ((bigIVWidth - event.getX()) < 100||event.getX()<100)) {
                            touchEvent = 2;      //Y方向游标
                            DownYed = event.getY();
                        } else {
                            touchEvent = 3;//平移事件
                            DownXed = event.getX();
                            DownYed = event.getY();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        touchEvent = 4; //缩放事件
                        //计算起始两指距离
                        DownXYed = (event.getX(0) - event.getX(1)) * (event.getX(0) - event.getX(1))
                                + (event.getY(0) - event.getY(1)) * (event.getY(0) - event.getY(1));
                        DownXYed = (float) Math.sqrt((double) DownXYed);
                        //二值中心
                        Y_Center = (event.getY(0) + event.getY(1)) / 2;
                        X_Center = (event.getX(0) + event.getX(1)) / 2;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (touchEvent == 1) {
                            cursorX = event.getX();
                            if (cursorX < 0) {
                                cursorX = 0;
                            } else if (cursorX > bigIVWidth) {
                                cursorX = bigIVWidth;
                            }
                            DrawCursor();
                        } else if (touchEvent == 2) {
                            cursorY = event.getY();
                            if (cursorY < 0) {
                                cursorY = 0;
                            } else if (cursorY > bigIVHeight) {
                                cursorY = bigIVHeight;
                            }
                            DrawCursor();
                        } else if (touchEvent == 3) {
                            offsetX += event.getX() - DownXed;
                            offsetY += event.getY() - DownYed;
                            DownXed = event.getX();
                            DownYed = event.getY();
                            DrawImage();
                            DrawCursor();
                        } else if (touchEvent == 4) {
                            DownXYing = (event.getX(0) - event.getX(1)) * (event.getX(0) - event.getX(1))
                                    + (event.getY(0) - event.getY(1)) * (event.getY(0) - event.getY(1));
                            DownXYing = (float) Math.sqrt(DownXYing);
                            //以两指为中心放大算法
                            float tempOX = X_Center - (X_Center - offsetX) * DownXYing / DownXYed;
                            float tempOY = Y_Center - (Y_Center - offsetY) * DownXYing / DownXYed;
                            float tempXY = (scaleXY * DownXYing / DownXYed);       //X方向比例
                            if (tempXY < 200 && tempXY > 0.1&&(greyRxWidth *tempXY>1)&&(greyRxHeight *tempXY>1)) {
                                scaleXY = tempXY;
                                offsetX = tempOX;
                                offsetY = tempOY;
                                DownXYed = DownXYing;
                                DrawImage();
                                DrawCursor();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        touchEvent = 0;  //不写就会出现程序卡死
                        break;
                }
                return true;
            }
        });
        fragment_grey_cursor_small_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFlag =!changeFlag;
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
            for (int i = 0; i < bytes.length; i++) {
                if(rxState ==4){
                    if(!reverseFlag) {   //开了反白
                        bytesGreyImage[rxNum] = bytes[i];
                    }else{      //未开反白
                        bytesGreyImage[rxNum] = (byte)~bytes[i];
                    }
                    rxNum++;
                    if(rxNum % seekbarLength ==0){
                        fragment_grey_process_sb.setProgress(rxNum);
                    }
                    if(rxNum >= imageLength){
                        rxState =0;
                        rxNum =0;
                        DealBytes();
                        DrawImage();
                    }
                }else if(rxState ==3){
                    if (bytes[i] == 0x07){
                        rxState = 4;
                        rxNum =0;
                    }
                    else {
                        if (bytes[i] == 0x01) {
                            rxState = 1;
                        }else {
                            rxState =0;
                        }
                    }
                }else if(rxState ==2){
                    if (bytes[i] == 0x08) {
                        rxState = 3;
                    }else {
                        if (bytes[i] == 0x01) {
                            rxState = 1;
                        }else {
                            rxState =0;
                        }
                    }
                }else if(rxState ==1){
                    if (bytes[i] == 0x09) {
                        rxState = 2;
                    } else {
                        if (bytes[i] == 0x01) {
                            rxState = 1;
                        }else {
                            rxState =0;
                        }
                    }
                }else if(rxState ==0){
                    if (bytes[i] == 0x01) {
                        rxState = 1;
                    }
                }
            }
        }
    }

    /**
     * 保存数据到txt文件
     */
    private void SaveFile(){
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE,"图像.grey");
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

    private void WriteFile(Uri uri){
        try{
            OutputStream outputStream =requireActivity().getContentResolver().openOutputStream(uri);
            DecimalFormat decimalFormat = new DecimalFormat("000");
            String s=decimalFormat.format(srcGreyBitmap.getWidth())+"#"+decimalFormat.format(srcGreyBitmap.getHeight())+"#";
            assert outputStream != null;
            outputStream.write(s.getBytes());
            outputStream.write(bytesGreyImage);
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
            byte[] buffer = new byte[inputStream.available()];//in.available() 表示要读取的文件中的数据长度
            inputStream.read(buffer);  //将文件中的数据读到buffer中
            inputStream.close();

            /*处理数据*/
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
            greyRxWidth =Integer.parseInt(string_w);
            greyRxHeight =Integer.parseInt(string_h);
            fragment_grey_width_et.setText(greyRxWidth +"");
            fragment_grey_height_et.setText(greyRxHeight +"");
            //显示图片
            bytesGreyImage =new byte[greyRxWidth * greyRxHeight];
            System.arraycopy(buffer,8, bytesGreyImage,0, bytesGreyImage.length);
            bytesBinaryImage =new byte[greyRxWidth * greyRxHeight];
            srcGreyBitmap = Bitmap.createBitmap(greyRxWidth, greyRxHeight, Bitmap.Config.ALPHA_8);
            srcBinaryBitmap=Bitmap.createBitmap(greyRxWidth, greyRxHeight, Bitmap.Config.ALPHA_8);
            scaleXY =1;
            offsetX =(bigIVWidth - greyRxWidth)/2;
            offsetY =(bigIVHeight - greyRxHeight)/2;
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

    private void putData(){
        cameraActivity.sp.putInt("Gray_greyRxWidth", greyRxWidth);
        cameraActivity.sp.putInt("Gray_greyRxHeight", greyRxHeight);
        cameraActivity.sp.putInt("Gray_greyThresholds[0]", greyThresholds[0]);
        cameraActivity.sp.putInt("Gray_greyThresholds[1]", greyThresholds[1]);
        cameraActivity.sp.putBoolean("Gray_reverseFlag", reverseFlag);
    }
    private void getData(){
        greyRxWidth =cameraActivity.sp.getInt("Gray_greyRxWidth",300);
        fragment_grey_width_et.setText(greyRxWidth +"");
        greyRxHeight =cameraActivity.sp.getInt("Gray_greyRxHeight",300);
        fragment_grey_height_et.setText(greyRxHeight +"");
        greyThresholds[0]=cameraActivity.sp.getInt("Gray_greyThresholds[0]",0);
        greyThresholds[1]=cameraActivity.sp.getInt("Gray_greyThresholds[1]",0);
        //刷新阈值
        for (int i = 0; i < 2; i++) {
            buttons.get(i).setText(greyThresholds[i]+"");
        }
        reverseFlag =cameraActivity.sp.getBoolean("Gray_reverseFlag",true);
        fragment_grey_light_sw.setChecked(!reverseFlag);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(myServiceConnection);
        if(fragment_grey_start_tb.isChecked()){
            getActivity().unregisterReceiver(mContentReceiver);
        }
        putData();
    }
}
