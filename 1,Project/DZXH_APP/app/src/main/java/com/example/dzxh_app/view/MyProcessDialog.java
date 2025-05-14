package com.example.dzxh_app.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.dzxh_app.R;

public class MyProcessDialog extends Dialog {

    private TextView layout_process_dialog_tv;

    public MyProcessDialog(Context context) {

        super(context, R.style.MyDialogTheme);
        setContentView(R.layout.layout_dialog_process);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout_process_dialog_tv = findViewById(R.id.layout_process_dialog_tv);
    }

    public void setTitle(String string){
        layout_process_dialog_tv.setText(string);
    }
}
