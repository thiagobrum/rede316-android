package br.com.rede316.app;

import android.net.Uri;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

public class RadioStation {

    public final String id;
    public final String title;
    public final String subtitle;
    public final String streamUrl;
    public final Uri artworkUri;

    public RadioStation(String id, String title, String subtitle, String streamUrl) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.streamUrl = streamUrl;
        this.artworkUri = Uri.parse("https://rede316.com.br/wp-content/uploads/2021/03/logo-rede-316-player-degrade.png");
    }

    public MediaItem toMediaItem() {
        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(subtitle)
                .setAlbumTitle("Rede 3.16")
                .setArtworkUri(artworkUri)
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .build();

        return new MediaItem.Builder()
                .setMediaId(id)
                .setUri(streamUrl)
                .setMediaMetadata(metadata)
                .setLiveConfiguration(
                        new MediaItem.LiveConfiguration.Builder()
                                .setMaxPlaybackSpeed(1.02f)
                                .build()
                )
                .build();
    }

    // ══ All stations ══
    public static final RadioStation[] ALL = {
            new RadioStation("brasil",
                    "Rede 3.16 — Brasil",
                    "Nacional",
                    "https://servidor21.brlogic.com:7976/live"),
            new RadioStation("planalto",
                    "Rede 3.16 — Planalto Central",
                    "Regional",
                    "https://servidor31.brlogic.com:7018/live"),
            new RadioStation("bahia",
                    "Rede 3.16 — Bahia",
                    "Regional",
                    "https://servidor33.brlogic.com:7126/live"),
    };

    public static RadioStation findById(String id) {
        for (RadioStation s : ALL) {
            if (s.id.equals(id)) return s;
        }
        return ALL[0];
    }
}
