package org.gafs.flutter_plugin_playlist.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.gafs.flutter_plugin_playlist.manager.LogTool;

public class InsomniacReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        LogTool.s("action="+action);
    }


}
