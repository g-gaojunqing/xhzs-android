package com.example.dzxh_app.api;

public interface MusicIService {
    public void callPlayMusic(String path);
    public void callPauseMusic();
    public void callRePlayMusic();
    public void callSeekTo(int position);
    public String callGetDuration(String string);
    public void callCloseTask();
}
