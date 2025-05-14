package com.example.dzxh_app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class ClockBoard extends SurfaceView implements SurfaceHolder.Callback{


    String[] strings;
    String[] strings_week;
    String[] strings_day;
    private String[] strings_clock;

    private Path pant;
    private Paint paint_now;
    private Paint paint_text;

    private int X_center;
    private int Y_center;
    private int Radius;


    public ClockBoard(Context context, AttributeSet attrs){
            super(context, attrs);
            getHolder().addCallback(this);
            PathInit();
    }
    private void PathInit(){

        strings=new String[]{
                "    零","    壹","    贰","    叁","    肆","    伍","    陆","    柒","    捌","    玖",
                "    拾","  拾壹","  拾贰","  拾叁","  拾肆","  拾伍","  拾陆","  拾柒","  拾捌","  拾玖",
                "  贰拾","贰拾壹","贰拾贰","贰拾叁","贰拾肆","贰拾伍","贰拾陆","贰拾柒","贰拾捌","贰拾玖",
                "  叁拾","叁拾壹","叁拾贰","叁拾叁","叁拾肆","叁拾伍","叁拾陆","叁拾柒","叁拾捌","叁拾玖",
                "  肆拾","肆拾壹","肆拾贰","肆拾叁","肆拾肆","肆拾伍","肆拾陆","肆拾柒","肆拾捌","肆拾玖",
                "  伍拾","伍拾壹","伍拾贰","伍拾叁","伍拾肆","伍拾伍","伍拾陆","伍拾柒","伍拾捌","伍拾玖",
        };
        strings_clock = new String[]{
                "拾贰","壹"," 贰","叁","肆","伍","陆","柒","捌"," 玖",
                "拾","拾壹",
        };
        strings_week=new String[]{"日","壹","贰","叁","肆","伍","陆",};
        strings_day=new String[]{
                "壹","贰","叁","肆","伍","陆","柒","捌","玖",
                "拾","拾壹","拾贰","拾叁","拾肆","拾伍","拾陆","拾柒","拾捌","拾玖",
                "贰拾","贰拾壹","贰拾贰","贰拾叁","贰拾肆","贰拾伍","贰拾陆","贰拾柒","贰拾捌","贰拾玖",
                "叁拾","叁拾壹"
        };
        pant = new Path();
        paint_now = new Paint();
        paint_now.setColor(Color.YELLOW);
        paint_now.setStyle(Paint.Style.FILL);

        paint_now.setStrokeWidth(2);
        paint_now.setAntiAlias(true);

        paint_text = new Paint();
        paint_text.setColor(Color.WHITE);
        paint_text.setStyle(Paint.Style.FILL);
        paint_text.setStrokeWidth(3);
        paint_text.setAntiAlias(true);
    }

    private int mMath_limit(int num,int max){
        if(num>=max){
            num-=max;
        }
        return num;
    }
    private void draw(int month,int day,int week,int hour,int minute,int second,int milli) {
        int temp;
        float x=Radius/50;
        Canvas canvas = getHolder().lockCanvas();
        if(canvas!=null){
            canvas.drawColor(Color.BLACK);
            //秒
            canvas.rotate(-milli,X_center,Y_center);
            canvas.drawTextOnPath(strings[second]+"秒",pant,x*42,0,paint_now);
            canvas.rotate(6,X_center,Y_center);
            for (int i = 1; i <60 ; i++) {
                temp=mMath_limit(i+second,60);
                canvas.drawTextOnPath(strings[temp]+"秒",pant,x*42,0,paint_text);
                canvas.rotate(6,X_center,Y_center);
            }
            canvas.rotate(milli,X_center,Y_center);
            //分
            canvas.drawTextOnPath(strings[minute]+"分",pant,x*33,0,paint_now);
            canvas.rotate(6,X_center,Y_center);
            for (int i = 1; i <60 ; i++) {
                temp=mMath_limit(i+minute,60);
                canvas.drawTextOnPath(strings[temp]+"分",pant,x*33,0,paint_text);
                canvas.rotate(6,X_center,Y_center);
            }
            //点
            canvas.drawTextOnPath(strings_clock[hour]+"点",pant,x*26,0,paint_now);
            canvas.rotate(30,X_center,Y_center);
            for (int i = 1; i <12 ; i++) {
                temp=mMath_limit(i+hour,12);
                canvas.drawTextOnPath(strings_clock[temp]+"点",pant,x*26,0,paint_text);
                canvas.rotate(30,X_center,Y_center);
            }
            //周
            canvas.drawTextOnPath("周"+strings_week[week],pant,x*21,0,paint_now);
            canvas.rotate(51,X_center,Y_center);
            for (int i = 1; i <7; i++) {
                temp=mMath_limit(i+week,7);
                canvas.drawTextOnPath("周"+strings_week[temp],pant,x*21,0,paint_text);
                canvas.rotate(51,X_center,Y_center);
            }
            canvas.rotate(-7*51,X_center,Y_center);
            //号
            canvas.drawTextOnPath(strings_day[day]+"号",pant,x*12,0,paint_now);
            canvas.rotate(12,X_center,Y_center);
            for (int i = 1; i <31; i++) {
                temp=mMath_limit(i+day,31);
                canvas.drawTextOnPath(strings_day[temp]+"号",pant,x*12,0,paint_text);
                if(i%2==0){
                    canvas.rotate(12,X_center,Y_center);
                }else{
                    canvas.rotate(11,X_center,Y_center);
                }
            }
            canvas.rotate(-356,X_center,Y_center);
            //月
            canvas.drawTextOnPath(strings_day[month]+"月",pant,x*5,0,paint_now);
            canvas.rotate(30,X_center,Y_center);
            for (int i = 1; i <12; i++) {
                temp=mMath_limit(i+month,12);
                canvas.drawTextOnPath(strings_day[temp]+"月",pant,x*5,0,paint_text);
                canvas.rotate(30,X_center,Y_center);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
    private int second_ed;
    public void Refresh_View(int month,int day,int week,int hour,int minute,int second){
        if(second_ed!=second){
            try {
                for (int i =1; i <=6; i++) {
                    draw(month-1,day-1,week,hour,minute,second_ed,i);
                }
                second_ed=second;
                draw(month-1,day-1,week,hour,minute,second_ed,0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        X_center = width/2;
        Y_center = height/2;
        if(X_center>Y_center){
            Radius=Y_center;
        }else{
            Radius=X_center;
        }
        paint_text.setTextSize(Radius/25);
        paint_now.setTextSize(Radius/25+1);
        pant.moveTo(X_center,Y_center);
        pant.lineTo(2*X_center,Y_center);
        draw(0,0,0,0,0,0,0);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}

