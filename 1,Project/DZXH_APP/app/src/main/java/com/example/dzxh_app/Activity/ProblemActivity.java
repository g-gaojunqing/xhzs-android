package com.example.dzxh_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;

public class ProblemActivity extends AppCompatActivity {

    private final String stringProblem0 ="扫描失败";
    private final String stringAnswer0 =
            "1、在手机【设置】-【应用管理】-【学会助手】-【权限管理】查看位置权限是否打开。\n\n"+
            "2、打开手机定位（如GPS）。\n\n";

    private final String stringProblem1 ="无法导入文件（图像、波形、音频）";
    private final String stringAnswer1 =
            "1、在手机【设置】-【应用管理】-【学会助手】-【权限管理】查看存储权限是否打开。\n\n" +
            "2、尝试使用“内部储存空间”或“文件管理”选项导入文件。\n\n";

    private final String stringProblem2 ="数据传输错误（数据丢失）";
    private final String stringAnswer2 =
            "1、可能受蓝牙模块或者软件性能限制，无法高速传输数据。\n\n"+
            "2、适当降低波特率或提高发送间隔。\n\n";

    private final String stringProblem3 ="蓝牙4.0自动断连";
    private final String stringAnswer3 ="1、换个蓝牙。";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem);
        Toolbar stop_watch_back_tb =findViewById(R.id.problem_tb);
        stop_watch_back_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent=getIntent();
        int p=intent.getIntExtra("problem",0);

        TextView problem_name = findViewById(R.id.problem_name);
        TextView problem_answer = findViewById(R.id.problem_answer);

        if(p==0){
            problem_name.setText(stringProblem0);
            problem_answer.setText(stringAnswer0);
        }else if(p==1){
            problem_name.setText(stringProblem1);
            problem_answer.setText(stringAnswer1);
        }else if(p==2){
            problem_name.setText(stringProblem2);
            problem_answer.setText(stringAnswer2);
        }else if(p==3){
            problem_name.setText(stringProblem3);
            problem_answer.setText(stringAnswer3);
        }
    }
}
