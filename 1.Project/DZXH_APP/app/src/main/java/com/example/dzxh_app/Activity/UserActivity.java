package com.example.dzxh_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;


public class UserActivity extends AppCompatActivity {

    private int page;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar stop_watch_back_tb =findViewById(R.id.user_tb);
        stop_watch_back_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent=getIntent();
        page=intent.getIntExtra("page",0);
        PDFInit();
    }

    private void PDFInit(){
        TextView help_page_now_tv = findViewById(R.id.user_page_now_tv);
        TextView help_page_sum_tv = findViewById(R.id.user_page_sum_tv);
        PDFView help_pv = findViewById(R.id.user_pv);
        help_pv.fromAsset("User[1.4.1].pdf")
                .defaultPage(page)     //从第几页开始显示
                //.spacing(10)    //页间间距
                .onLoad(new OnLoadCompleteListener() {  //加载监听
                    @Override
                    public void loadComplete(int nbPages) {
                        help_page_sum_tv.setText("/"+nbPages);
                    }
                })
                .onPageChange(new OnPageChangeListener() {  //翻页监听
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        help_page_now_tv.setText(page+1+"");//页数从零页开始的
                    }
                })
                .load(); //加载
    }
}
