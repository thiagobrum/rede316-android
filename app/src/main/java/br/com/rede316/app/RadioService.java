package br.com.rede316.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

/**
 * Media service that handles:
 * - Background audio playback
 * - Lock screen controls
 * - Notification controls
 * - Android Auto media browsing & playback
 */
public class RadioService extends MediaLibraryService {

    private static final String ROOT_ID = "rede316_root";
    private static final String STATIONS_ID = "rede316_stations";

    private MediaLibrarySession session;
    private ExoPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();

        // Configure ExoPlayer for live streaming
        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                                .setUsage(C.USAGE_MEDIA)
                                .build(),
                        true // handle audio focus
                )
                .setHandleAudioBecomingNoisy(true) // pause on headphone disconnect
                .setWakeMode(C.WAKE_MODE_NETWORK)   // keep WiFi alive
                .build();

        // Build session with callback for Android Auto browsing
        session = new MediaLibrarySession.Builder(this, player, new AutoCallback())
                .build();
    }

    @Override
    public MediaLibrarySession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return session;
    }

    @Override
    public void onDestroy() {
        if (session != null) {
            session.getPlayer().release();
            session.release();
            session = null;
        }
        super.onDestroy();
    }

    /**
     * Callback that provides browsable content for Android Auto.
     * Auto sees: Root → "Estações" → [Brasil, Planalto Central, Bahia]
     */
    private class AutoCallback implements MediaLibrarySession.Callback {

        @NonNull
        @Override
        public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
                @NonNull MediaLibrarySession session,
                @NonNull MediaSession.ControllerInfo browser,
                @Nullable LibraryParams params) {

            MediaItem root = new MediaItem.Builder()
                    .setMediaId(ROOT_ID)
                    .setMediaMetadata(
                            new MediaMetadata.Builder()
                                    .setTitle("Rede 3.16")
                                    .setIsPlayable(false)
                                    .setIsBrowsable(true)
                                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_RADIO_STATIONS)
                                    .build()
                    )
                    .build();

            return Futures.immediateFuture(LibraryResult.ofItem(root, params));
        }

        @NonNull
        @Override
        public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetChildren(
                @NonNull MediaLibrarySession session,
                @NonNull MediaSession.ControllerInfo browser,
                @NonNull String parentId,
                int page, int pageSize,
                @Nullable LibraryParams params) {

            List<MediaItem> items = new ArrayList<>();

            if (ROOT_ID.equals(parentId) || STATIONS_ID.equals(parentId)) {
                // Return all radio stations
                for (RadioStation station : RadioStation.ALL) {
                    items.add(station.toMediaItem());
                }
            }

            return Futures.immediateFuture(
                    LibraryResult.ofItemList(ImmutableList.copyOf(items), params)
            );
        }

        @NonNull
        @Override
        public ListenableFuture<LibraryResult<MediaItem>> onGetItem(
                @NonNull MediaLibrarySession session,
                @NonNull MediaSession.ControllerInfo browser,
                @NonNull String mediaId) {

            RadioStation station = RadioStation.findById(mediaId);
            return Futures.immediateFuture(
                    LibraryResult.ofItem(station.toMediaItem(), null)
            );
        }

        @NonNull
        @Override
        public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onSetMediaItems(
                @NonNull MediaSession session,
                @NonNull MediaSession.ControllerInfo controller,
                @NonNull List<MediaItem> mediaItems,
                int startIndex, long startPositionMs) {

            // When Android Auto selects a station, prepare the stream
            List<MediaItem> resolved = new ArrayList<>();
            for (MediaItem item : mediaItems) {
                RadioStation station = RadioStation.findById(item.mediaId);
                resolved.add(station.toMediaItem());
            }

            return Futures.immediateFuture(
                    new MediaSession.MediaItemsWithStartPosition(resolved, startIndex, startPositionMs)
            );
        }
    }
}
