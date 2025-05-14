package com.example.dzxh_app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;


public class GPSBoard extends SurfaceView implements SurfaceHolder.Callback,View.OnTouchListener {

    private Path pant_foreground;
    private Path pant_background;
    private Path pant_my;

    private Paint paint_foreground;
    private Paint paint_background;
    private Paint paint_my;
    private Paint paint_sweep;
    private Paint paint_text;


    private int mSweepColor;

    private int X_center;
    private int Y_center;
    private float Radius;


    private int Yaw_angle;

    private Timer timer;

    private Path pant_target;
    private Paint paint_target;

    private double Scale;
    private float target_x;
    private float target_y;

    private boolean Refresh_Flag;

    private double data_scale;//数据
    public GPSBoard(Context context, AttributeSet attrs){
        super(context, attrs);
        getHolder().addCallback(this);
        setOnTouchListener(this);//手势
        setZOrderOnTop(true);//将控件置于所有View上方,不置不透明
        getHolder().setFormat(PixelFormat.TRANSLUCENT);  //设置透明
        NumInit();
        PathInit();
    }
    private void NumInit(){
        timer = new Timer();
        mSweepColor= Color.WHITE;
        Scale=1;
        Zoom=1;
        Mul=0;
    }
    private void PathInit(){
        paint_sweep=new Paint();
        paint_sweep.setStyle(Paint.Style.FILL);
        paint_sweep.setAntiAlias(true);

        pant_foreground = new Path();
        paint_foreground = new Paint();
        paint_foreground.setColor(Color.argb(150,255,255,255));
        paint_foreground.setStrokeWidth(20);
        paint_foreground.setStyle(Paint.Style.STROKE);
        paint_foreground.setPathEffect(new DashPathEffect(new float[] {10, 10}, 1));
        paint_foreground.setAntiAlias(true);

        pant_background = new Path();
        paint_background = new Paint();
        paint_background.setColor(Color.argb(200,255,255,255));
        paint_background.setStrokeWidth(2);
        paint_background.setStyle(Paint.Style.STROKE);
        paint_background.setAntiAlias(true);

        pant_my = new Path();
        paint_my = new Paint();
        paint_my.setColor(Color.argb(200,0,0,200));
        paint_my.setStyle(Paint.Style.FILL);
        paint_my.setAntiAlias(true);

        pant_target = new Path();
        paint_target = new Paint();
        paint_target.setColor(Color.argb(200,255,255,255));
        paint_target.setStyle(Paint.Style.FILL);
        paint_target.setAntiAlias(true);

        paint_text = new Paint();
        paint_text.setColor(Color.YELLOW);
        paint_text.setStyle(Paint.Style.FILL);
        paint_text.setAntiAlias(true);     //去除锯齿
    }
    private void draw(int ang) {
        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        canvas.drawPath(pant_background, paint_background);
        canvas.drawPath(pant_foreground, paint_foreground);
        canvas.drawText("x:"+(int)target_x,20,Y_center-Radius-(float)X_center/12,paint_text);
        canvas.drawText("y:"+(int)target_y,20,Y_center-Radius,paint_text);
        canvas.drawText("s:"+data_scale,20,Y_center-Radius+(float) X_center/12,paint_text);
        canvas.rotate(-Yaw_angle,X_center,Y_center);
        canvas.drawPath(pant_my, paint_my);
        canvas.drawPath(pant_target,paint_target);
        canvas.rotate(Yaw_angle,X_center,Y_center);
        canvas.rotate(ang,X_center,Y_center);
        canvas.drawCircle(X_center,Y_center,Radius,paint_sweep);
        getHolder().unlockCanvasAndPost(canvas);
    }
    int num1 = 0,num2=0; //扫描角度 ，原点明暗值
    boolean dir; //原点亮暗方向
    private void Refresh_View(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                num1+=5;
                if(num1>=360) num1=0;
                if(!dir){
                    num2+=5;
                    if(num2>=255){
                        dir=true;
                    }
                }else {
                    num2-=15;
                    if(num2<=50){
                        dir=false;
                    }
                }
                paint_target.setColor(Color.argb(num2,255,255,0));
                try {
                    draw(num1);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },25,25);
    }

    private int Mul;  //标记刻度
    private double Zoom;
    private float R_div,R_div0;
    private void SetZoom(){
        if(Mul%3==0) {
            Zoom =Math.pow(10,(double)((int)(Mul/3)));
        }else if(Mul%3==1) {
            Zoom =2*Math.pow(10,(double)((int)(Mul/3)));
        }else if(Mul%3==2) {
            Zoom =5*Math.pow(10,(double)((int)(Mul/3)));
        }else if(Mul%3==-1) {
            Zoom =0.5*Math.pow(10,(double)((int)(Mul/3)));
        }else if(Mul%3==-2) {
            Zoom =0.2*Math.pow(10,(double)((int)(Mul/3)));
        }
        R_div = (float) Scale * R_div0 /(float) Zoom;
    }
    private void DrawWall(){
        R_div=(float)Scale*R_div0/(float)Zoom;
        if(R_div>2*R_div0){
            if(Mul%3!=1&&Mul%3!=-2){
                Mul++;
                SetZoom();
            }
        }
        if(R_div>2.5*R_div0) {
            Mul++;
            SetZoom();
        }
        if(R_div<R_div0){
            Mul--;
            SetZoom();
        }
        if(Mul%3==0){
            if(Mul>0) {
                data_scale =Math.pow(10, Mul/3);
            }else{
                data_scale =Math.pow(10, -Mul /3);
            }
        }else if(Mul%3==1){
            data_scale=2*Math.pow(10,(Mul-1)/3);
        }else if(Mul%3==-1){
            data_scale=2*Math.pow(10,-(Mul+1)/3);
        }else if(Mul%3==2){
            data_scale=5*Math.pow(10,(Mul-2)/3);
        }else if(Mul%3==-2){
            data_scale=5*Math.pow(10,-(Mul+2)/3);
        }
        pant_background.reset();
        for (int i = 1; i <=5; i++) {
            if(R_div*i<=Radius){
                pant_background.addCircle(X_center,Y_center,R_div*i, Path.Direction.CCW);
            }else {
                break;
            }
        }
        pant_background.addCircle(X_center,Y_center,Radius, Path.Direction.CCW);
        setTarget_XY(target_x,target_y);
    }
    /**
     * 改变颜色的透明度
     *
     * @param color
     * @param alpha
     * @return
     */
    private static int changeAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
    public void setStart(boolean b){
        Refresh_Flag=b;
        if(b){
            Refresh_View();
        }else {
            timer.cancel();
        }
    }

    public void setYaw_angle(int ang){
        Yaw_angle=ang;
    }

    public void setTarget_XY(float x,float y){
        float temp_x=X_center+(float)(x*Scale*R_div0);
        float temp_y=Y_center-(float)(y*Scale*R_div0);
        pant_target.reset();
        if(temp_x<10000&&temp_y<10000){
            pant_target.addCircle(temp_x,temp_y,20, Path.Direction.CCW);
        }
        target_x=x;
        target_y=y;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        X_center = width/2;
        Y_center = height/2;
        if(X_center<Y_center){
            Radius=width/10*4;
            R_div0 =Radius/5;
            paint_text.setTextSize(X_center/12);
        }else{
            Radius=height/10*4;
            R_div0 =Radius/5;
            paint_text.setTextSize(Y_center/12);
        }
        float r=3.14159f*(Radius+14)/180;
        paint_foreground.setPathEffect(new DashPathEffect(new float[] {r,r}, 1));

        SweepGradient sweepGradient = new SweepGradient(X_center, Y_center,
                new int[]{Color.TRANSPARENT, changeAlpha(mSweepColor, 0), changeAlpha(mSweepColor, 98),
                        changeAlpha(mSweepColor, 255), changeAlpha(mSweepColor, 255)
                }, new float[]{0.0f, 0.7f, 0.99f, 0.998f, 1f});
        paint_sweep.setShader(sweepGradient);
        pant_foreground.addCircle(X_center,Y_center,Radius+14, Path.Direction.CCW);

        pant_my.addCircle(X_center,Y_center,15, Path.Direction.CCW);
        DrawWall();
        draw(0);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        timer.cancel();
    }

    private float DownXYed,DownXYing;
    private int Two_flag;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()& MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Two_flag=1;
                DownXYed = (event.getX() - X_center) * (event.getX() - X_center)
                        + (event.getY() -Y_center) * (event.getY() - Y_center);
                DownXYed=(float) Math.sqrt((double) DownXYed);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Two_flag=2;
                DownXYed=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                        +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                DownXYed=(float) Math.sqrt((double) DownXYed);
                break;
            case MotionEvent.ACTION_MOVE:
                if(Two_flag==2) {
                    DownXYing = (event.getX(0) - event.getX(1)) * (event.getX(0) - event.getX(1))
                            + (event.getY(0) - event.getY(1)) * (event.getY(0) - event.getY(1));
                }else if(Two_flag==1){
                    DownXYing = (event.getX() - X_center) * (event.getX() - X_center)
                            + (event.getY() -Y_center) * (event.getY() - Y_center);
                }
                if(Two_flag!=0){
                    DownXYing=(float) Math.sqrt(DownXYing);
                    //放大
                    double temp =  ( Scale * DownXYing / DownXYed);       //X方向比例
                    //稍微限制一下缩放平移比例
                    if (temp <=2 && temp>0.000000001) {
                        Scale = temp;
                        DownXYed = DownXYing;
                        DrawWall();
                        if(!Refresh_Flag){
                            draw(num1);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Two_flag=0;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
}

