package org.gafs.flutter_plugin_playlist.manager;

import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import org.gafs.flutter_plugin_playlist.bean.MediaBean;
import org.gafs.flutter_plugin_playlist.service.MediaService;

public class MediaSessionManager {

    private static final String MY_MEDIA_ROOT_ID = "MediaSessionManager1314";

    private MediaService musicPlayService;
    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder stateBuilder;


    public MediaSessionManager(MediaService service) {
        this.musicPlayService = service;
        initSession();
    }

    public void initSession() {
        try {
            mMediaSession = new MediaSessionCompat(musicPlayService, MY_MEDIA_ROOT_ID);
            mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            stateBuilder = new PlaybackStateCompat.Builder()
                    .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE).setState(((MediaService.curPlayState == MediaService.PLAY_STATE_PAUSED) ? PlaybackState.STATE_PAUSED : PlaybackState.STATE_PLAYING),0,1.0f);
            mMediaSession.setPlaybackState(stateBuilder.build());
            mMediaSession.setCallback(sessionCb);
            mMediaSession.setActive(true);
        } catch (Exception e) {
            LogTool.ex(e);
        }
    }

    public void updatePlaybackState(int currentState) {
        int state = (currentState == MediaService.PLAY_STATE_PAUSED) ? PlaybackState.STATE_PAUSED : PlaybackState.STATE_PLAYING;
        stateBuilder.setState(state, musicPlayService.getCurrentPosition(), 1.0f);
        mMediaSession.setPlaybackState(stateBuilder.build());
    }

    public MediaSessionCompat.Token getMediaSessionToken(){
        return  mMediaSession.getSessionToken();
    }



    private MediaSessionCompat.Callback sessionCb = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            musicPlayService.play();
        }

        @Override
        public void onPause() {
            super.onPause();
            musicPlayService.pause();
        }

    };

    public void release() {
        mMediaSession.setCallback(null);
        mMediaSession.setActive(false);
        mMediaSession.release();
    }


}
