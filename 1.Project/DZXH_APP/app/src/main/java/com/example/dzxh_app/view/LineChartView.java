package com.example.dzxh_app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

public class LineChartView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    //画背景
    private Paint paintLine;  //画笔配置
    private Path pathLine;  //储存路径
    private Paint paintDottedLine, paintZeroLine, paintRuler, paintText;
    private Path pathDottedLine, pathZeroLine, pathRuler;
    private Paint clearPaint;
    //数据储存
    private int chartColor=0XFF999999;
    private int lineColor=0XFF0000FF;
    private  double[] doubleArray;
    private  int dataNum =0;//记录传输数据数

    //显示参数
    private int xAxisMax=50;
    private float yAxisMax=200,yAxisMin=0;
    private int chartStrokeWidth,lineStrokeWidth;
    private int xMax=0, yMax=0; //最大值
    private int ivWidth=0;  //窗口宽度
    private  double scaleX =3, scaleY =1; //缩放比例，越大图像越大,每隔ScaleX画一点 *零线
    private  double offsetX =0,offsetY =0;//图像偏移X、Y，用于图像拖动
    private  int multiplier =0;  //标记刻度放大次数，图像越大值越大
    private  double zoom =1; //刻度缩放，在原始单位根据缩放分为多少份
    private  double[] dataScales;//刻度值
    private  float[] dataWidth;//刻度宽度
    private  float yDiv, yDivO;//当前两根虚线之间像素距离，两根虚线间原始像素距离
    private  float yDivRem;//刻度偏移

    private Timer timer;
    private boolean refreshFlag;
    public LineChartView(Context context) {
        super(context);

        getHolder().addCallback(this);
        setOnTouchListener(this::onTouch);//手势
        setZOrderOnTop(true);
        //setZOrderMediaOverlay(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);  //设置透明
        setLayerType(LAYER_TYPE_SOFTWARE,null); //绘制加速

        NumInit();
        pathInit();
        TimerInit();
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d("LineChart","surfaceChanged--------------------------------");
        if(ivWidth==width){ //保持数据不恢复
            drawWall();
            draw();
            TimerInit();
            return;
        }
        ivWidth =width;
        yMax =height;
        yDivO = (float) yMax/10; //网格原始间隔
        paintText.setTextSize((float)(yDivO/1.5)); //字体大小
        chartStrokeWidth=height/80;
        if(chartStrokeWidth<1){
            chartStrokeWidth=1;
        }
        lineStrokeWidth=height/80;
        if(lineStrokeWidth<1){
            lineStrokeWidth=1;
        }
        Log.d("LineChart","chartStrokeWidth"+chartStrokeWidth+"lineStrokeWidth"+lineStrokeWidth);
        paintDottedLine.setStrokeWidth(chartStrokeWidth);
        paintZeroLine.setStrokeWidth(chartStrokeWidth);
        paintRuler.setStrokeWidth(chartStrokeWidth);
        paintLine.setStrokeWidth(lineStrokeWidth);
        getStringWidth();
        setXMax(xAxisMax);
        setYMax(yAxisMin,yAxisMax);
        setYDiv();
        drawWall();
        draw();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        timer.cancel();
    }


    private void NumInit(){

        timer =new Timer();
        doubleArray =new double[1024];
        dataScales = new double[11];
        dataWidth = new float[11];
    }

    private void pathInit(){
        //虚线
        pathDottedLine = new Path();
        paintDottedLine = new Paint();
        paintDottedLine.setColor(chartColor);
        paintDottedLine.setStrokeWidth(3);
        paintDottedLine.setStyle(Paint.Style.STROKE);
        paintDottedLine.setPathEffect(new DashPathEffect(new float[] {7, 7}, 1));
        //零线
        paintZeroLine = new Paint();
        pathZeroLine = new Path();
        paintZeroLine.setColor(chartColor);
        paintZeroLine.setStrokeWidth(3);
        paintZeroLine.setStyle(Paint.Style.STROKE);
        //格尺
        paintRuler = new Paint();
        pathRuler = new Path();
        paintRuler.setColor(chartColor);
        paintRuler.setStrokeWidth(3);
        paintRuler.setStyle(Paint.Style.STROKE);
        //数字
        paintText = new Paint();
        paintText.setColor(chartColor);
        paintText.setStrokeWidth(2);
        paintText.setTextSize(30);
        paintText.setAntiAlias(true);     //去除锯齿
        //波形
        pathLine = new Path();
        paintLine = new Paint();
        paintLine.setColor(lineColor);
        paintLine.setStrokeWidth(4);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setAntiAlias(true);
        //清除
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void setLineColor(int color){
        paintLine.setColor(color);
        refreshFlag=true;
    }

    public void setChartColor(int color){
        paintDottedLine.setColor(color);
        paintZeroLine.setColor(color);
        paintRuler.setColor(color);
        paintText.setColor(color);
        drawWall();
        refreshFlag=true;
    }

    public void setXMax(int max){
        xAxisMax=max;
        if(xMax!=0){
            scaleX=(float)xMax/xAxisMax;
        }
    }

    public void setYMax(float min,float max){
        if(min>max){
            return;
        }
        yAxisMax=max;
        yAxisMin=min;
        if(yMax!=0){
            scaleY=(float)yMax/(yAxisMax-yAxisMin);
            offsetY=yAxisMin*scaleY+yMax;
        }
    }
    /**
     * 按钮调整X轴
     * @param dir 方向
     */
    public void setScaleX(boolean dir){
        double tempX;
        double tempOX;
        if(dir){
            tempX = scaleX *2;
            tempOX =(offsetX +(double) xMax /2)*2-(double) xMax /2;      //新的X方向偏移
        }else{
            tempX = scaleX /2;
            tempOX =(offsetX +(double) xMax /2)/2-(double) xMax /2;      //新的X方向偏移
        }
        if (tempX < 1000 && tempX > 0.00001) {
            scaleX = tempX;
            offsetX =tempOX;
            drawWall();
            refreshFlag=true;
        }
    }
    /**
     * 外部调整Y轴
     */
    public void setScaleY(boolean dir){
        double tempY;
        double tempO;
        if(dir){
            tempY = scaleY *2;
            tempO=(offsetY -(double) yMax /2)*2+(double) yMax /2;
        }else{
            tempY = scaleY /2;
            tempO=(offsetY -(double) yMax /2)/2+(double) yMax /2;
        }
        if (tempY < 100000000 && tempY > 0.000000000001 && tempO<100000000 && tempO>-100000000) {
            scaleY = tempY;
            offsetY =tempO;
            drawWall();
            refreshFlag=true;
        }
    }

    public void clearData(){
        pathLine.reset();
        pathLine.moveTo(0,0);
        dataNum=0;
        refreshFlag =true;
    }

    /**
     * 定时刷新
     */
    private void TimerInit(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(refreshFlag){
                    refreshWave();
                    refreshFlag=false;
                }
            }
        },80,80);
    }

    private void draw(){
        Canvas canvas = getHolder().lockCanvas();
        if(canvas==null){
            Log.d("LineChart","canvas==null");
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawPath(pathDottedLine, paintDottedLine);
        canvas.drawPath(pathZeroLine, paintZeroLine);
        canvas.drawPath(pathLine, paintLine);
        //清除多余线条
        canvas.drawRect(0, 0, stringWidth, yMax, clearPaint);
        //格尺
        canvas.drawPath(pathRuler, paintRuler);
        for (int i = 0; i < 11; i++) {
            float temp= yDivRem + yDiv *i;
            canvas.drawText(dataScales[i]+"",stringWidth- dataWidth[i] ,temp, paintText);
        }
        getHolder().unlockCanvasAndPost(canvas);
    }

    /**
     * 获得数组张最大值
     * @param doubles 数组
     * @return 最大值
     */
    private double GetMax(double[] doubles){
        int len=doubles.length;
        double max=doubles[0];
        for (int i = 1; i < len; i++) {
            if(doubles[i]>max){
                max=doubles[i];
            }
        }
        return max;
    }

    /**
     * 获得数组中最小值
     * @param doubles 数组
     * @return 最小值
     */
    private double GetMin(double[] doubles){
        int len=doubles.length;
        double min=doubles[0];
        for (int i = 1; i < len; i++) {
            if(doubles[i]<min){
                min=doubles[i];
            }
        }
        return min;
    }

    /**
     * 将数值限制到-100000000至100000000
     * @param d 原始值
     * @return 限位值
     */
    private double Limiting(double d)
    {
        if(d>100000000){
            d=100000000;
        }else if(d<-100000000){
            d=-100000000;
        }
        return d;
    }

    /**
     *刷新波形
     */
    private void refreshWave() {
        double X,Y;
        double min; //叠加点的最小值
        double max; //叠加点的最大值
        pathLine.reset();
        //显示长度没有超过屏幕
        double tmpOffsetX=offsetX;
        if(scaleX *(dataNum -1)< xMax) {
            tmpOffsetX =0; //为后续缩放做准备
        }
        //偏移不可小于零
        if(offsetX <0) {
            tmpOffsetX =0;
        }
        //防止小于零卡死
        int start=(int)(tmpOffsetX / scaleX);//第一个刷新数据数组中位置
        int end=(int)((tmpOffsetX + ivWidth)/ scaleX +2);//最后一个刷新数组中位置，多刷一个，再加1减start表示数量
        if(end> dataNum) end= dataNum; //超出数量,限位
        int dx=(int)(2f/ scaleX);   //两个像素内显示数据量
        if(dx<3){
            for (int i =start; i<end; i++) {
                X=i* scaleX-tmpOffsetX+stringWidth;
                //从xStart刷起
                Y=Limiting(-scaleY * doubleArray[i]+ offsetY);  //Y值限位
                pathLine.lineTo((float)X,(float)Y);
            }
        }else{
            double[] doubles=new double[dx];
            int rem=(end-start)%dx;//余数
            end-=rem; //减去后面余数
            for (int i =start; i <end; i+=dx) {
                X=i* scaleX-tmpOffsetX+stringWidth;
                System.arraycopy(doubleArray,i,doubles,0,dx);
                min=GetMin(doubles);
                Y=Limiting(-scaleY*min+ offsetY); //Y值限位
                pathLine.lineTo((float)X,(float)Y);
                //Y值限位
                max=GetMax(doubles);
                Y=Limiting(-scaleY*max+ offsetY);//Y值限位
                pathLine.lineTo((float)X,(float)Y);
            }
            //有余数
            if(rem>0){
                doubles=new double[rem];
                X=end* scaleX-tmpOffsetX+stringWidth;
                System.arraycopy(doubleArray,end,doubles,0,rem);
                min=GetMin(doubles);
                Y=Limiting(-scaleY*min+ offsetY);//Y值限位
                pathLine.lineTo((float)X,(float)Y);
                max=GetMax(doubles);
                Y=Limiting(-scaleY*max+ offsetY);  //Y值限位
                pathLine.lineTo((float)X,(float)Y);
            }
        }
        draw();
    }

    /**
     * 得到窗口X显示最大值（减去刻度位置）
     */
    private void getXMax(){
        float maxStringWidth=0;
        for (int i = 0; i < 11; i++) {
            if((yDivRem + yDiv *i)< yMax){
                float stringWidth= paintText.measureText(dataScales[i]+"  ");
                if(maxStringWidth<stringWidth){
                    maxStringWidth=stringWidth;
                }
            }
        }
        xMax = ivWidth -(int)maxStringWidth;
    }

    /**
     * 获取字符串宽度
     * */
    private float stringWidth;
    private void getStringWidth(){
        float maxStringWidth=0;
        for (int i = 0; i < 11; i++) {
            if((yDivRem + yDiv *i)< (yMax+yDivO)){
                float width= paintText.measureText(dataScales[i]+" ");
                dataWidth[i]=width;
                if(maxStringWidth<width){
                    maxStringWidth=width;
                }
            }
        }
        stringWidth=maxStringWidth;
        xMax = ivWidth -(int)maxStringWidth;
    }

    /**
     * 设置网格行间距，放大3个一循环
     */
    private boolean setYDiv(){
        boolean adjust=false;
        yDiv =(float) (scaleY/zoom);
        //不是处于比例5xx到2xx的过程
        if(multiplier %3!=1&& multiplier %3!=-2) {
            if(yDiv >2* yDivO){
                multiplier++;
                adjust=true;
            }
        }else{
            if(yDiv >2.5* yDivO) {
                multiplier++;
                adjust=true;
            }
        }
        if(yDiv < yDivO){
            multiplier--;
            adjust=true;
        }
        if(!adjust){
            return true;
        }
        if(multiplier %3==0) {
            zoom =Math.pow(10,(int)(multiplier /3));
        }else if(multiplier %3==1) {
            zoom =2*Math.pow(10,(int)(multiplier /3));
        }else if(multiplier %3==2) {
            zoom =5*Math.pow(10,(int)(multiplier /3));
        }else if(multiplier %3==-1) {
            zoom =0.5*Math.pow(10,(int)(multiplier /3));
        }else if(multiplier %3==-2) {
            zoom =0.2*Math.pow(10,(int)(multiplier /3));
        }
        return false;
    }

    private void drawWall(){
        pathDottedLine.reset();
        pathZeroLine.reset();
        pathRuler.reset();
        while(!setYDiv());
        //计算坐标值
        for (int i =0; i <11; i++){
            if(multiplier %3==0){
                if(multiplier >0) {
                    dataScales[i] = ((int) (offsetY / yDiv) - i)/Math.pow(10,(int)(multiplier /3));
                }else{
                    dataScales[i] = ((int) (offsetY / yDiv) - i)*Math.pow(10, (int)(-multiplier /3));
                }
            }else if(multiplier %3==1){
                dataScales[i]=((int)(offsetY / yDiv)-i)/(2*Math.pow(10,(int)((multiplier-1)/3)));
            }else if(multiplier %3==-1){
                dataScales[i]=((int)(offsetY / yDiv)-i)*2*Math.pow(10,(int)(-(multiplier+1)/3));
            }else if(multiplier %3==2){
                dataScales[i]=((int)(offsetY / yDiv)-i)/(5*Math.pow(10,(int)((multiplier-2)/3)));
            }else if(multiplier %3==-2){
                dataScales[i]=((int)(offsetY / yDiv)-i)*5*Math.pow(10,(int)((-(multiplier+2)/3)));
            }
        }
        yDivRem =(float)(offsetY % yDiv);
        //虚线
        getStringWidth();//由于刻度尺标号长度变化，波形显示区域也随之变化
        for (int i = 0; i <11; i++) {
            pathDottedLine.moveTo(stringWidth,yDivRem + yDiv *i);
            pathDottedLine.lineTo(ivWidth, yDivRem + yDiv *i);
        }
        //格尺
        pathRuler.moveTo(stringWidth,0);
        pathRuler.lineTo(stringWidth, yMax-chartStrokeWidth/2f);
        pathRuler.lineTo(ivWidth, yMax-chartStrokeWidth/2f);
//        for (int i = 0; i <11; i++) {
//            pathRuler.moveTo(xMax, yDivRem + yDiv *i);
//            pathRuler.lineTo(xMax+10, yDivRem + yDiv *i);
//        }
        //pathWall3.addRect(stringWidth,0, ivWidth, yMax,Path.Direction.CCW);//方框
        //pathRuler.addRect(xMax,0, ivWidth, yMax,Path.Direction.CCW); //覆盖多余曲线
        //零线
        pathZeroLine.moveTo(stringWidth,(float) offsetY);
        pathZeroLine.lineTo(ivWidth,(float) offsetY);
    }

    public void addData(double data) {
        if(dataNum <1024){
            doubleArray[dataNum]=data;
            dataNum++;
            if(!refreshFlag){
                offsetX =(float) scaleX *(dataNum -1)- xMax;//置最大，实时显示数据
            }
        }else if(dataNum ==1024){
            System.arraycopy(doubleArray,1,doubleArray,0,1023);
            doubleArray[1023]=data;
            if(!refreshFlag) {
                offsetX = (float) scaleX * 1023 - xMax;//置最大，实时显示数据
            }
        }
        refreshFlag=true;
    }


    private double DownX,DownY,DownXYed,DownXYing,Y_Center,X_Center;
    private int twoFlag,gestureMode;
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()& MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                gestureMode =1;
                DownX=event.getX();
                DownY=event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                gestureMode =2;
                float x0=event.getX(0);
                float y0=event.getY(0);
                float x1=event.getX(1);
                float y1=event.getY(1);
                DownXYed=(x0-x1)*(x0-x1)
                        +(y0-y1)*(y0-y1);
                DownXYed=Math.sqrt(DownXYed);
                Y_Center=(y0+y1)/2;
                X_Center=(x0+x1)/2;
                double angleOfPointer=Math.asin(Math.abs(y1-y0)/DownXYed);
                if(angleOfPointer<0.34){
                    gestureMode=3;
                }else if(angleOfPointer>1.1){
                    gestureMode=4;
                }else{
                    gestureMode=2;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(gestureMode ==1){
                    offsetX -= event.getX() - DownX;
                    if(offsetX <0){
                        offsetX =0;
                    }
                    offsetY += event.getY() - DownY;
                    DownX = event.getX();
                    DownY = event.getY();
                    drawWall();
                    if(!refreshFlag) {
                        refreshWave();
                    }
                }else if(gestureMode ==2) {
                    DownXYing=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                            +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                    DownXYing=Math.sqrt(DownXYing);
                    //以两指为中心放大算法
                    double tempX =  ( scaleX * DownXYing / DownXYed);       //X方向比例
                    double tempY =  ( scaleY * DownXYing / DownXYed);       //Y方向比例
                    double tempOY  =(offsetY -Y_Center)*DownXYing/DownXYed+Y_Center;      //新的Y方向偏移
                    double tempOX  =(offsetX +X_Center)*DownXYing/DownXYed-X_Center;      //新的X方向偏移
                    //稍微限制一下缩放平移比例
                    if (tempX < 1000&& tempX > 0.00001
                            && tempY < 100000000&& tempY > 0.000000000001
                            &&tempOY<100000000&&tempOY>-100000000){
                        scaleX = tempX ;
                        scaleY =  tempY;
                        offsetY =tempOY;
                        offsetX =tempOX;
                        DownXYed = DownXYing;
                        drawWall();
                        if(!refreshFlag){
                            refreshWave();
                        }
                        Log.d("LineChart","RefreshWave");
                    }
                }else if(gestureMode==3){
                    DownXYing=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                            +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                    DownXYing=Math.sqrt(DownXYing);
                    //以两指为中心放大算法
                    double tempX =  ( scaleX * DownXYing / DownXYed);       //X方向比例
                    double tempOX  =(offsetX +X_Center)*DownXYing/DownXYed-X_Center;      //新的X方向偏移
                    //稍微限制一下缩放平移比例
                    if (tempX < 1000&& tempX > 0.00001){
                        scaleX = tempX ;
                        offsetX =tempOX;
                        DownXYed = DownXYing;
                        drawWall();
                        if(!refreshFlag) {
                            refreshWave();
                        }
                    }
                }else if(gestureMode==4){
                    DownXYing=(event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))
                            +(event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1));
                    DownXYing=Math.sqrt(DownXYing);
                    //以两指为中心放大算法
                    double tempY =  ( scaleY * DownXYing / DownXYed);       //Y方向比例
                    double tempOY  =(offsetY -Y_Center)*DownXYing/DownXYed+Y_Center;      //新的Y方向偏移
                    //稍微限制一下缩放平移比例
                    if (tempY < 100000000&& tempY > 0.000000000001
                            &&tempOY<100000000&&tempOY>-100000000){
                        scaleY =  tempY;
                        offsetY =tempOY;
                        DownXYed = DownXYing;
                        drawWall();
                        if(!refreshFlag) {
                            refreshWave();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                gestureMode =0;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true; //false 不消费，第一次 DAWN false Down以后的事件就进不来了
    }
}


