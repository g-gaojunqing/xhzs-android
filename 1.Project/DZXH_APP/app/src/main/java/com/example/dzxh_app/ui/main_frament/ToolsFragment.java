package com.example.dzxh_app.ui.main_frament;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.dzxh_app.Activity.BluetoothActivity;
import com.example.dzxh_app.Activity.DataActivity;
import com.example.dzxh_app.Activity.HelpActivity;
import com.example.dzxh_app.Activity.MainActivity;
import com.example.dzxh_app.Activity.SetActivity;
import com.example.dzxh_app.Activity.WifiActivity;
import com.example.dzxh_app.R;
import com.example.dzxh_app.util.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;

public class ToolsFragment extends Fragment {
    private MainActivity mainActivity;
    private View root;

    private MyApplication myApplication;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tools, container, false);
        NumInit();
        CreateDeviceView();
        CreateSetView();
        return root;
    }
    private void NumInit(){
        mainActivity = (MainActivity)getActivity();
        myApplication = (MyApplication) requireActivity().getApplication();
    }
    private void CreateDeviceView(){
        ArrayList<HashMap<String, Object>> SetListItem = new ArrayList<HashMap<String, Object>>();
        for(int i=0;i<2;i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            if(i==0) {
                map.put("tools_image",R.drawable.tool_bluetooth_black_24dp);
                map.put("tools_name", "蓝牙");
                if(myApplication.getBluetoothDevice()!=null){
                    map.put("tools_flag", "已连接");
                }else{
                    map.put("tools_flag", "未连接");
                }
            }else{
                map.put("tools_image",R.drawable.ic_network_wifi_black_24dp );
                map.put("tools_name", "WIFI");
                if(myApplication.getWifiDevice()!=null){
                    map.put("tools_flag", "已连接");
                }else{
                    map.put("tools_flag", "未连接");
                }
            }
            SetListItem.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), SetListItem,
                R.layout.item_tools,
                new String[]{"tools_image","tools_name","tools_flag"},
                new int[]{R.id.item_tools_image,R.id.item_tools_name,R.id.item_tools_flag});
        ListView tools_service_lv = root.findViewById(R.id.tools_service_lv);

        tools_service_lv.setAdapter(simpleAdapter); //列表配置
        mainActivity.setListViewHeightBasedOnChildren(tools_service_lv);//重置ListView大小
        //点击事件
        tools_service_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    Intent intent=new Intent(getActivity(), BluetoothActivity.class);
                    startActivityForResult(intent,0);
                }else if(position==1){
                    Intent intent=new Intent(getActivity(), WifiActivity.class);
                    startActivityForResult(intent,1);
                }
            }
        });
    }

    private void CreateSetView(){
        ArrayList<HashMap<String, Object>> SetListItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            if(i==0){
                map.put("tools_image", R.drawable.tool_library_books_24);
                map.put("tools_name", "资料");
                map.put("tools_flag", null);
            }else if(i==1){
                map.put("tools_image", R.drawable.tool_help_outline_24);
                map.put("tools_name", "帮助");
                map.put("tools_flag", null);
            }else {
                map.put("tools_image", R.drawable.ic_settings_black_24dp);
                map.put("tools_name", "设置");
                map.put("tools_flag", null);
            }
            SetListItem.add(map);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), SetListItem,
                R.layout.item_tools,
                new String[]{"tools_image","tools_name","tools_flag"},
                new int[]{R.id.item_tools_image,R.id.item_tools_name,R.id.item_tools_flag});
        ListView tools_set_lv = root.findViewById(R.id.tools_set_lv);
        //列表配置
        tools_set_lv.setAdapter(simpleAdapter);
        mainActivity.setListViewHeightBasedOnChildren(tools_set_lv);//重置ListView大小
        //点击事件
        tools_set_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if(position==0){
                    intent = new Intent(getActivity(), DataActivity.class);
                }else if(position==1){
                    intent=new Intent(getActivity(), HelpActivity.class);
                }else{
                    intent=new Intent(getActivity(), SetActivity.class);
                }
                startActivity(intent);
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CreateDeviceView(); //刷新列表，更新连接状态
        myApplication.setAutoBluetoothState(mainActivity.sp.getBoolean("AutoConnectState",false));//更新自动连接状态
        super.onActivityResult(requestCode, resultCode, data);
    }
}