package com.example.dzxh_app.Activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.dzxh_app.R;
import com.example.dzxh_app.api.CommunicationIService;
import com.example.dzxh_app.api.CommunicationService;
import com.example.dzxh_app.view.MySelectDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DrawBoardActivity extends BaseActivity {

    private ImageView draw_iv;
    private LinearLayout draw_pen_ll,draw_eraser_ll;
    private Bitmap draw_bitmap=null;
    private TextView draw_position_tv;
    private SeekBar draw_width_sb;
    private SeekBar draw_depth_sb;
    private SeekBar draw_eraser_sb;
    private ToggleButton draw_eraser_tb;
    private ToggleButton draw_pen_tb;
    private boolean eraserIsChecked;
    private final ArrayList<RadioButton> radioButtons=new ArrayList<>();

    private static final ArrayList<Paint> paints=new ArrayList<>();  //画笔配置
    private static final ArrayList<Path> paths=new ArrayList<>();  //储存路径
    private Paint paint;
    private Path path;
    private Paint paintEraser;
    private Path pathEraser;

    private int penWidth;
    private int penDepth;
    private int penColor;
    private int penColorNum;   //画笔色号
    private static int replyNum; //撤回步数

    private int eraserWidth;
    //服务
    private CommunicationIService mCommunicationIService;
    private MyServiceConnection myServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawboard);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);          //去除状态栏
        ServiceInit();
        NumInit();
        ViewInit();
        ButtonInit();
        SetGesture();
        getData();
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
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    /**
     * 数据初始化
     */
    private void NumInit(){
        //橡皮擦设置（圆环）
        pathEraser =new Path();
        paintEraser =new Paint();
        paintEraser.setColor(0X99000000);
        paintEraser.setStrokeWidth(2);
        paintEraser.setStyle(Paint.Style.STROKE);
        paintEraser.setAntiAlias(true);
    }
    /**
     * 视图初始化
     */
    private void ViewInit(){
        draw_pen_ll = findViewById(R.id.draw_pen_ll);
        draw_pen_ll.setVisibility(View.GONE);

        draw_eraser_ll = findViewById(R.id.draw_eraser_ll);
        draw_eraser_ll.setVisibility(View.GONE);

        draw_iv = findViewById(R.id.draw_iv);

        draw_position_tv = findViewById(R.id.draw_position_tv);
        //创建Radio组
        RadioButton draw_color0_rb = findViewById(R.id.draw_color0_rb);
        radioButtons.add(draw_color0_rb);
        RadioButton draw_color1_rb = findViewById(R.id.draw_color1_rb);
        radioButtons.add(draw_color1_rb);
        RadioButton draw_color2_rb = findViewById(R.id.draw_color2_rb);
        radioButtons.add(draw_color2_rb);
        RadioButton draw_color3_rb = findViewById(R.id.draw_color3_rb);
        radioButtons.add(draw_color3_rb);
        RadioButton draw_color4_rb = findViewById(R.id.draw_color4_rb);
        radioButtons.add(draw_color4_rb);
        RadioButton draw_color5_rb = findViewById(R.id.draw_color5_rb);
        radioButtons.add(draw_color5_rb);
        RadioButton draw_color6_rb = findViewById(R.id.draw_color6_rb);
        radioButtons.add(draw_color6_rb);
        RadioButton draw_color7_rb = findViewById(R.id.draw_color7_rb);
        radioButtons.add(draw_color7_rb);
    }
    /**
     * 按键初始化
     */
    private void ButtonInit(){
        draw_pen_tb = findViewById(R.id.draw_pen_tb);
        draw_pen_tb.setChecked(true);
        draw_pen_tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraserIsChecked=false;
                draw();
                draw_pen_tb.setChecked(true);
                draw_eraser_tb.setChecked(false);
                if(draw_eraser_ll.getVisibility()==View.VISIBLE){
                    draw_eraser_ll.setVisibility(View.GONE);
                }
                if(draw_pen_ll.getVisibility()==View.GONE){
                    draw_pen_ll.setVisibility(View.VISIBLE);
                }else{
                    draw_pen_ll.setVisibility(View.GONE);
                }
            }
        });

        draw_eraser_tb = findViewById(R.id.draw_eraser_tb);
        draw_eraser_tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraserIsChecked=true;
                draw_eraser_tb.setChecked(true);
                draw_pen_tb.setChecked(false);
                if(draw_pen_ll.getVisibility()==View.VISIBLE){
                    draw_pen_ll.setVisibility(View.GONE);
                }
                if(draw_eraser_ll.getVisibility()==View.GONE){
                    draw_eraser_ll.setVisibility(View.VISIBLE);
                }else{
                    draw_eraser_ll.setVisibility(View.GONE);
                }
            }
        });

        Button drawing_bt = findViewById(R.id.drawing_bt);
        drawing_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBackDialog();
            }
        });
        ImageButton draw_reply_bt = findViewById(R.id.draw_reply_bt);
        draw_reply_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(replyNum <paths.size()) replyNum++;
                draw();
            }
        });
        ImageButton draw_reply_reply_bt = findViewById(R.id.draw_reply_reply_bt);
        draw_reply_reply_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(replyNum >0) replyNum--;
                draw();
            }
        });
        draw_width_sb = findViewById(R.id.draw_width_sb);
        draw_width_sb.setMax(100);
        draw_width_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                penWidth =seekBar.getProgress();
                putData();
            }
        });
        draw_depth_sb = findViewById(R.id.draw_depth_sb);
        draw_depth_sb.setMax(255);
        draw_depth_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                penDepth =seekBar.getProgress();
                setPenColor(penColorNum);
            }
        });

        draw_eraser_sb = findViewById(R.id.draw_eraser_sb);
        draw_eraser_sb.setMax(100);
        draw_eraser_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                eraserWidth =seekBar.getProgress();
                putData();
            }
        });



        RadioGroup draw_pencil_rg = findViewById(R.id.draw_pencil_rg);
        draw_pencil_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.draw_color0_rb){
                    setPenColor(0);
                }else if(checkedId==R.id.draw_color1_rb){
                    setPenColor(1);
                } else if(checkedId==R.id.draw_color2_rb){
                    setPenColor(2);
                }else if(checkedId==R.id.draw_color3_rb){
                    setPenColor(3);
                }else if(checkedId==R.id.draw_color4_rb){
                    setPenColor(4);
                }else if(checkedId==R.id.draw_color5_rb){
                    setPenColor(5);
                }else if(checkedId==R.id.draw_color6_rb){
                    setPenColor(6);
                }else if(checkedId==R.id.draw_color7_rb){
                    setPenColor(7);
                }
            }
        });
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        int width=draw_iv.getWidth();
        int height=draw_iv.getHeight();
        draw_bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        draw();
        super.onWindowFocusChanged(hasFocus);
    }
    /**
     * 退出提示
     */
    private void ShowBackDialog(){
        MySelectDialog mySelectDialog=new MySelectDialog(this);
        mySelectDialog.show();
        mySelectDialog.setButtonText("取消","确定");
        mySelectDialog.setText("确定要清空画板吗？");
        mySelectDialog.setOnDialogClickListener(new MySelectDialog.OnDialogClickListener() {
            @Override
            public void OnClick(View view) {
                if(view.getId()==R.id.layout_dialog_select_yes_bt){
                    clear();
                }
            }
        });
    }
    /**
     * 设置画笔颜色
     */
    private void setPenColor(int num){
        switch (num){
            case 0:
                penColor =0x00000000;//黑
                break;
            case 1:
                penColor = 0x00FF0000;//红
                break;
            case 2:
                penColor =0x0000FF00;//绿
                break;
            case 3:
                penColor =0x000000FF;//蓝
                break;
            case 4:
                penColor =0x00FFFF00;//黄
                break;
            case 5:
                penColor =0x00FF00FF;//紫
                break;
            case 6:
                penColor =0x0000FFFF;//青
                break;
            case 7:
                penColor =0xFFFFEFDB;    //背景
                break;
        }
        if(num!=7){
            penColor |= penDepth <<24;
        }
        penColorNum =num;
        putData();
    }
    /**
     * 画轨迹
     */
    private void draw(){
        Canvas canvas = new Canvas(draw_bitmap);
        canvas.drawColor(0xFFFFEFDB);
        for(int i = 0; i<paths.size()- replyNum; i++){
            canvas.drawPath(paths.get(i),paints.get(i));
        }
        if(eraserIsChecked){
            canvas.drawPath(pathEraser, paintEraser);
        }
        draw_iv.setImageBitmap(draw_bitmap);
    }
    /**
     * 清除画板
     */
    public void clear(){
        paths.clear();
        paints.clear();
        replyNum =0;
        draw();
    }
    /**
     * 手势按下，消除之前撤回的轨迹
     */
    public void remove(){
        for (int i = 0; i < replyNum; i++) {
            paths.remove(paths.size()-1);
            paints.remove(paints.size()-1);
        }
        replyNum =0;
    }

    /**
     * 画橡皮
     * @param x
     * @param y
     */
    private void draw_eraser(float x,float y){
        pathEraser.reset();    //清除线条
        pathEraser.addCircle(x,y,eraserWidth/2, Path.Direction.CCW);
    }

    /**
     * 发送数据
     * @param x
     * @param y
     */
    private void SendByte(int x,int y){
        //x,y限幅不小于0
        if(x<0){
            x=0;
        }
        if(y<0){
            y=0;
        }
        draw_position_tv.setText(" X:"+x+"\r\n"+" Y:"+y);
        DecimalFormat decimalFormat =new DecimalFormat("0000");
        String string=decimalFormat.format(x)+decimalFormat.format(y)+"\r\n";
        mCommunicationIService.callWrite(string.getBytes());
    }
    /**
     * 手势操作
     */
    boolean move_flag=false;
    private void SetGesture(){
        draw_iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(draw_eraser_ll.getVisibility()==View.VISIBLE){
                            draw_eraser_ll.setVisibility(View.GONE);
                        }
                        if(draw_pen_ll.getVisibility()==View.VISIBLE){
                            draw_pen_ll.setVisibility(View.GONE);
                        }
                        if(replyNum !=0){
                            remove();
                        }
                        paint=new Paint();
                        path=new Path();
                        paints.add(paint);
                        paths.add(path);
                        paint.setAntiAlias(true);   //抗锯齿
                        paint.setStyle(Paint.Style.STROKE);
                        if(eraserIsChecked){
                            paint.setColor(0xFFFFEFDB);
                            paint.setStrokeWidth(eraserWidth);
                        }else{
                            paint.setColor(penColor);
                            paint.setStrokeWidth(penWidth);
                        }
                        path.moveTo(event.getX(),event.getY());
                        draw();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        move_flag=true;
                        path.lineTo(event.getX(),event.getY());
                        if(eraserIsChecked) {
                            draw_eraser(event.getX(), event.getY());
                        }else{
                            SendByte((int)event.getX(),(int)event.getY());
                        }
                        draw();
                        break;
                        case MotionEvent.ACTION_UP:
                            //只有点击动作时，原地画点
                            if(!eraserIsChecked&&!move_flag){
                                path.lineTo(event.getX()+ penWidth /2,event.getY());
                                path.lineTo(event.getX()- penWidth /2,event.getY());
                                SendByte((int)event.getX(),(int)event.getY());
                                draw();
                            }
                            move_flag=false;
                            break;
                }
                return true;
            }
        });
    }

    /**
     * 保存设置数据
     */
    private void putData(){
        sp.putInt("Draw_penColorNum", penColorNum);
        sp.putInt("Draw_penWidth", penWidth);
        sp.putInt("Draw_penDepth", penDepth);
        sp.putInt("Draw_eraserWidth", eraserWidth);

    }
    /**
     * 获取设置数据
     */
    private void getData(){
        penColorNum =sp.getInt("Draw_penColorNum",0);
        penWidth =sp.getInt("Draw_penWidth",10);
        penDepth =sp.getInt("Draw_penDepth",145);
        eraserWidth =sp.getInt("Draw_eraserWidth",50);
        setPenColor(penColorNum);
        radioButtons.get(penColorNum).setChecked(true);
        draw_width_sb.setProgress(penWidth);
        draw_depth_sb.setProgress(penDepth);
        draw_eraser_sb.setProgress(eraserWidth);
    }

    @Override
    protected void onDestroy() {
        putData();
        unbindService(myServiceConnection);
        super.onDestroy();
    }
}
