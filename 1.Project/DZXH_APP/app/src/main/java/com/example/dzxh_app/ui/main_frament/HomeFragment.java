package com.example.dzxh_app.ui.main_frament;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dzxh_app.Activity.CameraActivity;
import com.example.dzxh_app.Activity.ClockActivity;
import com.example.dzxh_app.Activity.DrawBoardActivity;
import com.example.dzxh_app.Activity.GPSActivity;
import com.example.dzxh_app.Activity.OscilloscopeActivity;
import com.example.dzxh_app.Activity.RemoteControlActivity;
import com.example.dzxh_app.Activity.SpeechActivity;
import com.example.dzxh_app.Activity.StopWatchActivity;
import com.example.dzxh_app.Activity.UIActivity;
import com.example.dzxh_app.R;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        Button home_bt = root.findViewById(R.id.home_bt);
        home_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"这玩意就是一摆设",Toast.LENGTH_SHORT).show();
            }
        });
        ViewInit(root);
        return root;
    }

    private void ViewInit(View root) {
        //创建一个数组列表对象
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 1; i < 10; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();// 建立hashmap对象
            if (i == 1) {
                map.put("item_home_iv", R.drawable.camera_image);
                map.put("item_home_tv", "图像");
            }
            if (i == 2) {
                map.put("item_home_iv", R.drawable.shiboqi_image);
                map.put("item_home_tv", "虚拟示波器");
            }
            if (i == 3) {
                map.put("item_home_iv", R.drawable.ui_image);
                map.put("item_home_tv", "UI");
            }
            if (i == 4) {
                map.put("item_home_iv", R.drawable.yaokong_image);
                map.put("item_home_tv", "遥控器");
            }
            if (i == 5) {
                map.put("item_home_iv", R.drawable.miaobiao_image);
                map.put("item_home_tv", "秒表");
            }
            if (i == 6) {
                map.put("item_home_iv", R.drawable.clock_image);
                map.put("item_home_tv", "时钟");
            }
            if (i == 7) {
                map.put("item_home_iv", R.drawable.yvyin_image);
                map.put("item_home_tv", "语音播报");
            }
            if (i == 8) {
                map.put("item_home_iv", R.drawable.gps_image);
                map.put("item_home_tv", "GPS");
            }
            if (i == 9) {
                map.put("item_home_iv", R.drawable.huaban_image);
                map.put("item_home_tv", "画板");
            }
            lstImageItem.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), lstImageItem,
                R.layout.item_home, new String[] { "item_home_iv", "item_home_tv" },
                new int[] { R.id.item_home_iv, R.id.item_home_tv });
        GridView gridview = root.findViewById(R.id.home_gv);
        gridview.setAdapter(simpleAdapter);// 添加适配器
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                int index=position+1;
                if(index==1){
                    intent = new Intent(getActivity(), CameraActivity.class);
                    startActivity(intent);
                }else if(index==2){
                    intent = new Intent(getActivity(), OscilloscopeActivity.class);
                    startActivity(intent);
                }else if(index==3){
                    intent = new Intent(getActivity(), UIActivity.class);
                    startActivity(intent);
                }else if(index==4){
                    intent = new Intent(getActivity(), RemoteControlActivity.class);
                    startActivity(intent);
                }else if(index==5){
                    intent = new Intent(getActivity(), StopWatchActivity.class);
                    startActivity(intent);
                }else if(index==6){
                    intent = new Intent(getActivity(), ClockActivity.class);
                    startActivity(intent);
                }else if(index==7){
                    intent = new Intent(getActivity(), SpeechActivity.class);
                    startActivity(intent);
                }else if(index==8){
                    intent = new Intent(getActivity(), GPSActivity.class);
                    startActivity(intent);
                }else if(index==9){
                    intent = new Intent(getActivity(), DrawBoardActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}