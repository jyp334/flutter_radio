package org.gafs.flutter_plugin_playlist;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;


import org.gafs.flutter_plugin_playlist.api.MediaConstans;
import org.gafs.flutter_plugin_playlist.bean.MediaBean;
import org.gafs.flutter_plugin_playlist.manager.LogTool;
import org.gafs.flutter_plugin_playlist.manager.MediaController;
import org.gafs.flutter_plugin_playlist.manager.MediaSessionManager;
import org.gafs.flutter_plugin_playlist.service.MediaService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;


/**
 *
 * The core Cordova interface for the audio player
 * TODO: Move the proxied calls audioPlayerImpl.getPlaylistManager()
 * into the audio player class itself so the plugin doesn't know about
 * the playlist manager.
 *
 */
public class FlutterRadioPlugin implements MethodCallHandler, PreferenceManager.OnActivityDestroyListener {
  public static String TAG = "RmxAudioPlayer";

  private MethodChannel channel;

  private Activity activity;


  private FlutterRadioPlugin(MethodChannel channel,Activity activity) {
      this.channel = channel;
      this.activity=activity;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {

    MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_radio");

    // Plugin instance
    FlutterRadioPlugin plugin = new FlutterRadioPlugin(channel,registrar.activity());

    MediaController.getInstance().init(registrar.activity(),plugin);

    // register the plugin as the method call handler
    channel.setMethodCallHandler(plugin);
  }

  @Override
  public void onMethodCall(final MethodCall call, final Result result) {
    String action = call.method;
    switch (action){
      case "audioStart":
        MediaController.initMusicService();
        MediaConstans.Media_Url = (String) call.argument("url");
        LogTool.s("audioStart"+MediaConstans.Media_Url);
        result.success(null);
        break;
      case "play":
        MediaController.startPlay();
        result.success(null);
        break;
      case "pause":
        MediaController.pausePlay();
        result.success(null);
        break;
      case "setMeta":
        Map<String,String> nextUser=call.argument("meta");
        if(nextUser!=null){
          MediaBean bean=new MediaBean();
          bean.setAlbum(nextUser.get("album"));
          bean.setAuthor(nextUser.get("artist"));
          bean.setName(nextUser.get("title"));
          bean.setImageUrl(nextUser.get("url"));
          MediaController.updateInfo(bean);
        }
        result.success(null);
        break;
    }
  }

  public void onStatusChange(boolean status){
    String statusStr=status?"1":"0";
    Map<String,String> map=new HashMap<>();
    map.put("status",statusStr);
    channel.invokeMethod("controlPlayChanged",map);
  }

  @Override
  public void onActivityDestroy() {
    LogTool.s("onActivityDestroy");
    Intent intent = new Intent();
    intent.setClass(activity, MediaService.class);
    activity.stopService(intent);
  }
}
