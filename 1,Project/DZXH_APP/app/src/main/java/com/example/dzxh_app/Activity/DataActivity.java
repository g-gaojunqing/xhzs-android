package com.example.dzxh_app.Activity;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dzxh_app.R;

public class DataActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView data_tv1;
    private TextView data_tv2;
    private TextView data_tv3;
    private TextView data_tv4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        Toolbar stop_watch_back_tb =findViewById(R.id.data_tb);
        stop_watch_back_tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        data_tv1 = findViewById(R.id.data_tv1);
        data_tv2 = findViewById(R.id.data_tv2);
        data_tv3 = findViewById(R.id.data_tv3);
    }

    @Override
    public void onClick(View v) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(v.getId()==R.id.data_bt1){
            clipboardManager.setText(data_tv1.getText());
        }else if(v.getId()==R.id.data_bt2){
            clipboardManager.setText(data_tv2.getText());
        }else if(v.getId()==R.id.data_bt3){
            clipboardManager.setText("593950753");
        }
        Toast.makeText(this, "已复制", Toast.LENGTH_LONG).show();
    }
}
