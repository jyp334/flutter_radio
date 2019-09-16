package com.jyp.flutterradio;

import android.content.Context;

public class MusicManager {

    private Context context;

    private MusicManager instance;

    private MusicService service=new MusicService(context);

    private Boolean serviceBound = false;

    public MusicManager(Context context) {
        this.context = context;
    }

    public void initPlayer() {
        service.onCreate();
    }

    public void playOrPause(String streamUrl) {
        service.playOrPause(streamUrl);
    }

    public void stop() {
        service.stop();
    }

    public Boolean isPlaying(){
        return service.isPlaying();
    }

}
