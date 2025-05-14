package com.example.dzxh_app.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dzxh_app.R;


public class MyOneEditDialog extends Dialog implements View.OnClickListener {
    private OnDialogClickListener listener;
    private TextView layout_dialog_edit_one_title_tv;
    private TextView layout_dialog_edit_one_inform_name_tv;
    private EditText layout_dialog_edit_one_inform_et;

    private Button layout_dialog_edit_one_no_bt;
    private Button layout_dialog_edit_one_yes_bt;

    public MyOneEditDialog(Context context) {
        super(context, R.style.MyDialogTheme);
        setContentView(R.layout.layout_dialog_edit_one);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout_dialog_edit_one_title_tv = findViewById(R.id.layout_dialog_edit_one_title_tv);
        layout_dialog_edit_one_inform_name_tv = findViewById(R.id.layout_dialog_edit_one_inform_name_tv);
        layout_dialog_edit_one_inform_et = findViewById(R.id.layout_dialog_edit_one_inform_et);

        layout_dialog_edit_one_no_bt = findViewById(R.id.layout_dialog_edit_one_no_bt);
        layout_dialog_edit_one_no_bt.setOnClickListener(this);
        layout_dialog_edit_one_yes_bt = findViewById(R.id.layout_dialog_edit_one_yes_bt);
        layout_dialog_edit_one_yes_bt.setOnClickListener(this);

    }

    public void setTitle(String string){
        layout_dialog_edit_one_title_tv.setText(string);
    }

    public void setInformName(String string){
        layout_dialog_edit_one_inform_name_tv.setText(string);
    }

    public void setHint(String s){
        layout_dialog_edit_one_inform_et.setHint(s);
    }

    public void setButtonText(String s1,String s2){
        layout_dialog_edit_one_no_bt.setText(s1);
        layout_dialog_edit_one_yes_bt.setText(s2);
    }

    public void setInform(String s){
        layout_dialog_edit_one_inform_et.setText(s);
        layout_dialog_edit_one_inform_et.setSelection(s.length());
    }

    public String getInform(){
        return layout_dialog_edit_one_inform_et.getText().toString().trim();
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
