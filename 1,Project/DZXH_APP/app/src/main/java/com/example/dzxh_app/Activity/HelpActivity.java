package com.example.dzxh_app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;

import java.util.ArrayList;
import java.util.HashMap;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar stop_watch_back_tb =findViewById(R.id.help_tb);
        stop_watch_back_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListViewInit1(); 
        ListViewInit2();
    }
    private void ListViewInit1(){
        ArrayList<HashMap<String, Object>> SetListItem = new ArrayList<HashMap<String, Object>>();
        for(int i=0;i<2;i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            if(i==0){
                map.put("help_name", "使用手册");
            }else {
                map.put("help_name", "视频教程");
            }
            SetListItem.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, SetListItem,
                R.layout.item_set,
                new String[]{"help_name"},
                new int[]{R.id.item_set_name});
        ListView set_lv = findViewById(R.id.help_lv1);
        //列表配置
        set_lv.setAdapter(simpleAdapter);
        //点击事件
        set_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent  intent;
                if(position==0){
                    intent = new Intent(HelpActivity.this, UserActivity.class);
                }else{
                    String url = "https://www.bilibili.com/video/BV1sG4y167cn";
                    intent = new Intent();
                    intent.setData(Uri.parse(url));//Url 要打开的网址
                    intent.setAction(Intent.ACTION_VIEW);
                }
                startActivity(intent);
            }
        });
    }
    private void ListViewInit2(){
        ArrayList<HashMap<String, Object>> SetListItem = new ArrayList<HashMap<String, Object>>();
        for(int i=0;i<7;i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
           if(i==0){
                map.put("help_name","扫描失败" );
                SetListItem.add(map);
            }else if(i==1){
                map.put("help_name", "文件无法导入");
                SetListItem.add(map);
            }else if(i==2){
               map.put("help_name", "数据传输错误");
                SetListItem.add(map);
            }else if(i==3){
                map.put("help_name", "蓝牙4.0自动断连");
                SetListItem.add(map);
            }else if(i==4){
                map.put("help_name", "其他问题");
                SetListItem.add(map);
            }
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, SetListItem,
                R.layout.item_set,
                new String[]{"help_name"},
                new int[]{R.id.item_set_name});
        ListView set_lv = findViewById(R.id.help_lv2);
        //列表配置
        set_lv.setAdapter(simpleAdapter);
        //点击事件
        set_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position<4){
                    Intent intent = new Intent(HelpActivity.this, ProblemActivity.class);
                    intent.putExtra("problem",position);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(HelpActivity.this, UpdateActivity.class);
                    startActivity(intent);
                }

            }
        });
    }
}
