package com.tools;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.browse.MediaBrowser.MediaItem;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.util.Log;

import java.util.*;

/**
 * Provides "background" audio playback capabilities, allowing the
 * user to switch between activities without stopping playback.
 */
public class AudioService extends MediaBrowserService {
    private MediaSession mSession;

    public AudioService() {}

    @Override
    public void onCreate() {
        super.onCreate();

        // Start a new MediaSession
        mSession = new MediaSession(this, "MediaPlaybackService");
        // Enable callbacks from MediaButtons and TransportControls
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder().setActions(
                PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE);
        mSession.setPlaybackState(stateBuilder.build());
        setSessionToken(mSession.getSessionToken());

        Context context = getApplicationContext();
        Intent intent = new Intent(context, AudioService.class);
        PendingIntent pi = PendingIntent.getActivity(
                context, 99 /*request code*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {}

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(final String parentMediaId, final Result<List<MediaItem>> result) {
        result.sendResult(null);
        return;
    }

    private final class MediaSessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {}

        @Override
        public void onSkipToQueueItem(long queueId) {}

        @Override
        public void onSeekTo(long position) {}

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {}

        @Override
        public void onPause() {}

        @Override
        public void onStop() {}

        @Override
        public void onSkipToNext() {}

        @Override
        public void onSkipToPrevious() {}

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {}

        @Override
        public void onCustomAction(String action, Bundle extras) {}
    }
}
