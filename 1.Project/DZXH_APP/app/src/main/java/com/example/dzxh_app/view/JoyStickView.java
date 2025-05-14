package com.example.dzxh_app.view;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoyStickView extends View{
    private OnDragListener onDragListener;
    private Path pathFore;
    private Paint paintFore;
    private Path pathBack;
    private Paint paintBack;
    private boolean joyStickIsCircular=true;

    private int xCenter;
    private int yCenter;
    private int radiusBack;//背景半径
    private int radiusFore;//前景半径
    private boolean moveFlag=false;

    private int xValue;
    private int yValue;


    public JoyStickView(Context context){
        super(context);
        init();
    }
    public JoyStickView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init();
    }
    private void init(){

        pathFore=new Path();
        paintFore=new Paint();
        paintFore.setColor(0xFFAAAAAA);
        paintFore.setStyle(Paint.Style.FILL);
        paintFore.setAntiAlias(true); //去除锯齿

        pathBack=new Path();
        paintBack=new Paint();
        paintBack.setColor(0xFFAAAAAA);
        paintBack.setStrokeWidth(8);
        paintBack.setStyle(Paint.Style.STROKE);
        paintBack.setAntiAlias(true);
    }

    public void setStickColor(int color) {
        paintFore.setColor(color);
        paintBack.setColor(color);
    }

    public void setStickShape(int shape){
        pathBack.reset();
        if(shape==0){
            joyStickIsCircular=true;
            pathBack.addCircle(xCenter, yCenter, radiusBack, Path.Direction.CCW);
        }else{
            joyStickIsCircular=false;
            float left=xCenter-radiusBack;
            float right=xCenter+radiusBack;
            float top=yCenter-radiusBack;
            float bottom=yCenter+radiusBack;
            pathBack.addRect(left,top,right,bottom, Path.Direction.CW);
        }
        if(xCenter!=0){
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(pathBack, paintBack);
        canvas.drawPath(pathFore, paintFore);
    }


    public void refreshView(float x, float y){
        pathFore.reset();
        pathFore.addCircle(x,y, radiusFore, Path.Direction.CCW);
        invalidate();
        int x_value=(int)((x- xCenter)*101/ radiusBack)+100;
        int y_value=(int)-((y- yCenter)*101/ radiusBack)+100;
        if(x_value>200)x_value=200;
        if(x_value<0)x_value=0;
        if(y_value>200)y_value=200;
        if(y_value<0)y_value=0;
        xValue=x_value;yValue=y_value;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width= MeasureSpec.getSize(widthMeasureSpec);
        int height=MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width,height);
        xCenter = width/2;
        yCenter = height/2;
        if(xCenter < yCenter){
            radiusBack =width/3;
            radiusFore =width/6;
            paintBack.setStrokeWidth(width/32f);
        }else{
            radiusBack =height/3;
            radiusFore=height/6;
            paintBack.setStrokeWidth(height/32f);
        }

        if(joyStickIsCircular){
            pathBack.addCircle(xCenter, yCenter, radiusBack, Path.Direction.CCW);
        }else{
            float left=xCenter-radiusBack;
            float right=xCenter+radiusBack;
            float top=yCenter-radiusBack;
            float bottom=yCenter+radiusBack;
            pathBack.addRect(left,top,right,bottom, Path.Direction.CW);
        }
        pathFore.reset();
        pathFore.addCircle(xCenter, yCenter, radiusFore, Path.Direction.CCW);
    }


    public void setOnDragListener(OnDragListener listener) {
        this.onDragListener =listener;
    }

    public interface OnDragListener {
        boolean onDrag(View v, int x, int y);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(Math.abs(event.getX()- xCenter)< radiusFore && Math.abs(event.getY()- yCenter)< radiusFore){
                    moveFlag=true;
                    refreshView(event.getX(),event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(!moveFlag) {
                    return true;
                }
                float x=event.getX()- xCenter;
                float y=event.getY()- yCenter;
                if(joyStickIsCircular){
                    float dxy=(float) Math.sqrt(x*x+y*y);
                    if (!(dxy <= radiusBack)) {
                        x = x * radiusBack/ dxy;
                        y = y * radiusBack/ dxy;
                    }
                }else{
                    if (abs(x) >radiusBack) {
                        x= x* radiusBack /abs(x);
                    }
                    if (abs(y) >radiusBack) {
                        y= y* radiusBack/abs(y);
                    }
                }
                refreshView(x+xCenter,y+yCenter);
                break;
            case MotionEvent.ACTION_UP:
                moveFlag=false;
                refreshView(xCenter, yCenter);
                break;
        }
        onDragListener.onDrag(this,xValue,yValue);
        return true;
    }
}

