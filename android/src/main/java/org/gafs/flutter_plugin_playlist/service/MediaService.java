package org.gafs.flutter_plugin_playlist.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.BaseMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback;
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter;
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;

import com.google.android.exoplayer2.util.Util;

import org.gafs.flutter_plugin_playlist.R;
import org.gafs.flutter_plugin_playlist.api.MediaConstans;
import org.gafs.flutter_plugin_playlist.bean.MediaBean;
import org.gafs.flutter_plugin_playlist.manager.LogTool;
import org.gafs.flutter_plugin_playlist.manager.MediaController;
import org.gafs.flutter_plugin_playlist.manager.MediaSessionManager;

import static com.google.android.exoplayer2.C.USAGE_MEDIA;

public class MediaService extends Service implements  AudioManager.OnAudioFocusChangeListener ,Player.EventListener{

    private DefaultBandwidthMeter BANDWIDTH_METER;

    public  SimpleExoPlayer exoPlayer;

    public MediaBean mediaBean;

    public static MediaService musicPlayService;

    public static int curPlayState = 0; //播放状态
    public static final int PLAT_STATE_NORAML = 0;
    public static final int PLAY_STATE_PLAYING = 1;
    public static final int PLAY_STATE_PAUSED = 2;

    private MediaSessionManager mediaSessionManager;

    private PlayerNotificationManager playerNotificationManager;

    private Bitmap icon=null;

    private NotificationManager notificationManager;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        musicPlayService = this;
        mediaSessionManager = new MediaSessionManager(this);
        notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        initMediaPlayer();
    }

    public Long getCurrentPosition(){
        return exoPlayer.getCurrentPosition();
    }


    private void initMediaPlayer() {
        LogTool.s("initMediaPlayer");
        curPlayState = PLAT_STATE_NORAML;
        BANDWIDTH_METER = new DefaultBandwidthMeter();
        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory((BandwidthMeter)BANDWIDTH_METER);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector((com.google.android.exoplayer2.trackselection.TrackSelection.Factory)trackSelectionFactory);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, (TrackSelector)trackSelector);
        exoPlayer.addListener(this);
        initNotificationManager();
    }

    private void initNotificationManager() {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                this,
                "channel",
                R.string.exo_download_notification_channel_name,
                33,
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public String getCurrentContentTitle(Player player) {
                        if (mediaBean!=null&&mediaBean.getName()!=null){
                            return mediaBean.getName();
                        }
                        return "Title";
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public String getCurrentContentText(Player player) {
                        if (mediaBean!=null&&mediaBean.getAuthor()!=null){
                            return mediaBean.getAuthor();
                        }
                        return "Author";
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player,final PlayerNotificationManager.BitmapCallback callback) {
                        if(mediaBean!=null&&mediaBean.getImageUrl()!=null&&mediaBean.getImageUrl()!=""){
                            Glide.with(MediaService.this).asBitmap().load(mediaBean.getImageUrl()).override(50, 50).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    icon=resource;
                                    callback.onBitmap(resource);
                                }
                            });
                        }
                        if(icon!=null){
                            LogTool.s("---------------icon!=null-----------");
                            return icon;
                        }else{
                            return BitmapFactory.decodeResource(getResources(),R.drawable.radio_default);
                        }
                    }
                }
        );
        playerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
//                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });
        playerNotificationManager.setSmallIcon(R.drawable.icon_notification);
        playerNotificationManager.setPlayer(exoPlayer);
        playerNotificationManager.setMediaSessionToken(mediaSessionManager.getMediaSessionToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }


    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        switch (intent.getAction()) {
            case MediaConstans.MUSIC_ACTICON_START_PLAY:
                play();
                break;
            case MediaConstans.MUSIC_ACTICON_PAUSE_PLAY:
                pause();
                break;
            case MediaConstans.MUSIC_INFO_UPDATE:
                updateMediaInfo(intent);
                break;
        }
    }

    public void updateMediaInfo(Intent intent) {
        if(intent.getSerializableExtra(MediaConstans.MEDIA_INFO_PARAMS_KEY)!=null){
            mediaBean= (MediaBean) intent.getSerializableExtra(MediaConstans.MEDIA_INFO_PARAMS_KEY);
            LogTool.s("---------------playerNotificationManager.invalidate-----------");
            if(icon!=null){
                icon=null;
                LogTool.s("---------------icon.recycle()-----------");
            }
            playerNotificationManager.invalidate();
        }
    }



    public void play() {
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, "flutter_radio", (TransferListener)this.BANDWIDTH_METER);
        MediaSource mediaSource = this.buildMediaSource(dataSourceFactory);
        if (exoPlayer == null) {
            throw  new NullPointerException("exoPlayer not init");
        }
        exoPlayer.stop();
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        curPlayState=PLAY_STATE_PLAYING;
        mediaSessionManager.updatePlaybackState(curPlayState);
    }



    public void pause() {
        if (exoPlayer == null) {
            throw  new NullPointerException("exoPlayer not init");
        }
        exoPlayer.setPlayWhenReady(false);
        curPlayState=PLAY_STATE_PAUSED;
        mediaSessionManager.updatePlaybackState(curPlayState);
    }

    public final void stop() {
        if (exoPlayer == null) {
            throw  new NullPointerException("exoPlayer not init");
        }
        exoPlayer.stop();
        curPlayState=PLAY_STATE_PAUSED;
        mediaSessionManager.updatePlaybackState(curPlayState);
    }

    private final void resume() {
        if (MediaConstans.Media_Url != null) {
            this.play();
        }
    }

    public final boolean isPlaying() {
        return curPlayState==PLAY_STATE_PLAYING;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancelAll();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        LogTool.s("Mediaservice-----ondestory");
        mediaSessionManager.release();
        playerNotificationManager.setPlayer(null);
        notificationManager.cancelAll();
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
        super.onDestroy();
    }

    private final MediaSource buildMediaSource(DefaultDataSourceFactory dataSourceFactory) {
        Uri uri = Uri.parse(MediaConstans.Media_Url);
        int type = Util.inferContentType(uri);
        BaseMediaSource mediaSource;
        switch(type) {
            case 2:
                mediaSource = (BaseMediaSource)(new com.google.android.exoplayer2.source.hls.HlsMediaSource.Factory((com.google.android.exoplayer2.upstream.DataSource.Factory)dataSourceFactory)).createMediaSource(uri);
                break;
            case 3:
                mediaSource = (BaseMediaSource)(new com.google.android.exoplayer2.source.ExtractorMediaSource.Factory((com.google.android.exoplayer2.upstream.DataSource.Factory)dataSourceFactory)).createMediaSource(uri);
                break;
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
        return (MediaSource)mediaSource;

    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        if (exoPlayer == null) {
            throw  new NullPointerException("exoPlayer not init");
        }
        switch(focusChange) {
            case -3:
                if (this.isPlaying()) {
                    exoPlayer.setVolume(0.1F);
                }
                break;
            case -2:
                if (this.isPlaying()) {
                    this.pause();
                }
                break;
            case -1:
                this.stop();
            case 0:
            default:
                break;
            case 1:
                exoPlayer.setVolume(0.8F);
                this.resume();
        }
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState){
            case 3:
                LogTool.s("onPlayerStateChanged==playWhenReady=="+playWhenReady+"----------playbackState=="+playbackState);
                MediaController.getInstance().onStatusChange(playWhenReady);
                break;
            case 1:
                LogTool.s("onPlayerStateChanged==playWhenReady=="+playWhenReady+"----------playbackState=="+playbackState);
                MediaController.getInstance().onStatusChange(false);
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
}
