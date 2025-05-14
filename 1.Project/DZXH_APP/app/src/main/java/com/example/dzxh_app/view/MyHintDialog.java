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


public class MyHintDialog extends Dialog implements View.OnClickListener {
    private OnDialogClickListener listener;
    private TextView layout_dialog_hint_tv;
    private Button layout_dialog_hint_bt;

    public MyHintDialog(Context context) {
        super(context,R.style.MyDialogTheme);
        setContentView(R.layout.layout_dialog_hint);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout_dialog_hint_tv = findViewById(R.id.layout_dialog_hint_tv);
        layout_dialog_hint_bt = findViewById(R.id.layout_dialog_hint_bt);
        layout_dialog_hint_bt.setOnClickListener(this);
    }

    public void setText(String string){
        layout_dialog_hint_tv.setText(string);
    }

    public void setButtonText(String string){
        layout_dialog_hint_bt.setText(string);
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
