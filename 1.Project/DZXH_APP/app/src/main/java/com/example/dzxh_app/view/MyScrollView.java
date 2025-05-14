package com.example.dzxh_app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

//可以用来禁止ScrollView滚动
 public class MyScrollView extends ScrollView {

    private boolean isScrollEnable=true;
    private boolean isUp=false;

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public void setScrollEnable(boolean b){
        this.isScrollEnable=b;
    }

    public void setIsUp(boolean b){
        isUp=b;
    }
    public boolean getIsUP(){
        return isUp;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //对滑动动作无响应
        if(!isScrollEnable){
            return true;
        }
        super.onTouchEvent(ev); //系统处理
        //自动选择
        if(ev.getAction()==MotionEvent.ACTION_UP){
            if((this.getScrollY()*2)<this.getHeight()){
                this.isUp=true;
                this.fullScroll(ScrollView.FOCUS_UP);
            }else{
                this.isUp=false;
                this.fullScroll(ScrollView.FOCUS_DOWN);

            }
        }
        return true;
    }
}
