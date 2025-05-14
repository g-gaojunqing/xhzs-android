package com.example.dzxh_app.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.dzxh_app.R;


public class MyInformDialog extends Dialog implements View.OnClickListener {
    private OnDialogClickListener listener;
    private TextView layout_dialog_title_tv;
    private TextView layout_inform_dialog_name1_tv;
    private TextView layout_inform_dialog_name2_tv;
    private TextView layout_inform_dialog_name3_tv;

    private TextView layout_inform_dialog_inform1_tv;
    private TextView layout_inform_dialog_inform2_tv;
    private TextView layout_inform_dialog_inform3_tv;

    private Button layout_inform_dialog_no;
    private Button layout_inform_dialog_yes;

    public MyInformDialog(Context context) {
        super(context,R.style.MyDialogTheme);
        setContentView(R.layout.layout_dialog_inform);
        //this.listener=listener;
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout_dialog_title_tv = findViewById(R.id.layout_dialog_title_tv);
        layout_inform_dialog_name1_tv = findViewById(R.id.layout_inform_dialog_name1_tv);
        layout_inform_dialog_name2_tv = findViewById(R.id.layout_inform_dialog_name2_tv);
        layout_inform_dialog_name3_tv = findViewById(R.id.layout_inform_dialog_name3_tv);

        layout_inform_dialog_inform1_tv = findViewById(R.id.layout_inform_dialog_inform1_tv);
        layout_inform_dialog_inform2_tv = findViewById(R.id.layout_inform_dialog_inform2_tv);
        layout_inform_dialog_inform3_tv = findViewById(R.id.layout_inform_dialog_inform3_tv);

        layout_inform_dialog_yes = findViewById(R.id.layout_inform_dialog_yes);
        layout_inform_dialog_yes.setOnClickListener(this);
        layout_inform_dialog_no = findViewById(R.id.layout_inform_dialog_no);
        layout_inform_dialog_no.setOnClickListener(this);
    }

    public void setTitle(String string){
        layout_dialog_title_tv.setText(string);
    }

    public void setName(String s1,String s2,String s3){
        layout_inform_dialog_name1_tv.setText(s1);
        layout_inform_dialog_name2_tv.setText(s2);
        layout_inform_dialog_name3_tv.setText(s3);
    }

    public void setInform1(String string){
        layout_inform_dialog_inform1_tv.setText(string);
    }

    public void setInform2(String string){
        layout_inform_dialog_inform2_tv.setText(string);
    }

    public void setInform3(String string){
        layout_inform_dialog_inform3_tv.setText(string);
    }

    public void setButtonText(String s1,String s2){
        layout_inform_dialog_no.setText(s1);
        layout_inform_dialog_yes.setText(s2);
    }
    public void setOnDialogClickListener(OnDialogClickListener listener) {
        this.listener = listener;
    }
    public interface OnDialogClickListener{
        void OnClick(View view);
    }
    @Override
    public void onClick(View v) {
        dismiss();
        listener.OnClick(v);
    }
}
