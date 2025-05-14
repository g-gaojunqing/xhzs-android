package com.example.dzxh_app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MySeekBar extends View {

    private int viewWidth, viewHeight;
    private boolean horizontalFlag;  //是否为水平方向
    private float backRadius;
    private float left,right,top,bottom;

    private int seekBarColor =0xFF6480FF;

    private Paint trackPaint,thumbPaint;
    private Paint clearPaint;
    private int max=100,progress;
    private float thumbProgress;

    private OnSeekBarChangeListener onSeekBarChangeListener;

    public MySeekBar(Context context){
        super(context);
        init();
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context,attrs);
        init();
    }
    private void init(){
        trackPaint=new Paint();
        trackPaint.setColor(0xFFBBBBBB);
        trackPaint.setStrokeWidth(8);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setAntiAlias(true);  //防锯齿

        thumbPaint=new Paint();
        thumbPaint.setColor(seekBarColor);
        thumbPaint.setAntiAlias(true);  //防锯齿

        //清除
        clearPaint = new Paint();
        clearPaint.setColor(0);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        setLayerType(LAYER_TYPE_HARDWARE,null);//硬件加速，清除不会变黑
    }

    public void setColor(int color){
        thumbPaint.setColor(color);
        invalidate();
    }
    public void setMax(int m){
        max=m;
        if(left!=0){
            thumbProgress=(float) progress/max*(right-left)+left;
            invalidate();
        }
    }
    public void setProgress(int p){
        progress=p;
        if(left!=0){
            if(progress<=max) {
                thumbProgress = (float) progress / max * (right - left) + left;
                invalidate();
            }
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectF;
        if(horizontalFlag){
            rectF=new RectF(left,top,right,bottom);
        }else{
            rectF=new RectF(top,left,bottom,right);
        }

        canvas.drawRoundRect(rectF,backRadius,backRadius, thumbPaint);
        if(horizontalFlag){
            canvas.drawRect(thumbProgress, top, right, bottom, clearPaint);
        }else{
            canvas.drawRect(top, left, bottom,viewWidth-thumbProgress , clearPaint);
        }

        canvas.drawRoundRect(rectF,backRadius,backRadius, trackPaint);

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //根据提供的测量值提取大小和模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //只考虑确定情况的值
        setMeasuredDimension(width, height);
        if(height>width){
            horizontalFlag=false;
            viewWidth=height;
            viewHeight=width;
        }else{
            horizontalFlag=true;
            viewWidth=width;
            viewHeight=height;
        }
        float stroke=viewHeight /16f;
        left= stroke;
        right= viewWidth-stroke;
        top= viewHeight *0.25f;
        bottom= viewHeight *0.75f;
        backRadius= viewHeight *0.25f;
        trackPaint.setStrokeWidth(stroke);
        thumbProgress=(float) progress/max*(right-left)+left;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.onSeekBarChangeListener =listener;
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(View v, int progress);
    }

    private void getProgress(float x){
        int tmpProgress=(int)((x-left)/(right-left)*max);
        if(tmpProgress<0) {
            tmpProgress =0;
        }else if(tmpProgress>max){
            tmpProgress =max;
        }
        if(tmpProgress!=progress){
            progress=tmpProgress;
            this.onSeekBarChangeListener.onProgressChanged(this,progress);
        }
    }

    private boolean dragFlag=false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float pro;
                if(horizontalFlag) {
                    pro=event.getX();
                }else{
                    pro=viewWidth-event.getY();
                }
                if(pro>left&&pro<right) {
                    thumbProgress =pro;
                    invalidate();
                    getProgress(thumbProgress);
                    dragFlag=true;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if(dragFlag){
                    if(horizontalFlag){
                        thumbProgress =event.getX();
                    }else{
                        thumbProgress =viewWidth-event.getY();
                    }
                    if(thumbProgress<left) {
                        thumbProgress =left;
                    }else if(thumbProgress>right){
                        thumbProgress =right;
                    }
                    invalidate();
                    getProgress(thumbProgress);
                }
                break;
            case MotionEvent.ACTION_UP:
                dragFlag=false;
                break;
        }
        return true;
    }
}
