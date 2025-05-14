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


public class MySelectDialog extends Dialog implements View.OnClickListener {
    private OnDialogClickListener listener;
    private TextView layout_dialog_select_tv;
    private Button layout_dialog_select_yes_bt;
    private Button layout_dialog_select_no_bt;

    public MySelectDialog(Context context) {
        super(context,R.style.MyDialogTheme);
        setContentView(R.layout.layout_dialog_select);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout_dialog_select_tv = findViewById(R.id.layout_dialog_select_tv);
        layout_dialog_select_yes_bt = findViewById(R.id.layout_dialog_select_yes_bt);
        layout_dialog_select_no_bt = findViewById(R.id.layout_dialog_select_no_bt);
        layout_dialog_select_yes_bt.setOnClickListener(this);
        layout_dialog_select_no_bt.setOnClickListener(this);
    }

    public void setText(String string){
        layout_dialog_select_tv.setText(string);
    }

    public void setButtonText(String s1,String s2){
        layout_dialog_select_no_bt.setText(s1);
        layout_dialog_select_yes_bt.setText(s2);
    }

    public void setButtonColor(int c1, int c2){
        layout_dialog_select_no_bt.setTextColor(c1);
        layout_dialog_select_yes_bt.setTextColor(c2);
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
