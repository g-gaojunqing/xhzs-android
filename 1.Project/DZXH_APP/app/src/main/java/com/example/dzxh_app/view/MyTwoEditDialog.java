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


public class MyTwoEditDialog extends Dialog implements View.OnClickListener {
    private OnDialogClickListener listener;
    private TextView layout_dialog_edit_two_title_tv;
    private TextView layout_dialog_edit_two_inform1_name_tv;
    private TextView layout_dialog_edit_two_inform2_name_tv;
    private EditText layout_dialog_edit_two_inform1_et;
    private EditText layout_dialog_edit_two_inform2_et;

    private Button layout_dialog_edit_two_no_bt;
    private Button layout_dialog_edit_two_yes_bt;

    public MyTwoEditDialog(Context context) {
        super(context, R.style.MyDialogTheme);
        setContentView(R.layout.layout_dialog_edit_two);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout_dialog_edit_two_title_tv = findViewById(R.id.layout_dialog_edit_two_title_tv);

        layout_dialog_edit_two_inform1_name_tv=findViewById(R.id.layout_dialog_edit_two_inform1_name_tv);
        layout_dialog_edit_two_inform2_name_tv=findViewById(R.id.layout_dialog_edit_two_inform2_name_tv);

        layout_dialog_edit_two_inform1_et = findViewById(R.id.layout_dialog_edit_two_inform1_et);
        layout_dialog_edit_two_inform2_et = findViewById(R.id.layout_dialog_edit_two_inform2_et);

        layout_dialog_edit_two_no_bt = findViewById(R.id.layout_dialog_edit_two_no_bt);
        layout_dialog_edit_two_no_bt.setOnClickListener(this);
        layout_dialog_edit_two_yes_bt = findViewById(R.id.layout_dialog_edit_two_yes_bt);
        layout_dialog_edit_two_yes_bt.setOnClickListener(this);

    }

    public void setTitle(String string){
        layout_dialog_edit_two_title_tv.setText(string);
    }

    public void setInformName(String s1,String s2){
        layout_dialog_edit_two_inform1_name_tv.setText(s1);
        layout_dialog_edit_two_inform2_name_tv.setText(s2);
    }

    public void setHint(String s1,String s2){
        layout_dialog_edit_two_inform1_et.setHint(s1);
        layout_dialog_edit_two_inform2_et.setHint(s2);
    }

    public void setInform(String s1,String s2){
        layout_dialog_edit_two_inform1_et.setText(s1);
        layout_dialog_edit_two_inform2_et.setText(s2);
        layout_dialog_edit_two_inform1_et.setSelection(s1.length());
    }
    public void setButtonText(String s1,String s2){
        layout_dialog_edit_two_no_bt.setText(s1);
        layout_dialog_edit_two_yes_bt.setText(s2);
    }

    public String getInform1(){
        return layout_dialog_edit_two_inform1_et.getText().toString().trim();
    }

    public String getInform2(){
        return layout_dialog_edit_two_inform2_et.getText().toString().trim();
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
