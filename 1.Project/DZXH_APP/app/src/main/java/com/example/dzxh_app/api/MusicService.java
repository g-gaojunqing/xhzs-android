package com.example.dzxh_app.api;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.example.dzxh_app.Activity.SpeechActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private Boolean start=false; //是否正在播放

    private String music_path=null;
    private Timer timer;
    private TimerTask task;

    public IBinder onBind(Intent intent){
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        updateSeekBar();
        super.onCreate();
    }

    public void onDestroy(){
        super.onDestroy();
    }
    /**获取音乐时长 音频信息显示使用*/
    public String getDuration(String path){
        MediaPlayer mp=null;
        try {
            mp = new MediaPlayer();
            mp.reset();
            mp.setDataSource(path);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DecimalFormat decimalFormat =new DecimalFormat("00");
        int min=(mp.getDuration())/60000;
        int sec=(mp.getDuration())%60000/1000;
        return decimalFormat.format(min)+":"+decimalFormat.format(sec);
    }
    /**实现指定播放的位置*/
    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    /**开始播放*/
    public void playMusic(String path){
        boolean err=false;
        music_path=path;
        try {
            mediaPlayer.reset();//从头开始
            mediaPlayer.setDataSource(path);//路径
            mediaPlayer.prepare(); //准备播放
            mediaPlayer.start();//开始播放
            start=true;
        } catch (Exception e) {
            err=true;
            start=false;
            e.printStackTrace();
        }
        if(err){  //出错后1s重新播放一次
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetMusic();
                }
            },1000);
        }
    }
    /**重新开始播放*/
    public void resetMusic(){
        try {
            mediaPlayer.reset();//从头开始
            mediaPlayer.setDataSource(music_path);//路径
            mediaPlayer.prepare();//准备播放
            mediaPlayer.start();//开始播放
            start=true;
        } catch (Exception e) {
            start=false;
            Toast.makeText(getApplicationContext(),"播放出错了",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    /**暂停*/
    public void pauseMusic(){
        mediaPlayer.pause();
    }
    /**继续播放*/
    public void rePlayMusic(){
        mediaPlayer.start();
    }
    /**更新进度条的方法*/
    public void updateSeekBar() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                String string_dur;
                int min,sec;
                int currentPosition=0;
                int duration=0;
                DecimalFormat decimalFormat =new DecimalFormat("00");
                if(start){
                    duration= mediaPlayer.getDuration();
                    currentPosition= mediaPlayer.getCurrentPosition();//获得当前进度
                }
                if(duration!=0){
                    min=(currentPosition)/60000;
                    sec=(currentPosition)%60000/1000;
                    string_dur=decimalFormat.format(min)+":"+decimalFormat.format(sec)+"/";

                    min=(duration)/60000;
                    sec=(duration)%60000/1000;
                    string_dur+=decimalFormat.format(min)+":"+decimalFormat.format(sec);
                    //打包数据
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration); //总时长
                    bundle.putInt("currentPosition", currentPosition); //当前时长
                    bundle.putString("string_dur",string_dur);
                    //发送数据
                    Message msg = Message.obtain();
                    msg.setData(bundle);
                    SpeechActivity.music_handler.sendMessage(msg);
                }
            }
        };
        //100毫秒后1s执行一次run
        timer.schedule(task, 100, 1000);
        //播放完成的监听
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //timer.cancel();
                //task.cancel();
            }
        });
    }

    private  class MyBinder extends Binder implements MusicIService {
        @Override
        public void callPlayMusic(String path){
            playMusic(path);
        }

        @Override
        public void callPauseMusic(){
            pauseMusic();
        }

        @Override
        public void callRePlayMusic(){
            rePlayMusic();
        }

        @Override
        public void callSeekTo(int position) {
            seekTo(position);
        }

        @Override
        public String callGetDuration(String string) {
            return getDuration(string);
        }

        @Override
        public void callCloseTask() {

        }
    }
}
