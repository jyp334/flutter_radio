package com.jyp.flutterradio;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.BaseMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import br.com.thyagoluciano.flutterradio.player.PlaybackStatus;
import kotlin.jvm.internal.Intrinsics;

public class MusicService  implements Player.EventListener, AudioManager.OnAudioFocusChangeListener {

    private DefaultBandwidthMeter BANDWIDTH_METER;
    private SimpleExoPlayer exoPlayer;
    private String status;
    private String streamUrl;
    private PlayerNotificationManager playerNotificationManager;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    @NotNull
    private Context context;

    public MusicService(@NotNull Context context) {
        super();
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.context = context;
        this.BANDWIDTH_METER = new DefaultBandwidthMeter();
    }

    public final void onCreate() {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory((BandwidthMeter)bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector((com.google.android.exoplayer2.trackselection.TrackSelection.Factory)trackSelectionFactory);
        SimpleExoPlayer var10001 = ExoPlayerFactory.newSimpleInstance(this.context, (TrackSelector)trackSelector);
        Intrinsics.checkExpressionValueIsNotNull(var10001, "ExoPlayerFactory.newSimp…e(context, trackSelector)");
        this.exoPlayer = var10001;
        SimpleExoPlayer var10000 = this.exoPlayer;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("exoPlayer");
        }

        var10000.addListener((Player.EventListener)this);
        this.status = PlaybackStatus.INSTANCE.getIDLE();
    }

    private final void play(String streamUrl) {
        this.streamUrl = streamUrl;
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this.context, "flutter_radio", (TransferListener)this.BANDWIDTH_METER);
        MediaSource mediaSource = this.buildMediaSource(streamUrl, dataSourceFactory);
        SimpleExoPlayer var10000 = this.exoPlayer;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("exoPlayer");
        }

        var10000.stop();
        var10000 = this.exoPlayer;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("exoPlayer");
        }

        var10000.prepare(mediaSource);
        var10000 = this.exoPlayer;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("exoPlayer");
        }

        var10000.setPlayWhenReady(true);
    }

    private final MediaSource buildMediaSource(String streamUrl, DefaultDataSourceFactory dataSourceFactory) {
        Uri uri = Uri.parse(streamUrl);
        int type = Util.inferContentType(uri);
        BaseMediaSource var10000;
        switch(type) {
            case 2:
                var10000 = (BaseMediaSource)(new com.google.android.exoplayer2.source.hls.HlsMediaSource.Factory((com.google.android.exoplayer2.upstream.DataSource.Factory)dataSourceFactory)).createMediaSource(uri);
                break;
            case 3:
                var10000 = (BaseMediaSource)(new com.google.android.exoplayer2.source.ExtractorMediaSource.Factory((com.google.android.exoplayer2.upstream.DataSource.Factory)dataSourceFactory)).createMediaSource(uri);
                break;
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }

        Intrinsics.checkExpressionValueIsNotNull(var10000, "when (type) {\n          …)\n            }\n        }");
        return (MediaSource)var10000;
    }

    private final void resume() {
        if (this.streamUrl != null) {
            String var10001 = this.streamUrl;
            if (var10001 == null) {
                Intrinsics.throwNpe();
            }

            this.play(var10001);
        }

    }

    private final void pause() {
        SimpleExoPlayer var10000 = this.exoPlayer;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("exoPlayer");
        }

        var10000.setPlayWhenReady(false);
    }

    public final void stop() {
        SimpleExoPlayer var10000 = this.exoPlayer;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("exoPlayer");
        }

        var10000.stop();
    }

    public final void playOrPause(@NotNull String url) {
        Intrinsics.checkParameterIsNotNull(url, "url");
        if (this.streamUrl != null && Intrinsics.areEqual(this.streamUrl, url)) {
            if (!this.isPlaying()) {
                String var10001 = this.streamUrl;
                if (var10001 == null) {
                    Intrinsics.throwNpe();
                }

                this.play(var10001);
            } else {
                this.pause();
            }
        } else {
            if (this.isPlaying()) {
                this.pause();
            }

            this.play(url);
        }

    }

    public final boolean isPlaying() {
        String var10000 = this.status;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("status");
        }

        return var10000.equals(PlaybackStatus.INSTANCE.getPLAYING());
    }

    @NotNull
    public final String getStatus() {
        String var10000 = this.status;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("status");
        }

        return var10000;
    }

    public void onAudioFocusChange(int focusChange) {
        SimpleExoPlayer var10000;
        switch(focusChange) {
            case -3:
                if (this.isPlaying()) {
                    var10000 = this.exoPlayer;
                    if (var10000 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("exoPlayer");
                    }

                    var10000.setVolume(0.1F);
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
                var10000 = this.exoPlayer;
                if (var10000 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("exoPlayer");
                }

                var10000.setVolume(0.8F);
                this.resume();
        }

    }

    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String var10001;
        switch(playbackState) {
            case 2:
                var10001 = PlaybackStatus.INSTANCE.getLOADING();
                break;
            case 3:
                var10001 = playWhenReady ? PlaybackStatus.INSTANCE.getPLAYING() : PlaybackStatus.INSTANCE.getPAUSED();
                break;
            case 4:
                var10001 = PlaybackStatus.INSTANCE.getSTOPPED();
                break;
            default:
                var10001 = PlaybackStatus.INSTANCE.getIDLE();
        }

        this.status = var10001;
        if (EventBus.getDefault().hasSubscriberForEvent(String.class)) {
            EventBus var10000 = EventBus.getDefault();
            var10001 = this.status;
            if (var10001 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("status");
            }

            var10000.post(var10001);
        }

    }

    public void onPlayerError(@Nullable ExoPlaybackException error) {
        if (EventBus.getDefault().hasSubscriberForEvent(String.class)) {
            EventBus.getDefault().post(PlaybackStatus.INSTANCE.getERROR());
        }

    }

    public void onPlaybackParametersChanged(@Nullable PlaybackParameters playbackParameters) {
    }

    public void onSeekProcessed() {
    }

    public void onTracksChanged(@Nullable TrackGroupArray trackGroups, @Nullable TrackSelectionArray trackSelections) {
    }

    public void onLoadingChanged(boolean isLoading) {
    }

    public void onPositionDiscontinuity(int reason) {
    }

    public void onRepeatModeChanged(int repeatMode) {
    }

    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    public void onTimelineChanged(@Nullable Timeline timeline, @Nullable Object manifest, int reason) {
    }

    @NotNull
    public final Context getContext() {
        return this.context;
    }



}
