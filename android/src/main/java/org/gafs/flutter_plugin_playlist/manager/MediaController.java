package org.gafs.flutter_plugin_playlist.manager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import org.gafs.flutter_plugin_playlist.FlutterRadioPlugin;
import org.gafs.flutter_plugin_playlist.api.MediaConstans;
import org.gafs.flutter_plugin_playlist.bean.MediaBean;
import org.gafs.flutter_plugin_playlist.service.MediaService;

/**
 * Created by Clearlee on 2017/12/27 0027.
 */

public class MediaController {

    private static Activity context;

    private static MediaController instance;

    private  FlutterRadioPlugin mPlugin;

    private MediaController(){
    }

    public static MediaController getInstance(){
        if (instance==null){
            instance=new MediaController();
        }
        return instance;
    }

    public  void init(Activity context, FlutterRadioPlugin plugin){
        this.context=context;
        this.mPlugin=plugin;
        initListener();
    }




    private void initListener() {
        Application application=context.getApplication();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                LogTool.s("onActivityDestroyed");
                Intent intent = new Intent();
                intent.setClass(activity, MediaService.class);
                activity.stopService(intent);
            }
        });
    }

    public static void initMusicService() {
        sendCommandToService(MediaConstans.MUSIC_ACTICON_INIT, null, null);
    }

    public static void startPlay() {
        sendCommandToService(MediaConstans.MUSIC_ACTICON_START_PLAY, null, null);
    }

    public static void pausePlay() {
        sendCommandToService(MediaConstans.MUSIC_ACTICON_PAUSE_PLAY, null, null);
    }

    public static void connectToChromecast() {
        sendCommandToService(MediaConstans.MUSIC_ACTICON_CONNECT_TO_CHROMECAST, null, null);
    }

    public static void disconnectChromecast() {
        sendCommandToService(MediaConstans.MUSIC_ACTICON_DISCONNECT_CHROMECAST, null, null);
    }



    public static void updateInfo(MediaBean data) {
        sendCommandToService(MediaConstans.MUSIC_INFO_UPDATE, MediaConstans.MEDIA_INFO_PARAMS_KEY, data);
    }

    //发送指令到音乐服务
    private static void sendCommandToService(String action, String param, MediaBean data) {
        Intent intent = new Intent();
        intent.setClass(context, MediaService.class);
        intent.setAction(action);
        if (param != null) {
            intent.putExtra(param, data);
        }
        context.startService(intent);
    }

    public void onStatusChange(boolean status){
        mPlugin.onStatusChange(status);
    }


}
