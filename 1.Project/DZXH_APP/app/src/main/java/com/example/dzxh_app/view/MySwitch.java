package com.example.dzxh_app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MySwitch extends View {

    private int viewWidth, viewHeight;
    private boolean horizontalFlag;
    private float backRadius,foreRadius;
    private float xCenter,yCenter;

    private float switchWidth;
    private int switchColor =0xFF6480FF;

    private Paint trackPaint,thumbPaint;

    private boolean isChecked=false;
    private float refreshProgress;

    private OnCheckedChangeListener onCheckedChangeListener;

    public MySwitch(Context context){
        super(context);
        init();
    }

    public MySwitch(Context context, AttributeSet attrs) {
        super(context,attrs);
        init();
    }
    private void init(){
        trackPaint=new Paint();
        trackPaint.setAntiAlias(true);  //防锯齿

        thumbPaint=new Paint();
        thumbPaint.setAntiAlias(true);  //防锯齿
    }

    public void setColor(int color){
        switchColor=color;
        invalidate();
    }
    public void setChecked(boolean checked){
        isChecked=checked;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int color=0xBB+(int)((0xFF-0xBB)*refreshProgress);
        color=0xFF000000+(color<<16)|(color<<8)|color;
        trackPaint.setColor(color);
        RectF rectF=new RectF((viewWidth -switchWidth)/2,yCenter- backRadius,(viewWidth +switchWidth)/2,yCenter+backRadius);
        canvas.drawRoundRect(rectF,backRadius,backRadius, trackPaint);

        color=(switchColor &0x00FFFFFF)|(int)(((switchColor >>24)&0xFF)*refreshProgress)<<24;
        trackPaint.setColor(color);
        canvas.drawRoundRect(rectF,backRadius,backRadius, trackPaint);
        float thumb_x=(viewWidth -switchWidth)/2+backRadius+(switchWidth-backRadius*2)*refreshProgress;
        thumbPaint.setColor(Color.WHITE);
        canvas.drawCircle(thumb_x,yCenter,foreRadius,thumbPaint);
//        thumbPaint.setColor(switchColor);
//        canvas.drawCircle(thumb_x,yCenter, foreRadius*0.8f,thumbPaint);

        if(isChecked){
            if(refreshProgress<1){
                refreshProgress+=0.1f;
                invalidate();
            }else{
                refreshProgress=1;
            }
        }else{
            if(refreshProgress>0){
                refreshProgress-=0.1f;
                invalidate();
            }else{
                refreshProgress=0;
            }
        }
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
        setMeasuredDimension(viewWidth, viewHeight);
        if(height > width){
            horizontalFlag=false;
            viewWidth = width;
            viewHeight =height;
        }else{
            horizontalFlag=true;
            viewWidth = height;
            viewHeight =width;
        }
        xCenter=(float) viewWidth /2;
        yCenter=(float) viewHeight /2;
        if(3*yCenter>xCenter){
            backRadius =xCenter/2;
        }else{
            backRadius =yCenter;
        }
        foreRadius=backRadius*0.9f;
        switchWidth=backRadius*4*0.95f;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.onCheckedChangeListener =listener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(View v, boolean isChecked);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                if((event.getX()>0)&&(event.getX()< viewWidth)&&(event.getY()>0)&&(event.getY()< viewHeight)){
                    isChecked=!isChecked;
                    invalidate();
                    onCheckedChangeListener.onCheckedChange(this,isChecked);
                }
                break;
        }
        return true;
    }
}
